package org.example.service;

import org.example.dao.BookDAO;
import org.example.dao.DashboardDAO;
import org.example.dao.MemberDAO;
import org.example.dao.TransactionDAO;
import org.example.model.Book;
import org.example.model.Member;
import org.example.model.Transaction;
import org.example.util.ValidationException;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class LibraryServiceTest {

    private static BookService bookService;
    private static MemberService memberService;
    private static TransactionService transactionService;
    private static DashboardDAO dashboardDAO;

    private static int testBookId;
    private static int testMemberId;
    private static int testTxId;

    @BeforeAll
    static void setUp() {
        bookService = new BookService();
        memberService = new MemberService();
        transactionService = new TransactionService();
        dashboardDAO = new DashboardDAO();
    }

    // ==========================================
    // BOOK TESTS
    // ==========================================

    @Test
    @Order(1)
    void testAddInvalidBook_BlankTitle() {
        Book book = new Book("", "Test Author", "Tech", "TEST-ISBN-01", 5, 5, 2024);
        ValidationException ex = assertThrows(ValidationException.class, () -> bookService.addBook(book));
        assertTrue(ex.getMessage().contains("title is required"));
    }

    @Test
    @Order(2)
    void testAddInvalidBook_InvalidQuantity() {
        Book book = new Book("Valid Title", "Test Author", "Tech", "TEST-ISBN-02", 0, 0, 2024);
        ValidationException ex = assertThrows(ValidationException.class, () -> bookService.addBook(book));
        assertTrue(ex.getMessage().contains("quantity must be greater than 0"));
    }

    @Test
    @Order(3)
    void testAddInvalidBook_InvalidYear() {
        Book book = new Book("Valid Title", "Test Author", "Tech", "TEST-ISBN-03", 5, 5, 800);
        ValidationException ex = assertThrows(ValidationException.class, () -> bookService.addBook(book));
        assertTrue(ex.getMessage().contains("Published year must be between 1000"));
    }

    @Test
    @Order(4)
    void testAddValidBook() {
        String uniqueIsbn = "TEST-ISBN-" + System.currentTimeMillis();
        Book book = new Book("Automated Test Book", "Unit Test Author", "Testing", uniqueIsbn, 3, 3, 2024);
        boolean added = bookService.addBook(book);
        assertTrue(added);

        List<Book> search = bookService.searchBooks("Automated Test Book");
        assertFalse(search.isEmpty());
        testBookId = search.get(0).getBookId();
        assertTrue(testBookId > 0);
        assertEquals(3, search.get(0).getQuantity());
        assertEquals(3, search.get(0).getAvailableQuantity());
    }

    @Test
    @Order(5)
    void testAddDuplicateIsbnBook() {
        Book existing = bookService.getBookById(testBookId);
        assertNotNull(existing);

        Book duplicate = new Book("Duplicate ISBN Book", "Author", "Testing", existing.getIsbn(), 2, 2, 2024);
        ValidationException ex = assertThrows(ValidationException.class, () -> bookService.addBook(duplicate));
        assertTrue(ex.getMessage().contains("already exists"));
    }

    @Test
    @Order(6)
    void testUpdateBook() {
        Book book = bookService.getBookById(testBookId);
        assertNotNull(book);

        book.setTitle("Automated Test Book (Updated)");
        book.setQuantity(4);
        boolean updated = bookService.updateBook(book);
        assertTrue(updated);

        Book refreshed = bookService.getBookById(testBookId);
        assertEquals("Automated Test Book (Updated)", refreshed.getTitle());
        assertEquals(4, refreshed.getQuantity());
        assertEquals(4, refreshed.getAvailableQuantity());
    }

    @Test
    @Order(7)
    void testSearchBook() {
        List<Book> results = bookService.searchBooks("Automated Test Book");
        assertFalse(results.isEmpty());
        assertTrue(results.stream().anyMatch(b -> b.getBookId() == testBookId));
    }

    // ==========================================
    // MEMBER TESTS
    // ==========================================

    @Test
    @Order(8)
    void testAddInvalidMember_BlankName() {
        Member member = new Member("", "test@example.com", "9876543210", "Address");
        ValidationException ex = assertThrows(ValidationException.class, () -> memberService.addMember(member));
        assertTrue(ex.getMessage().contains("name is required"));
    }

    @Test
    @Order(9)
    void testAddInvalidMember_InvalidEmail() {
        Member member = new Member("Test User", "not-an-email", "9876543210", "Address");
        ValidationException ex = assertThrows(ValidationException.class, () -> memberService.addMember(member));
        assertTrue(ex.getMessage().contains("Invalid email address format"));
    }

    @Test
    @Order(10)
    void testAddInvalidMember_InvalidPhone() {
        Member member = new Member("Test User", "test_user_phone@example.com", "123", "Address");
        ValidationException ex = assertThrows(ValidationException.class, () -> memberService.addMember(member));
        assertTrue(ex.getMessage().contains("Invalid phone number"));
    }

    @Test
    @Order(11)
    void testAddValidMember() {
        String uniqueEmail = "testuser_" + System.currentTimeMillis() + "@example.com";
        Member member = new Member("Unit Test Member", uniqueEmail, "9988776655", "Test City");
        boolean added = memberService.addMember(member);
        assertTrue(added);

        List<Member> search = memberService.searchMembers("Unit Test Member");
        assertFalse(search.isEmpty());
        testMemberId = search.get(0).getMemberId();
        assertTrue(testMemberId > 0);
    }

    @Test
    @Order(12)
    void testAddDuplicateEmailMember() {
        Member existing = memberService.getMemberById(testMemberId);
        assertNotNull(existing);

        Member duplicate = new Member("Another User", existing.getEmail(), "9988776655", "City");
        ValidationException ex = assertThrows(ValidationException.class, () -> memberService.addMember(duplicate));
        assertTrue(ex.getMessage().contains("already exists"));
    }

    @Test
    @Order(13)
    void testUpdateMember() {
        Member member = memberService.getMemberById(testMemberId);
        assertNotNull(member);

        member.setName("Unit Test Member (Updated)");
        boolean updated = memberService.updateMember(member);
        assertTrue(updated);

        Member refreshed = memberService.getMemberById(testMemberId);
        assertEquals("Unit Test Member (Updated)", refreshed.getName());
    }

    @Test
    @Order(14)
    void testSearchMember() {
        List<Member> results = memberService.searchMembers("Unit Test Member");
        assertFalse(results.isEmpty());
        assertTrue(results.stream().anyMatch(m -> m.getMemberId() == testMemberId));
    }

    // ==========================================
    // TRANSACTION TESTS
    // ==========================================

    @Test
    @Order(15)
    void testIssueBook_InvalidIds() {
        ValidationException ex1 = assertThrows(ValidationException.class, () -> transactionService.issueBook(0, 1, 14));
        assertTrue(ex1.getMessage().contains("Book ID must be a positive"));

        ValidationException ex2 = assertThrows(ValidationException.class, () -> transactionService.issueBook(1, 0, 14));
        assertTrue(ex2.getMessage().contains("Member ID must be a positive"));

        ValidationException ex3 = assertThrows(ValidationException.class, () -> transactionService.issueBook(1, 1, 0));
        assertTrue(ex3.getMessage().contains("Loan days must be greater than 0"));
    }

    @Test
    @Order(16)
    void testIssueBook_BookNotFound() {
        ValidationException ex = assertThrows(ValidationException.class, () -> transactionService.issueBook(999999, testMemberId, 14));
        assertTrue(ex.getMessage().contains("Book not found"));
    }

    @Test
    @Order(17)
    void testIssueBook_MemberNotFound() {
        ValidationException ex = assertThrows(ValidationException.class, () -> transactionService.issueBook(testBookId, 999999, 14));
        assertTrue(ex.getMessage().contains("Member not found"));
    }

    @Test
    @Order(18)
    void testIssueValidBook_QuantityDecreases() {
        Book beforeBook = bookService.getBookById(testBookId);
        int availableBefore = beforeBook.getAvailableQuantity();
        int issuedCountBefore = dashboardDAO.getIssuedBooks();

        boolean issued = transactionService.issueBook(testBookId, testMemberId, 14);
        assertTrue(issued);

        Book afterBook = bookService.getBookById(testBookId);
        assertEquals(availableBefore - 1, afterBook.getAvailableQuantity());
        assertEquals(issuedCountBefore + 1, dashboardDAO.getIssuedBooks());

        List<Transaction> txList = transactionService.getAllTransactions();
        assertFalse(txList.isEmpty());
        Transaction latest = txList.get(0);
        assertEquals(testBookId, latest.getBookId());
        assertEquals(testMemberId, latest.getMemberId());
        assertEquals("BORROWED", latest.getStatus());
        testTxId = latest.getTransactionId();
    }

    @Test
    @Order(19)
    void testIssueDuplicateActiveBorrowing_Blocked() {
        // Same member borrowing same book while already borrowed
        ValidationException ex = assertThrows(ValidationException.class,
                () -> transactionService.issueBook(testBookId, testMemberId, 14));
        assertTrue(ex.getMessage().contains("already has this book issued"));
    }

    @Test
    @Order(20)
    void testDeleteBook_WithActiveLoan_Blocked() {
        ValidationException ex = assertThrows(ValidationException.class, () -> bookService.deleteBook(testBookId));
        assertTrue(ex.getMessage().contains("active borrowed copies"));
    }

    @Test
    @Order(21)
    void testDeleteMember_WithActiveLoan_Blocked() {
        ValidationException ex = assertThrows(ValidationException.class, () -> memberService.deleteMember(testMemberId));
        assertTrue(ex.getMessage().contains("currently has borrowed books"));
    }

    @Test
    @Order(22)
    void testReturnBook_QuantityIncreases() {
        Book beforeBook = bookService.getBookById(testBookId);
        int availableBefore = beforeBook.getAvailableQuantity();
        int issuedCountBefore = dashboardDAO.getIssuedBooks();

        boolean returned = transactionService.returnBook(testTxId, testBookId);
        assertTrue(returned);

        Book afterBook = bookService.getBookById(testBookId);
        assertEquals(availableBefore + 1, afterBook.getAvailableQuantity());
        assertEquals(issuedCountBefore - 1, dashboardDAO.getIssuedBooks());

        List<Transaction> txList = transactionService.getAllTransactions();
        Transaction updatedTx = txList.stream().filter(t -> t.getTransactionId() == testTxId).findFirst().orElse(null);
        assertNotNull(updatedTx);
        assertEquals("RETURNED", updatedTx.getStatus());
        assertNotNull(updatedTx.getReturnDate());
    }

    @Test
    @Order(23)
    void testReturnAlreadyReturnedBook_Blocked() {
        ValidationException ex = assertThrows(ValidationException.class,
                () -> transactionService.returnBook(testTxId, testBookId));
        assertTrue(ex.getMessage().contains("already been returned"));
    }

    // ==========================================
    // DASHBOARD & CLEANUP TESTS
    // ==========================================

    @Test
    @Order(24)
    void testDashboardStatistics() {
        assertTrue(dashboardDAO.getTotalBooks() > 0);
        assertTrue(dashboardDAO.getTotalMembers() > 0);
        assertTrue(dashboardDAO.getAvailableBooks() >= 0);
        assertTrue(dashboardDAO.getIssuedBooks() >= 0);
        assertTrue(dashboardDAO.getOverdueBooks() >= 0);
    }

    @AfterAll
    static void tearDown() {
        // Clean up test transaction, book, and member created during the tests
        try {
            org.example.util.DBConnection.getConnection().createStatement().executeUpdate(
                    "DELETE FROM transactions WHERE transaction_id = " + testTxId
            );
            if (testBookId > 0) {
                bookService.deleteBook(testBookId);
            }
            if (testMemberId > 0) {
                memberService.deleteMember(testMemberId);
            }
        } catch (Exception e) {
            System.err.println("Test cleanup notice: " + e.getMessage());
        }
    }
}
