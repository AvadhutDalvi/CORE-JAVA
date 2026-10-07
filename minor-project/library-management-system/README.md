# Library Management System

A desktop-based **Library Management System** built with **Core Java**, **Java Swing**, **JDBC**, and **MySQL**. Developed as a minor project following the layered Architecture (Model-View-Service-DAO) pattern.

---

## 1. Project Overview

The Library Management System is designed to automate core library workflows:
- Cataloging and managing books and their stock levels.
- Registering and maintaining library member profiles.
- Processing book borrowings (Issue) and returns (Return) with transactional integrity.
- Tracking overdue loans dynamically.
- Providing an operational dashboard with live statistics.

The system ensures data consistency through ACID database transactions, rigorous business validation, foreign key constraints, and stock tracking.

---

## 2. Architecture & Design Pattern

The application implements a 3-tier / layered architecture that separates presentation, business logic, data persistence, and database storage:

```
User
  │
  ▼
Swing GUI (MainFrame, DashboardPanel, BookPanel, MemberPanel, TransactionPanel)
  │
  ▼
Service Layer (BookService, MemberService, TransactionService)
  │
  ▼
DAO Layer (BookDAO, MemberDAO, TransactionDAO, DashboardDAO)
  │
  ▼
JDBC (DriverManager, Connection, PreparedStatement, ResultSet)
  │
  ▼
MySQL Database (library_db: books, members, transactions)
```

### Layer Responsibilities

| Layer | Classes | Responsibilities |
| :--- | :--- | :--- |
| **GUI Layer** | `MainFrame`, `DashboardPanel`, `BookPanel`, `MemberPanel`, `TransactionPanel` | Renders the Swing user interface, captures user inputs, displays tabular data, and shows user-friendly error dialogs. |
| **Service Layer** | `BookService`, `MemberService`, `TransactionService` | Enforces business validation rules (e.g. positive stock, duplicate email/ISBN checks, borrowing limits, date bounds), coordinates operations, and raises `ValidationException`. |
| **DAO Layer** | `BookDAO`, `MemberDAO`, `TransactionDAO`, `DashboardDAO` | Executes parameterized SQL statements via JDBC, manages database transactions (`commit`/`rollback`), and maps query results to Domain models. |
| **Model Layer** | `Book`, `Member`, `Transaction` | Plain Java objects (POJOs) representing domain entities with encapsulated fields and accessors. |
| **Utility Layer** | `DBConnection`, `ValidationException` | Manages JDBC connection pooling/lifecycle with externalized configuration, and provides standard validation exceptions. |

---

## 3. Technology Stack

- **Programming Language:** Java (JDK 21 / 25)
- **GUI Framework:** Java Swing (`javax.swing`, `java.awt`)
- **Persistence / Data Access:** JDBC (Java Database Connectivity)
- **Database:** MySQL Server 8.0+
- **Database Driver:** MySQL Connector/J (`com.mysql:mysql-connector-j:9.5.0`)
- **Build Tool:** Apache Maven
- **Testing Framework:** JUnit 5 (`org.junit.jupiter:junit-jupiter:5.10.2`)

---

## 4. Key Features

### 📚 Book Catalog Management
- **Add Book:** Add new books with Title, Author, Category, ISBN, Total Quantity, and Published Year.
- **Update Book:** Modify book information with automatic available quantity recalculation, ensuring stock cannot be reduced below actively borrowed copies.
- **Delete Book:** Safeguarded against accidental deletion if active loans or transaction records exist.
- **Search:** Search catalog instantly across Title, Author, Category, or ISBN.
- **Stock Tracking:** Automatic tracking of total copies vs. available copies.

### 👤 Member Management
- **Register Member:** Register members with Name, Email, Phone, Address, and registration date.
- **Duplicate Prevention:** Unique email constraint enforced at both service and database levels.
- **Update Member:** Edit member details safely.
- **Delete Member:** Prevented if member currently holds borrowed books.
- **Search:** Filter members by Name, Email, or Phone number.

### 🔄 Issue & Return Transactions
- **Atomic Issue Operation:**
  - Verifies book existence and availability (`available_quantity > 0`).
  - Verifies member existence.
  - Blocks duplicate active borrowings (a member cannot borrow the same book twice simultaneously).
  - Decrements `available_quantity` and inserts the transaction record atomically within a single database transaction.
- **Atomic Return Operation:**
  - Validates that the transaction exists and is currently in `BORROWED` status.
  - Updates transaction status to `RETURNED` and records return date (`CURDATE()`).
  - Increments `available_quantity` atomically while ensuring it never exceeds total `quantity`.
- **Dynamic Overdue Tracking:**
  - Identifies overdue loans dynamically when `status = 'BORROWED'` and `due_date < CURDATE()`.
  - Visually flags overdue transactions in soft red in the transaction table without altering the database enum status.

### 📊 Dashboard
- Live count of **Total Book Titles**.
- Real-time count of **Available Copies** on shelves.
- Count of currently **Issued Copies**.
- Total **Registered Members**.
- Count of **Overdue Loans**.
- One-click **Refresh Statistics** button.

