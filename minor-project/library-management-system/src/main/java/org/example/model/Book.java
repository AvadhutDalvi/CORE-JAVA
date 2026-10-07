package org.example.model;

public class Book {

    private int bookId;
    private String title;
    private String author;
    private String category;
    private String isbn;
    private int quantity;
    private int availableQuantity;
    private int publishedYear;

    public Book() {
    }

    public Book(String title, String author, String category,
                String isbn, int quantity, int availableQuantity,
                int publishedYear) {

        this.title = title;
        this.author = author;
        this.category = category;
        this.isbn = isbn;
        this.quantity = quantity;
        this.availableQuantity = availableQuantity;
        this.publishedYear = publishedYear;
    }

    public Book(int bookId, String title, String author, String category,
                String isbn, int quantity, int availableQuantity,
                int publishedYear) {

        this.bookId = bookId;
        this.title = title;
        this.author = author;
        this.category = category;
        this.isbn = isbn;
        this.quantity = quantity;
        this.availableQuantity = availableQuantity;
        this.publishedYear = publishedYear;
    }

    public int getBookId() {
        return bookId;
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getAvailableQuantity() {
        return availableQuantity;
    }

    public void setAvailableQuantity(int availableQuantity) {
        this.availableQuantity = availableQuantity;
    }

    public int getPublishedYear() {
        return publishedYear;
    }

    public void setPublishedYear(int publishedYear) {
        this.publishedYear = publishedYear;
    }

    @Override
    public String toString() {
        return "Book{" +
                "bookId=" + bookId +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", category='" + category + '\'' +
                ", isbn='" + isbn + '\'' +
                ", quantity=" + quantity +
                ", availableQuantity=" + availableQuantity +
                ", publishedYear=" + publishedYear +
                '}';
    }
}