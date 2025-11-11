package com.example.g4prmlibrary.database;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.g4prmlibrary.models.Book;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static com.example.g4prmlibrary.database.DatabaseHelper.*;

public class BookDAO {
    private DatabaseHelper dbHelper;

    public BookDAO(Context context) {
        this.dbHelper = new DatabaseHelper(context);
    }

    public long addBook(Book book) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(BOOK_TITLE, book.getTitle());
        values.put(BOOK_AUTHOR, book.getAuthor());
        values.put(BOOK_CATEGORY, book.getCategory());
        values.put(BOOK_ISBN, book.getIsbn());
        values.put(BOOK_QUANTITY, book.getQuantity());
        values.put(BOOK_AVAILABLE, book.getAvailable());
        values.put(BOOK_IMAGE, book.getImageName());
        values.put(BOOK_DESCRIPTION, book.getDescription());

        long id = db.insert(TABLE_BOOKS, null, values);
        db.close();
        return id;
    }

    public List<Book> getAllBooks() {
        List<Book> books = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(TABLE_BOOKS, null, null, null, null, null, BOOK_TITLE);

        if (cursor.moveToFirst()) {
            do {
                Book book = createBookFromCursor(cursor);
                books.add(book);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return books;
    }

    public Book getBookById(int id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = BOOK_ID + " = ?";
        String[] selectionArgs = {String.valueOf(id)};

        Cursor cursor = db.query(TABLE_BOOKS, null, selection, selectionArgs, null, null, null);

        if (cursor.moveToFirst()) {
            Book book = createBookFromCursor(cursor);
            cursor.close();
            db.close();
            return book;
        }

        cursor.close();
        db.close();
        return null;
    }

    public boolean updateBook(Book book) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(BOOK_TITLE, book.getTitle());
        values.put(BOOK_AUTHOR, book.getAuthor());
        values.put(BOOK_CATEGORY, book.getCategory());
        values.put(BOOK_ISBN, book.getIsbn());
        values.put(BOOK_QUANTITY, book.getQuantity());
        values.put(BOOK_AVAILABLE, book.getAvailable());
        values.put(BOOK_DESCRIPTION, book.getDescription());

        String whereClause = BOOK_ID + " = ?";
        String[] whereArgs = {String.valueOf(book.getId())};

        int rowsAffected = db.update(TABLE_BOOKS, values, whereClause, whereArgs);
        db.close();

        return rowsAffected > 0;
    }

    public boolean deleteBook(int bookId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String whereClause = BOOK_ID + " = ?";
        String[] whereArgs = {String.valueOf(bookId)};

        int rowsAffected = db.delete(TABLE_BOOKS, whereClause, whereArgs);
        db.close();

        return rowsAffected > 0;
    }

    public int getTotalBookCount() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM " + TABLE_BOOKS;
        Cursor cursor = db.rawQuery(query, null);

        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }

        cursor.close();
        db.close();
        return count;
    }

    public List<String> getAllCategories() {
        List<String> categories = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT DISTINCT " + BOOK_CATEGORY + " FROM " + TABLE_BOOKS + " ORDER BY " + BOOK_CATEGORY;
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                String category = cursor.getString(0);
                if (category != null && !category.isEmpty()) {
                    categories.add(category);
                }
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return categories;
    }

    public List<Book> searchBooks(String query) {
        List<Book> books = new ArrayList<>();
        if (query == null || query.trim().isEmpty()) {
            return books;
        }

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Normalize search query (remove diacritics)
        String normalizedQuery = removeVietnameseDiacritics(query.toLowerCase().trim());

        // Get all books and filter in memory for Vietnamese accent-insensitive search
        String sql = "SELECT * FROM " + TABLE_BOOKS + " ORDER BY " + BOOK_TITLE + " ASC";

        Cursor cursor = db.rawQuery(sql, null);

        if (cursor.moveToFirst()) {
            do {
                Book book = createBookFromCursor(cursor);

                // Normalize the book data for comparison
                String normalizedTitle = removeVietnameseDiacritics(book.getTitle().toLowerCase());
                String normalizedAuthor = removeVietnameseDiacritics(book.getAuthor().toLowerCase());
                String normalizedCategory = removeVietnameseDiacritics(book.getCategory().toLowerCase());

                // Check if any field contains the search query (accent-insensitive)
                if (normalizedTitle.contains(normalizedQuery) ||
                        normalizedAuthor.contains(normalizedQuery) ||
                        normalizedCategory.contains(normalizedQuery)) {
                    books.add(book);
                }
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return books;
    }

    public void updateBookAvailability(int bookId, int change) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        Cursor cursor = db.query(TABLE_BOOKS, new String[]{BOOK_AVAILABLE},
                BOOK_ID + " = ?", new String[]{String.valueOf(bookId)},
                null, null, null);

        if (cursor.moveToFirst()) {
            int currentAvailable = cursor.getInt(0);
            int newAvailable = Math.max(0, currentAvailable + change);

            ContentValues values = new ContentValues();
            values.put(BOOK_AVAILABLE, newAvailable);

            db.update(TABLE_BOOKS, values, BOOK_ID + " = ?", new String[]{String.valueOf(bookId)});
        }

        cursor.close();
        db.close();
    }

    private String removeAccents(String text) {
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(normalized).replaceAll("");
    }

    // Pagination methods
    public List<Book> getBooksPaginated(int pageSize, int offset) {
        List<Book> books = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_BOOKS + " ORDER BY " + BOOK_TITLE + " LIMIT ? OFFSET ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(pageSize), String.valueOf(offset)});

        if (cursor.moveToFirst()) {
            do {
                Book book = createBookFromCursor(cursor);
                books.add(book);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return books;
    }

    public List<Book> getBooksByCategory(String category, int pageSize, int offset) {
        List<Book> books = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query;
        String[] selectionArgs;

        if ("all".equals(category)) {
            query = "SELECT * FROM " + TABLE_BOOKS + " ORDER BY " + BOOK_TITLE + " LIMIT ? OFFSET ?";
            selectionArgs = new String[]{String.valueOf(pageSize), String.valueOf(offset)};
        } else {
            query = "SELECT * FROM " + TABLE_BOOKS + " WHERE " + BOOK_CATEGORY + " = ? ORDER BY " + BOOK_TITLE + " LIMIT ? OFFSET ?";
            selectionArgs = new String[]{category, String.valueOf(pageSize), String.valueOf(offset)};
        }

        Cursor cursor = db.rawQuery(query, selectionArgs);

        if (cursor.moveToFirst()) {
            do {
                Book book = createBookFromCursor(cursor);
                books.add(book);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return books;
    }

    public int getBookCountByCategory(String category) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query;
        String[] selectionArgs;

        if ("all".equals(category)) {
            query = "SELECT COUNT(*) FROM " + TABLE_BOOKS;
            selectionArgs = null;
        } else {
            query = "SELECT COUNT(*) FROM " + TABLE_BOOKS + " WHERE " + BOOK_CATEGORY + " = ?";
            selectionArgs = new String[]{category};
        }

        Cursor cursor = db.rawQuery(query, selectionArgs);

        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }

        cursor.close();
        db.close();
        return count;
    }

    private Book createBookFromCursor(Cursor cursor) {
        Book book = new Book();
        book.setId(cursor.getInt(cursor.getColumnIndexOrThrow(BOOK_ID)));
        book.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(BOOK_TITLE)));
        book.setAuthor(cursor.getString(cursor.getColumnIndexOrThrow(BOOK_AUTHOR)));
        book.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(BOOK_CATEGORY)));
        book.setIsbn(cursor.getString(cursor.getColumnIndexOrThrow(BOOK_ISBN)));
        book.setQuantity(cursor.getInt(cursor.getColumnIndexOrThrow(BOOK_QUANTITY)));
        book.setAvailable(cursor.getInt(cursor.getColumnIndexOrThrow(BOOK_AVAILABLE)));
        book.setImageName(cursor.getString(cursor.getColumnIndexOrThrow(BOOK_IMAGE)));
        book.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(BOOK_DESCRIPTION)));
        return book;
    }

    /**
     * Lấy tổng số sách trong thư viện
     */
    public int getTotalBooksCount() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT SUM(" + BOOK_QUANTITY + ") FROM " + TABLE_BOOKS;

        Cursor cursor = db.rawQuery(query, null);

        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }

        cursor.close();
        db.close();
        return count;
    }

    /**
     * Lấy số sách có sẵn
     */
    public int getAvailableBooksCount() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT SUM(" + BOOK_AVAILABLE + ") FROM " + TABLE_BOOKS;

        Cursor cursor = db.rawQuery(query, null);

        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }

        cursor.close();
        db.close();
        return count;
    }

    /**
     * Remove Vietnamese diacritics for better search
     * Enhanced version with more comprehensive character mapping
     */
    private String removeVietnameseDiacritics(String input) {
        if (input == null || input.isEmpty()) return "";

        String result = input;

        // Vietnamese characters mapping (comprehensive)
        String[][] diacritics = {
                // Lowercase vowels
                {"à|á|ạ|ả|ã|â|ầ|ấ|ậ|ẩ|ẫ|ă|ằ|ắ|ặ|ẳ|ẵ", "a"},
                {"è|é|ẹ|ẻ|ẽ|ê|ề|ế|ệ|ể|ễ", "e"},
                {"ì|í|ị|ỉ|ĩ", "i"},
                {"ò|ó|ọ|ỏ|õ|ô|ồ|ố|ộ|ổ|ỗ|ơ|ờ|ớ|ợ|ở|ỡ", "o"},
                {"ù|ú|ụ|ủ|ũ|ư|ừ|ứ|ự|ử|ữ", "u"},
                {"ỳ|ý|ỵ|ỷ|ỹ", "y"},
                {"đ", "d"},

                // Uppercase vowels
                {"À|Á|Ạ|Ả|Ã|Â|Ầ|Ấ|Ậ|Ẩ|Ẫ|Ă|Ằ|Ắ|Ặ|Ẳ|Ẵ", "A"},
                {"È|É|Ẹ|Ẻ|Ẽ|Ê|Ề|Ế|Ệ|Ể|Ễ", "E"},
                {"Ì|Í|Ị|Ỉ|Ĩ", "I"},
                {"Ò|Ó|Ọ|Ỏ|Õ|Ô|Ồ|Ố|Ộ|Ổ|Ỗ|Ơ|Ờ|Ớ|Ợ|Ở|Ỡ", "O"},
                {"Ù|Ú|Ụ|Ủ|Ũ|Ư|Ừ|Ứ|Ự|Ử|Ữ", "U"},
                {"Ỳ|Ý|Ỵ|Ỷ|Ỹ", "Y"},
                {"Đ", "D"}
        };

        for (String[] pair : diacritics) {
            result = result.replaceAll(pair[0], pair[1]);
        }

        return result;
    }
}

