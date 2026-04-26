package com.bank.izbank.MainScreen;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bank.izbank.Adapters.BankSelectionAdapter;
import com.bank.izbank.R;
import com.bank.izbank.Sign.SignIn;
import com.bank.izbank.UserInfo.BankAccount;
import com.bank.izbank.UserInfo.History;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.Date;
import java.util.List;

public class ConfirmPaymentActivity extends AppCompatActivity {

    private static final String TAG = "ConfirmPaymentActivity";
    private TextView txtRecipientName, txtRecipientUpiId, txtSelectedBankName, txtSelectedBankBalance;
    private EditText edtAmount, edtNote;
    private Button btnPayNow;
    private BankAccount selectedAccount;
    private String recipientUpiId, recipientName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_payment);

        txtRecipientName = findViewById(R.id.txt_recipient_name);
        txtRecipientUpiId = findViewById(R.id.txt_recipient_upi_id);
        txtSelectedBankName = findViewById(R.id.txt_selected_bank_name);
        txtSelectedBankBalance = findViewById(R.id.txt_selected_bank_balance);
        edtAmount = findViewById(R.id.edt_amount);
        edtNote = findViewById(R.id.edt_note);
        btnPayNow = findViewById(R.id.btn_pay_now);
        ImageButton btnBack = findViewById(R.id.btn_back);
        View cardSelectedBank = findViewById(R.id.card_selected_bank);

        btnBack.setOnClickListener(v -> finish());

        String upiUri = getIntent().getStringExtra("upi_uri");
        if (upiUri != null) {
            parseUpiUri(upiUri);
        }

        if (SignIn.mainUser != null && !SignIn.mainUser.getBankAccounts().isEmpty()) {
            selectedAccount = SignIn.mainUser.getBankAccounts().get(0);
            updateSelectedBankUI();
        }

        cardSelectedBank.setOnClickListener(v -> showBankSelectionSheet());

        btnPayNow.setOnClickListener(v -> startSecureTransferProtocol());
    }

    private void parseUpiUri(String uriString) {
        Uri uri = Uri.parse(uriString);
        recipientUpiId = uri.getQueryParameter("pa");
        recipientName = uri.getQueryParameter("pn");
        String amount = uri.getQueryParameter("am");

        txtRecipientUpiId.setText(recipientUpiId);
        txtRecipientName.setText(recipientName != null ? recipientName : "Unknown Recipient");
        if (amount != null) {
            edtAmount.setText(amount);
        }
    }

    private void updateSelectedBankUI() {
        if (selectedAccount != null) {
            String accNo = selectedAccount.getAccountno();
            String lastFour = accNo.length() > 4 ? accNo.substring(accNo.length() - 4) : accNo;
            txtSelectedBankName.setText("IzBank - " + lastFour);
            txtSelectedBankBalance.setText("Balance: ₹ " + selectedAccount.getCash());
        }
    }

    private void showBankSelectionSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.layout_bank_selection_sheet, null);
        RecyclerView recyclerView = view.findViewById(R.id.recycler_bank_accounts_sheet);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        BankSelectionAdapter adapter = new BankSelectionAdapter(SignIn.mainUser.getBankAccounts(), account -> {
            selectedAccount = account;
            updateSelectedBankUI();
            bottomSheetDialog.dismiss();
        });
        recyclerView.setAdapter(adapter);
        
        bottomSheetDialog.setContentView(view);
        bottomSheetDialog.show();
    }

    private void startSecureTransferProtocol() {
        String amountStr = edtAmount.getText().toString().replace("₹", "").trim();
        if (amountStr.isEmpty()) {
            Toast.makeText(this, "Enter amount", Toast.LENGTH_SHORT).show();
            return;
        }

        int amount;
        try {
            amount = Integer.parseInt(amountStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show();
            return;
        }

        if (amount <= 0) {
            Toast.makeText(this, "Amount must be greater than 0", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedAccount.getCash() < amount) {
            Toast.makeText(this, "Insufficient balance", Toast.LENGTH_SHORT).show();
            return;
        }

        btnPayNow.setEnabled(false);
        btnPayNow.setText("Processing...");

        // PROTOCOL STEP 1: VERIFY RECIPIENT EXISTS BEFORE DEDUCTION
        verifyRecipientConnection(amount);
    }

    private void verifyRecipientConnection(int amount) {
        ParseQuery<ParseObject> recipientQuery = ParseQuery.getQuery("BankAccount");
        recipientQuery.whereEqualTo("upiId", recipientUpiId);
        
        recipientQuery.getFirstInBackground((recipientAccObj, e) -> {
            if (e == null && recipientAccObj != null) {
                deductFromSender(amount, recipientAccObj);
            } else {
                handleError("Recipient not found.");
            }
        });
    }

    private void deductFromSender(int amount, ParseObject recipientAccObj) {
        ParseQuery<ParseObject> senderQuery = ParseQuery.getQuery("BankAccount");
        senderQuery.whereEqualTo("userId", SignIn.mainUser.getId());
        senderQuery.whereEqualTo("accountNo", selectedAccount.getAccountno());
        
        senderQuery.getFirstInBackground((senderAccObj, e) -> {
            if (e == null && senderAccObj != null) {
                int currentBalance = Integer.parseInt(senderAccObj.getString("cash"));
                if (currentBalance < amount) {
                    handleError("Insufficient balance.");
                    return;
                }

                int newBalance = currentBalance - amount;
                senderAccObj.put("cash", String.valueOf(newBalance));
                senderAccObj.saveInBackground(e1 -> {
                    if (e1 == null) {
                        selectedAccount.setCash(newBalance);
                        logSenderHistory(amount);
                        creditRecipientAndVerify(amount, recipientAccObj);
                    } else {
                        handleError("Deduction failed.");
                    }
                });
            } else {
                handleError("Sender account error.");
            }
        });
    }

    private void creditRecipientAndVerify(int amount, ParseObject recipientAccObj) {
        int initialRecipientBalance = Integer.parseInt(recipientAccObj.getString("cash"));
        int expectedBalance = initialRecipientBalance + amount;

        recipientAccObj.put("cash", String.valueOf(expectedBalance));
        recipientAccObj.saveInBackground(e -> {
            if (e == null) {
                logRecipientHistory(recipientAccObj.getString("userId"), amount);
                
                // LAUNCH THE ANIMATION ACTIVITY
                Intent intent = new Intent(ConfirmPaymentActivity.this, PaymentSuccessActivity.class);
                intent.putExtra("amount", String.valueOf(amount));
                intent.putExtra("recipient", recipientName != null ? recipientName : recipientUpiId);
                startActivity(intent);
                finish();
            } else {
                handleError("Failed to credit recipient.");
            }
        });
    }

    private void logSenderHistory(int amount) {
        String note = edtNote.getText().toString();
        String process = "UPI Pay to " + (recipientName != null ? recipientName : recipientUpiId);
        if (!note.isEmpty()) process += " - " + note;
        process += " (-₹" + amount + ")";

        ParseObject historyObj = new ParseObject("History");
        historyObj.put("userId", SignIn.mainUser.getId());
        historyObj.put("process", process);
        historyObj.put("date", new Date());
        historyObj.put("isIncome", false);
        historyObj.saveInBackground();
        
        SignIn.mainUser.getHistory().push(new History(SignIn.mainUser.getId(), process, new Date(), false));
    }

    private void logRecipientHistory(String recipientUserId, int amount) {
        String note = edtNote.getText().toString();
        String senderName = SignIn.mainUser != null ? SignIn.mainUser.getName() : "Someone";
        String process = "UPI Received from " + senderName;
        if (!note.isEmpty()) process += " - " + note;
        process += " (+₹" + amount + ")";

        ParseObject historyObj = new ParseObject("History");
        historyObj.put("userId", recipientUserId);
        historyObj.put("process", process);
        historyObj.put("date", new Date());
        historyObj.put("isIncome", true);
        historyObj.saveInBackground();
    }

    private void handleError(String message) {
        runOnUiThread(() -> {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            resetPayButton();
        });
    }

    private void resetPayButton() {
        btnPayNow.setEnabled(true);
        btnPayNow.setText("Pay Now");
    }
}
