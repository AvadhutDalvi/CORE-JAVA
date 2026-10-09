package com.banking.service;

import com.banking.dao.AccountDAO;
import com.banking.dao.UserDAO;
import com.banking.model.AccountType;
import com.banking.util.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Authentication Business Rule Validation Tests")
public class AuthServiceValidationTest {

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(new UserDAO(), new AccountDAO(), new AuditService());
    }

    @Test
    @DisplayName("Should reject registration with invalid username format")
    void testInvalidUsername() {
        ValidationException ex = assertThrows(ValidationException.class, () ->
                authService.register("ab", "Valid Name", "user@example.com", "9876543210",
                        "SecretPass@123", "SecretPass@123", AccountType.SAVINGS)
        );
        assertTrue(ex.getMessage().contains("Username"));
    }

    @Test
    @DisplayName("Should reject registration with malformed email")
    void testInvalidEmail() {
        ValidationException ex = assertThrows(ValidationException.class, () ->
                authService.register("validuser", "Valid Name", "invalid-email-format", "9876543210",
                        "SecretPass@123", "SecretPass@123", AccountType.SAVINGS)
        );
        assertTrue(ex.getMessage().contains("email"));
    }

    @Test
    @DisplayName("Should reject registration with short password")
    void testShortPassword() {
        ValidationException ex = assertThrows(ValidationException.class, () ->
                authService.register("validuser", "Valid Name", "valid@example.com", "9876543210",
                        "short", "short", AccountType.SAVINGS)
        );
        assertTrue(ex.getMessage().contains("at least 8 characters"));
    }

    @Test
    @DisplayName("Should reject registration with mismatched passwords")
    void testMismatchedPassword() {
        ValidationException ex = assertThrows(ValidationException.class, () ->
                authService.register("validuser", "Valid Name", "valid@example.com", "9876543210",
                        "Password@123", "Password@999", AccountType.SAVINGS)
        );
        assertTrue(ex.getMessage().contains("Passwords do not match"));
    }

    @Test
    @DisplayName("Should reject login with blank credentials")
    void testBlankLoginCredentials() {
        assertThrows(ValidationException.class, () -> authService.login("", "password"));
        assertThrows(ValidationException.class, () -> authService.login("user", ""));
    }
}
