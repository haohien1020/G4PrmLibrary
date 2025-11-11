package com.example.g4prmlibrary;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.g4prmlibrary.authentication.ChangePasswordActivity;
import com.example.g4prmlibrary.authentication.LoginActivity;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private TextView tvUserName;
    private ImageView ivUserProfile, ivProfile;
    private SharedPreferences sharedPreferences;
    private boolean isAdmin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            // FORCE LOGOUT check (nếu có SessionManager)
            if (!com.example.g4prmlibrary.utils.SessionManager.checkAndForceLogoutIfNeeded(this)) {
                return;
            }

            setContentView(R.layout.activity_main);

            sharedPreferences = getSharedPreferences("LibraryPrefs", MODE_PRIVATE);

            tvUserName = findViewById(R.id.tv_user_name);
            ivUserProfile = findViewById(R.id.iv_user_profile);
            ivProfile = findViewById(R.id.iv_profile);

            checkUserRole();
//            setupClickListeners();

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: ", e);
            finish();
        }
    }

    private void checkUserRole() {
        try {
            isAdmin = sharedPreferences.getBoolean("is_admin", false);

            String realUserName = sharedPreferences.getString("user_name", "Guest");

            if (tvUserName != null) {
                if (isAdmin) {
                    tvUserName.setText("Thủ thư viện");
                } else {
                    tvUserName.setText(realUserName);
                }
            }

            setupRoleBasedVisibility();

        } catch (Exception e) {
            Log.e(TAG, "Error checking user role: ", e);
        }
    }

    private void setupRoleBasedVisibility() {
        try {
            if (ivUserProfile != null) {
                ivUserProfile.setVisibility(isAdmin ? View.GONE : View.VISIBLE);
            }

            if (ivProfile != null) {
                ivProfile.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error setting up role-based visibility: ", e);
        }
    }

//    private void setupClickListeners() {
//        try {
//            if (ivUserProfile != null) {
//                ivUserProfile.setOnClickListener(v -> showUserProfileMenu());
//            }
//
//            if (ivProfile != null) {
//                ivProfile.setOnClickListener(v -> {
//                    try {
//                        Intent intent = new Intent(MainActivity.this, com.example.g4prmlibrary.admin.AdminProfileActivity.class);
//                        startActivity(intent);
//                    } catch (Exception e) {
//                        Log.e(TAG, "Error starting AdminProfileActivity: ", e);
//                        Toast.makeText(this, "Lỗi khi mở trang quản lý", Toast.LENGTH_SHORT).show();
//                    }
//                });
//            }
//
//        } catch (Exception e) {
//            Log.e(TAG, "Error setting up click listeners: ", e);
//        }
//    }

    private void showUserProfileMenu() {
        try {
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
            builder.setTitle("👤 Tài khoản của bạn");

            String userName = sharedPreferences.getString("user_name", "Guest");
            String userEmail = sharedPreferences.getString("user_email", "N/A");

            builder.setMessage("👋 Xin chào: " + userName + "\n📧 Email: " + userEmail + "\n\n🔐 Bạn muốn đổi mật khẩu không?");

            builder.setPositiveButton("🔑 Đổi mật khẩu", (dialog, which) -> {
                try {
                    Intent intent = new Intent(MainActivity.this, ChangePasswordActivity.class);
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e(TAG, "Error starting ChangePasswordActivity: ", e);
                    Toast.makeText(this, "Lỗi khi mở trang đổi mật khẩu", Toast.LENGTH_SHORT).show();
                }
            });

            builder.setNegativeButton("🚪 Đăng xuất", (dialog, which) -> {
                android.app.AlertDialog.Builder logoutBuilder = new android.app.AlertDialog.Builder(this);
                logoutBuilder.setTitle("⚠️ Xác nhận đăng xuất");
                logoutBuilder.setMessage("Bạn có chắc chắn muốn đăng xuất không?");

                logoutBuilder.setPositiveButton("Đăng xuất", (d, w) -> {
                    // Clear session
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.clear();
                    editor.apply();

                    // Redirect to LoginActivity
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                });

                logoutBuilder.setNegativeButton("Hủy", (d, w) -> d.dismiss());
                logoutBuilder.show();
            });

            builder.show();

        } catch (Exception e) {
            Log.e(TAG, "Error showing user profile menu: ", e);
            Toast.makeText(this, "Lỗi khi hiển thị menu", Toast.LENGTH_SHORT).show();
        }
    }
}
