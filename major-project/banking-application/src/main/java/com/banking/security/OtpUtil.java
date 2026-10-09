package com.banking.security;

import org.mindrot.jbcrypt.BCrypt;

import java.security.SecureRandom;
import java.time.LocalDateTime;

/**
 * Utility for generating and verifying cryptographically secure One-Time Passwords (OTPs).
 */
public class OtpUtil {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    public static final int OTP_LENGTH = 6;
    public static final int DEFAULT_EXPIRY_MINUTES = 5;
    public static final int DEFAULT_MAX_ATTEMPTS = 3;

    /**
     * Generates a cryptographically random numeric 6-digit OTP string.
     */
    public static String generateOtp() {
        int bound = (int) Math.pow(10, OTP_LENGTH);
        int min = (int) Math.pow(10, OTP_LENGTH - 1);
        int otpNumber = min + SECURE_RANDOM.nextInt(bound - min);
        return String.valueOf(otpNumber);
    }

    /**
     * Hashes an OTP before persisting in the database.
     */
    public static String hashOtp(String plainOtp) {
        if (plainOtp == null || plainOtp.trim().isEmpty()) {
            throw new IllegalArgumentException("OTP cannot be empty.");
        }
        return BCrypt.hashpw(plainOtp, BCrypt.gensalt(10));
    }

    /**
     * Verifies candidate OTP against its hash.
     */
    public static boolean verifyOtp(String candidateOtp, String storedHash) {
        if (candidateOtp == null || storedHash == null) {
            return false;
        }
        try {
            return BCrypt.checkpw(candidateOtp, storedHash);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Calculates expiry timestamp based on configured minutes.
     */
    public static LocalDateTime calculateExpiryTime(int minutes) {
        return LocalDateTime.now().plusMinutes(minutes);
    }
}
