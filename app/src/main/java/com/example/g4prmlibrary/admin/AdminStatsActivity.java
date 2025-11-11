package com.example.g4prmlibrary.admin;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.g4prmlibrary.database.BorrowingDAO;
import com.example.g4prmlibrary.database.BookDAO;
import com.example.g4prmlibrary.database.UserDAO;
import com.example.g4prmlibrary.models.User;
import com.example.g4prmlibrary.models.BorrowingRecord;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AdminStatsActivity extends AppCompatActivity {

    private static final String TAG = "AdminStatsActivity";
    private ImageView ivBack;
    private TextView tvTitle;
    private LinearLayout layoutContent;
    private ProgressBar progressBar;

    private BorrowingDAO borrowingDAO;
    private BookDAO bookDAO;
    private UserDAO userDAO;
    private ExecutorService executor;
    private Handler mainHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            createSimpleUI();
            setupDatabase();
            loadStatsData();

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: ", e);
            Toast.makeText(this, "Lỗi khi tải trang thống kê", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void createSimpleUI() {
        executor = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());

        // Create main layout
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(16, 16, 16, 16);

        // Header
        LinearLayout layoutHeader = new LinearLayout(this);
        layoutHeader.setOrientation(LinearLayout.HORIZONTAL);
        layoutHeader.setGravity(android.view.Gravity.CENTER_VERTICAL);

        ivBack = new ImageView(this);
        ivBack.setImageResource(android.R.drawable.ic_menu_revert);
        ivBack.setPadding(16, 16, 16, 16);
        ivBack.setOnClickListener(v -> finish());

        tvTitle = new TextView(this);
        tvTitle.setText("📊 Thống kê & Quản lý User");
        tvTitle.setTextSize(18);
        tvTitle.setPadding(16, 0, 0, 0);

        layoutHeader.addView(ivBack);
        layoutHeader.addView(tvTitle);

        // Progress bar
        progressBar = new ProgressBar(this);
        progressBar.setVisibility(View.VISIBLE);

        // Content
        layoutContent = new LinearLayout(this);
        layoutContent.setOrientation(LinearLayout.VERTICAL);

        // Add to main layout
        mainLayout.addView(layoutHeader);
        mainLayout.addView(progressBar);
        mainLayout.addView(layoutContent);

        setContentView(mainLayout);
    }

    private void setupDatabase() {
        try {
            borrowingDAO = new BorrowingDAO(this);
            bookDAO = new BookDAO(this);
            userDAO = new UserDAO(this);
        } catch (Exception e) {
            Log.e(TAG, "Error setting up database: ", e);
        }
    }

    private void loadStatsData() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }

        executor.execute(() -> {
            try {
                // Get statistics
                List<BorrowingRecord> allBorrowings = borrowingDAO.getAllBorrowingRecords();
                List<BorrowingRecord> currentBorrowings = borrowingDAO.getAllBorrowingRecordsByStatus("borrowed");
                List<BorrowingRecord> returnedBooks = borrowingDAO.getAllBorrowingRecordsByStatus("returned");
                List<User> allUsers = userDAO.getAllUsers();

                int totalBooks = bookDAO.getTotalBooksCount();
                int availableBooks = bookDAO.getAvailableBooksCount();

                mainHandler.post(() -> {
                    try {
                        displayStats(allBorrowings.size(), currentBorrowings.size(),
                                returnedBooks.size(), allUsers.size(), totalBooks, availableBooks);
                        displayUserManagement(allUsers);
                    } catch (Exception e) {
                        Log.e(TAG, "Error displaying stats: ", e);
                        hideProgressBar();
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "Error loading stats: ", e);
                mainHandler.post(() -> {
                    hideProgressBar();
                    Toast.makeText(AdminStatsActivity.this, "Lỗi khi tải dữ liệu thống kê", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void hideProgressBar() {
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
    }

    private void displayStats(int totalBorrowings, int currentBorrowings, int returnedBooks,
                              int totalUsers, int totalBooks, int availableBooks) {
        try {
            hideProgressBar();

            if (layoutContent != null) {
                layoutContent.removeAllViews();
            }

            // Stats section
            TextView statsTitle = new TextView(this);
            statsTitle.setText("📈 THỐNG KÊ TỔNG QUAN");
            statsTitle.setTextSize(16);
            statsTitle.setTypeface(null, android.graphics.Typeface.BOLD);
            statsTitle.setPadding(0, 0, 0, 16);
            layoutContent.addView(statsTitle);

            // Library stats
            LinearLayout libraryStatsLayout = createStatsCard("📚 Thư viện",
                    "📖 Tổng sách: " + totalBooks + "\n" +
                            "✅ Sách có sẵn: " + availableBooks + "\n" +
                            "📤 Đang được mượn: " + (totalBooks - availableBooks));
            layoutContent.addView(libraryStatsLayout);

            // Borrowing stats
            LinearLayout borrowingStatsLayout = createStatsCard("📋 Mượn/Trả sách",
                    "📊 Tổng lượt mượn: " + totalBorrowings + "\n" +
                            "📤 Đang mượn: " + currentBorrowings + "\n" +
                            "📥 Đã trả: " + returnedBooks);
            layoutContent.addView(borrowingStatsLayout);

            // User stats
            LinearLayout userStatsLayout = createStatsCard("👥 Người dùng",
                    "👤 Tổng user: " + totalUsers + "\n" +
                            "📚 Trung bình mượn/user: " + (totalUsers > 0 ? String.format("%.1f", (double)totalBorrowings/totalUsers) : "0"));
            layoutContent.addView(userStatsLayout);

        } catch (Exception e) {
            Log.e(TAG, "Error displaying stats: ", e);
        }
    }

    private LinearLayout createStatsCard(String title, String content) {
        LinearLayout cardLayout = new LinearLayout(this);
        cardLayout.setOrientation(LinearLayout.VERTICAL);
        cardLayout.setPadding(16, 16, 16, 16);
        cardLayout.setBackground(getDrawable(android.R.drawable.dialog_holo_light_frame));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 16);
        cardLayout.setLayoutParams(params);

        TextView titleView = new TextView(this);
        titleView.setText(title);
        titleView.setTextSize(14);
        titleView.setTypeface(null, android.graphics.Typeface.BOLD);

        TextView contentView = new TextView(this);
        contentView.setText(content);
        contentView.setTextSize(12);
        contentView.setPadding(0, 8, 0, 0);

        cardLayout.addView(titleView);
        cardLayout.addView(contentView);

        return cardLayout;
    }

    private void displayUserManagement(List<User> users) {
        try {
            // User management section
            TextView userTitle = new TextView(this);
            userTitle.setText("👥 QUẢN LÝ NGƯỜI DÙNG");
            userTitle.setTextSize(16);
            userTitle.setTypeface(null, android.graphics.Typeface.BOLD);
            userTitle.setPadding(0, 24, 0, 16);
            layoutContent.addView(userTitle);

            // Filter out admin users
            for (User user : users) {
                if (!"admin".equals(user.getRole())) {
                    addUserItem(user);
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "Error displaying user management: ", e);
        }
    }

    private void addUserItem(User user) {
        try {
            LinearLayout itemLayout = new LinearLayout(this);
            itemLayout.setOrientation(LinearLayout.VERTICAL);
            itemLayout.setPadding(16, 12, 16, 12);
            itemLayout.setBackground(getDrawable(android.R.drawable.dialog_holo_light_frame));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, 0, 8);
            itemLayout.setLayoutParams(params);

            // User info
            TextView tvUserInfo = new TextView(this);
            tvUserInfo.setText("👤 " + user.getName() + " (" + user.getStudentId() + ")");
            tvUserInfo.setTextSize(16);
            tvUserInfo.setTypeface(null, android.graphics.Typeface.BOLD);

            TextView tvUserDetails = new TextView(this);
            tvUserDetails.setText("📧 " + user.getEmail() + "\n" +
                    "📞 " + (user.getPhone() != null ? user.getPhone() : "Chưa có") + "\n" +
                    "📚 Giới hạn/tuần: " + user.getWeeklyLimit());
            tvUserDetails.setTextSize(12);
            tvUserDetails.setPadding(0, 4, 0, 0);

            // Action buttons
            LinearLayout buttonLayout = new LinearLayout(this);
            buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
            buttonLayout.setPadding(0, 8, 0, 0);

            TextView btnViewBorrowings = new TextView(this);
            btnViewBorrowings.setText("📋 Xem lịch sử");
            btnViewBorrowings.setTextSize(12);
            btnViewBorrowings.setPadding(12, 6, 12, 6);
            btnViewBorrowings.setBackground(getDrawable(android.R.drawable.btn_default));
            btnViewBorrowings.setTextColor(getResources().getColor(android.R.color.white));
            btnViewBorrowings.setOnClickListener(v -> showUserBorrowingHistory(user));

            TextView btnResetPassword = new TextView(this);
            btnResetPassword.setText("🔑 Reset mật khẩu");
            btnResetPassword.setTextSize(12);
            btnResetPassword.setPadding(12, 6, 12, 6);
            btnResetPassword.setBackground(getDrawable(android.R.drawable.btn_default));
            btnResetPassword.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
            btnResetPassword.setOnClickListener(v -> showResetPasswordDialog(user));

            TextView btnDeleteUser = new TextView(this);
            btnDeleteUser.setText("🗑️ Xóa user");
            btnDeleteUser.setTextSize(12);
            btnDeleteUser.setPadding(12, 6, 12, 6);
            btnDeleteUser.setBackground(getDrawable(android.R.drawable.btn_default));
            btnDeleteUser.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            btnDeleteUser.setOnClickListener(v -> showDeleteUserDialog(user));

            LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            btnParams.setMargins(0, 0, 8, 0);
            btnViewBorrowings.setLayoutParams(btnParams);
            btnResetPassword.setLayoutParams(btnParams);

            buttonLayout.addView(btnViewBorrowings);
            buttonLayout.addView(btnResetPassword);
            buttonLayout.addView(btnDeleteUser);

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

    private void showUserBorrowingHistory(User user) {
        executor.execute(() -> {
            try {
                List<BorrowingRecord> userHistory = borrowingDAO.getAllUserBorrowingHistory(user.getId());

                mainHandler.post(() -> {
                    try {
                        StringBuilder historyText = new StringBuilder();
                        historyText.append("📚 Lịch sử mượn sách của ").append(user.getName()).append(":\n\n");

                        if (userHistory.isEmpty()) {
                            historyText.append("Chưa có lịch sử mượn sách nào.");
                        } else {
                            for (BorrowingRecord record : userHistory) {
                                historyText.append("📖 ").append(record.getBookTitle()).append("\n");
                                historyText.append("📅 Mượn: ").append(record.getBorrowDate()).append("\n");
                                historyText.append("📅 Hạn: ").append(record.getDueDate()).append("\n");
                                historyText.append("📊 Trạng thái: ").append(record.getStatus()).append("\n");
                                if (record.getReturnDate() != null) {
                                    historyText.append("📅 Trả: ").append(record.getReturnDate()).append("\n");
                                }
                                historyText.append("\n");
                            }
                        }

                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("Lịch sử mượn sách");
                        builder.setMessage(historyText.toString());
                        builder.setPositiveButton("Đóng", (dialog, which) -> dialog.dismiss());
                        builder.show();

                    } catch (Exception e) {
                        Log.e(TAG, "Error showing user history: ", e);
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "Error loading user history: ", e);
                mainHandler.post(() -> {
                    Toast.makeText(this, "Lỗi khi tải lịch sử người dùng", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void showResetPasswordDialog(User user) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Reset mật khẩu");
            builder.setMessage("Bạn có muốn reset mật khẩu của " + user.getName() +
                    " về mặc định (123456)?");

            builder.setPositiveButton("Reset", (dialog, which) -> {
                resetUserPassword(user);
            });

            builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
            builder.show();

        } catch (Exception e) {
            Log.e(TAG, "Error showing reset password dialog: ", e);
        }
    }

    private void showDeleteUserDialog(User user) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Xóa người dùng");
            builder.setMessage("Bạn có chắc chắn muốn xóa người dùng " + user.getName() +
                    "?\n\nHành động này không thể hoàn tác.");

            builder.setPositiveButton("Xóa", (dialog, which) -> {
                deleteUser(user);
            });

            builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
            builder.show();

        } catch (Exception e) {
            Log.e(TAG, "Error showing delete user dialog: ", e);
        }
    }

    private void resetUserPassword(User user) {
        executor.execute(() -> {
            try {
                boolean success = userDAO.resetUserPassword(user.getId(), "123456");

                mainHandler.post(() -> {
                    if (success) {
                        Toast.makeText(this, "Đã reset mật khẩu thành công", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Lỗi khi reset mật khẩu", Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "Error resetting password: ", e);
                mainHandler.post(() -> {
                    Toast.makeText(this, "Lỗi khi reset mật khẩu", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void deleteUser(User user) {
        executor.execute(() -> {
            try {
                // Check if user has active borrowings
                List<BorrowingRecord> activeBorrowings = borrowingDAO.getBorrowedBooksByUser(user.getId());

                if (!activeBorrowings.isEmpty()) {
                    mainHandler.post(() -> {
                        Toast.makeText(this, "Không thể xóa user có sách đang mượn", Toast.LENGTH_LONG).show();
                    });
                    return;
                }

                boolean success = userDAO.deleteUser(user.getId());

                mainHandler.post(() -> {
                    if (success) {
                        Toast.makeText(this, "Đã xóa người dùng thành công", Toast.LENGTH_SHORT).show();
                        loadStatsData(); // Reload data
                    } else {
                        Toast.makeText(this, "Lỗi khi xóa người dùng", Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "Error deleting user: ", e);
                mainHandler.post(() -> {
                    Toast.makeText(this, "Lỗi khi xóa người dùng", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (executor != null && !executor.isShutdown()) {
                executor.shutdown();
            }
        } catch (Exception e) {
            // Ignore
        }
    }
}

