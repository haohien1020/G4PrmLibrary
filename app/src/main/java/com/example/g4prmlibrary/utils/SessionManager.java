package com.example.g4prmlibrary.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.g4prmlibrary.authentication.LoginActivity;

/**
 * Utility để quản lý force logout mechanism
 */
public class SessionManager {
    private static final String TAG = "SessionManager";
    private static final String PREF_NAME = "LibraryPrefs";
    private static final String KEY_FORCE_LOGOUT_VERSION = "force_logout_version";
    
    // Tăng version này khi muốn force logout tất cả users
    private static final int CURRENT_LOGOUT_VERSION = 2; // Tăng từ 1 lên 2
    
    /**
     * Kiểm tra và force logout nếu cần
     * @param activity Activity hiện tại
     * @return true nếu user vẫn valid, false nếu đã bị logout
     */
    public static boolean checkAndForceLogoutIfNeeded(Activity activity) {
        try {
            SharedPreferences sharedPref = activity.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            
            int savedLogoutVersion = sharedPref.getInt(KEY_FORCE_LOGOUT_VERSION, 0);
            
            // Nếu version cũ hơn current version -> force logout
            if (savedLogoutVersion < CURRENT_LOGOUT_VERSION) {
                Log.i(TAG, "Force logout triggered - version mismatch: " + savedLogoutVersion + " < " + CURRENT_LOGOUT_VERSION);
                
                // Clear tất cả session data
                clearAllSessionData(activity);
                
                // Update logout version
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putInt(KEY_FORCE_LOGOUT_VERSION, CURRENT_LOGOUT_VERSION);
                editor.apply();
                
                // Redirect về LoginActivity
                redirectToLogin(activity);
                
                return false; // User đã bị logout
            }
            
            // Kiểm tra user có đang logged in không
            boolean isLoggedIn = sharedPref.getBoolean("is_logged_in", false);
            if (!isLoggedIn) {
                Log.i(TAG, "User not logged in - redirecting to login");
                redirectToLogin(activity);
                return false;
            }
            
            return true; // User vẫn valid
            
        } catch (Exception e) {
            Log.e(TAG, "Error checking session: ", e);
            // Nếu có lỗi, redirect về login để safety
            redirectToLogin(activity);
            return false;
        }
    }
    
    /**
     * Force logout tất cả users ngay lập tức
     */
    public static void forceLogoutAllUsers(Activity activity) {
        try {
            // Tăng logout version để trigger force logout
            SharedPreferences sharedPref = activity.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putInt(KEY_FORCE_LOGOUT_VERSION, CURRENT_LOGOUT_VERSION + 1);
            editor.apply();
            
            // Clear session data
            clearAllSessionData(activity);
            
            // Redirect về login
            redirectToLogin(activity);
            
            Log.i(TAG, "Force logout all users completed");
            
        } catch (Exception e) {
            Log.e(TAG, "Error during force logout: ", e);
        }
    }
    
    /**
     * Clear tất cả session data
     */
    private static void clearAllSessionData(Activity activity) {
        try {
            SharedPreferences sharedPref = activity.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            
            // Giữ lại force logout version, clear tất cả data khác
            int logoutVersion = sharedPref.getInt(KEY_FORCE_LOGOUT_VERSION, CURRENT_LOGOUT_VERSION);
            editor.clear();
            editor.putInt(KEY_FORCE_LOGOUT_VERSION, logoutVersion);
            editor.apply();
            
            Log.i(TAG, "All session data cleared");
            
        } catch (Exception e) {
            Log.e(TAG, "Error clearing session data: ", e);
        }
    }
    
    /**
     * Redirect về LoginActivity
     */
    private static void redirectToLogin(Activity activity) {
        try {
            Intent intent = new Intent(activity, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            activity.startActivity(intent);
            activity.finish();
            
        } catch (Exception e) {
            Log.e(TAG, "Error redirecting to login: ", e);
        }
    }
    
    /**
     * Mark user as logged in
     */
    public static void markUserLoggedIn(Context context, int userId, String userName, String userEmail, boolean isAdmin) {
        try {
            SharedPreferences sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            
            editor.putBoolean("is_logged_in", true);
            editor.putInt("user_id", userId);
            editor.putString("user_name", userName);
            editor.putString("user_email", userEmail);
            editor.putBoolean("is_admin", isAdmin);
            editor.putInt(KEY_FORCE_LOGOUT_VERSION, CURRENT_LOGOUT_VERSION);
            
            editor.apply();
            
            Log.i(TAG, "User marked as logged in: " + userName);
            
        } catch (Exception e) {
            Log.e(TAG, "Error marking user as logged in: ", e);
        }
    }
    
    /**
     * Check if user is currently logged in
     */
    public static boolean isUserLoggedIn(Context context) {
        try {
            SharedPreferences sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            return sharedPref.getBoolean("is_logged_in", false);
        } catch (Exception e) {
            Log.e(TAG, "Error checking login status: ", e);
            return false;
        }
    }
}