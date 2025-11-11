package com.example.g4prmlibrary.admin;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.g4prmlibrary.R;
import com.google.android.material.card.MaterialCardView;

public class LibraryRulesActivity extends AppCompatActivity {

    private static final String TAG = "LibraryRulesActivity";
    private ImageView ivBack;
    private TextView tvTitle;
    private MaterialCardView cardBorrowingRules, cardReturnRules, cardFineRules, cardGeneralRules;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_library_rules);

            initializeViews();
            setupClickListeners();

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: ", e);
            Toast.makeText(this, "Lỗi khi tải quy định thư viện", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initializeViews() {
        try {
            ivBack = findViewById(R.id.iv_back);
            tvTitle = findViewById(R.id.tv_title);
            cardBorrowingRules = findViewById(R.id.card_borrowing_rules);
            cardReturnRules = findViewById(R.id.card_return_rules);
            cardFineRules = findViewById(R.id.card_fine_rules);
            cardGeneralRules = findViewById(R.id.card_general_rules);

            if (tvTitle != null) {
                tvTitle.setText("Quy định thư viện");
            }

            Log.d(TAG, "Views initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views: ", e);
            throw e;
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

        } catch (Exception e) {
            Log.e(TAG, "Error setting up click listeners: ", e);
        }
    }
}
