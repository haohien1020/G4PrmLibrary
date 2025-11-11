package com.example.g4prmlibrary.admin;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.g4prmlibrary.database.BorrowingDAO;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AdminCleanupActivity extends AppCompatActivity {

    private static final String TAG = "AdminCleanupActivity";
    private BorrowingDAO borrowingDAO;
    private ExecutorService executor;
    private Handler mainHandler;
    private TextView tvResults;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        executor = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
        borrowingDAO = new BorrowingDAO(this);

        createUI();
    }

    private void createUI() {
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(32, 32, 32, 32);

        TextView title = new TextView(this);
        title.setText("🔧 Admin Cleanup Tools");
        title.setTextSize(20);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setPadding(0, 0, 0, 24);

        Button btnDebugUser9 = new Button(this);
        btnDebugUser9.setText("🔍 Debug User 0009 Data");
        btnDebugUser9.setOnClickListener(v -> debugUserData(9)); // User ID 9 cho MSSV0009

        Button btnDebugUser10 = new Button(this);
        btnDebugUser10.setText("🔍 Debug User 0010 Data");
        btnDebugUser10.setOnClickListener(v -> debugUserData(10)); // User ID 10 cho MSSV0010

        Button btnCleanDuplicates = new Button(this);
        btnCleanDuplicates.setText("🧹 Clean All Duplicates");
        btnCleanDuplicates.setOnClickListener(v -> cleanDuplicates());

        tvResults = new TextView(this);
        tvResults.setText("Kết quả sẽ hiển thị ở LogCat (tag: BorrowingDAO)");
        tvResults.setTextSize(12);
        tvResults.setPadding(0, 24, 0, 0);

        Button btnBack = new Button(this);
        btnBack.setText("◀ Quay lại");
        btnBack.setOnClickListener(v -> finish());

        mainLayout.addView(title);
        mainLayout.addView(btnDebugUser9);
        mainLayout.addView(btnDebugUser10);
        mainLayout.addView(btnCleanDuplicates);
        mainLayout.addView(tvResults);
        mainLayout.addView(btnBack);

        setContentView(mainLayout);
    }

    private void debugUserData(int userId) {
        executor.execute(() -> {
            borrowingDAO.debugDuplicateData(userId);

            mainHandler.post(() -> {
                Toast.makeText(this, "Debug data User " + userId + " - Xem LogCat!", Toast.LENGTH_SHORT).show();
                tvResults.setText("Debug completed for User " + userId + ". Check LogCat for details.");
            });
        });
    }

    private void cleanDuplicates() {
        executor.execute(() -> {
            try {
                int cleaned = borrowingDAO.cleanDuplicateRecords();

                mainHandler.post(() -> {
                    String message = "Đã clean xong!\n\n" +
                            "• Số duplicate records đã xóa: " + cleaned + "\n" +
                            "• Đề xuất restart app để thấy kết quả\n" +
                            "• Kiểm tra lại user 0009 và 0010";

                    Toast.makeText(this, "Clean hoàn tất: " + cleaned + " duplicates", Toast.LENGTH_LONG).show();
                    tvResults.setText(message);
                });

            } catch (Exception e) {
                Log.e(TAG, "Error cleaning duplicates", e);
                mainHandler.post(() -> {
                    Toast.makeText(this, "Lỗi khi clean: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    tvResults.setText("Lỗi: " + e.getMessage());
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
}
