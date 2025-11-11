package com.example.library.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "library.db";
    private static final int DATABASE_VERSION = 6;
    
    // Tables
    public static final String TABLE_USERS = "users";
    public static final String TABLE_BOOKS = "books";
    public static final String TABLE_BORROWING_RECORDS = "borrowing_records";
    
    // Users table columns
    public static final String USER_ID = "id";
    public static final String USER_NAME = "name";
    public static final String USER_EMAIL = "email";
    public static final String USER_PASSWORD = "password";
    public static final String USER_PHONE = "phone";
    public static final String USER_STUDENT_ID = "student_id";
    public static final String USER_ROLE = "role";
    public static final String USER_WEEKLY_LIMIT = "weekly_limit";
    
    // Books table columns
    public static final String BOOK_ID = "id";
    public static final String BOOK_TITLE = "title";
    public static final String BOOK_AUTHOR = "author";
    public static final String BOOK_CATEGORY = "category";
    public static final String BOOK_ISBN = "isbn";
    public static final String BOOK_QUANTITY = "quantity";
    public static final String BOOK_AVAILABLE = "available";
    public static final String BOOK_IMAGE = "image_name";
    public static final String BOOK_DESCRIPTION = "description";
    
    // Borrowing records table columns
    public static final String BORROW_ID = "id";
    public static final String BORROW_USER_ID = "user_id";
    public static final String BORROW_BOOK_ID = "book_id";
    public static final String BORROW_DATE = "borrow_date";
    public static final String BORROW_DUE_DATE = "due_date";
    public static final String BORROW_RETURN_DATE = "return_date";
    public static final String BORROW_STATUS = "status";
    
    // Fines table
    public static final String TABLE_FINES = "fines";
    public static final String FINE_ID = "id";
    public static final String FINE_USER_ID = "user_id";
    public static final String FINE_BORROW_ID = "borrow_id";
    public static final String FINE_AMOUNT = "amount";
    public static final String FINE_REASON = "reason";
    public static final String FINE_STATUS = "status"; // 'unpaid', 'paid'
    public static final String FINE_CREATED_DATE = "created_date";
    public static final String FINE_PAID_DATE = "paid_date";
    
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create users table
        String createUsersTable = "CREATE TABLE " + TABLE_USERS + "("
                + USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + USER_NAME + " TEXT NOT NULL,"
                + USER_EMAIL + " TEXT UNIQUE NOT NULL,"
                + USER_PASSWORD + " TEXT NOT NULL,"
                + USER_PHONE + " TEXT,"
                + USER_STUDENT_ID + " TEXT UNIQUE,"
                + USER_ROLE + " TEXT DEFAULT 'user',"
                + USER_WEEKLY_LIMIT + " INTEGER DEFAULT 3"
                + ")";
        
        // Create books table
        String createBooksTable = "CREATE TABLE " + TABLE_BOOKS + "("
                + BOOK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + BOOK_TITLE + " TEXT NOT NULL,"
                + BOOK_AUTHOR + " TEXT NOT NULL,"
                + BOOK_CATEGORY + " TEXT,"
                + BOOK_ISBN + " TEXT UNIQUE,"
                + BOOK_QUANTITY + " INTEGER DEFAULT 1,"
                + BOOK_AVAILABLE + " INTEGER DEFAULT 1,"
                + BOOK_IMAGE + " TEXT,"
                + BOOK_DESCRIPTION + " TEXT"
                + ")";
        
        // Create borrowing records table
        String createBorrowingRecordsTable = "CREATE TABLE " + TABLE_BORROWING_RECORDS + "("
                + BORROW_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + BORROW_USER_ID + " INTEGER NOT NULL,"
                + BORROW_BOOK_ID + " INTEGER NOT NULL,"
                + BORROW_DATE + " TEXT NOT NULL,"
                + BORROW_DUE_DATE + " TEXT NOT NULL,"
                + BORROW_RETURN_DATE + " TEXT,"
                + BORROW_STATUS + " TEXT DEFAULT 'borrowed',"
                + "FOREIGN KEY(" + BORROW_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + USER_ID + "),"
                + "FOREIGN KEY(" + BORROW_BOOK_ID + ") REFERENCES " + TABLE_BOOKS + "(" + BOOK_ID + ")"
                + ")";
        
        // Create fines table
        String createFinesTable = "CREATE TABLE " + TABLE_FINES + "("
                + FINE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + FINE_USER_ID + " INTEGER NOT NULL,"
                + FINE_BORROW_ID + " INTEGER NOT NULL,"
                + FINE_AMOUNT + " REAL NOT NULL,"
                + FINE_REASON + " TEXT NOT NULL,"
                + FINE_STATUS + " TEXT DEFAULT 'unpaid',"
                + FINE_CREATED_DATE + " TEXT NOT NULL,"
                + FINE_PAID_DATE + " TEXT,"
                + "FOREIGN KEY(" + FINE_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + USER_ID + "),"
                + "FOREIGN KEY(" + FINE_BORROW_ID + ") REFERENCES " + TABLE_BORROWING_RECORDS + "(" + BORROW_ID + ")"
                + ")";
        
        db.execSQL(createUsersTable);
        db.execSQL(createBooksTable);
        db.execSQL(createBorrowingRecordsTable);
        db.execSQL(createFinesTable);
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FINES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BORROWING_RECORDS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOKS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }
}
