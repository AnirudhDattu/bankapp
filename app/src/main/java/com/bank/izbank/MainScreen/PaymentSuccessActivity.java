package com.bank.izbank.MainScreen;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bank.izbank.R;

public class PaymentSuccessActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_success);

        String amount = getIntent().getStringExtra("amount");
        String recipient = getIntent().getStringExtra("recipient");

        LinearLayout layoutProcessing = findViewById(R.id.layout_processing);
        LinearLayout layoutSuccess = findViewById(R.id.layout_success);
        TextView txtAmount = findViewById(R.id.txt_success_amount);
        TextView txtRecipient = findViewById(R.id.txt_success_recipient);
        ImageView imgCheck = findViewById(R.id.img_success_check);
        Button btnDone = findViewById(R.id.btn_done_success);

        txtAmount.setText("₹ " + amount);
        txtRecipient.setText("To: " + recipient);

        // Simulated Processing Phase (0.8 seconds)
        new Handler().postDelayed(() -> {
            layoutProcessing.setVisibility(View.GONE);
            layoutSuccess.setVisibility(View.VISIBLE);
            btnDone.setVisibility(View.VISIBLE);

            // Trigger the Bounce Animation on the Checkmark
            Animation bounce = AnimationUtils.loadAnimation(this, R.anim.bounce);
            imgCheck.startAnimation(bounce);
            
        }, 800);

        btnDone.setOnClickListener(v -> finish());
    }
}
