package com.example.library.admin;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.library.database.BorrowingDAO;
import com.example.library.database.UserDAO;
import com.example.library.models.User;
import com.example.library.models.BorrowingRecord;

import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AdminUserManagementActivity extends AppCompatActivity {
    
    private static final String TAG = "AdminUserManagement";
    private ImageView ivBack;
    private TextView tvTitle;
    private ScrollView scrollView;
    private LinearLayout layoutContent;
    private ProgressBar progressBar;
    private LinearLayout layoutFilterButtons;
    
    private UserDAO userDAO;
    private BorrowingDAO borrowingDAO;
    private ExecutorService executor;
    private Handler mainHandler;
    
    // Filter states
    private String currentFilter = "all"; // "all", "overdue"
    private List<User> allUsers = new ArrayList<>();
    private List<Integer> overdueUserIds = new ArrayList<>();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            createUI();
            setupDatabase();
            loadUsersData();
            
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: ", e);
            Toast.makeText(this, "Lỗi khi tải trang quản lý user", Toast.LENGTH_LONG).show();
            finish();
        }
    }
    
    private void createUI() {
        executor = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
        
        // Create main layout
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 
            LinearLayout.LayoutParams.MATCH_PARENT));
        
        // Header layout
        LinearLayout layoutHeader = new LinearLayout(this);
        layoutHeader.setOrientation(LinearLayout.HORIZONTAL);
        layoutHeader.setGravity(android.view.Gravity.CENTER_VERTICAL);
        layoutHeader.setPadding(16, 16, 16, 16);
        layoutHeader.setBackground(getDrawable(android.R.drawable.dialog_holo_light_frame));
        
        ivBack = new ImageView(this);
        ivBack.setImageResource(android.R.drawable.ic_menu_revert);
        ivBack.setPadding(16, 16, 16, 16);
        ivBack.setOnClickListener(v -> finish());
        
        tvTitle = new TextView(this);
        tvTitle.setText("👥 Quản lý User");
        tvTitle.setTextSize(18);
        tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        tvTitle.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
        tvTitle.setPadding(16, 0, 0, 0);
        
        layoutHeader.addView(ivBack);
        layoutHeader.addView(tvTitle);
        
        // Filter buttons section
        layoutFilterButtons = new LinearLayout(this);
        layoutFilterButtons.setOrientation(LinearLayout.HORIZONTAL);
        layoutFilterButtons.setPadding(16, 16, 16, 16);
        layoutFilterButtons.setGravity(android.view.Gravity.CENTER);
        layoutFilterButtons.setBackground(getDrawable(android.R.drawable.dialog_holo_light_frame));
        
        // All users button
        com.google.android.material.button.MaterialButton btnAllUsers = new com.google.android.material.button.MaterialButton(this);
        btnAllUsers.setText("👥 Tất cả user");
        btnAllUsers.setTextSize(14);
        btnAllUsers.setBackgroundColor(getColor(android.R.color.holo_blue_dark));
        btnAllUsers.setTextColor(getColor(android.R.color.white));
        LinearLayout.LayoutParams allParams = new LinearLayout.LayoutParams(
            0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        allParams.setMarginEnd(8);
        btnAllUsers.setLayoutParams(allParams);
        btnAllUsers.setOnClickListener(v -> filterUsers("all"));
        
        // Overdue users button
        com.google.android.material.button.MaterialButton btnOverdueUsers = new com.google.android.material.button.MaterialButton(this);
        btnOverdueUsers.setText("⚠️ User nợ sách");
        btnOverdueUsers.setTextSize(14);
        btnOverdueUsers.setBackgroundColor(getColor(android.R.color.transparent));
        btnOverdueUsers.setTextColor(getColor(android.R.color.holo_red_dark));
        LinearLayout.LayoutParams overdueParams = new LinearLayout.LayoutParams(
            0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        btnOverdueUsers.setLayoutParams(overdueParams);
        btnOverdueUsers.setOnClickListener(v -> filterUsers("overdue"));
        
        layoutFilterButtons.addView(btnAllUsers);
        layoutFilterButtons.addView(btnOverdueUsers);
        
        // Progress bar
        progressBar = new ProgressBar(this);
        progressBar.setVisibility(View.VISIBLE);
        LinearLayout.LayoutParams progressParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT);
        progressParams.gravity = android.view.Gravity.CENTER;
        progressParams.setMargins(0, 20, 0, 20);
        progressBar.setLayoutParams(progressParams);
        
        // ScrollView để có thể cuộn xem tất cả user
        scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        scrollView.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 
            LinearLayout.LayoutParams.MATCH_PARENT));
        
        // Content layout bên trong ScrollView
        layoutContent = new LinearLayout(this);
        layoutContent.setOrientation(LinearLayout.VERTICAL);
        layoutContent.setPadding(16, 16, 16, 16);
        layoutContent.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT));
        
        scrollView.addView(layoutContent);
        
        // Add all views to main layout
        mainLayout.addView(layoutHeader);
        mainLayout.addView(layoutFilterButtons);
        mainLayout.addView(progressBar);
        mainLayout.addView(scrollView);
        
        setContentView(mainLayout);
    }
    
    private void setupDatabase() {
        try {
            userDAO = new UserDAO(this);
            borrowingDAO = new BorrowingDAO(this);
        } catch (Exception e) {
            Log.e(TAG, "Error setting up database: ", e);
        }
    }
    
    private void loadUsersData() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        
        executor.execute(() -> {
            try {
                allUsers = userDAO.getAllUsers();
                overdueUserIds = borrowingDAO.getUsersWithOverdueBooks();
                
                mainHandler.post(() -> {
                    try {
                        applyCurrentFilter();
                    } catch (Exception e) {
                        Log.e(TAG, "Error displaying users: ", e);
                        hideProgressBar();
                    }
                });
                
            } catch (Exception e) {
                Log.e(TAG, "Error loading users: ", e);
                mainHandler.post(() -> {
                    hideProgressBar();
                    Toast.makeText(AdminUserManagementActivity.this, "Lỗi khi tải dữ liệu users", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    
    private void filterUsers(String filterType) {
        currentFilter = filterType;
        updateFilterButtonStyles();
        applyCurrentFilter();
    }
    
    private void updateFilterButtonStyles() {
        for (int i = 0; i < layoutFilterButtons.getChildCount(); i++) {
            View child = layoutFilterButtons.getChildAt(i);
            if (child instanceof com.google.android.material.button.MaterialButton) {
                com.google.android.material.button.MaterialButton btn = (com.google.android.material.button.MaterialButton) child;
                if ((i == 0 && "all".equals(currentFilter)) || (i == 1 && "overdue".equals(currentFilter))) {
                    btn.setBackgroundColor(getColor(android.R.color.holo_blue_dark));
                    btn.setTextColor(getColor(android.R.color.white));
                } else {
                    btn.setBackgroundColor(getColor(android.R.color.transparent));
                    btn.setTextColor(getColor(i == 0 ? android.R.color.holo_blue_dark : android.R.color.holo_red_dark));
                }
            }
        }
    }
    
    private void applyCurrentFilter() {
        List<User> filteredUsers = new ArrayList<>();
        
        if ("overdue".equals(currentFilter)) {
            // Only show users with overdue books
            for (User user : allUsers) {
                if (!"admin".equals(user.getRole()) && overdueUserIds.contains(user.getId())) {
                    filteredUsers.add(user);
                }
            }
        } else {
            // Show all users (excluding admin)
            for (User user : allUsers) {
                if (!"admin".equals(user.getRole())) {
                    filteredUsers.add(user);
                }
            }
        }
        
        displayUsers(filteredUsers);
    }
    
    private void hideProgressBar() {
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
    }
    
    private void displayUsers(List<User> users) {
        try {
            hideProgressBar();
            
            if (layoutContent != null) {
                layoutContent.removeAllViews();
            }
            
            // Instruction text
            TextView instructionText = new TextView(this);
            String filterInstruction = "overdue".equals(currentFilter) ? 
                "⚠️ Danh sách sinh viên đang nợ sách (quá hạn trả)" : 
                "📖 Cuộn xuống để xem tất cả sinh viên trong hệ thống";
            instructionText.setText(filterInstruction);
            instructionText.setTextSize(14);
            instructionText.setTextColor(getResources().getColor(
                "overdue".equals(currentFilter) ? android.R.color.holo_red_dark : android.R.color.holo_orange_dark));
            instructionText.setTypeface(null, android.graphics.Typeface.ITALIC);
            instructionText.setPadding(0, 0, 0, 20);
            layoutContent.addView(instructionText);
            
            // User list section title
            TextView userTitle = new TextView(this);
            String titleText = "overdue".equals(currentFilter) ? 
                "🚨 SINH VIÊN NỢ SÁCH" : 
                "📋 DANH SÁCH SINH VIÊN";
            userTitle.setText(titleText);
            userTitle.setTextSize(18);
            userTitle.setTypeface(null, android.graphics.Typeface.BOLD);
            userTitle.setTextColor(getResources().getColor(
                "overdue".equals(currentFilter) ? android.R.color.holo_red_dark : android.R.color.holo_blue_dark));
            userTitle.setPadding(0, 0, 0, 20);
            layoutContent.addView(userTitle);
            
            // Add users
            int studentCount = 0;
            for (User user : users) {
                addUserItem(user);
                studentCount++;
            }
            
            // Add divider
            View divider = new View(this);
            divider.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 2));
            divider.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
            LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 2);
            dividerParams.setMargins(0, 20, 0, 20);
            divider.setLayoutParams(dividerParams);
            layoutContent.addView(divider);
            
            // Summary statistics
            TextView summaryText = new TextView(this);
            String summaryMsg;
            if ("overdue".equals(currentFilter)) {
                summaryMsg = "🚨 TỔNG KẾT:\n• " + studentCount + " sinh viên đang nợ sách\n• Cần liên hệ để thu hồi sách";
            } else {
                int totalOverdue = overdueUserIds.size();
                summaryMsg = "📊 TỔNG KẾT:\n• " + studentCount + " sinh viên đã đăng ký\n• " + totalOverdue + " sinh viên đang nợ sách";
            }
            summaryText.setText(summaryMsg);
            summaryText.setTextSize(14);
            summaryText.setTypeface(null, android.graphics.Typeface.BOLD);
            summaryText.setTextColor(getResources().getColor(
                "overdue".equals(currentFilter) ? android.R.color.holo_red_dark : android.R.color.holo_green_dark));
            summaryText.setPadding(12, 12, 12, 12);
            summaryText.setBackground(getDrawable(android.R.drawable.dialog_holo_light_frame));
            summaryText.setLineSpacing(4, 1.2f);
            layoutContent.addView(summaryText);
            
        } catch (Exception e) {
            Log.e(TAG, "Error displaying users: ", e);
        }
    }
    
    private void addUserItem(User user) {
        try {
            LinearLayout itemLayout = new LinearLayout(this);
            itemLayout.setOrientation(LinearLayout.VERTICAL);
            itemLayout.setPadding(16, 16, 16, 16);
            
            // Check if user has overdue books
            boolean hasOverdue = overdueUserIds.contains(user.getId());
            if (hasOverdue) {
                itemLayout.setBackground(getDrawable(android.R.drawable.dialog_holo_light_frame));
                itemLayout.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
            } else {
                itemLayout.setBackground(getDrawable(android.R.drawable.dialog_holo_light_frame));
            }
            
            LinearLayout.LayoutParams itemParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            itemParams.setMargins(0, 0, 0, 12);
            itemLayout.setLayoutParams(itemParams);
            
            // User header info
            TextView tvUserInfo = new TextView(this);
            String userIcon = hasOverdue ? "⚠️" : "👤";
            tvUserInfo.setText(userIcon + " " + user.getName() + " (" + user.getStudentId() + ")");
            tvUserInfo.setTextSize(16);
            tvUserInfo.setTypeface(null, android.graphics.Typeface.BOLD);
            tvUserInfo.setTextColor(getResources().getColor(
                hasOverdue ? android.R.color.holo_red_dark : android.R.color.holo_blue_dark));
            
            // User details
            TextView tvUserDetails = new TextView(this);
            String phoneText = (user.getPhone() != null && !user.getPhone().isEmpty()) ? user.getPhone() : "Chưa có";
            String detailsText = "📧 Email: " + user.getEmail() + "\n" +
                                 "📞 SĐT: " + phoneText + "\n" +
                                 "📚 Giới hạn/tuần: " + user.getWeeklyLimit() + " cuốn";
            
            if (hasOverdue) {
                // Get overdue count and add to details
                executor.execute(() -> {
                    try {
                        int overdueCount = borrowingDAO.countOverdueBooksForUser(user.getId());
                        mainHandler.post(() -> {
                            String overdueInfo = "\n🚨 Nợ sách: " + overdueCount + " cuốn quá hạn";
                            tvUserDetails.setText(detailsText + overdueInfo);
                        });
                    } catch (Exception e) {
                        Log.e(TAG, "Error getting overdue count: ", e);
                    }
                });
            } else {
                tvUserDetails.setText(detailsText);
            }
            
            tvUserDetails.setTextSize(12);
            tvUserDetails.setPadding(0, 8, 0, 0);
            tvUserDetails.setLineSpacing(2, 1.1f);
            tvUserDetails.setTextColor(getResources().getColor(android.R.color.darker_gray));
            
            // Action buttons layout
            LinearLayout buttonLayout = new LinearLayout(this);
            buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
            buttonLayout.setPadding(0, 12, 0, 0);
            
            // View detail button
            TextView btnViewDetail = new TextView(this);
            btnViewDetail.setText("👁️ Xem chi tiết");
            btnViewDetail.setTextSize(14);
            btnViewDetail.setPadding(16, 8, 16, 8);
            btnViewDetail.setBackground(getDrawable(android.R.drawable.btn_default));
            btnViewDetail.setTextColor(getResources().getColor(android.R.color.white));
            btnViewDetail.setOnClickListener(v -> showUserDetail(user));
            
            LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            btnParams.setMargins(0, 0, 8, 0);
            btnViewDetail.setLayoutParams(btnParams);
            
            buttonLayout.addView(btnViewDetail);
            
            // Add overdue books button if user has overdue books
            if (hasOverdue) {
                TextView btnOverdueBooks = new TextView(this);
                btnOverdueBooks.setText("🚨 Sách quá hạn");
                btnOverdueBooks.setTextSize(14);
                btnOverdueBooks.setPadding(16, 8, 16, 8);
                btnOverdueBooks.setBackground(getDrawable(android.R.drawable.btn_default));
                btnOverdueBooks.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark));
                btnOverdueBooks.setTextColor(getResources().getColor(android.R.color.white));
                btnOverdueBooks.setOnClickListener(v -> showOverdueBooks(user));
                
                LinearLayout.LayoutParams overdueParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                );
                overdueParams.setMargins(0, 0, 8, 0);
                btnOverdueBooks.setLayoutParams(overdueParams);
                
                buttonLayout.addView(btnOverdueBooks);
                
                // Add "Đã thanh toán" button
                TextView btnMarkPaid = new TextView(this);
                btnMarkPaid.setText("💰 Đã thanh toán");
                btnMarkPaid.setTextSize(14);
                btnMarkPaid.setPadding(16, 8, 16, 8);
                btnMarkPaid.setBackground(getDrawable(android.R.drawable.btn_default));
                btnMarkPaid.setBackgroundColor(getResources().getColor(android.R.color.holo_green_dark));
                btnMarkPaid.setTextColor(getResources().getColor(android.R.color.white));
                btnMarkPaid.setOnClickListener(v -> showPaymentConfirmDialog(user));
                
                buttonLayout.addView(btnMarkPaid);
            }
            
            itemLayout.addView(tvUserInfo);
            itemLayout.addView(tvUserDetails);
            itemLayout.addView(buttonLayout);
            
            if (layoutContent != null) {
                layoutContent.addView(itemLayout);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error adding user item: ", e);
        }
    }
    
    private void showUserDetail(User user) {
        executor.execute(() -> {
            try {
                List<BorrowingRecord> userHistory = borrowingDAO.getAllUserBorrowingHistory(user.getId());
                List<BorrowingRecord> currentBorrowing = borrowingDAO.getBorrowedBooksByUser(user.getId());
                
                mainHandler.post(() -> {
                    try {
                        showUserDetailDialog(user, userHistory, currentBorrowing);
                    } catch (Exception e) {
                        Log.e(TAG, "Error showing user detail dialog: ", e);
                    }
                });
                
            } catch (Exception e) {
                Log.e(TAG, "Error loading user detail: ", e);
                mainHandler.post(() -> {
                    Toast.makeText(this, "Lỗi khi tải chi tiết user", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    
    private void showUserDetailDialog(User user, List<BorrowingRecord> userHistory, List<BorrowingRecord> currentBorrowing) {
        // Create ScrollView for dialog content
        ScrollView dialogScrollView = new ScrollView(this);
        LinearLayout dialogLayout = new LinearLayout(this);
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        dialogLayout.setPadding(24, 24, 24, 24);
        
        // Basic info section
        TextView titleInfo = new TextView(this);
        titleInfo.setText("👤 THÔNG TIN CÁ NHÂN");
        titleInfo.setTextSize(16);
        titleInfo.setTypeface(null, android.graphics.Typeface.BOLD);
        titleInfo.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
        titleInfo.setPadding(0, 0, 0, 16);
        
        TextView userInfo = new TextView(this);
        userInfo.setText(
            "📝 Tên: " + user.getName() + "\n" +
            "🆔 MSSV: " + user.getStudentId() + "\n" +
            "📧 Email: " + user.getEmail() + "\n" +
            "📞 SĐT: " + (user.getPhone() != null ? user.getPhone() : "Chưa có") + "\n" +
            "📚 Giới hạn/tuần: " + user.getWeeklyLimit() + " cuốn"
        );
        userInfo.setTextSize(14);
        userInfo.setLineSpacing(4, 1.2f);
        userInfo.setPadding(0, 0, 0, 20);
        
        // Statistics section
        TextView titleStats = new TextView(this);
        titleStats.setText("📊 THỐNG KÊ MƯỢN SÁCH");
        titleStats.setTextSize(16);
        titleStats.setTypeface(null, android.graphics.Typeface.BOLD);
        titleStats.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
        titleStats.setPadding(0, 0, 0, 12);
        
        TextView statsInfo = new TextView(this);
        statsInfo.setText(
            "📤 Đang mượn: " + currentBorrowing.size() + " cuốn\n" +
            "📋 Tổng lịch sử: " + userHistory.size() + " lượt mượn"
        );
        statsInfo.setTextSize(14);
        statsInfo.setLineSpacing(4, 1.2f);
        statsInfo.setPadding(0, 0, 0, 20);
        
        dialogLayout.addView(titleInfo);
        dialogLayout.addView(userInfo);
        dialogLayout.addView(titleStats);
        dialogLayout.addView(statsInfo);
        
        // Current borrowed books section
        if (!currentBorrowing.isEmpty()) {
            TextView titleBooks = new TextView(this);
            titleBooks.setText("📚 SÁCH ĐANG MƯỢN");
            titleBooks.setTextSize(16);
            titleBooks.setTypeface(null, android.graphics.Typeface.BOLD);
            titleBooks.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            titleBooks.setPadding(0, 0, 0, 12);
            
            dialogLayout.addView(titleBooks);
            
            for (BorrowingRecord record : currentBorrowing) {
                LinearLayout bookItem = new LinearLayout(this);
                bookItem.setOrientation(LinearLayout.VERTICAL);
                bookItem.setPadding(12, 8, 12, 8);
                bookItem.setBackground(getDrawable(android.R.drawable.dialog_holo_light_frame));
                
                TextView bookTitle = new TextView(this);
                bookTitle.setText("📖 " + record.getBookTitle());
                bookTitle.setTextSize(13);
                bookTitle.setTypeface(null, android.graphics.Typeface.BOLD);
                
                TextView bookDue = new TextView(this);
                bookDue.setText("📅 Hạn trả: " + record.getDueDate());
                bookDue.setTextSize(12);
                bookDue.setTextColor(getResources().getColor(android.R.color.darker_gray));
                
                bookItem.addView(bookTitle);
                bookItem.addView(bookDue);
                
                LinearLayout.LayoutParams bookParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                );
                bookParams.setMargins(0, 0, 0, 8);
                bookItem.setLayoutParams(bookParams);
                
                dialogLayout.addView(bookItem);
            }
        }
        
        dialogScrollView.addView(dialogLayout);
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("👤 Chi tiết: " + user.getName());
        builder.setView(dialogScrollView);
        builder.setPositiveButton("Đóng", (dialog, which) -> dialog.dismiss());
        
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    
    private void showOverdueBooks(User user) {
        executor.execute(() -> {
            try {
                List<BorrowingRecord> overdueBooks = borrowingDAO.getOverdueBooksForUser(user.getId());
                
                mainHandler.post(() -> {
                    try {
                        showOverdueBooksDialog(user, overdueBooks);
                    } catch (Exception e) {
                        Log.e(TAG, "Error showing overdue books dialog: ", e);
                    }
                });
                
            } catch (Exception e) {
                Log.e(TAG, "Error loading overdue books: ", e);
                mainHandler.post(() -> {
                    Toast.makeText(this, "Lỗi khi tải danh sách sách quá hạn", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    
    private void showOverdueBooksDialog(User user, List<BorrowingRecord> overdueBooks) {
        // Create ScrollView for dialog content
        ScrollView dialogScrollView = new ScrollView(this);
        LinearLayout dialogLayout = new LinearLayout(this);
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        dialogLayout.setPadding(24, 24, 24, 24);
        
        // Header info
        TextView titleInfo = new TextView(this);
        titleInfo.setText("🚨 SÁCH QUÁ HẠN TRẢ");
        titleInfo.setTextSize(18);
        titleInfo.setTypeface(null, android.graphics.Typeface.BOLD);
        titleInfo.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        titleInfo.setPadding(0, 0, 0, 16);
        
        TextView userInfo = new TextView(this);
        userInfo.setText(
            "👤 Sinh viên: " + user.getName() + "\n" +
            "🆔 MSSV: " + user.getStudentId() + "\n" +
            "📧 Email: " + user.getEmail() + "\n" +
            "🚨 Tổng sách quá hạn: " + overdueBooks.size() + " cuốn"
        );
        userInfo.setTextSize(14);
        userInfo.setLineSpacing(4, 1.2f);
        userInfo.setPadding(0, 0, 0, 20);
        
        dialogLayout.addView(titleInfo);
        dialogLayout.addView(userInfo);
        
        // Overdue books list
        if (!overdueBooks.isEmpty()) {
            TextView booksTitle = new TextView(this);
            booksTitle.setText("📚 CHI TIẾT SÁCH QUÁ HẠN");
            booksTitle.setTextSize(16);
            booksTitle.setTypeface(null, android.graphics.Typeface.BOLD);
            booksTitle.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            booksTitle.setPadding(0, 0, 0, 12);
            dialogLayout.addView(booksTitle);
            
            for (int i = 0; i < overdueBooks.size(); i++) {
                BorrowingRecord record = overdueBooks.get(i);
                
                LinearLayout bookItem = new LinearLayout(this);
                bookItem.setOrientation(LinearLayout.VERTICAL);
                bookItem.setPadding(12, 12, 12, 12);
                bookItem.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
                
                TextView bookTitle = new TextView(this);
                bookTitle.setText((i + 1) + ". 📖 " + record.getBookTitle());
                bookTitle.setTextSize(14);
                bookTitle.setTypeface(null, android.graphics.Typeface.BOLD);
                bookTitle.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                
                TextView bookDetails = new TextView(this);
                // Calculate overdue days
                String overdueText = calculateOverdueDays(record.getDueDate());
                bookDetails.setText(
                    "✍️ Tác giả: " + record.getBookAuthor() + "\n" +
                    "📅 Hạn trả: " + record.getDueDate() + "\n" +
                    "⏰ " + overdueText
                );
                bookDetails.setTextSize(12);
                bookDetails.setTextColor(getResources().getColor(android.R.color.darker_gray));
                bookDetails.setLineSpacing(2, 1.1f);
                
                bookItem.addView(bookTitle);
                bookItem.addView(bookDetails);
                
                LinearLayout.LayoutParams bookParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                );
                bookParams.setMargins(0, 0, 0, 12);
                bookItem.setLayoutParams(bookParams);
                
                dialogLayout.addView(bookItem);
            }
            
            // Action note
            TextView actionNote = new TextView(this);
            actionNote.setText(
                "📞 HÀNH ĐỘNG CẦN THỰC HIỆN:\n" +
                "• Liên hệ sinh viên để thu hồi sách\n" +
                "• Áp dụng phạt tiền nếu cần\n" +
                "• Tạm khóa quyền mượn sách nếu quá lâu"
            );
            actionNote.setTextSize(13);
            actionNote.setTypeface(null, android.graphics.Typeface.BOLD);
            actionNote.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
            actionNote.setPadding(12, 16, 12, 12);
            actionNote.setBackground(getDrawable(android.R.drawable.dialog_holo_light_frame));
            actionNote.setLineSpacing(4, 1.2f);
            dialogLayout.addView(actionNote);
        }
        
        dialogScrollView.addView(dialogLayout);
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("🚨 " + user.getName() + " - Sách quá hạn");
        builder.setView(dialogScrollView);
        builder.setPositiveButton("Đóng", (dialog, which) -> dialog.dismiss());
        builder.setNeutralButton("📞 Liên hệ", (dialog, which) -> {
            // Open phone dialer if phone number exists
            if (user.getPhone() != null && !user.getPhone().isEmpty()) {
                try {
                    android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_DIAL);
                    intent.setData(android.net.Uri.parse("tel:" + user.getPhone()));
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(this, "Không thể mở ứng dụng gọi điện", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Sinh viên chưa có số điện thoại", Toast.LENGTH_SHORT).show();
            }
        });
        
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    
    private String calculateOverdueDays(String dueDate) {
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
            java.util.Date due = sdf.parse(dueDate);
            java.util.Date now = new java.util.Date();
            
            long diffInMillis = now.getTime() - due.getTime();
            long diffInDays = diffInMillis / (24 * 60 * 60 * 1000);
            
            if (diffInDays > 0) {
                return "Quá hạn " + diffInDays + " ngày";
            } else {
                return "Hết hạn hôm nay";
            }
        } catch (Exception e) {
            return "Quá hạn";
        }
    }
    
    private void showPaymentConfirmDialog(User user) {
        try {
            // First get the overdue count
            executor.execute(() -> {
                try {
                    int overdueCount = borrowingDAO.countOverdueBooksForUser(user.getId());
                    List<BorrowingRecord> overdueBooks = borrowingDAO.getOverdueBooksForUser(user.getId());
                    
                    mainHandler.post(() -> {
                        try {
                            showConfirmPaymentDialog(user, overdueCount, overdueBooks);
                        } catch (Exception e) {
                            Log.e(TAG, "Error showing confirm dialog: ", e);
                        }
                    });
                    
                } catch (Exception e) {
                    Log.e(TAG, "Error loading overdue count: ", e);
                    mainHandler.post(() -> {
                        Toast.makeText(this, "Lỗi khi tải dữ liệu nợ sách", Toast.LENGTH_SHORT).show();
                    });
                }
            });
            
        } catch (Exception e) {
            Log.e(TAG, "Error in showPaymentConfirmDialog: ", e);
        }
    }
    
    private void showConfirmPaymentDialog(User user, int overdueCount, List<BorrowingRecord> overdueBooks) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("💰 Xác nhận thanh toán");
            
            // Create message with book details
            StringBuilder message = new StringBuilder();
            message.append("👤 Sinh viên: ").append(user.getName()).append("\n");
            message.append("🆔 MSSV: ").append(user.getStudentId()).append("\n");
            message.append("🚨 Tổng sách nợ: ").append(overdueCount).append(" cuốn\n\n");
            
            message.append("📚 CHI TIẾT SÁCH NỢ:\n");
            for (int i = 0; i < overdueBooks.size() && i < 5; i++) { // Show max 5 books
                BorrowingRecord book = overdueBooks.get(i);
                String overdueText = calculateOverdueDays(book.getDueDate());
                message.append("\u2022 ").append(book.getBookTitle()).append(" (").append(overdueText).append(")\n");
            }
            
            if (overdueBooks.size() > 5) {
                message.append("... và ").append(overdueBooks.size() - 5).append(" sách khác\n");
            }
            
            message.append("\n⚠️ HÀNH ĐỘNG:\n");
            message.append("• Tất cả sách nợ sẽ được đánh dấu là 'đã trả'\n");
            message.append("• User sẽ không còn trong danh sách nợ sách\n");
            message.append("• Thao tác này KHÔNG THỂ HOÀN TÁC!\n\n");
            message.append("💰 Bạn có chắc chắn sinh viên đã thanh toán?");
            
            builder.setMessage(message.toString());
            
            builder.setPositiveButton("💰 Xác nhận thanh toán", (dialog, which) -> {
                processPayment(user, overdueCount);
            });
            
            builder.setNegativeButton("❌ Hủy", (dialog, which) -> dialog.dismiss());
            
            builder.setNeutralButton("👁️ Xem chi tiết", (dialog, which) -> {
                showOverdueBooks(user);
            });
            
            AlertDialog dialog = builder.create();
            dialog.show();
            
            // Style the buttons
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            
        } catch (Exception e) {
            Log.e(TAG, "Error showing confirm payment dialog: ", e);
        }
    }
    
    private void processPayment(User user, int overdueCount) {
        try {
            // Show progress
            android.app.ProgressDialog progressDialog = new android.app.ProgressDialog(this);
            progressDialog.setTitle("💰 Đang xử lý thanh toán...");
            progressDialog.setMessage("Vui lòng chờ...");
            progressDialog.setCancelable(false);
            progressDialog.show();
            
            executor.execute(() -> {
                try {
                    // Mark all overdue books as returned_paid
                    boolean success = borrowingDAO.markOverdueBooksAsReturned(user.getId());
                    
                    mainHandler.post(() -> {
                        try {
                            progressDialog.dismiss();
                            
                            if (success) {
                                showPaymentSuccessDialog(user, overdueCount);
                                // Refresh the user list to remove user from overdue list
                                loadUsersData();
                            } else {
                                Toast.makeText(this, "❌ Lỗi khi xử lý thanh toán", Toast.LENGTH_LONG).show();
                            }
                        } catch (Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(this, "Lỗi khi hiển thị kết quả", Toast.LENGTH_SHORT).show();
                        }
                    });
                    
                } catch (Exception e) {
                    Log.e(TAG, "Error processing payment: ", e);
                    mainHandler.post(() -> {
                        progressDialog.dismiss();
                        Toast.makeText(this, "❌ Lỗi khi xử lý thanh toán", Toast.LENGTH_LONG).show();
                    });
                }
            });
            
        } catch (Exception e) {
            Log.e(TAG, "Error in processPayment: ", e);
        }
    }
    
    private void showPaymentSuccessDialog(User user, int processedCount) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("✅ Thanh toán thành công");
            
            String message = "🎉 Đã xử lý thành công!\n\n" +
                           "👤 Sinh viên: " + user.getName() + "\n" +
                           "🆔 MSSV: " + user.getStudentId() + "\n" +
                           "📚 Sách đã xử lý: " + processedCount + " cuốn\n\n" +
                           "✅ Tất cả sách nợ đã được đánh dấu 'đã trả'\n" +
                           "✅ User không còn trong danh sách nợ sách\n" +
                           "💼 Quá trình thanh toán hoàn tất!";
            
            builder.setMessage(message);
            builder.setPositiveButton("👍 OK", (dialog, which) -> dialog.dismiss());
            
            AlertDialog dialog = builder.create();
            dialog.show();
            
        } catch (Exception e) {
            Log.e(TAG, "Error showing success dialog: ", e);
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (executor != null && !executor.isShutdown()) {
                executor.shutdown();
            }
        } catch (Exception e) {
            // Ignore cleanup errors
        }
    }
}
