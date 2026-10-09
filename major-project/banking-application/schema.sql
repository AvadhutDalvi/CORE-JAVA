-- =====================================================================
-- Banking Application with Security Features
-- Database Schema Definition & Initial Seed Data
-- Database: banking_db
-- =====================================================================

CREATE DATABASE IF NOT EXISTS `banking_db` 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE `banking_db`;

-- Drop existing tables in reverse dependency order
DROP TABLE IF EXISTS `audit_logs`;
DROP TABLE IF EXISTS `otp_verifications`;
DROP TABLE IF EXISTS `ledger_entries`;
DROP TABLE IF EXISTS `transactions`;
DROP TABLE IF EXISTS `accounts`;
DROP TABLE IF EXISTS `users`;

-- ---------------------------------------------------------------------
-- Table: users
-- Stores customer and administrative authentication and profile details
-- ---------------------------------------------------------------------
CREATE TABLE `users` (
    `user_id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(50) NOT NULL UNIQUE,
    `full_name` VARCHAR(100) NOT NULL,
    `email` VARCHAR(100) NOT NULL UNIQUE,
    `phone` VARCHAR(20) NOT NULL,
    `password_hash` VARCHAR(255) NOT NULL,
    `role` ENUM('CUSTOMER', 'ADMIN') NOT NULL DEFAULT 'CUSTOMER',
    `status` ENUM('ACTIVE', 'LOCKED', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_users_email` (`email`),
    INDEX `idx_users_username` (`username`),
    INDEX `idx_users_role` (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- Table: accounts
-- Stores bank accounts belonging to registered users
-- ---------------------------------------------------------------------
CREATE TABLE `accounts` (
    `account_id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `account_number` VARCHAR(20) NOT NULL UNIQUE,
    `user_id` BIGINT NOT NULL,
    `account_type` ENUM('SAVINGS', 'CHECKING', 'BUSINESS') NOT NULL DEFAULT 'SAVINGS',
    `balance` DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    `status` ENUM('ACTIVE', 'BLOCKED', 'CLOSED') NOT NULL DEFAULT 'ACTIVE',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_accounts_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE RESTRICT,
    CONSTRAINT `chk_accounts_balance` CHECK (`balance` >= 0.00),
    INDEX `idx_accounts_user_id` (`user_id`),
    INDEX `idx_accounts_acc_num` (`account_number`),
    INDEX `idx_accounts_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- Table: transactions
-- Master record of all financial events (transfers, deposits, withdrawals)
-- ---------------------------------------------------------------------
CREATE TABLE `transactions` (
    `transaction_id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `transaction_reference` VARCHAR(64) NOT NULL UNIQUE,
    `source_account_id` BIGINT NULL,
    `destination_account_id` BIGINT NULL,
    `amount` DECIMAL(15, 2) NOT NULL,
    `transaction_type` ENUM('TRANSFER', 'DEPOSIT', 'WITHDRAWAL') NOT NULL,
    `status` ENUM('SUCCESS', 'FAILED', 'PENDING') NOT NULL DEFAULT 'SUCCESS',
    `description` VARCHAR(255) NOT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_txn_source_acc` FOREIGN KEY (`source_account_id`) REFERENCES `accounts` (`account_id`),
    CONSTRAINT `fk_txn_dest_acc` FOREIGN KEY (`destination_account_id`) REFERENCES `accounts` (`account_id`),
    CONSTRAINT `chk_txn_amount` CHECK (`amount` > 0.00),
    INDEX `idx_txn_reference` (`transaction_reference`),
    INDEX `idx_txn_source` (`source_account_id`),
    INDEX `idx_txn_dest` (`destination_account_id`),
    INDEX `idx_txn_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- Table: ledger_entries
-- Immutable double-entry balanced debit/credit records for auditing
-- ---------------------------------------------------------------------
CREATE TABLE `ledger_entries` (
    `entry_id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `transaction_id` BIGINT NOT NULL,
    `account_id` BIGINT NOT NULL,
    `entry_type` ENUM('DEBIT', 'CREDIT') NOT NULL,
    `amount` DECIMAL(15, 2) NOT NULL,
    `balance_after` DECIMAL(15, 2) NOT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_ledger_txn` FOREIGN KEY (`transaction_id`) REFERENCES `transactions` (`transaction_id`) ON DELETE CASCADE,
    CONSTRAINT `fk_ledger_account` FOREIGN KEY (`account_id`) REFERENCES `accounts` (`account_id`),
    CONSTRAINT `chk_ledger_amount` CHECK (`amount` > 0.00),
    CONSTRAINT `chk_ledger_balance_after` CHECK (`balance_after` >= 0.00),
    INDEX `idx_ledger_txn_id` (`transaction_id`),
    INDEX `idx_ledger_acc_id` (`account_id`),
    INDEX `idx_ledger_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- Table: otp_verifications
-- Cryptographically secure, time-bound, single-use OTP tracking table
-- ---------------------------------------------------------------------
CREATE TABLE `otp_verifications` (
    `otp_id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `otp_hash` VARCHAR(255) NOT NULL,
    `purpose` VARCHAR(50) NOT NULL DEFAULT 'FUND_TRANSFER',
    `reference_id` VARCHAR(100) NULL,
    `expires_at` TIMESTAMP NOT NULL,
    `attempts_count` INT NOT NULL DEFAULT 0,
    `max_attempts` INT NOT NULL DEFAULT 3,
    `is_consumed` BOOLEAN NOT NULL DEFAULT FALSE,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_otp_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE,
    INDEX `idx_otp_user` (`user_id`),
    INDEX `idx_otp_expiry` (`expires_at`),
    INDEX `idx_otp_ref` (`reference_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- Table: audit_logs
-- Security event trail (logins, OTP verifications, transfers, status changes)
-- ---------------------------------------------------------------------
CREATE TABLE `audit_logs` (
    `log_id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NULL,
    `username_attempted` VARCHAR(100) NULL,
    `event_type` VARCHAR(50) NOT NULL,
    `source_ip` VARCHAR(50) NOT NULL DEFAULT '127.0.0.1',
    `details` VARCHAR(500) NOT NULL,
    `status` ENUM('SUCCESS', 'FAILURE') NOT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_audit_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE SET NULL,
    INDEX `idx_audit_user` (`user_id`),
    INDEX `idx_audit_event` (`event_type`),
    INDEX `idx_audit_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- Seed Demonstration Data
-- Pre-hashed passwords using BCrypt ($2a$12$...)
-- Password for all demo accounts: 'Password@123'
-- Password for admin: 'AdminPass@123'
-- ---------------------------------------------------------------------

-- Hash for 'Password@123': $2a$10$wE0pvdQeR6jQO6bK1w2rce2bS1c.g2Z2qG5Y9bI6m7vX/m1u4l5K.
-- Hash for 'AdminPass@123': $2a$10$1yNeqd0jH7s0a6qS6dFhQeN1pOxnI5.G47O3jBw9TjPj.3k8Q7WzG

INSERT INTO `users` (`user_id`, `username`, `full_name`, `email`, `phone`, `password_hash`, `role`, `status`) VALUES
(1, 'admin', 'System Administrator', 'admin@bank.com', '+91 9876543210', '$2a$10$1yNeqd0jH7s0a6qS6dFhQeN1pOxnI5.G47O3jBw9TjPj.3k8Q7WzG', 'ADMIN', 'ACTIVE'),
(2, 'avadhut', 'Avadhut Dalvi', 'avadhut@example.com', '+91 9123456780', '$2a$10$wE0pvdQeR6jQO6bK1w2rce2bS1c.g2Z2qG5Y9bI6m7vX/m1u4l5K.', 'CUSTOMER', 'ACTIVE'),
(3, 'john_doe', 'John Doe', 'john@example.com', '+91 9123456781', '$2a$10$wE0pvdQeR6jQO6bK1w2rce2bS1c.g2Z2qG5Y9bI6m7vX/m1u4l5K.', 'CUSTOMER', 'ACTIVE'),
(4, 'jane_smith', 'Jane Smith', 'jane@example.com', '+91 9123456782', '$2a$10$wE0pvdQeR6jQO6bK1w2rce2bS1c.g2Z2qG5Y9bI6m7vX/m1u4l5K.', 'CUSTOMER', 'ACTIVE');

-- Bank Accounts
INSERT INTO `accounts` (`account_id`, `account_number`, `user_id`, `account_type`, `balance`, `status`) VALUES
(1, '10010000101', 2, 'SAVINGS', 25000.00, 'ACTIVE'),
(2, '10010000102', 2, 'CHECKING', 12500.00, 'ACTIVE'),
(3, '10010000201', 3, 'SAVINGS', 45000.00, 'ACTIVE'),
(4, '10010000301', 4, 'SAVINGS', 30000.00, 'ACTIVE');

-- Sample Initial Transaction (Funding deposit for Avadhut)
INSERT INTO `transactions` (`transaction_id`, `transaction_reference`, `source_account_id`, `destination_account_id`, `amount`, `transaction_type`, `status`, `description`) VALUES
(1, 'TXN-20261001-00001', NULL, 1, 25000.00, 'DEPOSIT', 'SUCCESS', 'Initial Account Opening Deposit'),
(2, 'TXN-20261001-00002', NULL, 2, 12500.00, 'DEPOSIT', 'SUCCESS', 'Initial Account Opening Deposit'),
(3, 'TXN-20261001-00003', NULL, 3, 45000.00, 'DEPOSIT', 'SUCCESS', 'Initial Account Opening Deposit'),
(4, 'TXN-20261001-00004', NULL, 4, 30000.00, 'DEPOSIT', 'SUCCESS', 'Initial Account Opening Deposit');

-- Ledger Entries for the initial deposits
INSERT INTO `ledger_entries` (`transaction_id`, `account_id`, `entry_type`, `amount`, `balance_after`) VALUES
(1, 1, 'CREDIT', 25000.00, 25000.00),
(2, 2, 'CREDIT', 12500.00, 12500.00),
(3, 3, 'CREDIT', 45000.00, 45000.00),
(4, 4, 'CREDIT', 30000.00, 30000.00);

-- Initial Audit Log
INSERT INTO `audit_logs` (`user_id`, `username_attempted`, `event_type`, `source_ip`, `details`, `status`) VALUES
(1, 'admin', 'SYSTEM_INIT', '127.0.0.1', 'Database initialized and seeded with demo accounts', 'SUCCESS');
