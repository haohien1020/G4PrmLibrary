package com.example.g4prmlibrary;

import android.content.Context;
import com.example.g4prmlibrary.database.UserDAO;
import com.example.g4prmlibrary.database.BookDAO;
import com.example.g4prmlibrary.database.BorrowingDAO;
import com.example.g4prmlibrary.database.DataInitializer;
import com.example.g4prmlibrary.models.User;
import com.example.g4prmlibrary.models.Book;
import com.example.g4prmlibrary.models.BorrowingRecord;

import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Legacy DatabaseHelper - wraps new DAO pattern for backward compatibility
 */
public class DatabaseHelper {
    private UserDAO userDAO;
    private BookDAO bookDAO;
    private BorrowingDAO borrowingDAO;
    private DataInitializer dataInitializer;
    private Context context;

    public DatabaseHelper(Context context) {
        this.context = context;
        this.userDAO = new UserDAO(context);
        this.bookDAO = new BookDAO(context);
        this.borrowingDAO = new BorrowingDAO(context);
        this.dataInitializer = new DataInitializer(context);
    }

    // User methods
    public long addUser(User user) { return userDAO.addUser(user); }
    public User authenticateUser(String email, String password) { return userDAO.authenticateUser(email, password); }
    public User authenticateUserByMSSV(String mssv, String password) { return userDAO.authenticateUserByMSSV(mssv, password); }
    public User getUserById(int userId) { return userDAO.getUserById(userId); }
    public User getUserByStudentId(String studentId) { return userDAO.getUserByStudentId(studentId); }
    public boolean updateUser(User user) { return userDAO.updateUser(user); }
    public boolean isStudentIdExists(String studentId) { return userDAO.isStudentIdExists(studentId); }

    // Book methods
    public long addBook(Book book) { return bookDAO.addBook(book); }
    public List<Book> getAllBooks() { return bookDAO.getAllBooks(); }
    public Book getBookById(int id) { return bookDAO.getBookById(id); }
    public boolean updateBook(Book book) { return bookDAO.updateBook(book); }
    public boolean deleteBook(int bookId) { return bookDAO.deleteBook(bookId); }
    public int getTotalBookCount() { return bookDAO.getTotalBookCount(); }
    public List<String> getAllCategories() { return bookDAO.getAllCategories(); }

    // Enhanced Borrowing methods
    public boolean createBorrowingRequest(int userId, int bookId) {
        return borrowingDAO.createBorrowingRequest(userId, bookId);
    }

    public int getTotalBorrowedAndPendingBooks(int userId) {
        return borrowingDAO.getTotalBorrowedAndPendingBooks(userId);
    }

    public boolean hasActiveRequestForBook(int userId, int bookId) {
        return borrowingDAO.hasActiveRequestForBook(userId, bookId);
    }

    public boolean isCurrentlyBorrowingBook(int userId, int bookId) {
        return borrowingDAO.isCurrentlyBorrowingBook(userId, bookId);
    }

    public List<BorrowingRecord> getPendingRequestsByUser(int userId) {
        return borrowingDAO.getPendingRequestsByUser(userId);
    }
    public List<Book> searchBooks(String query) { return bookDAO.searchBooks(query); }

    // Borrowing methods
    public long addBorrowingRecord(BorrowingRecord record) { return borrowingDAO.addBorrowingRecord(record); }
    public List<BorrowingRecord> getBorrowedBooksByUser(int userId) { return borrowingDAO.getBorrowedBooksByUser(userId); }
    public List<BorrowingRecord> getAllBorrowingRecordsByStatus(String status) { return borrowingDAO.getAllBorrowingRecordsByStatus(status); }
    public List<BorrowingRecord> getAllBorrowingRecords() { return borrowingDAO.getAllBorrowingRecords(); }
    public List<BorrowingRecord> getReturnedBooksByUser(int userId) { return borrowingDAO.getReturnedBooksByUser(userId); }
    public List<BorrowingRecord> getAllUserBorrowingHistory(int userId) { return borrowingDAO.getAllUserBorrowingHistory(userId); }
    public boolean returnBook(int borrowId) { return borrowingDAO.returnBook(borrowId); }

    // Data initialization
    public void initializeSampleData() { dataInitializer.initializeSampleData(); }

    // Missing methods that are being called in the code

    // Pagination methods
    public List<Book> getBooksPaginated(int pageSize, int offset) {
        return bookDAO.getBooksPaginated(pageSize, offset);
    }

    public List<Book> getBooksByCategory(String category, int pageSize, int offset) {
        return bookDAO.getBooksByCategory(category, pageSize, offset);
    }

    public int getBookCountByCategory(String category) {
        return bookDAO.getBookCountByCategory(category);
    }

    // Borrowing history method (alias for existing method)
    public List<BorrowingRecord> getBorrowingHistoryByUser(int userId) {
        return borrowingDAO.getAllUserBorrowingHistory(userId);
    }

    // Fine management methods
    public List<com.example.g4prmlibrary.models.Fine> getUnpaidFines() {
        return new com.example.g4prmlibrary.database.FineDAO(context).getUnpaidFines();
    }

    public boolean markFineAsPaid(int fineId) {
        return new com.example.g4prmlibrary.database.FineDAO(context).markFineAsPaid(fineId);
    }

    public double getTotalUnpaidFinesByUser(int userId) {
        return new com.example.g4prmlibrary.database.FineDAO(context).getTotalUnpaidFinesByUser(userId);
    }

    public boolean deleteFine(int fineId) {
        return new com.example.g4prmlibrary.database.FineDAO(context).deleteFine(fineId);
    }

    // Utility methods
    public String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date());
    }

    public String getDueDateAfterDays(int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, days);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(calendar.getTime());
    }
}
