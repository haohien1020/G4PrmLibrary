package com.example.library.models;

public class Book {
    private int id;
    private String title;
    private String author;
    private String category;
    private String isbn;
    private int quantity;
    private int available;
    private String imageName;
    private String description;
    
    public Book() {}
    
    public Book(String title, String author, String category, String isbn, int quantity, int available, String imageName, String description) {
        this.title = title;
        this.author = author;
        this.category = category;
        this.isbn = isbn;
        this.quantity = quantity;
        this.available = available;
        this.imageName = imageName;
        this.description = description;
    }
    
    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    
    public int getAvailable() { return available; }
    public void setAvailable(int available) { this.available = available; }
    
    public String getImageName() { return imageName; }
    public void setImageName(String imageName) { this.imageName = imageName; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public boolean isAvailable() {
        return available > 0;
    }
    
    // Removed the slow getImageResourceId method - now handled in BookAdapter asynchronously
    // This prevents UI thread blocking
    
    @Override
    public String toString() {
        return "Book{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", category='" + category + '\'' +
                ", available=" + available +
                '}';
    }
}
