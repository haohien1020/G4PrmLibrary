package com.example.g4prmlibrary.authentication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.g4prmlibrary.MainActivity;
import com.example.g4prmlibrary.R;
import com.example.g4prmlibrary.models.User;
import com.example.g4prmlibrary.database.UserDAO;
import com.example.g4prmlibrary.database.DataInitializer;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private EditText etCredential, etPassword;
    private Button btnLogin;
    private TextView tvRegister, tvForgotPassword;
    private UserDAO userDAO;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_login);

            initializeViews();
            setupDatabase();
            setupClickListeners();

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: ", e);
            Toast.makeText(this, "Lỗi khi khởi tạo ứng dụng", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initializeViews() {
        try {
            etCredential = findViewById(R.id.et_email);
            etPassword = findViewById(R.id.et_password);
            btnLogin = findViewById(R.id.btn_login);
            tvRegister = findViewById(R.id.tv_register);
            tvForgotPassword = findViewById(R.id.tv_forgot_password);

            sharedPreferences = getSharedPreferences("LibraryPrefs", MODE_PRIVATE);

            Log.d(TAG, "Views initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views: ", e);
            throw e;
        }
    }

    private void setupDatabase() {
        try {
            userDAO = new UserDAO(this);

            // Initialize sample data
            DataInitializer initializer = new DataInitializer(this);
            initializer.initializeSampleData();

            // Force reinitialize data after database upgrade
            // Database version 6 has updated admin credentials
            int currentDbVersion = sharedPreferences.getInt("db_version", 0);
            if (currentDbVersion < 6) {
                // Reset first_time flag to reinitialize data
                sharedPreferences.edit()
                        .putBoolean("first_time", true)
                        .putInt("db_version", 6)
                        .apply();
            }

            // Initialize sample data if first time
            if (sharedPreferences.getBoolean("first_time", true)) {
                initializer.initializeSampleData();
                sharedPreferences.edit().putBoolean("first_time", false).apply();
                Log.d(TAG, "Sample data initialized");
            }

        } catch (Exception e) {
            Log.e(TAG, "Error setting up database: ", e);
            Toast.makeText(this, "Lỗi khi khởi tạo cơ sở dữ liệu", Toast.LENGTH_LONG).show();
        }
    }

    private void setupClickListeners() {
        try {
            if (btnLogin != null) {
                btnLogin.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        performLogin();
                    }
                });
            }

            if (tvRegister != null) {
                tvRegister.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                            startActivity(intent);
                        } catch (Exception e) {
                            Log.e(TAG, "Error starting RegisterActivity: ", e);
                            Toast.makeText(LoginActivity.this, "Lỗi khi mở trang đăng ký", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            if (tvForgotPassword != null) {
                tvForgotPassword.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                            startActivity(intent);
                        } catch (Exception e) {
                            Log.e(TAG, "Error starting ForgotPasswordActivity: ", e);
                            Toast.makeText(LoginActivity.this, "Lỗi khi mở trang quên mật khẩu", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

        } catch (Exception e) {
            Log.e(TAG, "Error setting up click listeners: ", e);
        }
    }

    private void performLogin() {
        try {
            String credential = etCredential.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (credential.isEmpty()) {
                etCredential.setError("MSSV hoặc Email không được để trống");
                return;
            }

            if (password.isEmpty()) {
                etPassword.setError("Mật khẩu không được để trống");
                return;
            }

            // Kiểm tra loại đăng nhập: MSSV hoặc Email
            User user = null;
            if (credential.toUpperCase().startsWith("MSSV")) {
                // Đăng nhập bằng MSSV (sinh viên)
                Log.d(TAG, "Attempting MSSV login: " + credential.toUpperCase());
                user = userDAO.authenticateUserByMSSV(credential.toUpperCase(), password);
            } else if (credential.contains("@")) {
                // Đăng nhập bằng Email (admin)
                Log.d(TAG, "Attempting Email login: " + credential);
                user = userDAO.authenticateUser(credential, password);
            } else {
                Toast.makeText(this, "Vui lòng nhập MSSV (VD: MSSV0001) hoặc Email hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }

            Log.d(TAG, "Authentication result: " + (user != null ? "SUCCESS" : "FAILED"));

            if (user != null) {
                // Sử dụng SessionManager để save user info
                com.example.g4prmlibrary.utils.SessionManager.markUserLoggedIn(
                        this,
                        user.getId(),
                        user.getName(),
                        user.getEmail(),
                        "admin".equals(user.getRole())
                );

                // Navigate to MainActivity
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "MSSV/Email hoặc mật khẩu không đúng", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Log.e(TAG, "Error in performLogin: ", e);
            Toast.makeText(this, "Lỗi khi đăng nhập", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        try {
            // Sử dụng SessionManager để kiểm tra login status
            if (com.example.g4prmlibrary.utils.SessionManager.isUserLoggedIn(this)) {
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onStart: ", e);
        }
    }
}
