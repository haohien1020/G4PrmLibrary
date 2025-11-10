package com.example.g4prmlibrary.authentication;

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

public class ForgotPasswordActivity extends AppCompatActivity {
    
    private static final String TAG = "ForgotPasswordActivity";
    private EditText etStudentId, etEmail;
    private Button btnResetPassword;
    private ImageView ivBack;
    private TextView tvResult;
    
    private UserDAO userDAO;
    private ExecutorService executor;
    private Handler mainHandler;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        executor = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
        userDAO = new UserDAO(this);
        
        createUI();
        setupListeners();
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
        tvTitle.setText("🔐 Quên mật khẩu");
        tvTitle.setTextSize(20);
        tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        tvTitle.setPadding(16, 0, 0, 0);
        
        headerLayout.addView(ivBack);
        headerLayout.addView(tvTitle);
        
        // Instructions
        TextView tvInstructions = new TextView(this);
        tvInstructions.setText("Nhập MSSV và Email để reset mật khẩu về 123456");
        tvInstructions.setTextSize(14);
        tvInstructions.setPadding(0, 0, 0, 24);
        tvInstructions.setTextColor(getResources().getColor(android.R.color.darker_gray));
        
        // Student ID input
        TextView lblStudentId = new TextView(this);
        lblStudentId.setText("🆔 MSSV:");
        lblStudentId.setTextSize(14);
        lblStudentId.setTypeface(null, android.graphics.Typeface.BOLD);
        lblStudentId.setPadding(0, 0, 0, 8);
        
        etStudentId = new EditText(this);
        etStudentId.setHint("Nhập MSSV (VD: MSSV0001)");
        etStudentId.setTextSize(16);
        etStudentId.setPadding(16, 12, 16, 12);
        etStudentId.setBackground(getDrawable(android.R.drawable.edit_text));
        
        // Email input
        TextView lblEmail = new TextView(this);
        lblEmail.setText("📧 Email:");
        lblEmail.setTextSize(14);
        lblEmail.setTypeface(null, android.graphics.Typeface.BOLD);
        lblEmail.setPadding(0, 16, 0, 8);
        
        etEmail = new EditText(this);
        etEmail.setHint("Nhập email đã đăng ký");
        etEmail.setTextSize(16);
        etEmail.setPadding(16, 12, 16, 12);
        etEmail.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        etEmail.setBackground(getDrawable(android.R.drawable.edit_text));
        
        // Reset button
        btnResetPassword = new Button(this);
        btnResetPassword.setText("🔄 Reset mật khẩu");
        btnResetPassword.setTextSize(16);
        btnResetPassword.setTypeface(null, android.graphics.Typeface.BOLD);
        btnResetPassword.setPadding(0, 16, 0, 16);
        
        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        btnParams.setMargins(0, 24, 0, 16);
        btnResetPassword.setLayoutParams(btnParams);
        
        // Result text
        tvResult = new TextView(this);
        tvResult.setTextSize(14);
        tvResult.setPadding(0, 16, 0, 0);
        tvResult.setVisibility(android.view.View.GONE);
        
        // Add all views
        mainLayout.addView(headerLayout);
        mainLayout.addView(tvInstructions);
        mainLayout.addView(lblStudentId);
        mainLayout.addView(etStudentId);
        mainLayout.addView(lblEmail);
        mainLayout.addView(etEmail);
        mainLayout.addView(btnResetPassword);
        mainLayout.addView(tvResult);
        
        setContentView(mainLayout);
    }
    
    private void setupListeners() {
        ivBack.setOnClickListener(v -> finish());
        
        btnResetPassword.setOnClickListener(v -> {
            String studentId = etStudentId.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            
            if (studentId.isEmpty()) {
                etStudentId.setError("Vui lòng nhập MSSV");
                return;
            }
            
            if (email.isEmpty()) {
                etEmail.setError("Vui lòng nhập email");
                return;
            }
            
            // Auto format MSSV
            if (studentId.matches("\\d{4}")) {
                studentId = "MSSV" + studentId;
            } else if (!studentId.toUpperCase().startsWith("MSSV")) {
                etStudentId.setError("MSSV không đúng định dạng");
                return;
            }
            
            resetPassword(studentId.toUpperCase(), email);
        });
    }
    
    private void resetPassword(String studentId, String email) {
        btnResetPassword.setEnabled(false);
        btnResetPassword.setText("🔄 Đang xử lý...");
        
        executor.execute(() -> {
            try {
                // Tìm user theo MSSV
                User user = userDAO.getUserByStudentId(studentId);
                
                if (user == null) {
                    mainHandler.post(() -> {
                        showResult("❌ Không tìm thấy MSSV: " + studentId, false);
                        resetButton();
                    });
                    return;
                }
                
                // Kiểm tra email
                if (!email.equalsIgnoreCase(user.getEmail())) {
                    mainHandler.post(() -> {
                        showResult("❌ Email không khớp với MSSV này", false);
                        resetButton();
                    });
                    return;
                }
                
                // Reset password về 123456
                boolean success = userDAO.resetUserPassword(user.getId(), "123456");
                
                mainHandler.post(() -> {
                    if (success) {
                        showResult("✅ Đã reset mật khẩu thành công!\\n\\n" +
                                 "🔑 Mật khẩu mới: 123456\\n" +
                                 "🎯 Hãy đăng nhập và đổi mật khẩu mới", true);
                    } else {
                        showResult("❌ Lỗi khi reset mật khẩu. Vui lòng thử lại.", false);
                    }
                    resetButton();
                });
                
            } catch (Exception e) {
                Log.e(TAG, "Error resetting password: ", e);
                mainHandler.post(() -> {
                    showResult("❌ Lỗi hệ thống: " + e.getMessage(), false);
                    resetButton();
                });
            }
        });
    }
    
    private void showResult(String message, boolean isSuccess) {
        tvResult.setText(message);
        tvResult.setTextColor(getResources().getColor(
                isSuccess ? android.R.color.holo_green_dark : android.R.color.holo_red_dark));
        tvResult.setVisibility(android.view.View.VISIBLE);
        
        if (isSuccess) {
            Toast.makeText(this, "Reset mật khẩu thành công!", Toast.LENGTH_LONG).show();
        }
    }
    
    private void resetButton() {
        btnResetPassword.setEnabled(true);
        btnResetPassword.setText("🔄 Reset mật khẩu");
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
}
