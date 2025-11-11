package com.example.library.models;

public class User {
    private int id;
    private String name;
    private String email;
    private String password;
    private String phone;
    private String studentId;
    private String role; // "admin" or "user"
    private int weeklyLimit;
    private int booksRead; // for rankings
    private int rank; // for rankings
    
    public User() {
        this.weeklyLimit = 3; // default weekly limit
    }
    
    public User(String name, String email, String password, String phone, String studentId, String role) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.studentId = studentId;
        this.role = role;
        this.weeklyLimit = 3;
    }
    
    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    
    public int getWeeklyLimit() { return weeklyLimit; }
    public void setWeeklyLimit(int weeklyLimit) { this.weeklyLimit = weeklyLimit; }
    
    public int getBooksRead() { return booksRead; }
    public void setBooksRead(int booksRead) { this.booksRead = booksRead; }
    
    public int getRank() { return rank; }
    public void setRank(int rank) { this.rank = rank; }
    
    public boolean isAdmin() {
        return "admin".equals(role);
    }
}