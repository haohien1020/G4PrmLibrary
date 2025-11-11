package com.example.g4prmlibrary.models;

public class Fine {
    private int id;
    private int userId;
    private int borrowId;
    private double amount;
    private String reason;
    private String status; // "unpaid", "paid"
    private String createdDate;
    private String paidDate;

    // Additional fields for display
    private String userName;
    private String userStudentId;
    private String bookTitle;
    private String dueDate;

    public Fine() {}

    public Fine(int userId, int borrowId, double amount, String reason, String status, String createdDate) {
        this.userId = userId;
        this.borrowId = borrowId;
        this.amount = amount;
        this.reason = reason;
        this.status = status;
        this.createdDate = createdDate;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getBorrowId() { return borrowId; }
    public void setBorrowId(int borrowId) { this.borrowId = borrowId; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatedDate() { return createdDate; }
    public void setCreatedDate(String createdDate) { this.createdDate = createdDate; }

    public String getPaidDate() { return paidDate; }
    public void setPaidDate(String paidDate) { this.paidDate = paidDate; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserStudentId() { return userStudentId; }
    public void setUserStudentId(String userStudentId) { this.userStudentId = userStudentId; }

    public String getBookTitle() { return bookTitle; }
    public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }

    public String getDueDate() { return dueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }

    public String getStatusDisplayText() {
        switch (status) {
            case "unpaid": return "Chưa thanh toán";
            case "paid": return "Đã thanh toán";
            default: return status;
        }
    }

    public boolean isUnpaid() {
        return "unpaid".equals(status);
    }

    public boolean isPaid() {
        return "paid".equals(status);
    }

    public String getFormattedAmount() {
        return String.format("%,.0f VNĐ", amount);
    }
}