---

## 5. Database Design

Database name: `library_db`

### Entity Relationship Diagram (ERD)

```
+--------------------+            +------------------------+            +--------------------+
|       books        |            |      transactions      |            |      members       |
+--------------------+            +------------------------+            +--------------------+
| PK  book_id        |<-------+   | PK  transaction_id     |   +------->| PK  member_id      |
|     title          |        |   | FK  book_id            |---+        |     name           |
|     author         |        +---| FK  member_id          |            |     email (UNIQUE) |
|     category       |            |     issue_date         |            |     phone          |
|     isbn (UNIQUE)  |            |     due_date           |            |     address        |
|     quantity       |            |     return_date        |            |     registration_d |
| available_quantity |            |     status             |            +--------------------+
|     published_year |            +------------------------+
+--------------------+
```

### Table Definitions

#### 1. `books`
| Column | Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `book_id` | INT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier for book |
| `title` | VARCHAR(200) | NOT NULL | Title of the book |
| `author` | VARCHAR(150) | NOT NULL | Author of the book |
| `category` | VARCHAR(100) | NULL | Genre or category |
| `isbn` | VARCHAR(30) | UNIQUE, NULL | International Standard Book Number |
| `quantity` | INT | NOT NULL, >= 0 | Total owned copies |
| `available_quantity`| INT | NOT NULL, 0 to quantity | Copies currently available |
| `published_year` | INT | NULL | Publication year |
| `created_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Record creation timestamp |

#### 2. `members`
| Column | Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `member_id` | INT | PRIMARY KEY, AUTO_INCREMENT | Unique member identifier |
| `name` | VARCHAR(150) | NOT NULL | Full name of the member |
| `email` | VARCHAR(150) | UNIQUE, NOT NULL | Member email address |
| `phone` | VARCHAR(20) | NULL | 10-digit contact number |
| `address` | VARCHAR(255) | NULL | Residential address |
| `registration_date`| DATE | DEFAULT (CURDATE()) | Date of membership registration |

#### 3. `transactions`
| Column | Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `transaction_id`| INT | PRIMARY KEY, AUTO_INCREMENT | Unique transaction receipt ID |
| `book_id` | INT | FOREIGN KEY (books.book_id) | Book borrowed |
| `member_id` | INT | FOREIGN KEY (members.member_id) | Member borrowing |
| `issue_date` | DATE | NOT NULL | Date issued |
| `due_date` | DATE | NOT NULL | Loan expiration date |
| `return_date` | DATE | NULL | Date returned |
| `status` | ENUM | 'BORROWED', 'RETURNED' | Current loan status |

---

## 6. Project Package Structure

```
library-management-system/
├── pom.xml
├── schema.sql
├── README.md
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── org/
│   │   │       └── example/
│   │   │           ├── Main.java
│   │   │           ├── dao/
│   │   │           │   ├── BookDAO.java
│   │   │           │   ├── DashboardDAO.java
│   │   │           │   ├── MemberDAO.java
│   │   │           │   └── TransactionDAO.java
│   │   │           ├── gui/
│   │   │           │   ├── BookPanel.java
│   │   │           │   ├── DashboardPanel.java
│   │   │           │   ├── MainFrame.java
│   │   │           │   ├── MemberPanel.java
│   │   │           │   └── TransactionPanel.java
│   │   │           ├── model/
│   │   │           │   ├── Book.java
│   │   │           │   ├── Member.java
│   │   │           │   └── Transaction.java
│   │   │           ├── service/
│   │   │           │   ├── BookService.java
│   │   │           │   ├── MemberService.java
│   │   │           │   └── TransactionService.java
│   │   │           └── util/
│   │   │               ├── DBConnection.java
│   │   │               └── ValidationException.java
│   │   └── resources/
│   │       ├── db.properties.example
│   │       └── db.properties
│   └── test/
│       └── java/
│           └── org/
│               └── example/
│                   └── service/
│                       └── LibraryServiceTest.java
```

---

## 7. Setup & Installation Instructions

### Prerequisites
1. **Java JDK 21+** (or JDK 25) installed with `JAVA_HOME` configured.
2. **MySQL Server 8.0+** running locally.
3. **Apache Maven** or an IDE with bundled Maven (such as IntelliJ IDEA).

### Step 1: Database Setup
1. Open MySQL Command Line Client or MySQL Workbench.
2. Execute the included `schema.sql` script:
   ```sql
   source schema.sql;
   ```
   Or execute directly from the terminal:
   ```bash
   mysql -u root -p < schema.sql
   ```

### Step 2: Database Configuration
Configure database connection settings through one of the following methods:

1. **Configuration File (`db.properties`):**
   Copy `src/main/resources/db.properties.example` to `src/main/resources/db.properties` and edit:
   ```properties
   db.url=jdbc:mysql://localhost:3306/library_db
   db.user=root
   db.password=your_mysql_password
   ```

2. **Environment Variables (Optional for CI/CD):**
   ```bash
   export DB_URL="jdbc:mysql://localhost:3306/library_db"
   export DB_USER="root"
   export DB_PASSWORD="your_mysql_password"
   ```

> *Note: `db.properties` is included in `.gitignore` to prevent committing credentials.*

---

## 8. How to Run the Project

### Option A: From Command Line (Maven)
```bash
# Navigate to the project directory
cd library-management-system

