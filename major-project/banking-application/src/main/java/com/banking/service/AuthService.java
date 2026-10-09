package com.banking.service;

import com.banking.dao.AccountDAO;
import com.banking.dao.UserDAO;
import com.banking.model.*;
import com.banking.security.PasswordUtil;
import com.banking.security.UserSession;
import com.banking.util.ValidationException;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Authentication and User Registration Service.
 * Implements secure password hashing via BCrypt and generic error reporting.
 */
public class AuthService {

    private static final Pattern EMAIL_PATTERN = 
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern USERNAME_PATTERN = 
            Pattern.compile("^[a-zA-Z0-9_]{3,30}$");

    private final UserDAO userDAO;
    private final AccountDAO accountDAO;
    private final AuditService auditService;

    public AuthService() {
        this.userDAO = new UserDAO();
        this.accountDAO = new AccountDAO();
        this.auditService = new AuditService();
    }

    public AuthService(UserDAO userDAO, AccountDAO accountDAO, AuditService auditService) {
        this.userDAO = userDAO;
        this.accountDAO = accountDAO;
        this.auditService = auditService;
    }

    /**
     * Registers a new customer and generates their primary bank account.
     */
    public User register(String username, String fullName, String email, String phone,
                         String password, String confirmPassword, AccountType accountType) {

        // 1. Validation
        if (username == null || !USERNAME_PATTERN.matcher(username.trim()).matches()) {
            throw new ValidationException("Username must be between 3 and 30 alphanumeric characters.");
        }
        if (fullName == null || fullName.trim().length() < 2) {
            throw new ValidationException("Full name is required (at least 2 characters).");
        }
        if (email == null || !EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new ValidationException("Please provide a valid email address.");
        }
        if (phone == null || phone.trim().replaceAll("[^0-9]", "").length() < 10) {
            throw new ValidationException("Phone number must have at least 10 digits.");
        }
        if (password == null || password.length() < 8) {
            throw new ValidationException("Password must be at least 8 characters long.");
        }
        if (!password.equals(confirmPassword)) {
            throw new ValidationException("Passwords do not match.");
        }

        String cleanUsername = username.trim().toLowerCase();
        String cleanEmail = email.trim().toLowerCase();
        String cleanFullName = fullName.trim();
        String cleanPhone = phone.trim();

        // 2. Uniqueness checks
        if (userDAO.isUsernameExists(cleanUsername, 0)) {
            throw new ValidationException("The username '" + cleanUsername + "' is already taken.");
        }
        if (userDAO.isEmailExists(cleanEmail, 0)) {
            throw new ValidationException("An account with email '" + cleanEmail + "' already exists.");
        }

        // 3. Password hashing using BCrypt
        String passwordHash = PasswordUtil.hashPassword(password);

        // 4. Create User entity
        User newUser = new User();
        newUser.setUsername(cleanUsername);
        newUser.setFullName(cleanFullName);
        newUser.setEmail(cleanEmail);
        newUser.setPhone(cleanPhone);
        newUser.setPasswordHash(passwordHash);
        newUser.setRole(UserRole.CUSTOMER);
        newUser.setStatus(UserStatus.ACTIVE);

        boolean userCreated = userDAO.createUser(newUser);
        if (!userCreated) {
            auditService.log(null, cleanUsername, AuditEventType.REGISTRATION, "User creation failed in database", false);
            throw new ValidationException("Registration failed due to a database error. Please try again.");
        }

        // 5. Create default initial bank account for the user with an initial welcome balance of ₹1,000.00
        Account initialAccount = new Account();
        initialAccount.setUserId(newUser.getUserId());
        initialAccount.setAccountNumber(accountDAO.generateUniqueAccountNumber());
        initialAccount.setAccountType(accountType != null ? accountType : AccountType.SAVINGS);
        initialAccount.setBalance(new BigDecimal("1000.00"));
        initialAccount.setStatus(AccountStatus.ACTIVE);

        accountDAO.createAccount(initialAccount);

        auditService.log(newUser.getUserId(), cleanUsername, AuditEventType.REGISTRATION,
                "User registered with primary account " + initialAccount.getAccountNumber(), true);
        auditService.log(newUser.getUserId(), cleanUsername, AuditEventType.ACCOUNT_CREATED,
                "Initial account created with ₹1,000.00 welcome balance", true);

        return newUser;
    }

    /**
     * Authenticates a user.
     * Uses generic error messages to prevent email/username enumeration.
     */
    public User login(String identifier, String password) {
        if (identifier == null || identifier.trim().isEmpty() || password == null || password.isEmpty()) {
            throw new ValidationException("Please enter both username/email and password.");
        }

        String cleanIdentifier = identifier.trim();
        Optional<User> optionalUser = userDAO.findByEmailOrUsername(cleanIdentifier);

        // Fail generically if user not found
        if (optionalUser.isEmpty()) {
            auditService.log(null, cleanIdentifier, AuditEventType.LOGIN_FAILURE, "User identifier not found", false);
            throw new ValidationException("Invalid email/username or password.");
        }

        User user = optionalUser.get();

        // Check user account status
        if (user.getStatus() == UserStatus.LOCKED) {
            auditService.log(user.getUserId(), user.getUsername(), AuditEventType.LOGIN_FAILURE, "Account locked", false);
            throw new ValidationException("Your account is locked. Please contact bank support.");
        }
        if (user.getStatus() == UserStatus.INACTIVE) {
            auditService.log(user.getUserId(), user.getUsername(), AuditEventType.LOGIN_FAILURE, "Account inactive", false);
            throw new ValidationException("Your account is inactive. Please contact bank support.");
        }

        // Verify BCrypt password
        boolean matches = PasswordUtil.checkPassword(password, user.getPasswordHash());
        if (!matches) {
            auditService.log(user.getUserId(), user.getUsername(), AuditEventType.LOGIN_FAILURE, "Password mismatch", false);
            throw new ValidationException("Invalid email/username or password.");
        }

        // Initialize active session
        UserSession.startSession(user);

        auditService.log(user.getUserId(), user.getUsername(), AuditEventType.LOGIN_SUCCESS, "Login successful", true);
        return user;
    }

    /**
     * Logs out the currently authenticated user.
     */
    public void logout() {
        if (UserSession.isAuthenticated()) {
            User user = UserSession.getCurrentSession().getUser();
            auditService.log(user.getUserId(), user.getUsername(), AuditEventType.LOGOUT, "User logged out", true);
            UserSession.clearSession();
        }
    }
}
