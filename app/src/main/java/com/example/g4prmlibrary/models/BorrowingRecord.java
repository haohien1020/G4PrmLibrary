package com.example.g4prmlibrary.models;

public class BorrowingRecord {
    private int id;
    private int userId;
    private int bookId;
    private String borrowDate;
    private String returnDate;
    private String dueDate;
    private String status; // "borrowed", "returned", "overdue", "pending"
    private String requestDate;
    private String rejectedReason;

    // Additional fields for display
    private String bookTitle;
    private String bookAuthor;
    private String bookImage;
    private String userName;

    public BorrowingRecord() {}

    public BorrowingRecord(int userId, int bookId, String borrowDate, String dueDate, String status) {
        this.userId = userId;
        this.bookId = bookId;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.status = status;
    }

    // Constructor with return date for sample data
    public BorrowingRecord(int userId, int bookId, String borrowDate, String dueDate, String status, String returnDate) {
        this.userId = userId;
        this.bookId = bookId;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.status = status;
        this.returnDate = returnDate;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getBookId() { return bookId; }
    public void setBookId(int bookId) { this.bookId = bookId; }

    public String getBorrowDate() { return borrowDate; }
    public void setBorrowDate(String borrowDate) { this.borrowDate = borrowDate; }

    public String getReturnDate() { return returnDate; }
    public void setReturnDate(String returnDate) { this.returnDate = returnDate; }

    public String getDueDate() { return dueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRequestDate() {
        return requestDate != null ? requestDate : borrowDate;
    }
    public void setRequestDate(String requestDate) { this.requestDate = requestDate; }

    public String getRejectedReason() { return rejectedReason; }
    public void setRejectedReason(String rejectedReason) { this.rejectedReason = rejectedReason; }

    public String getBookTitle() { return bookTitle; }
    public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }

    public String getBookAuthor() { return bookAuthor; }
    public void setBookAuthor(String bookAuthor) { this.bookAuthor = bookAuthor; }

    public String getBookImage() { return bookImage; }
    public void setBookImage(String bookImage) { this.bookImage = bookImage; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getStatusDisplayText() {
        switch (status) {
            case "borrowed": return "Đang mượn";
            case "returned": return "Đã trả";
            case "overdue": return "Quá hạn";
            case "pending": return "Chờ duyệt";
            case "rejected": return "Bị từ chối";
            default: return status;
        }
    }

    public boolean isOverdue() {
        return "overdue".equals(status);
    }

    public boolean isReturned() {
        return "returned".equals(status);
    }

    public boolean isBorrowed() {
        return "borrowed".equals(status);
    }

    public boolean isPending() {
        return "pending".equals(status);
    }

    public boolean isRejected() {
        return "rejected".equals(status);
    }
}

