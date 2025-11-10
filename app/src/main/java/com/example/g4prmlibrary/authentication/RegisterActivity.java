package com.example.g4prmlibrary.authentication;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.g4prmlibrary.R;
import com.example.g4prmlibrary.database.UserDAO;
import com.example.g4prmlibrary.models.User;

import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {
    
    private EditText etName, etPassword, etStudentId;
    private Button btnRegister;
    private TextView tvLogin;
    private UserDAO userDAO;
    
    // Regex pattern for MSSV0001 - MSSV9999
    private static final Pattern STUDENT_ID_PATTERN = Pattern.compile("^MSSV\\d{4}$");
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        
        initializeViews();
        setupDatabase();
        setupClickListeners();
        setupStudentIdValidation();
    }
    
    private void initializeViews() {
        etName = findViewById(R.id.et_name);
        etPassword = findViewById(R.id.et_password);
        etStudentId = findViewById(R.id.et_student_id);
        btnRegister = findViewById(R.id.btn_register);
        tvLogin = findViewById(R.id.tv_login);
    }
    
    private void setupDatabase() {
        userDAO = new UserDAO(this);
    }
    
    private void setupClickListeners() {
        btnRegister.setOnClickListener(v -> performRegister());
        tvLogin.setOnClickListener(v -> finish());
    }
    
    private void setupStudentIdValidation() {
        etStudentId.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String input = s.toString().toUpperCase();
                if (!input.equals(s.toString())) {
                    etStudentId.setText(input);
                    etStudentId.setSelection(input.length());
                }
            }
            
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
    
    private void performRegister() {
        String name = etName.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String studentId = etStudentId.getText().toString().trim().toUpperCase();
        
        // Validate required fields
        if (name.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập họ tên", Toast.LENGTH_SHORT).show();
            etName.requestFocus();
            return;
        }
        
        if (studentId.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập mã số sinh viên", Toast.LENGTH_SHORT).show();
            etStudentId.requestFocus();
            return;
        }
        
        // Validate MSSV format
        if (!isValidStudentId(studentId)) {
            Toast.makeText(this, "Mã sinh viên phải có định dạng MSSV0001 - MSSV9999", Toast.LENGTH_SHORT).show();
            etStudentId.requestFocus();
            return;
        }
        
        if (password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập mật khẩu", Toast.LENGTH_SHORT).show();
            etPassword.requestFocus();
            return;
        }
        
        if (password.length() < 6) {
            Toast.makeText(this, "Mật khẩu phải có ít nhất 6 ký tự", Toast.LENGTH_SHORT).show();
            etPassword.requestFocus();
            return;
        }
        
        // Generate email from student ID
        String email = studentId.toLowerCase() + "@student.library.edu.vn";
        
        // Check if student ID already exists
        if (userDAO.isStudentIdExists(studentId)) {
            Toast.makeText(this, "Mã sinh viên đã tồn tại", Toast.LENGTH_SHORT).show();
            etStudentId.requestFocus();
            return;
        }
        
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(password);
        user.setStudentId(studentId);
        user.setRole("user");
        user.setWeeklyLimit(3); // Default 3 books per week
        
        long result = userDAO.addUser(user);
        
        if (result > 0) {
            Toast.makeText(this, "Đăng ký thành công!\nEmail của bạn: " + email, Toast.LENGTH_LONG).show();
            finish();
        } else {
            Toast.makeText(this, "Đăng ký thất bại. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
        }
    }
    
    private boolean isValidStudentId(String studentId) {
        if (studentId == null || studentId.length() != 8) {
            return false;
        }
        
        // Check if starts with MSSV
        if (!studentId.startsWith("MSSV")) {
            return false;
        }
        
        // Check if last 4 characters are digits
        String numberPart = studentId.substring(4);
        try {
            int number = Integer.parseInt(numberPart);
            return number >= 1 && number <= 9999;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}