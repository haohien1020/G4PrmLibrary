package com.example.g4prmlibrary.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.g4prmlibrary.models.Fine;

import java.util.ArrayList;
import java.util.List;

import static com.example.g4prmlibrary.database.DatabaseHelper.*;

public class FineDAO {
    private DatabaseHelper dbHelper;

    public FineDAO(Context context) {
        this.dbHelper = new DatabaseHelper(context);
    }

    /**
     * Thêm một khoản phạt mới
     */
    public long addFine(Fine fine) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(FINE_USER_ID, fine.getUserId());
        values.put(FINE_BORROW_ID, fine.getBorrowId());
        values.put(FINE_AMOUNT, fine.getAmount());
        values.put(FINE_REASON, fine.getReason());
        values.put(FINE_STATUS, fine.getStatus());
        values.put(FINE_CREATED_DATE, fine.getCreatedDate());

        long id = db.insert(TABLE_FINES, null, values);
        db.close();
        return id;
    }

    /**
     * Lấy tất cả các khoản phạt chưa thanh toán
     */
    public List<Fine> getUnpaidFines() {
        List<Fine> fines = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT f.*, u." + USER_NAME + ", u." + USER_STUDENT_ID +
                ", b." + BOOK_TITLE + ", br." + BORROW_DUE_DATE +
                " FROM " + TABLE_FINES + " f " +
                " JOIN " + TABLE_USERS + " u ON f." + FINE_USER_ID + " = u." + USER_ID +
                " JOIN " + TABLE_BORROWING_RECORDS + " br ON f." + FINE_BORROW_ID + " = br." + BORROW_ID +
                " JOIN " + TABLE_BOOKS + " b ON br." + BORROW_BOOK_ID + " = b." + BOOK_ID +
                " WHERE f." + FINE_STATUS + " = 'unpaid'" +
                " ORDER BY f." + FINE_CREATED_DATE + " DESC";

        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                Fine fine = createFineFromCursor(cursor);
                fine.setUserName(cursor.getString(cursor.getColumnIndexOrThrow(USER_NAME)));
                fine.setUserStudentId(cursor.getString(cursor.getColumnIndexOrThrow(USER_STUDENT_ID)));
                fine.setBookTitle(cursor.getString(cursor.getColumnIndexOrThrow(BOOK_TITLE)));
                fine.setDueDate(cursor.getString(cursor.getColumnIndexOrThrow(BORROW_DUE_DATE)));
                fines.add(fine);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return fines;
    }

    /**
     * Đánh dấu phạt đã được thanh toán
     */
    public boolean markFineAsPaid(int fineId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(FINE_STATUS, "paid");
        values.put(FINE_PAID_DATE, getCurrentDate());

        int rowsAffected = db.update(TABLE_FINES, values,
                FINE_ID + " = ?", new String[]{String.valueOf(fineId)});
        db.close();

        return rowsAffected > 0;
    }

    /**
     * Tạo Fine object từ Cursor
     */
    private Fine createFineFromCursor(Cursor cursor) {
        Fine fine = new Fine();
        fine.setId(cursor.getInt(cursor.getColumnIndexOrThrow(FINE_ID)));
        fine.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow(FINE_USER_ID)));
        fine.setBorrowId(cursor.getInt(cursor.getColumnIndexOrThrow(FINE_BORROW_ID)));
        fine.setAmount(cursor.getDouble(cursor.getColumnIndexOrThrow(FINE_AMOUNT)));
        fine.setReason(cursor.getString(cursor.getColumnIndexOrThrow(FINE_REASON)));
        fine.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(FINE_STATUS)));
        fine.setCreatedDate(cursor.getString(cursor.getColumnIndexOrThrow(FINE_CREATED_DATE)));

        int paidDateIndex = cursor.getColumnIndex(FINE_PAID_DATE);
        if (paidDateIndex != -1 && !cursor.isNull(paidDateIndex)) {
            fine.setPaidDate(cursor.getString(paidDateIndex));
        }

        return fine;
    }

    /**
     * Lấy ngày hiện tại
     */
    private String getCurrentDate() {
        return new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(new java.util.Date());
    }

    /**
     * Xóa phạt (chỉ dành cho admin)
     */
    public boolean deleteFine(int fineId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        int rowsDeleted = db.delete(TABLE_FINES,
                FINE_ID + " = ?", new String[]{String.valueOf(fineId)});
        db.close();

        return rowsDeleted > 0;
    }

    /**
     * Lấy tổng số tiền phạt chưa thanh toán của user
     */
    public double getTotalUnpaidFinesByUser(int userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT SUM(" + FINE_AMOUNT + ") FROM " + TABLE_FINES +
                " WHERE " + FINE_USER_ID + " = ? AND " + FINE_STATUS + " = 'unpaid'";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

        double total = 0;
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0);
        }

        cursor.close();
        db.close();
        return total;
    }
}
