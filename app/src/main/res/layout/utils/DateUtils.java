package com.example.library.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Utility class để xử lý các operations liên quan đến ngày tháng
 */
public class DateUtils {
    
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
    
    /**
     * Kiểm tra xem ngày có quá hạn không
     */
    public static boolean isDateOverdue(String dueDate) {
        try {
            Date due = sdf.parse(dueDate);
            Date today = new Date();
            return due != null && today.after(due);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Lấy ngày hiện tại theo format yyyy-MM-dd
     */
    public static String getCurrentDate() {
        return sdf.format(new Date());
    }
    
    /**
     * Lấy ngày hạn trả sau số ngày nhất định
     */
    public static String getDueDateAfterDays(int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, days);
        return sdf.format(calendar.getTime());
    }
    
    /**
     * Tính số ngày giữa 2 ngày
     */
    public static int getDaysBetween(String startDate, String endDate) {
        try {
            Date start = sdf.parse(startDate);
            Date end = sdf.parse(endDate);
            
            if (start != null && end != null) {
                long diffInMillis = end.getTime() - start.getTime();
                return (int) (diffInMillis / (24 * 60 * 60 * 1000));
            }
        } catch (Exception e) {
            // Ignore
        }
        return 0;
    }
    
    /**
     * Format ngày theo định dạng dd/MM/yyyy để hiển thị
     */
    public static String formatDateForDisplay(String dateString) {
        try {
            Date date = sdf.parse(dateString);
            SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            return displayFormat.format(date);
        } catch (Exception e) {
            return dateString; // Return original if parsing fails
        }
    }
    
    /**
     * Kiểm tra xem ngày có hợp lệ không
     */
    public static boolean isValidDate(String dateString) {
        try {
            Date date = sdf.parse(dateString);
            return date != null;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * So sánh 2 ngày
     * @return -1 nếu date1 < date2, 0 nếu bằng nhau, 1 nếu date1 > date2
     */
    public static int compareDates(String date1, String date2) {
        try {
            Date d1 = sdf.parse(date1);
            Date d2 = sdf.parse(date2);
            
            if (d1 != null && d2 != null) {
                return d1.compareTo(d2);
            }
        } catch (Exception e) {
            // Ignore
        }
        return 0;
    }
}