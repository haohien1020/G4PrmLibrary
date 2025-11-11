package com.example.library.admin;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.library.R;
import com.example.library.models.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

public class AdminProfileActivity extends AppCompatActivity {
    
    private static final String TAG = "AdminProfileActivity";
    private ImageView ivBack;
    private TextView tvName, tvEmail, tvRole, tvUserId;
    private MaterialButton btnChangePassword, btnBorrowBook, btnLogout, btnLibraryRules;
    private MaterialCardView cardProfile, cardActions;
    private DatabaseHelper databaseHelper;
    private SharedPreferences sharedPreferences;
    private User currentUser;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            setContentView(R.layout.activity_admin_profile);
            
            initializeViews();
            setupDatabase();
            loadUserData();
            setupClickListeners();
            
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: ", e);
            Toast.makeText(this, "Lỗi khi tải thông tin admin", Toast.LENGTH_LONG).show();
            finish();
        }
    }
    
    private void initializeViews() {
        try {
            ivBack = findViewById(R.id.iv_back);
            tvName = findViewById(R.id.tv_name);
            tvEmail = findViewById(R.id.tv_email);
            tvRole = findViewById(R.id.tv_role);
            tvUserId = findViewById(R.id.tv_user_id);
            btnChangePassword = findViewById(R.id.btn_change_password);
            btnBorrowBook = findViewById(R.id.btn_borrow_book);
            btnLogout = findViewById(R.id.btn_logout);
            btnLibraryRules = findViewById(R.id.btn_library_rules);
            cardProfile = findViewById(R.id.card_profile);
            cardActions = findViewById(R.id.card_actions);
            
            sharedPreferences = getSharedPreferences("LibraryPrefs", MODE_PRIVATE);
            
            Log.d(TAG, "Views initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views: ", e);
            throw e;
        }
    }
    
    private void setupDatabase() {
        try {
            databaseHelper = new DatabaseHelper(this);
            Log.d(TAG, "Database setup successful");
        } catch (Exception e) {
            Log.e(TAG, "Error setting up database: ", e);
        }
    }
    
    private void loadUserData() {
        try {
            String userEmail = sharedPreferences.getString("user_email", "");
            String userName = sharedPreferences.getString("user_name", "");
            String userRole = sharedPreferences.getString("user_role", "");
            int userId = sharedPreferences.getInt("user_id", -1);
            
            // Set user information
            if (tvName != null) tvName.setText(userName);
            if (tvEmail != null) tvEmail.setText(userEmail);
            if (tvRole != null) tvRole.setText("Vai trò: " + (userRole.equals("admin") ? "Quản trị viên" : "Người dùng"));
            if (tvUserId != null) tvUserId.setText("ID: #" + userId);
            
            Log.d(TAG, "User data loaded successfully");
            
        } catch (Exception e) {
            Log.e(TAG, "Error loading user data: ", e);
            Toast.makeText(this, "Lỗi khi tải thông tin người dùng", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void setupClickListeners() {
        try {
            if (ivBack != null) {
                ivBack.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });
            }
            
            if (btnChangePassword != null) {
                btnChangePassword.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showChangePasswordDialog();
                    }
                });
            }
            
            if (btnBorrowBook != null) {
                btnBorrowBook.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openBorrowActivity();
                    }
                });
            }
            
            if (btnLogout != null) {
                btnLogout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showLogoutConfirmationDialog();
                    }
                });
            }
            
            if (btnLibraryRules != null) {
                btnLibraryRules.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openLibraryRules();
                    }
                });
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error setting up click listeners: ", e);
        }
    }
    
    private void showChangePasswordDialog() {
        try {
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_change_password, null);
            EditText etCurrentPassword = dialogView.findViewById(R.id.et_current_password);
            EditText etNewPassword = dialogView.findViewById(R.id.et_new_password);
            EditText etConfirmPassword = dialogView.findViewById(R.id.et_confirm_password);
            
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Đổi mật khẩu");
            builder.setView(dialogView);
            
            builder.setPositiveButton("Đổi mật khẩu", (dialog, which) -> {
                String currentPassword = etCurrentPassword.getText().toString().trim();
                String newPassword = etNewPassword.getText().toString().trim();
                String confirmPassword = etConfirmPassword.getText().toString().trim();
                
                if (validatePasswordChange(currentPassword, newPassword, confirmPassword)) {
                    changePassword(currentPassword, newPassword);
                }
            });
            
            builder.setNegativeButton("Hủy", (dialog, which) -> {
                dialog.dismiss();
            });
            
            AlertDialog dialog = builder.create();
            dialog.show();
            
        } catch (Exception e) {
            Log.e(TAG, "Error showing change password dialog: ", e);
            Toast.makeText(this, "Lỗi khi mở dialog đổi mật khẩu", Toast.LENGTH_SHORT).show();
        }
    }
    
    private boolean validatePasswordChange(String currentPassword, String newPassword, String confirmPassword) {
        if (currentPassword.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập mật khẩu hiện tại", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        if (newPassword.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập mật khẩu mới", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        if (newPassword.length() < 6) {
            Toast.makeText(this, "Mật khẩu mới phải có ít nhất 6 ký tự", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(this, "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        return true;
    }
    
    private void changePassword(String currentPassword, String newPassword) {
        try {
            String userEmail = sharedPreferences.getString("user_email", "");
            
            // Verify current password
            User user = databaseHelper.authenticateUser(userEmail, currentPassword);
            if (user == null) {
                Toast.makeText(this, "Mật khẩu hiện tại không đúng", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Update password in database
            user.setPassword(newPassword);
            boolean success = databaseHelper.updateUser(user);
            
            if (success) {
                Toast.makeText(this, "Đổi mật khẩu thành công", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Lỗi khi đổi mật khẩu", Toast.LENGTH_SHORT).show();
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error changing password: ", e);
            Toast.makeText(this, "Lỗi khi đổi mật khẩu", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void showLogoutConfirmationDialog() {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Xác nhận đăng xuất");
            builder.setMessage("Bạn có chắc chắn muốn đăng xuất?");
            
            builder.setPositiveButton("Đăng xuất", (dialog, which) -> {
                logout();
            });
            
            builder.setNegativeButton("Hủy", (dialog, which) -> {
                dialog.dismiss();
            });
            
            AlertDialog dialog = builder.create();
            dialog.show();
            
        } catch (Exception e) {
            Log.e(TAG, "Error showing logout dialog: ", e);
        }
    }
    
    private void logout() {
        try {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();
            
            Intent intent = new Intent(AdminProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            
        } catch (Exception e) {
            Log.e(TAG, "Error in logout: ", e);
            Toast.makeText(this, "Lỗi khi đăng xuất", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void openLibraryRules() {
        try {
            Intent intent = new Intent(AdminProfileActivity.this, LibraryRulesActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error opening library rules: ", e);
            Toast.makeText(this, "Lỗi khi mở quy định thư viện", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void openBorrowActivity() {
        try {
            Intent intent = new Intent(AdminProfileActivity.this, AdminBorrowActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error opening borrow activity: ", e);
            Toast.makeText(this, "Lỗi khi mở trang cho mượn sách", Toast.LENGTH_SHORT).show();
        }
    }
}
