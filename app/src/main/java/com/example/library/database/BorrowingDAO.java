package com.example.library.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.library.models.BorrowingRecord;

import java.util.ArrayList;
import java.util.List;

import static com.example.library.database.DatabaseHelper.*;

public class BorrowingDAO {
    private DatabaseHelper dbHelper;
    private BookDAO bookDAO;
    
    public BorrowingDAO(Context context) {
        this.dbHelper = new DatabaseHelper(context);
        this.bookDAO = new BookDAO(context);
    }
    
    public long addBorrowingRecord(BorrowingRecord record) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(BORROW_USER_ID, record.getUserId());
        values.put(BORROW_BOOK_ID, record.getBookId());
        values.put(BORROW_DATE, record.getBorrowDate());
        values.put(BORROW_DUE_DATE, record.getDueDate());
        values.put(BORROW_STATUS, record.getStatus());
        
        long id = db.insert(TABLE_BORROWING_RECORDS, null, values);
        db.close();
        
        // Update book availability
        if (id > 0) {
            bookDAO.updateBookAvailability(record.getBookId(), -1);
        }
        
        return id;
    }
    
    public List<BorrowingRecord> getBorrowedBooksByUser(int userId) {
        List<BorrowingRecord> records = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        String query = "SELECT DISTINCT br." + BORROW_ID + ", br." + BORROW_USER_ID + ", br." + BORROW_BOOK_ID + 
                       ", br." + BORROW_DATE + ", br." + BORROW_DUE_DATE + ", br." + BORROW_RETURN_DATE + 
                       ", br." + BORROW_STATUS + ", b." + BOOK_TITLE + ", b." + BOOK_AUTHOR + ", b." + BOOK_IMAGE + 
                       " FROM " + TABLE_BORROWING_RECORDS + " br " +
                       " JOIN " + TABLE_BOOKS + " b ON br." + BORROW_BOOK_ID + " = b." + BOOK_ID +
                       " WHERE br." + BORROW_USER_ID + " = ? AND br." + BORROW_STATUS + " = 'borrowed'" +
                       " ORDER BY br." + BORROW_DATE + " DESC";
        
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});
        
        if (cursor.moveToFirst()) {
            do {
                BorrowingRecord record = createBorrowingRecordFromCursor(cursor);
                records.add(record);
            } while (cursor.moveToNext());
        }
        
        cursor.close();
        db.close();
        return records;
    }
    
    public List<BorrowingRecord> getAllBorrowingRecordsByStatus(String status) {
        List<BorrowingRecord> records = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        String query = "SELECT DISTINCT br.*, b." + BOOK_TITLE + ", b." + BOOK_AUTHOR + ", b." + BOOK_IMAGE + 
                       ", u." + USER_NAME + " FROM " + TABLE_BORROWING_RECORDS + " br " +
                       " JOIN " + TABLE_BOOKS + " b ON br." + BORROW_BOOK_ID + " = b." + BOOK_ID +
                       " JOIN " + TABLE_USERS + " u ON br." + BORROW_USER_ID + " = u." + USER_ID +
                       " WHERE br." + BORROW_STATUS + " = ?" +
                       " ORDER BY br." + BORROW_DATE + " DESC";
        
        Cursor cursor = db.rawQuery(query, new String[]{status});
        
        if (cursor.moveToFirst()) {
            do {
                BorrowingRecord record = createBorrowingRecordFromCursor(cursor);
                record.setUserName(cursor.getString(cursor.getColumnIndexOrThrow(USER_NAME)));
                records.add(record);
            } while (cursor.moveToNext());
        }
        
        cursor.close();
        db.close();
        return records;
    }
    
    public List<BorrowingRecord> getAllBorrowingRecords() {
        List<BorrowingRecord> records = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        String query = "SELECT DISTINCT br.*, b." + BOOK_TITLE + ", b." + BOOK_AUTHOR + ", b." + BOOK_IMAGE + 
                       ", u." + USER_NAME + " FROM " + TABLE_BORROWING_RECORDS + " br " +
                       " JOIN " + TABLE_BOOKS + " b ON br." + BORROW_BOOK_ID + " = b." + BOOK_ID +
                       " JOIN " + TABLE_USERS + " u ON br." + BORROW_USER_ID + " = u." + USER_ID +
                       " ORDER BY br." + BORROW_DATE + " DESC";
        
        Cursor cursor = db.rawQuery(query, null);
        
        if (cursor.moveToFirst()) {
            do {
                BorrowingRecord record = createBorrowingRecordFromCursor(cursor);
                record.setUserName(cursor.getString(cursor.getColumnIndexOrThrow(USER_NAME)));
                records.add(record);
            } while (cursor.moveToNext());
        }
        
        cursor.close();
        db.close();
        return records;
    }
    
    public List<BorrowingRecord> getReturnedBooksByUser(int userId) {
        List<BorrowingRecord> records = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        String query = "SELECT br.*, b." + BOOK_TITLE + ", b." + BOOK_AUTHOR + ", b." + BOOK_IMAGE + 
                       " FROM " + TABLE_BORROWING_RECORDS + " br " +
                       " JOIN " + TABLE_BOOKS + " b ON br." + BORROW_BOOK_ID + " = b." + BOOK_ID +
                       " WHERE br." + BORROW_USER_ID + " = ? AND br." + BORROW_STATUS + " = 'returned'" +
                       " ORDER BY br." + BORROW_RETURN_DATE + " DESC";
        
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});
        
        if (cursor.moveToFirst()) {
            do {
                BorrowingRecord record = createBorrowingRecordFromCursor(cursor);
                records.add(record);
            } while (cursor.moveToNext());
        }
        
        cursor.close();
        db.close();
        return records;
    }
    
    public List<BorrowingRecord> getAllUserBorrowingHistory(int userId) {
        List<BorrowingRecord> records = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        String query = "SELECT DISTINCT br." + BORROW_ID + ", br." + BORROW_USER_ID + ", br." + BORROW_BOOK_ID + 
                       ", br." + BORROW_DATE + ", br." + BORROW_DUE_DATE + ", br." + BORROW_RETURN_DATE + 
                       ", br." + BORROW_STATUS + ", b." + BOOK_TITLE + ", b." + BOOK_AUTHOR + ", b." + BOOK_IMAGE + 
                       " FROM " + TABLE_BORROWING_RECORDS + " br " +
                       " JOIN " + TABLE_BOOKS + " b ON br." + BORROW_BOOK_ID + " = b." + BOOK_ID +
                       " WHERE br." + BORROW_USER_ID + " = ?" +
                       " ORDER BY br." + BORROW_DATE + " DESC";
        
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});
        
        if (cursor.moveToFirst()) {
            do {
                BorrowingRecord record = createBorrowingRecordFromCursor(cursor);
                records.add(record);
            } while (cursor.moveToNext());
        }
        
        cursor.close();
        db.close();
        return records;
    }

    public boolean returnBook(int borrowId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        
        // Get borrowing record first
        BorrowingRecord record = getBorrowingRecordById(borrowId);
        if (record == null) return false;
        
        ContentValues values = new ContentValues();
        values.put(BORROW_RETURN_DATE, getCurrentDate());
        values.put(BORROW_STATUS, "returned");
        
        int rowsAffected = db.update(TABLE_BORROWING_RECORDS, values, 
                                   BORROW_ID + " = ?", new String[]{String.valueOf(borrowId)});
        db.close();
        
        // Update book availability
        if (rowsAffected > 0) {
            bookDAO.updateBookAvailability(record.getBookId(), 1);
        }
        
        return rowsAffected > 0;
    }
    
    private BorrowingRecord getBorrowingRecordById(int borrowId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        BorrowingRecord record = null;
        
        Cursor cursor = db.query(TABLE_BORROWING_RECORDS, null, 
                               BORROW_ID + " = ?", new String[]{String.valueOf(borrowId)}, 
                               null, null, null);
        
        if (cursor.moveToFirst()) {
            record = new BorrowingRecord();
            record.setId(cursor.getInt(cursor.getColumnIndexOrThrow(BORROW_ID)));
            record.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow(BORROW_USER_ID)));
            record.setBookId(cursor.getInt(cursor.getColumnIndexOrThrow(BORROW_BOOK_ID)));
            record.setBorrowDate(cursor.getString(cursor.getColumnIndexOrThrow(BORROW_DATE)));
            record.setDueDate(cursor.getString(cursor.getColumnIndexOrThrow(BORROW_DUE_DATE)));
            record.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(BORROW_STATUS)));
        }
        
        cursor.close();
        db.close();
        return record;
    }

    private BorrowingRecord createBorrowingRecordFromCursor(Cursor cursor) {
        BorrowingRecord record = new BorrowingRecord();
        record.setId(cursor.getInt(cursor.getColumnIndexOrThrow(BORROW_ID)));
        record.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow(BORROW_USER_ID)));
        record.setBookId(cursor.getInt(cursor.getColumnIndexOrThrow(BORROW_BOOK_ID)));
        record.setBorrowDate(cursor.getString(cursor.getColumnIndexOrThrow(BORROW_DATE)));
        record.setDueDate(cursor.getString(cursor.getColumnIndexOrThrow(BORROW_DUE_DATE)));
        record.setReturnDate(cursor.getString(cursor.getColumnIndexOrThrow(BORROW_RETURN_DATE)));
        record.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(BORROW_STATUS)));
        
        // Book info
        record.setBookTitle(cursor.getString(cursor.getColumnIndexOrThrow(BOOK_TITLE)));
        record.setBookAuthor(cursor.getString(cursor.getColumnIndexOrThrow(BOOK_AUTHOR)));
        record.setBookImage(cursor.getString(cursor.getColumnIndexOrThrow(BOOK_IMAGE)));
        
        return record;
    }
    
    // Get pending requests for a user
    public List<BorrowingRecord> getPendingRequestsByUser(int userId) {
        List<BorrowingRecord> records = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        String query = "SELECT DISTINCT br." + BORROW_ID + ", br." + BORROW_USER_ID + ", br." + BORROW_BOOK_ID + 
                       ", br." + BORROW_DATE + ", br." + BORROW_DUE_DATE + ", br." + BORROW_RETURN_DATE + 
                       ", br." + BORROW_STATUS + ", b." + BOOK_TITLE + ", b." + BOOK_AUTHOR + ", b." + BOOK_IMAGE + 
                       " FROM " + TABLE_BORROWING_RECORDS + " br " +
                       " JOIN " + TABLE_BOOKS + " b ON br." + BORROW_BOOK_ID + " = b." + BOOK_ID +
                       " WHERE br." + BORROW_USER_ID + " = ? AND br." + BORROW_STATUS + " = 'pending'" +
                       " ORDER BY br." + BORROW_DATE + " DESC";
        
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});
        
        if (cursor.moveToFirst()) {
            do {
                BorrowingRecord record = createBorrowingRecordFromCursor(cursor);
                records.add(record);
            } while (cursor.moveToNext());
        }
        
        cursor.close();
        db.close();
        return records;
    }

    // Utility method
    private String getCurrentDate() {
        return new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(new java.util.Date());
    }
    
    /**
     * Tạo borrowing request cho user
     */
    public boolean createBorrowingRequest(int userId, int bookId) {
        try {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            
            ContentValues values = new ContentValues();
            values.put(BORROW_USER_ID, userId);
            values.put(BORROW_BOOK_ID, bookId);
            values.put(BORROW_DATE, getCurrentDate());
            values.put(BORROW_DUE_DATE, getDueDateAfterDays(30)); // 30 ngày sau
            values.put(BORROW_STATUS, "pending"); // Chờ duyệt
            
            long result = db.insert(TABLE_BORROWING_RECORDS, null, values);
            db.close();
            
            return result != -1;
            
        } catch (Exception e) {
            android.util.Log.e("BorrowingDAO", "Error creating borrowing request: ", e);
            return false;
        }
    }
    
    /**
     * Đếm tổng số sách đang mượn và pending của user
     */
    public int getTotalBorrowedAndPendingBooks(int userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        String query = "SELECT COUNT(*) FROM " + TABLE_BORROWING_RECORDS + 
                       " WHERE " + BORROW_USER_ID + " = ? AND " + BORROW_STATUS + " IN ('borrowed', 'pending')";
        
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});
        
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        
        cursor.close();
        db.close();
        return count;
    }
    
    /**
     * Kiểm tra user có request hoặc đang mượn sách này không
     */
    public boolean hasActiveRequestForBook(int userId, int bookId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        String query = "SELECT COUNT(*) FROM " + TABLE_BORROWING_RECORDS + 
                       " WHERE " + BORROW_USER_ID + " = ? AND " + BORROW_BOOK_ID + " = ? AND " + 
                       BORROW_STATUS + " IN ('pending', 'borrowed')";
        
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId), String.valueOf(bookId)});
        
        boolean hasActive = false;
        if (cursor.moveToFirst()) {
            hasActive = cursor.getInt(0) > 0;
        }
        
        cursor.close();
        db.close();
        return hasActive;
    }
    
    /**
     * Kiểm tra user có đang mượn sách này không (chỉ status = 'borrowed')
     */
    public boolean isCurrentlyBorrowingBook(int userId, int bookId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        String query = "SELECT COUNT(*) FROM " + TABLE_BORROWING_RECORDS + 
                       " WHERE " + BORROW_USER_ID + " = ? AND " + BORROW_BOOK_ID + " = ? AND " + 
                       BORROW_STATUS + " = 'borrowed'";
        
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId), String.valueOf(bookId)});
        
        boolean isBorrowing = false;
        if (cursor.moveToFirst()) {
            isBorrowing = cursor.getInt(0) > 0;
        }
        
        cursor.close();
        db.close();
        return isBorrowing;
    }
    
    /**
     * Tính ngày due sau số ngày nhất định
     */
    private String getDueDateAfterDays(int days) {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.add(java.util.Calendar.DAY_OF_MONTH, days);
        return new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(calendar.getTime());
    }
    
    /**
     * Update request status from pending to borrowed
     */
    public boolean updateRequestStatus(int requestId, String newStatus) {
        try {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            
            ContentValues values = new ContentValues();
            values.put(BORROW_STATUS, newStatus);
            if ("borrowed".equals(newStatus)) {
                values.put(BORROW_DATE, getCurrentDate());
            }
            
            int updated = db.update(TABLE_BORROWING_RECORDS, values, 
                BORROW_ID + " = ?", new String[]{String.valueOf(requestId)});
            db.close();
            
            return updated > 0;
            
        } catch (Exception e) {
            android.util.Log.e("BorrowingDAO", "Error updating request status: ", e);
            return false;
        }
    }
    
    /**
     * Debug method - lấy raw data để kiểm tra duplicate
     */
    public void debugDuplicateData(int userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        String query = "SELECT br." + BORROW_ID + ", br." + BORROW_USER_ID + ", br." + BORROW_BOOK_ID + 
                       ", br." + BORROW_DATE + ", br." + BORROW_STATUS + ", b." + BOOK_TITLE + 
                       " FROM " + TABLE_BORROWING_RECORDS + " br " +
                       " JOIN " + TABLE_BOOKS + " b ON br." + BORROW_BOOK_ID + " = b." + BOOK_ID +
                       " WHERE br." + BORROW_USER_ID + " = ?" +
                       " ORDER BY br." + BORROW_DATE + " DESC";
        
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});
        
        android.util.Log.d("BorrowingDAO", "=== DEBUG DATA FOR USER " + userId + " ===");
        if (cursor.moveToFirst()) {
            do {
                int borrowId = cursor.getInt(0);
                int bookId = cursor.getInt(2);
                String date = cursor.getString(3);
                String status = cursor.getString(4);
                String title = cursor.getString(5);
                
                android.util.Log.d("BorrowingDAO", 
                    "ID: " + borrowId + " | BookID: " + bookId + " | Date: " + date + 
                    " | Status: " + status + " | Title: " + title);
            } while (cursor.moveToNext());
        }
        android.util.Log.d("BorrowingDAO", "=== END DEBUG ===");
        
        cursor.close();
        db.close();
    }
    
    /**
     * Clean duplicate records - xóa các bản ghi trùng lặp
     */
    public int cleanDuplicateRecords() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        
        try {
            // Đếm số records trước khi clean
            Cursor beforeCursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_BORROWING_RECORDS, null);
            beforeCursor.moveToFirst();
            int beforeCount = beforeCursor.getInt(0);
            beforeCursor.close();
            
            // Tạo temporary table với các record duy nhất
            String createTempTable = 
                "CREATE TEMPORARY TABLE temp_borrowing AS " +
                "SELECT MIN(" + BORROW_ID + ") as id, " +
                BORROW_USER_ID + ", " + BORROW_BOOK_ID + ", " +
                BORROW_DATE + ", " + BORROW_DUE_DATE + ", " +
                BORROW_RETURN_DATE + ", " + BORROW_STATUS +
                " FROM " + TABLE_BORROWING_RECORDS +
                " GROUP BY " + BORROW_USER_ID + ", " + BORROW_BOOK_ID + ", " +
                BORROW_DATE + ", " + BORROW_STATUS;
            
            db.execSQL(createTempTable);
            
            // Xóa tất cả records
            db.execSQL("DELETE FROM " + TABLE_BORROWING_RECORDS);
            
            // Insert lại từ temp table
            String insertFromTemp = 
                "INSERT INTO " + TABLE_BORROWING_RECORDS + " (" +
                BORROW_USER_ID + ", " + BORROW_BOOK_ID + ", " +
                BORROW_DATE + ", " + BORROW_DUE_DATE + ", " +
                BORROW_RETURN_DATE + ", " + BORROW_STATUS + ") " +
                "SELECT " + BORROW_USER_ID + ", " + BORROW_BOOK_ID + ", " +
                BORROW_DATE + ", " + BORROW_DUE_DATE + ", " +
                BORROW_RETURN_DATE + ", " + BORROW_STATUS +
                " FROM temp_borrowing";
            
            db.execSQL(insertFromTemp);
            
            // Drop temp table
            db.execSQL("DROP TABLE temp_borrowing");
            
            // Đếm số records sau khi clean
            Cursor afterCursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_BORROWING_RECORDS, null);
            afterCursor.moveToFirst();
            int afterCount = afterCursor.getInt(0);
            afterCursor.close();
            
            int deletedCount = beforeCount - afterCount;
            android.util.Log.d("BorrowingDAO", "Cleaned " + deletedCount + " duplicate records. Before: " + beforeCount + ", After: " + afterCount);
            
            return deletedCount;
            
        } catch (Exception e) {
            android.util.Log.e("BorrowingDAO", "Error cleaning duplicates: ", e);
            return 0;
        } finally {
            db.close();
        }
    }
    
    /**
     * Xóa tất cả data mượn/trả của user
     */
    public boolean deleteAllUserBorrowingData(int userId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        
        try {
            int deleted = db.delete(TABLE_BORROWING_RECORDS, 
                BORROW_USER_ID + " = ?", new String[]{String.valueOf(userId)});
            
            android.util.Log.d("BorrowingDAO", "Deleted " + deleted + " borrowing records for user " + userId);
            return deleted > 0;
            
        } catch (Exception e) {
            android.util.Log.e("BorrowingDAO", "Error deleting user borrowing data: ", e);
            return false;
        } finally {
            db.close();
        }
    }
    
    /**
     * Delete a pending request
     */
    public boolean deleteRequest(int requestId) {
        try {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            
            int deleted = db.delete(TABLE_BORROWING_RECORDS, 
                BORROW_ID + " = ?", new String[]{String.valueOf(requestId)});
            db.close();
            
            return deleted > 0;
            
        } catch (Exception e) {
            android.util.Log.e("BorrowingDAO", "Error deleting request: ", e);
            return false;
        }
    }
    
    /**
     * Get all users with overdue books
     */
    public java.util.List<Integer> getUsersWithOverdueBooks() {
        java.util.List<Integer> overdueUserIds = new java.util.ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        String currentDate = getCurrentDate();
        String query = "SELECT DISTINCT " + BORROW_USER_ID + 
                       " FROM " + TABLE_BORROWING_RECORDS + 
                       " WHERE " + BORROW_STATUS + " = 'borrowed' AND " + 
                       BORROW_DUE_DATE + " < ?";
        
        Cursor cursor = db.rawQuery(query, new String[]{currentDate});
        
        if (cursor.moveToFirst()) {
            do {
                int userId = cursor.getInt(0);
                overdueUserIds.add(userId);
            } while (cursor.moveToNext());
        }
        
        cursor.close();
        db.close();
        return overdueUserIds;
    }
    
    /**
     * Get overdue books for a specific user
     */
    public java.util.List<BorrowingRecord> getOverdueBooksForUser(int userId) {
        java.util.List<BorrowingRecord> overdueBooks = new java.util.ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        String currentDate = getCurrentDate();
        String query = "SELECT DISTINCT br." + BORROW_ID + ", br." + BORROW_USER_ID + ", br." + BORROW_BOOK_ID + 
                       ", br." + BORROW_DATE + ", br." + BORROW_DUE_DATE + ", br." + BORROW_RETURN_DATE + 
                       ", br." + BORROW_STATUS + ", b." + BOOK_TITLE + ", b." + BOOK_AUTHOR + ", b." + BOOK_IMAGE + 
                       " FROM " + TABLE_BORROWING_RECORDS + " br " +
                       " JOIN " + TABLE_BOOKS + " b ON br." + BORROW_BOOK_ID + " = b." + BOOK_ID +
                       " WHERE br." + BORROW_USER_ID + " = ? AND br." + BORROW_STATUS + " = 'borrowed' AND " +
                       "br." + BORROW_DUE_DATE + " < ?" +
                       " ORDER BY br." + BORROW_DUE_DATE + " ASC";
        
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId), currentDate});
        
        if (cursor.moveToFirst()) {
            do {
                BorrowingRecord record = createBorrowingRecordFromCursor(cursor);
                overdueBooks.add(record);
            } while (cursor.moveToNext());
        }
        
        cursor.close();
        db.close();
        return overdueBooks;
    }
    
    /**
     * Count total overdue books for a user
     */
    public int countOverdueBooksForUser(int userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        String currentDate = getCurrentDate();
        String query = "SELECT COUNT(*) FROM " + TABLE_BORROWING_RECORDS + 
                       " WHERE " + BORROW_USER_ID + " = ? AND " + BORROW_STATUS + " = 'borrowed' AND " + 
                       BORROW_DUE_DATE + " < ?";
        
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId), currentDate});
        
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        
        cursor.close();
        db.close();
        return count;
    }
    
    /**
     * Mark all overdue books as returned (admin payment action)
     * This will remove user from overdue list
     */
    public boolean markOverdueBooksAsReturned(int userId) {
        try {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            
            String currentDate = getCurrentDate();
            ContentValues values = new ContentValues();
            values.put(BORROW_RETURN_DATE, currentDate);
            values.put(BORROW_STATUS, "returned_paid"); // Special status for admin-forced return
            
            // Update all overdue books for this user
            String whereClause = BORROW_USER_ID + " = ? AND " + BORROW_STATUS + " = 'borrowed' AND " + 
                                BORROW_DUE_DATE + " < ?";
            String[] whereArgs = {String.valueOf(userId), currentDate};
            
            int updatedRows = db.update(TABLE_BORROWING_RECORDS, values, whereClause, whereArgs);
            db.close();
            
            android.util.Log.d("BorrowingDAO", "Marked " + updatedRows + " overdue books as returned_paid for user " + userId);
            return updatedRows > 0;
            
        } catch (Exception e) {
            android.util.Log.e("BorrowingDAO", "Error marking overdue books as returned: ", e);
            return false;
        }
    }
    
    /**
     * Get count of books that were marked as returned due to payment
     */
    public int getReturnedPaidBooksCount(int userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        String query = "SELECT COUNT(*) FROM " + TABLE_BORROWING_RECORDS + 
                       " WHERE " + BORROW_USER_ID + " = ? AND " + BORROW_STATUS + " = 'returned_paid'";
        
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});
        
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        
        cursor.close();
        db.close();
        return count;
    }
}
