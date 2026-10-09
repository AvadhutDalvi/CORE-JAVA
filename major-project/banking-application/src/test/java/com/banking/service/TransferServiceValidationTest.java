package com.banking.service;

import com.banking.dao.*;
import com.banking.security.SimulatedOtpDeliveryService;
import com.banking.util.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Fund Transfer Business Rule & Amount Validation Tests")
public class TransferServiceValidationTest {

    private TransferService transferService;

    @BeforeEach
    void setUp() {
        transferService = new TransferService(
                new AccountDAO(),
                new TransactionDAO(),
                new LedgerDAO(),
                new OtpDAO(),
                new UserDAO(),
                new AuditService(),
                SimulatedOtpDeliveryService.getInstance()
        );
    }

    @Test
    @DisplayName("Should reject null transfer amount")
    void testNullAmount() {
        ValidationException ex = assertThrows(ValidationException.class, () ->
                transferService.initiateTransfer(1, 101, "10010000201", null, "test")
        );
        assertTrue(ex.getMessage().contains("amount is required"));
    }

    @Test
    @DisplayName("Should reject zero or sub-minimum transfer amount")
    void testSubMinimumAmount() {
        ValidationException ex = assertThrows(ValidationException.class, () ->
                transferService.initiateTransfer(1, 101, "10010000201", new BigDecimal("0.50"), "test")
        );
        assertTrue(ex.getMessage().contains("Minimum transfer amount"));
    }

    @Test
    @DisplayName("Should reject negative transfer amount")
    void testNegativeAmount() {
        ValidationException ex = assertThrows(ValidationException.class, () ->
                transferService.initiateTransfer(1, 101, "10010000201", new BigDecimal("-100.00"), "test")
        );
        assertTrue(ex.getMessage().contains("Minimum transfer amount"));
    }

    @Test
    @DisplayName("Should reject excessive transfer amount beyond limit")
    void testExcessiveAmount() {
        ValidationException ex = assertThrows(ValidationException.class, () ->
                transferService.initiateTransfer(1, 101, "10010000201", new BigDecimal("600000.00"), "test")
        );
        assertTrue(ex.getMessage().contains("maximum per-transaction limit"));
    }

    @Test
    @DisplayName("Should reject amounts with more than two decimal places")
    void testInvalidScale() {
        ValidationException ex = assertThrows(ValidationException.class, () ->
                transferService.initiateTransfer(1, 101, "10010000201", new BigDecimal("50.123"), "test")
        );
        assertTrue(ex.getMessage().contains("more than 2 decimal places"));
    }
}
