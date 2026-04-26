package com.bank.izbank.MainScreen;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bank.izbank.R;
import com.bank.izbank.UserInfo.BankAccount;
import com.parse.ParseObject;
import com.parse.ParseQuery;

public class PayToUpiActivity extends AppCompatActivity {

    private static final String TAG = "PayToUpiActivity";
    private EditText edtUpiId;
    private Button btnVerifyPay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay_to_upi_id);

        edtUpiId = findViewById(R.id.edt_recipient_upi_id);
        btnVerifyPay = findViewById(R.id.btn_verify_pay);
        ImageButton btnBack = findViewById(R.id.btn_back);

        btnBack.setOnClickListener(v -> finish());

        btnVerifyPay.setOnClickListener(v -> {
            String upiId = edtUpiId.getText().toString().trim().toLowerCase(); // Normalize input
            if (upiId.isEmpty()) {
                Toast.makeText(this, "Please enter a UPI ID", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!upiId.contains("@")) {
                Toast.makeText(this, "Invalid UPI ID format. Must include @izbank", Toast.LENGTH_SHORT).show();
                return;
            }

            btnVerifyPay.setEnabled(false);
            btnVerifyPay.setText("Verifying...");
            verifyUpiId(upiId);
        });
    }

    private void verifyUpiId(String upiId) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("BankAccount");
        query.whereEqualTo("upiId", upiId);
        query.findInBackground((objects, e) -> {
            if (e == null && objects != null && !objects.isEmpty()) {
                ParseObject accountObj = objects.get(0);
                String userId = accountObj.getString("userId");
                
                // Fetch recipient name from UserInfo
                ParseQuery<ParseObject> userQuery = ParseQuery.getQuery("UserInfo");
                userQuery.whereEqualTo("username", userId);
                userQuery.findInBackground((userObjects, e1) -> {
                    if (e1 == null && userObjects != null && !userObjects.isEmpty()) {
                        String recipientName = userObjects.get(0).getString("userRealName");
                        String uri = "upi://pay?pa=" + upiId + "&pn=" + recipientName + "&cu=INR";
                        
                        Intent intent = new Intent(PayToUpiActivity.this, ConfirmPaymentActivity.class);
                        intent.putExtra("upi_uri", uri);
                        startActivity(intent);
                        finish();
                    } else {
                        btnVerifyPay.setEnabled(true);
                        btnVerifyPay.setText("Verify and Pay");
                        Toast.makeText(this, "Recipient details found but name is missing.", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                btnVerifyPay.setEnabled(true);
                btnVerifyPay.setText("Verify and Pay");
                Toast.makeText(this, "UPI ID '" + upiId + "' not found.", Toast.LENGTH_LONG).show();
                Log.e(TAG, "UPI Verification Failed for: " + upiId);
            }
        });
    }
}
