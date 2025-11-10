package com.example.g4prmlibrary.authentication;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.g4prmlibrary.database.UserDAO;
import com.example.g4prmlibrary.models.User;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChangePasswordActivity extends AppCompatActivity {
    
    private static final String TAG = "ChangePasswordActivity";
    private EditText etCurrentPassword, etNewPassword, etConfirmPassword;
    private Button btnChangePassword;
    private ImageView ivBack;
    private TextView tvUserInfo;
    
    private UserDAO userDAO;
    private SharedPreferences sharedPreferences;
    private ExecutorService executor;
    private Handler mainHandler;
    private int currentUserId;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        executor = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
        userDAO = new UserDAO(this);
        sharedPreferences = getSharedPreferences("LibraryPrefs", MODE_PRIVATE);
        currentUserId = sharedPreferences.getInt("user_id", -1);
        
        if (currentUserId == -1) {
            Toast.makeText(this, "Phiên đăng nhập hết hạn", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        createUI();
        setupListeners();
        loadUserInfo();
    }
    
    private void createUI() {
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(32, 32, 32, 32);
        
        // Header
        LinearLayout headerLayout = new LinearLayout(this);
        headerLayout.setOrientation(LinearLayout.HORIZONTAL);
        headerLayout.setGravity(android.view.Gravity.CENTER_VERTICAL);
        headerLayout.setPadding(0, 0, 0, 32);
        
        ivBack = new ImageView(this);
        ivBack.setImageResource(android.R.drawable.ic_menu_revert);
        ivBack.setPadding(16, 16, 16, 16);
        
        TextView tvTitle = new TextView(this);
        tvTitle.setText("🔐 Đổi mật khẩu");
        tvTitle.setTextSize(20);
        tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        tvTitle.setPadding(16, 0, 0, 0);
        
        headerLayout.addView(ivBack);
        headerLayout.addView(tvTitle);
        
        // User info
        tvUserInfo = new TextView(this);
        tvUserInfo.setTextSize(14);
        tvUserInfo.setPadding(0, 0, 0, 24);
        tvUserInfo.setTextColor(getResources().getColor(android.R.color.darker_gray));
        
        // Current password
        TextView lblCurrentPassword = new TextView(this);
        lblCurrentPassword.setText("🔒 Mật khẩu hiện tại:");
        lblCurrentPassword.setTextSize(14);
        lblCurrentPassword.setTypeface(null, android.graphics.Typeface.BOLD);
        lblCurrentPassword.setPadding(0, 0, 0, 8);
        
        etCurrentPassword = new EditText(this);
        etCurrentPassword.setHint("Nhập mật khẩu hiện tại");
        etCurrentPassword.setTextSize(16);
        etCurrentPassword.setPadding(16, 12, 16, 12);
        etCurrentPassword.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        etCurrentPassword.setBackground(getDrawable(android.R.drawable.edit_text));
        
        // New password
        TextView lblNewPassword = new TextView(this);
        lblNewPassword.setText("🔑 Mật khẩu mới:");
        lblNewPassword.setTextSize(14);
        lblNewPassword.setTypeface(null, android.graphics.Typeface.BOLD);
        lblNewPassword.setPadding(0, 16, 0, 8);
        
        etNewPassword = new EditText(this);
        etNewPassword.setHint("Nhập mật khẩu mới (tối thiểu 6 ký tự)");
        etNewPassword.setTextSize(16);
        etNewPassword.setPadding(16, 12, 16, 12);
        etNewPassword.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        etNewPassword.setBackground(getDrawable(android.R.drawable.edit_text));
        
        // Confirm password
        TextView lblConfirmPassword = new TextView(this);
        lblConfirmPassword.setText("🔐 Xác nhận mật khẩu mới:");
        lblConfirmPassword.setTextSize(14);
        lblConfirmPassword.setTypeface(null, android.graphics.Typeface.BOLD);
        lblConfirmPassword.setPadding(0, 16, 0, 8);
        
        etConfirmPassword = new EditText(this);
        etConfirmPassword.setHint("Nhập lại mật khẩu mới");
        etConfirmPassword.setTextSize(16);
        etConfirmPassword.setPadding(16, 12, 16, 12);
        etConfirmPassword.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        etConfirmPassword.setBackground(getDrawable(android.R.drawable.edit_text));
        
        // Change password button
        btnChangePassword = new Button(this);
        btnChangePassword.setText("🔄 Đổi mật khẩu");
        btnChangePassword.setTextSize(16);
        btnChangePassword.setTypeface(null, android.graphics.Typeface.BOLD);
        btnChangePassword.setPadding(0, 16, 0, 16);
        
        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        btnParams.setMargins(0, 24, 0, 0);
        btnChangePassword.setLayoutParams(btnParams);
        
        // Security note
        TextView tvSecurityNote = new TextView(this);
        tvSecurityNote.setText("🛡️ Lưu ý: Sau khi đổi mật khẩu, bạn sẽ cần đăng nhập lại");
        tvSecurityNote.setTextSize(12);
        tvSecurityNote.setPadding(0, 16, 0, 0);
        tvSecurityNote.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
        
        // Add all views
        mainLayout.addView(headerLayout);
        mainLayout.addView(tvUserInfo);
        mainLayout.addView(lblCurrentPassword);
        mainLayout.addView(etCurrentPassword);
        mainLayout.addView(lblNewPassword);
        mainLayout.addView(etNewPassword);
        mainLayout.addView(lblConfirmPassword);
        mainLayout.addView(etConfirmPassword);
        mainLayout.addView(btnChangePassword);
        mainLayout.addView(tvSecurityNote);
        
        setContentView(mainLayout);
    }
    
    private void setupListeners() {
        ivBack.setOnClickListener(v -> finish());
        
        btnChangePassword.setOnClickListener(v -> {
            String currentPassword = etCurrentPassword.getText().toString().trim();
            String newPassword = etNewPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();
            
            if (currentPassword.isEmpty()) {
                etCurrentPassword.setError("Vui lòng nhập mật khẩu hiện tại");
                return;
            }
            
            if (newPassword.isEmpty()) {
                etNewPassword.setError("Vui lòng nhập mật khẩu mới");
                return;
            }
            
            if (newPassword.length() < 6) {
                etNewPassword.setError("Mật khẩu mới phải có ít nhất 6 ký tự");
                return;
            }
            
            if (!newPassword.equals(confirmPassword)) {
                etConfirmPassword.setError("Xác nhận mật khẩu không khớp");
                return;
            }
            
            if (currentPassword.equals(newPassword)) {
                etNewPassword.setError("Mật khẩu mới phải khác mật khẩu hiện tại");
                return;
            }
            
            changePassword(currentPassword, newPassword);
        });
    }
    
    private void loadUserInfo() {
        executor.execute(() -> {
            try {
                User user = userDAO.getUserById(currentUserId);
                
                mainHandler.post(() -> {
                    if (user != null) {
                        tvUserInfo.setText("👤 Đang đổi mật khẩu cho: " + user.getName() + " (" + user.getStudentId() + ")");
                    }
                });
                
            } catch (Exception e) {
                Log.e(TAG, "Error loading user info: ", e);
            }
        });
    }
    
    private void changePassword(String currentPassword, String newPassword) {
        btnChangePassword.setEnabled(false);
        btnChangePassword.setText("🔄 Đang xử lý...");
        
        executor.execute(() -> {
            try {
                // Lấy user hiện tại
                User user = userDAO.getUserById(currentUserId);
                
                if (user == null) {
                    mainHandler.post(() -> {
                        Toast.makeText(this, "❌ Không tìm thấy thông tin user", Toast.LENGTH_SHORT).show();
                        resetButton();
                    });
                    return;
                }
                
                // Kiểm tra mật khẩu hiện tại
                User authenticatedUser = userDAO.authenticateUser(user.getEmail(), currentPassword);
                
                if (authenticatedUser == null) {
                    mainHandler.post(() -> {
                        etCurrentPassword.setError("Mật khẩu hiện tại không đúng");
                        resetButton();
                    });
                    return;
                }
                
                // Đổi mật khẩu
                boolean success = userDAO.resetUserPassword(currentUserId, newPassword);
                
                mainHandler.post(() -> {
                    if (success) {
                        Toast.makeText(this, "✅ Đổi mật khẩu thành công!", Toast.LENGTH_LONG).show();
                        
                        // Logout và quay về LoginActivity
                        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
                        builder.setTitle("✅ Thành công");
                        builder.setMessage("Đã đổi mật khẩu thành công!\\n\\nBạn sẽ được đăng xuất để đăng nhập lại với mật khẩu mới.");
                        builder.setCancelable(false);
                        builder.setPositiveButton("Đăng xuất", (dialog, which) -> {
                            logout();
                        });
                        builder.show();
                        
                    } else {
                        Toast.makeText(this, "❌ Lỗi khi đổi mật khẩu. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
                        resetButton();
                    }
                });
                
            } catch (Exception e) {
                Log.e(TAG, "Error changing password: ", e);
                mainHandler.post(() -> {
                    Toast.makeText(this, "❌ Lỗi hệ thống: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    resetButton();
                });
            }
        });
    }
    
    private void logout() {
        // Clear session
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
        
        // Redirect to LoginActivity
        android.content.Intent intent = new android.content.Intent(this, LoginActivity.class);
        intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    
    private void resetButton() {
        btnChangePassword.setEnabled(true);
        btnChangePassword.setText("🔄 Đổi mật khẩu");
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
}
