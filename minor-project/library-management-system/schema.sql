-- ====================================================================
-- LIBRARY MANAGEMENT SYSTEM - DATABASE SCHEMA
-- Minor Project: Core Java + Swing + JDBC + MySQL
-- Database: library_db
-- ====================================================================

CREATE DATABASE IF NOT EXISTS `library_db`
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;

USE `library_db`;

-- ====================================================================
-- TABLE: books
-- Description: Stores book catalog and inventory stock tracking.
-- Relationships:
--   books (1) <----> (M) transactions (One book can appear in many transactions)
-- ====================================================================
CREATE TABLE IF NOT EXISTS `books` (
  `book_id` INT NOT NULL AUTO_INCREMENT,
  `title` VARCHAR(200) NOT NULL,
  `author` VARCHAR(150) NOT NULL,
  `category` VARCHAR(100) DEFAULT NULL,
  `isbn` VARCHAR(30) DEFAULT NULL,
  `quantity` INT NOT NULL,
  `available_quantity` INT NOT NULL,
  `published_year` INT DEFAULT NULL,
  `created_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`book_id`),
  UNIQUE KEY `uk_books_isbn` (`isbn`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ====================================================================
-- TABLE: members
-- Description: Stores registered library members.
-- Relationships:
--   members (1) <----> (M) transactions (One member can borrow multiple books)
-- ====================================================================
CREATE TABLE IF NOT EXISTS `members` (
  `member_id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(150) NOT NULL,
  `email` VARCHAR(150) NOT NULL,
  `phone` VARCHAR(20) DEFAULT NULL,
  `address` VARCHAR(255) DEFAULT NULL,
  `registration_date` DATE DEFAULT (CURDATE()),
  PRIMARY KEY (`member_id`),
  UNIQUE KEY `uk_members_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ====================================================================
-- TABLE: transactions
-- Description: Tracks book borrowings, return events, and loan states.
-- Foreign Keys:
--   book_id -> references books(book_id) [RESTRICT ON DELETE]
--   member_id -> references members(member_id) [RESTRICT ON DELETE]
-- ====================================================================
CREATE TABLE IF NOT EXISTS `transactions` (
  `transaction_id` INT NOT NULL AUTO_INCREMENT,
  `book_id` INT NOT NULL,
  `member_id` INT NOT NULL,
  `issue_date` DATE NOT NULL,
  `due_date` DATE NOT NULL,
  `return_date` DATE DEFAULT NULL,
  `status` ENUM('BORROWED','RETURNED') DEFAULT 'BORROWED',
  PRIMARY KEY (`transaction_id`),
  KEY `idx_transactions_book_id` (`book_id`),
  KEY `idx_transactions_member_id` (`member_id`),
  CONSTRAINT `transactions_ibfk_1` FOREIGN KEY (`book_id`) REFERENCES `books` (`book_id`) ON DELETE RESTRICT,
  CONSTRAINT `transactions_ibfk_2` FOREIGN KEY (`member_id`) REFERENCES `members` (`member_id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ====================================================================
-- SAMPLE DATA (Insert if empty)
-- ====================================================================

INSERT IGNORE INTO `books` (`book_id`, `title`, `author`, `category`, `isbn`, `quantity`, `available_quantity`, `published_year`)
VALUES
  (1, 'Clean Code', 'Robert C. Martin', 'Programming', '9780132350884', 5, 4, 2008),
  (2, 'The Pragmatic Programmer', 'Andrew Hunt', 'Programming', '9780135957059', 3, 3, 2019),
  (3, 'Java: The Complete Reference', 'Herbert Schildt', 'Java', '9781260440232', 4, 4, 2018),
  (4, 'Database System Concepts', 'Abraham Silberschatz', 'Database', '9780078022159', 2, 2, 2019),
  (6, 'Effective Java', 'Joshua Bloch', 'Java', '9780134685991', 3, 3, 2018);

INSERT IGNORE INTO `members` (`member_id`, `name`, `email`, `phone`, `address`, `registration_date`)
VALUES
  (1, 'Avadhut Dalvi', 'avadhut@example.com', '9989012552', 'Satara', '2026-10-06'),
  (2, 'Rahul Patil', 'rahul@example.com', '9876543211', 'Pune', '2026-10-06'),
  (3, 'Sneha Joshi', 'sneha@example.com', '9876543212', 'Mumbai', '2026-10-06');

INSERT IGNORE INTO `transactions` (`transaction_id`, `book_id`, `member_id`, `issue_date`, `due_date`, `return_date`, `status`)
VALUES
  (1, 1, 1, '2026-10-08', '2026-10-22', '2026-10-08', 'RETURNED'),
  (2, 1, 2, '2026-10-08', '2026-10-22', '2026-10-08', 'RETURNED'),
  (3, 1, 1, '2026-10-08', '2026-10-13', NULL, 'BORROWED');
