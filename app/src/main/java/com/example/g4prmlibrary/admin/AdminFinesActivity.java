package com.example.g4prmlibrary.admin;

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

import com.example.g4prmlibrary.database.FineDAO;
import com.example.g4prmlibrary.R;
import com.example.g4prmlibrary.models.Fine;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AdminFinesActivity extends AppCompatActivity {

    private static final String TAG = "AdminFinesActivity";
    private ImageView ivBack;
    private TextView tvTitle, tvTotalAmount, tvTotalCount, tvEmptyMessage;
    private LinearLayout layoutContent, layoutHeader;
    private ProgressBar progressBar;

    private FineDAO fineDAO;
    private ExecutorService executor;
    private Handler mainHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            createSimpleUI();
            setupDatabase();
            loadFinesData();

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: ", e);
            Toast.makeText(this, "Lỗi khi tải trang quản lý phạt", Toast.LENGTH_LONG).show();
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
        layoutHeader = new LinearLayout(this);
        layoutHeader.setOrientation(LinearLayout.HORIZONTAL);
        layoutHeader.setGravity(android.view.Gravity.CENTER_VERTICAL);

        ivBack = new ImageView(this);
        ivBack.setImageResource(android.R.drawable.ic_menu_revert);
        ivBack.setPadding(16, 16, 16, 16);
        ivBack.setOnClickListener(v -> finish());

        tvTitle = new TextView(this);
        tvTitle.setText("💰 Quản lý phạt trễ hạn");
        tvTitle.setTextSize(18);
        tvTitle.setPadding(16, 0, 0, 0);

        layoutHeader.addView(ivBack);
        layoutHeader.addView(tvTitle);

        // Summary info
        LinearLayout summaryLayout = new LinearLayout(this);
        summaryLayout.setOrientation(LinearLayout.VERTICAL);
        summaryLayout.setPadding(16, 16, 16, 16);
        summaryLayout.setBackground(getDrawable(android.R.drawable.dialog_holo_light_frame));

        tvTotalCount = new TextView(this);
        tvTotalCount.setText("📊 Tổng số phạt: 0");
        tvTotalCount.setTextSize(14);

        tvTotalAmount = new TextView(this);
        tvTotalAmount.setText("💸 Tổng tiền: 0 VNĐ");
        tvTotalAmount.setTextSize(14);
        tvTotalAmount.setPadding(0, 8, 0, 0);

        summaryLayout.addView(tvTotalCount);
        summaryLayout.addView(tvTotalAmount);

        // Progress bar
        progressBar = new ProgressBar(this);
        progressBar.setVisibility(View.VISIBLE);

        // Content
        layoutContent = new LinearLayout(this);
        layoutContent.setOrientation(LinearLayout.VERTICAL);

        tvEmptyMessage = new TextView(this);
        tvEmptyMessage.setText("🎉 Không có khoản phạt nào chưa thanh toán");
        tvEmptyMessage.setTextSize(16);
        tvEmptyMessage.setGravity(android.view.Gravity.CENTER);
        tvEmptyMessage.setPadding(32, 32, 32, 32);
        tvEmptyMessage.setVisibility(View.GONE);

        // Add to main layout
        mainLayout.addView(layoutHeader);
        mainLayout.addView(summaryLayout);
        mainLayout.addView(progressBar);
        mainLayout.addView(tvEmptyMessage);
        mainLayout.addView(layoutContent);

        setContentView(mainLayout);
    }

    private void setupDatabase() {
        try {
            fineDAO = new FineDAO(this);
        } catch (Exception e) {
            Log.e(TAG, "Error setting up database: ", e);
        }
    }

    private void loadFinesData() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }

        executor.execute(() -> {
            try {
                List<Fine> fines = fineDAO.getUnpaidFines();

                mainHandler.post(() -> {
                    try {
                        displayFines(fines);
                    } catch (Exception e) {
                        Log.e(TAG, "Error displaying fines: ", e);
                        hideProgressBar();
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "Error loading fines: ", e);
                mainHandler.post(() -> {
                    hideProgressBar();
                    Toast.makeText(AdminFinesActivity.this, "Lỗi khi tải dữ liệu phạt", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void displayFines(List<Fine> fines) {
        try {
            hideProgressBar();

            if (layoutContent != null) {
                layoutContent.removeAllViews();
            }

            if (fines.isEmpty()) {
                showEmptyState();
            } else {
                showFinesList(fines);
            }

            updateSummary(fines);

        } catch (Exception e) {
            Log.e(TAG, "Error displaying fines: ", e);
        }
    }

    private void hideProgressBar() {
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
    }

    private void showEmptyState() {
        if (tvEmptyMessage != null) {
            tvEmptyMessage.setVisibility(View.VISIBLE);
        }
    }

    private void showFinesList(List<Fine> fines) {
        if (tvEmptyMessage != null) {
            tvEmptyMessage.setVisibility(View.GONE);
        }

        for (Fine fine : fines) {
            addFineItem(fine);
        }
    }

    private void addFineItem(Fine fine) {
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

            // Student info
            TextView tvStudentInfo = new TextView(this);
            tvStudentInfo.setText("👤 " + fine.getUserName() + " (" + fine.getUserStudentId() + ")");
            tvStudentInfo.setTextSize(16);
            tvStudentInfo.setTypeface(null, android.graphics.Typeface.BOLD);

            // Book info
            TextView tvBookInfo = new TextView(this);
            tvBookInfo.setText("📖 " + fine.getBookTitle());
            tvBookInfo.setTextSize(14);
            tvBookInfo.setPadding(0, 4, 0, 0);

            // Fine details
            TextView tvFineInfo = new TextView(this);
            StringBuilder info = new StringBuilder();
            info.append("💰 Số tiền: ").append(fine.getFormattedAmount()).append("\n");
            info.append("📅 Hạn trả: ").append(fine.getDueDate()).append("\n");
            info.append("⚠️ Lý do: ").append(fine.getReason()).append("\n");
            info.append("📅 Ngày tạo: ").append(fine.getCreatedDate());

            tvFineInfo.setText(info.toString());
            tvFineInfo.setTextSize(12);
            tvFineInfo.setPadding(0, 4, 0, 0);

            // Action button
            LinearLayout buttonLayout = new LinearLayout(this);
            buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
            buttonLayout.setPadding(0, 8, 0, 0);

            TextView btnMarkPaid = new TextView(this);
            btnMarkPaid.setText("✅ Sinh viên đã thanh toán");
            btnMarkPaid.setTextSize(14);
            btnMarkPaid.setPadding(16, 8, 16, 8);
            btnMarkPaid.setBackground(getDrawable(android.R.drawable.btn_default));
            btnMarkPaid.setTextColor(getResources().getColor(android.R.color.white));
            btnMarkPaid.setOnClickListener(v -> showPaymentConfirmDialog(fine));

            TextView btnDelete = new TextView(this);
            btnDelete.setText("🗑️ Xóa phạt");
            btnDelete.setTextSize(14);
            btnDelete.setPadding(16, 8, 16, 8);
            btnDelete.setBackground(getDrawable(android.R.drawable.btn_default));
            btnDelete.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            btnDelete.setOnClickListener(v -> showDeleteConfirmDialog(fine));

            LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            btnParams.setMargins(0, 0, 16, 0);
            btnMarkPaid.setLayoutParams(btnParams);

            buttonLayout.addView(btnMarkPaid);
            buttonLayout.addView(btnDelete);

            itemLayout.addView(tvStudentInfo);
            itemLayout.addView(tvBookInfo);
            itemLayout.addView(tvFineInfo);
            itemLayout.addView(buttonLayout);

            if (layoutContent != null) {
                layoutContent.addView(itemLayout);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error adding fine item: ", e);
        }
    }

    private void showPaymentConfirmDialog(Fine fine) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Xác nhận thanh toán");

            String message = "Xác nhận sinh viên " + fine.getUserName() +
                    " đã thanh toán phạt " + fine.getFormattedAmount() + "?\n\n" +
                    "Hành động này không thể hoàn tác.";

            builder.setMessage(message);

            builder.setPositiveButton("Xác nhận", (dialog, which) -> {
                markFineAsPaid(fine);
            });

            builder.setNegativeButton("Hủy", (dialog, which) -> {
                dialog.dismiss();
            });

            AlertDialog dialog = builder.create();
            dialog.show();

        } catch (Exception e) {
            Log.e(TAG, "Error showing payment confirm dialog: ", e);
        }
    }

    private void showDeleteConfirmDialog(Fine fine) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Xác nhận xóa phạt");

            String message = "Bạn có chắc chắn muốn xóa khoản phạt " + fine.getFormattedAmount() +
                    " của sinh viên " + fine.getUserName() + "?\n\n" +
                    "Hành động này không thể hoàn tác.";

            builder.setMessage(message);

            builder.setPositiveButton("Xóa", (dialog, which) -> {
                deleteFine(fine);
            });

            builder.setNegativeButton("Hủy", (dialog, which) -> {
                dialog.dismiss();
            });

            AlertDialog dialog = builder.create();
            dialog.show();

        } catch (Exception e) {
            Log.e(TAG, "Error showing delete confirm dialog: ", e);
        }
    }

    private void markFineAsPaid(Fine fine) {
        executor.execute(() -> {
            try {
                boolean success = fineDAO.markFineAsPaid(fine.getId());

                mainHandler.post(() -> {
                    if (success) {
                        Toast.makeText(this, "Đã đánh dấu thanh toán thành công", Toast.LENGTH_SHORT).show();
                        loadFinesData(); // Reload data
                    } else {
                        Toast.makeText(this, "Lỗi khi cập nhật trạng thái thanh toán", Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "Error marking fine as paid: ", e);
                mainHandler.post(() -> {
                    Toast.makeText(this, "Lỗi khi cập nhật phạt", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void deleteFine(Fine fine) {
        executor.execute(() -> {
            try {
                boolean success = fineDAO.deleteFine(fine.getId());

                mainHandler.post(() -> {
                    if (success) {
                        Toast.makeText(this, "Đã xóa phạt thành công", Toast.LENGTH_SHORT).show();
                        loadFinesData(); // Reload data
                    } else {
                        Toast.makeText(this, "Lỗi khi xóa phạt", Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "Error deleting fine: ", e);
                mainHandler.post(() -> {
                    Toast.makeText(this, "Lỗi khi xóa phạt", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void updateSummary(List<Fine> fines) {
        try {
            int totalCount = fines.size();
            double totalAmount = 0;

            for (Fine fine : fines) {
                totalAmount += fine.getAmount();
            }

            if (tvTotalCount != null) {
                tvTotalCount.setText("📊 Tổng số phạt: " + totalCount);
            }

            if (tvTotalAmount != null) {
                NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
                String formattedAmount = formatter.format(totalAmount);
                tvTotalAmount.setText("💸 Tổng tiền: " + formattedAmount + " VNĐ");
            }

        } catch (Exception e) {
            Log.e(TAG, "Error updating summary: ", e);
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
            // Ignore
        }
    }
}
