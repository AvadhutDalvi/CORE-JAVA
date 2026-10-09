package com.banking.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Password Security & BCrypt Tests")
public class PasswordUtilTest {

    @Test
    @DisplayName("Should generate valid BCrypt hash starting with $2a$")
    void testHashPasswordValid() {
        String rawPassword = "StrongPassword@2026";
        String hash = PasswordUtil.hashPassword(rawPassword);

        assertNotNull(hash);
        assertTrue(hash.startsWith("$2a$"), "Hash should follow BCrypt format");
        assertNotEquals(rawPassword, hash, "Hash must never equal plain password");
    }

    @Test
    @DisplayName("Should generate unique hashes for identical passwords due to salt")
    void testUniqueSalts() {
        String password = "IdenticalPassword@123";
        String hash1 = PasswordUtil.hashPassword(password);
        String hash2 = PasswordUtil.hashPassword(password);

        assertNotEquals(hash1, hash2, "Distinct salts must produce distinct hashes");
        assertTrue(PasswordUtil.checkPassword(password, hash1));
        assertTrue(PasswordUtil.checkPassword(password, hash2));
    }

    @Test
    @DisplayName("Should successfully verify correct password")
    void testCheckPasswordSuccess() {
        String raw = "SecretPassword123";
        String hash = PasswordUtil.hashPassword(raw);

        assertTrue(PasswordUtil.checkPassword(raw, hash));
    }

    @Test
    @DisplayName("Should reject incorrect password")
    void testCheckPasswordFailure() {
        String raw = "SecretPassword123";
        String hash = PasswordUtil.hashPassword(raw);

        assertFalse(PasswordUtil.checkPassword("WrongPassword123", hash));
    }

    @Test
    @DisplayName("Should reject null or empty password inputs")
    void testNullOrEmptyInput() {
        assertThrows(IllegalArgumentException.class, () -> PasswordUtil.hashPassword(null));
        assertThrows(IllegalArgumentException.class, () -> PasswordUtil.hashPassword("   "));
        assertFalse(PasswordUtil.checkPassword(null, "hash"));
        assertFalse(PasswordUtil.checkPassword("pwd", null));
    }
}
