package com.banking.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OTP Generation & Validation Tests")
public class OtpUtilTest {

    @Test
    @DisplayName("Should generate 6-digit numeric OTP")
    void testGenerateOtp() {
        for (int i = 0; i < 20; i++) {
            String otp = OtpUtil.generateOtp();
            assertNotNull(otp);
            assertEquals(6, otp.length(), "OTP must be exactly 6 digits");
            assertTrue(otp.matches("^[0-9]{6}$"), "OTP must contain only numbers");
        }
    }

    @Test
    @DisplayName("Should hash and verify OTP properly")
    void testHashAndVerifyOtp() {
        String plainOtp = "654321";
        String hash = OtpUtil.hashOtp(plainOtp);

        assertNotNull(hash);
        assertNotEquals(plainOtp, hash);
        assertTrue(OtpUtil.verifyOtp("654321", hash));
        assertFalse(OtpUtil.verifyOtp("123456", hash));
    }

    @Test
    @DisplayName("Should calculate valid future expiry timestamp")
    void testCalculateExpiryTime() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiry = OtpUtil.calculateExpiryTime(5);

        assertTrue(expiry.isAfter(now));
        assertTrue(expiry.isBefore(now.plusMinutes(6)));
    }
}
