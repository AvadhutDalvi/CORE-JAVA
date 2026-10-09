package com.banking.security;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utility for one-way cryptographic password hashing and verification using BCrypt.
 * Uses salt rounds = 12 for strong work factor.
 */
public class PasswordUtil {

    private static final int BCRYPT_LOG_ROUNDS = 12;

    /**
     * Hashes a plaintext password using BCrypt with a randomly generated salt.
     *
     * @param plainTextPassword the password to hash
     * @return the resulting BCrypt hash string
     */
    public static String hashPassword(String plainTextPassword) {
        if (plainTextPassword == null || plainTextPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty.");
        }
        return BCrypt.hashpw(plainTextPassword, BCrypt.gensalt(BCRYPT_LOG_ROUNDS));
    }

    /**
     * Verifies a candidate plaintext password against an existing BCrypt hash.
     *
     * @param plainTextPassword the password attempt
     * @param hashedPassword the stored BCrypt hash
     * @return true if the password matches, false otherwise
     */
    public static boolean checkPassword(String plainTextPassword, String hashedPassword) {
        if (plainTextPassword == null || hashedPassword == null) {
            return false;
        }
        try {
            return BCrypt.checkpw(plainTextPassword, hashedPassword);
        } catch (IllegalArgumentException e) {
            // Malformed hash
            return false;
        }
    }
}
