package com.banking.service;

import com.banking.dao.*;
import com.banking.model.*;
import com.banking.security.SimulatedOtpDeliveryService;
import com.banking.security.UserSession;
import com.banking.util.DBConnection;
import com.banking.util.ValidationException;
import org.junit.jupiter.api.*;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("End-to-End Banking Integration & Atomic Transaction Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BankingIntegrationTest {

    private static final String H2_URL = "jdbc:h2:mem:banking_integration_test;MODE=MySQL;DATABASE_TO_UPPER=false;DB_CLOSE_DELAY=-1";
    private static final String H2_USER = "sa";
    private static final String H2_PASS = "";

    private static AuthService authService;
    private static AccountService accountService;
    private static TransferService transferService;
    private static TransactionService transactionService;
    private static UserDAO userDAO;
    private static AccountDAO accountDAO;
    private static TransactionDAO transactionDAO;
    private static LedgerDAO ledgerDAO;
    private static OtpDAO otpDAO;
    private static AuditService auditService;

    @BeforeAll
    static void initDatabase() throws Exception {
        // Point DBConnection to isolated H2 in-memory MySQL mode
        DBConnection.setConfiguration(H2_URL, H2_USER, H2_PASS);

        // Execute test schema script
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             InputStream is = BankingIntegrationTest.class.getClassLoader().getResourceAsStream("schema-test.sql")) {

            assertNotNull(is, "schema-test.sql resource must exist");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().startsWith("--") || line.trim().isEmpty()) continue;
                sb.append(line).append(" ");
                if (line.trim().endsWith(";")) {
                    stmt.execute(sb.toString().trim());
                    sb.setLength(0);
                }
            }
        }

        userDAO = new UserDAO();
        accountDAO = new AccountDAO();
        transactionDAO = new TransactionDAO();
        ledgerDAO = new LedgerDAO();
        otpDAO = new OtpDAO();
        auditService = new AuditService();

        authService = new AuthService(userDAO, accountDAO, auditService);
        accountService = new AccountService(accountDAO, auditService);
        transferService = new TransferService(accountDAO, transactionDAO, ledgerDAO, otpDAO, userDAO, auditService, SimulatedOtpDeliveryService.getInstance());
        transactionService = new TransactionService(transactionDAO, ledgerDAO, accountDAO);
    }

    @AfterAll
    static void tearDown() {
        DBConnection.resetToDefault();
    }

    @Test
    @Order(1)
    @DisplayName("1. User registration should create customer and initial account with ₹1,000 balance")
    void testUserRegistration() {
        User user = authService.register(
                "alice_w",
                "Alice Wonder",
                "alice@example.com",
                "9876543211",
                "AliceSecret@123",
                "AliceSecret@123",
                AccountType.SAVINGS
        );

        assertNotNull(user);
        assertTrue(user.getUserId() > 0);

        List<Account> accounts = accountService.getAccountsForUser(user.getUserId());
        assertEquals(1, accounts.size(), "Should have exactly one initial account");
        assertEquals(new BigDecimal("1000.00"), accounts.get(0).getBalance());
        assertEquals(AccountStatus.ACTIVE, accounts.get(0).getStatus());
    }

    @Test
    @Order(2)
    @DisplayName("2. Registration should reject duplicate email and duplicate username")
    void testDuplicateRegistrationPrevention() {
        assertThrows(ValidationException.class, () ->
                authService.register("alice_w", "Duplicate User", "different@example.com",
                        "9876543212", "Password@123", "Password@123", AccountType.SAVINGS)
        );

        assertThrows(ValidationException.class, () ->
                authService.register("new_username", "Duplicate User", "alice@example.com",
                        "9876543212", "Password@123", "Password@123", AccountType.SAVINGS)
        );
    }

    @Test
    @Order(3)
    @DisplayName("3. Authentication should succeed with valid credentials and reject invalid")
    void testAuthentication() {
        User loggedIn = authService.login("alice_w", "AliceSecret@123");
        assertNotNull(loggedIn);
        assertTrue(UserSession.isAuthenticated());
        assertEquals("alice_w", UserSession.getCurrentSession().getUsername());

        // Incorrect password
        assertThrows(ValidationException.class, () -> authService.login("alice_w", "WrongPassword@999"));

        // Nonexistent user
        assertThrows(ValidationException.class, () -> authService.login("nonexistent", "SomePassword@123"));

        authService.logout();
        assertFalse(UserSession.isAuthenticated());
    }

    @Test
    @Order(4)
    @DisplayName("4. Opening additional bank accounts should succeed")
    void testOpenAdditionalAccount() {
        User user = userDAO.findByEmailOrUsername("alice_w").orElseThrow();
        Account checking = accountService.createAdditionalAccount(user.getUserId(), AccountType.CHECKING);

        assertNotNull(checking);
        assertEquals(AccountType.CHECKING, checking.getAccountType());
        assertEquals(new BigDecimal("0.00"), checking.getBalance());

        List<Account> accounts = accountService.getAccountsForUser(user.getUserId());
        assertEquals(2, accounts.size());
    }

    @Test
    @Order(5)
    @DisplayName("5. Atomic fund transfer with 2FA OTP, double-entry ledger, and balance debit/credit")
    void testAtomicTransferWorkflow() {
        // Register Bob as recipient
        User bob = authService.register(
                "bob_builder",
                "Bob Builder",
                "bob@example.com",
                "9876543220",
                "BobSecret@123",
                "BobSecret@123",
                AccountType.SAVINGS
        );

        User alice = userDAO.findByEmailOrUsername("alice_w").orElseThrow();
        Account aliceAccount = accountService.getAccountsForUser(alice.getUserId()).get(0); // ₹1000 balance
        Account bobAccount = accountService.getAccountsForUser(bob.getUserId()).get(0);     // ₹1000 balance

        // 1. Alice initiates transfer of ₹400.00 to Bob
        BigDecimal transferAmount = new BigDecimal("400.00");
        TransferDraft draft = transferService.initiateTransfer(
                alice.getUserId(),
                aliceAccount.getAccountId(),
                bobAccount.getAccountNumber(),
                transferAmount,
                "Payment for services"
        );

        assertNotNull(draft);
        assertNotNull(draft.getDraftId());

        // 2. Fetch dispatched simulated OTP
        String otp = SimulatedOtpDeliveryService.getInstance().getLastDeliveredOtp();
        assertNotNull(otp);
        assertEquals(6, otp.length());

        // 3. Complete transfer using OTP
        Transaction txn = transferService.completeTransfer(draft.getDraftId(), otp);
        assertNotNull(txn);
        assertEquals(TransactionStatus.SUCCESS, txn.getStatus());
        assertEquals(transferAmount, txn.getAmount());

        // 4. Verify balances updated accurately
        Account updatedAlice = accountDAO.findById(aliceAccount.getAccountId()).orElseThrow();
        Account updatedBob = accountDAO.findById(bobAccount.getAccountId()).orElseThrow();

        assertEquals(new BigDecimal("600.00"), updatedAlice.getBalance(), "Alice balance must be debited by 400");
        assertEquals(new BigDecimal("1400.00"), updatedBob.getBalance(), "Bob balance must be credited by 400");

        // 5. Verify double-entry ledger entries
        List<LedgerEntry> entries = ledgerDAO.getEntriesByTransactionId(txn.getTransactionId());
        assertEquals(2, entries.size(), "Must have exactly 2 ledger entries (DEBIT and CREDIT)");

        LedgerEntry debit = entries.stream().filter(e -> e.getEntryType() == EntryType.DEBIT).findFirst().orElseThrow();
        LedgerEntry credit = entries.stream().filter(e -> e.getEntryType() == EntryType.CREDIT).findFirst().orElseThrow();

        assertEquals(aliceAccount.getAccountId(), debit.getAccountId());
        assertEquals(new BigDecimal("600.00"), debit.getBalanceAfter());

        assertEquals(bobAccount.getAccountId(), credit.getAccountId());
        assertEquals(new BigDecimal("1400.00"), credit.getBalanceAfter());
    }

    @Test
    @Order(6)
    @DisplayName("6. Transfer should be rejected when source account has insufficient balance")
    void testTransferInsufficientBalance() {
        User alice = userDAO.findByEmailOrUsername("alice_w").orElseThrow();
        User bob = userDAO.findByEmailOrUsername("bob_builder").orElseThrow();

        Account aliceAccount = accountService.getAccountsForUser(alice.getUserId()).get(0); // ₹600 balance
        Account bobAccount = accountService.getAccountsForUser(bob.getUserId()).get(0);

        // Attempt transfer of ₹9,000 (exceeds ₹600)
        ValidationException ex = assertThrows(ValidationException.class, () ->
                transferService.initiateTransfer(
                        alice.getUserId(),
                        aliceAccount.getAccountId(),
                        bobAccount.getAccountNumber(),
                        new BigDecimal("9000.00"),
                        "Excessive transfer"
                )
        );
        assertTrue(ex.getMessage().contains("Insufficient balance"));

        // Balances remain untouched
        Account currentAlice = accountDAO.findById(aliceAccount.getAccountId()).orElseThrow();
        assertEquals(new BigDecimal("600.00"), currentAlice.getBalance());
    }

    @Test
    @Order(7)
    @DisplayName("7. Transfer should reject transfers to the same source account")
    void testSameAccountTransferRejection() {
        User alice = userDAO.findByEmailOrUsername("alice_w").orElseThrow();
        Account aliceAccount = accountService.getAccountsForUser(alice.getUserId()).get(0);

        ValidationException ex = assertThrows(ValidationException.class, () ->
                transferService.initiateTransfer(
                        alice.getUserId(),
                        aliceAccount.getAccountId(),
                        aliceAccount.getAccountNumber(),
                        new BigDecimal("100.00"),
                        "Self transfer"
                )
        );
        assertTrue(ex.getMessage().contains("same source account"));
    }

    @Test
    @Order(8)
    @DisplayName("8. Invalid OTP attempts should increment and lock after max attempts")
    void testInvalidOtpLockout() {
        User alice = userDAO.findByEmailOrUsername("alice_w").orElseThrow();
        User bob = userDAO.findByEmailOrUsername("bob_builder").orElseThrow();

        Account aliceAccount = accountService.getAccountsForUser(alice.getUserId()).get(0);
        Account bobAccount = accountService.getAccountsForUser(bob.getUserId()).get(0);

        TransferDraft draft = transferService.initiateTransfer(
                alice.getUserId(),
                aliceAccount.getAccountId(),
                bobAccount.getAccountNumber(),
                new BigDecimal("50.00"),
                "Test invalid OTP"
        );

        // Attempt 1: wrong OTP
        assertThrows(ValidationException.class, () ->
                transferService.completeTransfer(draft.getDraftId(), "000000")
        );

        // Attempt 2: wrong OTP
        assertThrows(ValidationException.class, () ->
                transferService.completeTransfer(draft.getDraftId(), "111111")
        );

        // Attempt 3: wrong OTP -> Lockout
        ValidationException ex = assertThrows(ValidationException.class, () ->
                transferService.completeTransfer(draft.getDraftId(), "222222")
        );
        assertTrue(ex.getMessage().contains("Maximum OTP attempts exceeded"));
    }

    @Test
    @Order(9)
    @DisplayName("9. High concurrency transfer test: pessimistic locks prevent overdraft or double-spend")
    void testConcurrentTransfersSafety() throws Exception {
        // Register Charlie with ₹1,000 balance
        User charlie = authService.register(
                "charlie_concurrent",
                "Charlie Concurrent",
                "charlie@example.com",
                "9876543230",
                "CharlieSecret@123",
                "CharlieSecret@123",
                AccountType.SAVINGS
        );

        User bob = userDAO.findByEmailOrUsername("bob_builder").orElseThrow();

        Account charlieAccount = accountService.getAccountsForUser(charlie.getUserId()).get(0); // ₹1,000.00
        Account bobAccount = accountService.getAccountsForUser(bob.getUserId()).get(0);

        // We will create two concurrent threads trying to transfer ₹700 each simultaneously from Charlie to Bob.
        // Charlie only has ₹1,000.
        // Exactly ONE transaction can succeed. The second MUST fail with insufficient balance.
        // Balance must never become negative (-₹400).

        TransferDraft draft1 = transferService.initiateTransfer(
                charlie.getUserId(), charlieAccount.getAccountId(), bobAccount.getAccountNumber(),
                new BigDecimal("700.00"), "Concurrent 1"
        );
        String otp1 = SimulatedOtpDeliveryService.getInstance().getLastDeliveredOtp();

        // Create draft 2 for concurrency test
        TransferDraft draft2 = new TransferDraft(
                java.util.UUID.randomUUID().toString(),
                charlie.getUserId(),
                charlieAccount.getAccountId(),
                charlieAccount.getAccountNumber(),
                bobAccount.getAccountId(),
                bobAccount.getAccountNumber(),
                bobAccount.getOwnerName(),
                new BigDecimal("700.00"),
                "Concurrent 2"
        );
        // Save matching OTP for draft 2
        String otp2 = "888999";
        OtpRecord otpRecord2 = new OtpRecord();
        otpRecord2.setUserId(charlie.getUserId());
        otpRecord2.setOtpHash(com.banking.security.OtpUtil.hashOtp(otp2));
        otpRecord2.setPurpose("FUND_TRANSFER");
        otpRecord2.setReferenceId(draft2.getDraftId());
        otpRecord2.setExpiresAt(com.banking.security.OtpUtil.calculateExpiryTime(5));
        otpDAO.saveOtp(otpRecord2);

        // Put draft 2 into active drafts using reflection or initiate
        java.lang.reflect.Field field = TransferService.class.getDeclaredField("activeDrafts");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        java.util.Map<String, TransferDraft> draftsMap = (java.util.Map<String, TransferDraft>) field.get(transferService);
        draftsMap.put(draft2.getDraftId(), draft2);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CyclicBarrier barrier = new CyclicBarrier(2);

        AtomicInteger successes = new AtomicInteger(0);
        AtomicInteger failures = new AtomicInteger(0);

        Callable<Void> task1 = () -> {
            barrier.await();
            try {
                transferService.completeTransfer(draft1.getDraftId(), otp1);
                successes.incrementAndGet();
            } catch (Exception ex) {
                failures.incrementAndGet();
            }
            return null;
        };

        Callable<Void> task2 = () -> {
            barrier.await();
            try {
                transferService.completeTransfer(draft2.getDraftId(), otp2);
                successes.incrementAndGet();
            } catch (Exception ex) {
                failures.incrementAndGet();
            }
            return null;
        };

        Future<Void> f1 = executor.submit(task1);
        Future<Void> f2 = executor.submit(task2);

        f1.get(10, TimeUnit.SECONDS);
        f2.get(10, TimeUnit.SECONDS);
        executor.shutdown();

        assertEquals(1, successes.get(), "Exactly one concurrent transfer should succeed");
        assertEquals(1, failures.get(), "The competing concurrent transfer must be safely rejected");

        Account finalCharlie = accountDAO.findById(charlieAccount.getAccountId()).orElseThrow();
        assertEquals(new BigDecimal("300.00"), finalCharlie.getBalance(), "Final balance must be exactly ₹300.00 (not negative)");
    }
}