# Compile source files
mvn clean compile

# Run the desktop application
mvn exec:java -Dexec.mainClass="org.example.Main"
```

### Option B: From IntelliJ IDEA
1. Open IntelliJ IDEA.
2. Select **File -> Open...** and choose the `library-management-system` folder.
3. Wait for Maven dependencies to import.
4. Locate `src/main/java/org/example/Main.java`.
5. Right-click and choose **Run 'Main.main()'**.

---

## 9. Running Automated Tests

A comprehensive suite of 24 unit and integration tests is located in `src/test/java/org/example/service/LibraryServiceTest.java`.

To execute all tests:
```bash
mvn test
```

### What the Tests Verify
- ✅ Title, quantity, year, and bounds validation on Books.
- ✅ Duplicate ISBN rejection.
- ✅ Stock adjustment and inventory preservation during updates.
- ✅ Member name, email regex, and 10-digit phone number validation.
- ✅ Duplicate email rejection.
- ✅ Transaction input verification (positive IDs and loan period).
- ✅ Rejection of issue requests for unavailable or nonexistent books.
- ✅ Rejection of duplicate active loans by the same member.
- ✅ Atomic decrease of `available_quantity` upon issue.
- ✅ Atomic increase of `available_quantity` upon return.
- ✅ Prevention of duplicate returns.
- ✅ Blocking deletion of books/members associated with active loans.
- ✅ Dashboard statistics calculation.

---

## 10. Transaction Flow & ACID Compliance

### Issue Transaction Flow:
```
1. Validate inputs (bookId > 0, memberId > 0, loanDays > 0)
2. Begin Database Transaction (connection.setAutoCommit(false))
3. Check book existence & lock row (SELECT available_quantity FROM books WHERE book_id = ? FOR UPDATE)
4. Verify available_quantity > 0
5. Check member existence
6. Verify no existing BORROWED transaction for (book_id, member_id)
7. Update books: available_quantity = available_quantity - 1 WHERE available_quantity > 0
8. Insert into transactions (book_id, member_id, issue_date, due_date, 'BORROWED')
9. Commit Database Transaction (connection.commit())
   (If any step fails -> connection.rollback())
```

### Return Transaction Flow:
```
1. Validate inputs (transactionId > 0)
2. Begin Database Transaction (connection.setAutoCommit(false))
3. Check transaction status (SELECT status, book_id FROM transactions WHERE transaction_id = ? FOR UPDATE)
4. Verify status is 'BORROWED' (not already 'RETURNED')
5. Verify book's available_quantity < total quantity
6. Update transactions: return_date = CURDATE(), status = 'RETURNED'
7. Update books: available_quantity = available_quantity + 1 WHERE available_quantity < quantity
8. Commit Database Transaction (connection.commit())
   (If any step fails -> connection.rollback())
```

---

## 11. Validation and User-Friendly Errors

All validation checks provide clear error dialogs through `ValidationException`:

| Scenario | User-Facing Error Message |
| :--- | :--- |
| Blank book title | `"Book title is required."` |
| Zero / negative quantity | `"Book quantity must be greater than 0."` |
| Invalid publication year | `"Published year must be between 1000 and 2027."` |
| Existing ISBN duplicate | `"A book with ISBN '...' already exists."` |
| Reducing stock below borrowed count | `"Total quantity cannot be less than currently borrowed copies (N)."` |
| Non-existent book | `"Book not found with ID: N"` |
| Non-existent member | `"Member not found with ID: N"` |
| Out of stock book | `"This book is currently unavailable (0 copies available)."` |
| Duplicate active borrow | `"This member already has this book issued."` |
| Delete book with active loans | `"Cannot delete book: this book currently has active borrowed copies."` |
| Invalid email syntax | `"Invalid email address format (e.g., user@example.com)."` |
| Invalid phone format | `"Invalid phone number. It must be exactly 10 digits."` |
| Duplicate member email | `"A member with email '...' already exists."` |
| Returning already returned book | `"This book has already been returned."` |
| Available quantity overflow | `"Cannot return book: available copies cannot exceed total stock."` |

---

## 12. Future Improvements

- **Fine Calculation:** Add automated overdue fine calculation based on elapsed days past the due date.
- **Role-Based Access Control (RBAC):** Admin vs. Librarian user login authentication.
- **Export Reports:** Export catalog and borrowing history to PDF / Excel / CSV formats.
- **Barcode / QR Integration:** Scan book ISBN and member card barcodes for faster checkout.
- **Email Notifications:** Automated reminders sent to members with upcoming or overdue loan dates.