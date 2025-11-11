package com.example.g4prmlibrary.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.g4prmlibrary.models.User;

import static com.example.g4prmlibrary.database.DatabaseHelper.*;

public class UserDAO {
    private DatabaseHelper dbHelper;

    public UserDAO(Context context) {
        this.dbHelper = new DatabaseHelper(context);
    }

    public long addUser(User user) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(USER_NAME, user.getName());
        values.put(USER_EMAIL, user.getEmail());
        values.put(USER_PASSWORD, user.getPassword());
        values.put(USER_PHONE, user.getPhone());
        values.put(USER_STUDENT_ID, user.getStudentId());
        values.put(USER_ROLE, user.getRole());
        values.put(USER_WEEKLY_LIMIT, user.getWeeklyLimit());

        long id = db.insert(TABLE_USERS, null, values);
        db.close();
        return id;
    }

    public User authenticateUser(String email, String password) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] columns = {USER_ID, USER_NAME, USER_EMAIL, USER_PHONE, USER_STUDENT_ID, USER_ROLE, USER_WEEKLY_LIMIT};
        String selection = USER_EMAIL + " = ? AND " + USER_PASSWORD + " = ?";
        String[] selectionArgs = {email, password};

        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);

        if (cursor.moveToFirst()) {
            User user = createUserFromCursor(cursor);
            cursor.close();
            db.close();
            return user;
        }

        cursor.close();
        db.close();
        return null;
    }

    public User authenticateUserByMSSV(String mssv, String password) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] columns = {USER_ID, USER_NAME, USER_EMAIL, USER_PHONE, USER_STUDENT_ID, USER_ROLE, USER_WEEKLY_LIMIT};
        String selection = USER_STUDENT_ID + " = ? AND " + USER_PASSWORD + " = ?";
        String[] selectionArgs = {mssv, password};

        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);

        if (cursor.moveToFirst()) {
            User user = createUserFromCursor(cursor);
            cursor.close();
            db.close();
            return user;
        }

        cursor.close();
        db.close();
        return null;
    }

    public User getUserById(int userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = USER_ID + " = ?";
        String[] selectionArgs = {String.valueOf(userId)};

        Cursor cursor = db.query(TABLE_USERS, null, selection, selectionArgs, null, null, null);

        User user = null;
        if (cursor.moveToFirst()) {
            user = createUserFromCursor(cursor);
        }

        cursor.close();
        db.close();
        return user;
    }

    public User getUserByStudentId(String studentId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = USER_STUDENT_ID + " = ?";
        String[] selectionArgs = {studentId};

        Cursor cursor = db.query(TABLE_USERS, null, selection, selectionArgs, null, null, null);

        User user = null;
        if (cursor.moveToFirst()) {
            user = createUserFromCursor(cursor);
        }

        cursor.close();
        db.close();
        return user;
    }

    public boolean updateUser(User user) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(USER_NAME, user.getName());
        values.put(USER_EMAIL, user.getEmail());
        values.put(USER_PASSWORD, user.getPassword());
        values.put(USER_PHONE, user.getPhone());
        values.put(USER_STUDENT_ID, user.getStudentId());
        values.put(USER_ROLE, user.getRole());
        values.put(USER_WEEKLY_LIMIT, user.getWeeklyLimit());

        String whereClause = USER_ID + " = ?";
        String[] whereArgs = {String.valueOf(user.getId())};

        int rowsAffected = db.update(TABLE_USERS, values, whereClause, whereArgs);
        db.close();

        return rowsAffected > 0;
    }

    public boolean isStudentIdExists(String studentId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = USER_STUDENT_ID + " = ?";
        String[] selectionArgs = {studentId};

        Cursor cursor = db.query(TABLE_USERS, new String[]{USER_ID}, selection, selectionArgs, null, null, null);

        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();

        return exists;
    }

    private User createUserFromCursor(Cursor cursor) {
        User user = new User();
        user.setId(cursor.getInt(cursor.getColumnIndexOrThrow(USER_ID)));
        user.setName(cursor.getString(cursor.getColumnIndexOrThrow(USER_NAME)));
        user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(USER_EMAIL)));
        user.setPhone(cursor.getString(cursor.getColumnIndexOrThrow(USER_PHONE)));
        user.setStudentId(cursor.getString(cursor.getColumnIndexOrThrow(USER_STUDENT_ID)));
        user.setRole(cursor.getString(cursor.getColumnIndexOrThrow(USER_ROLE)));
        user.setWeeklyLimit(cursor.getInt(cursor.getColumnIndexOrThrow(USER_WEEKLY_LIMIT)));
        return user;
    }

    /**
     * Lấy tất cả user (dành cho admin)
     */
    public java.util.List<User> getAllUsers() {
        java.util.List<User> users = new java.util.ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(TABLE_USERS, null, null, null, null, null, USER_NAME + " ASC");

        if (cursor.moveToFirst()) {
            do {
                User user = createUserFromCursor(cursor);
                users.add(user);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return users;
    }

    /**
     * Reset mật khẩu user (dành cho admin)
     */
    public boolean resetUserPassword(int userId, String newPassword) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(USER_PASSWORD, newPassword);

        String whereClause = USER_ID + " = ?";
        String[] whereArgs = {String.valueOf(userId)};

        int rowsAffected = db.update(TABLE_USERS, values, whereClause, whereArgs);
        db.close();

        return rowsAffected > 0;
    }

    /**
     * Xóa user (dành cho admin)
     */
    public boolean deleteUser(int userId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String whereClause = USER_ID + " = ?";
        String[] whereArgs = {String.valueOf(userId)};

        int rowsDeleted = db.delete(TABLE_USERS, whereClause, whereArgs);
        db.close();

        return rowsDeleted > 0;
    }
}
