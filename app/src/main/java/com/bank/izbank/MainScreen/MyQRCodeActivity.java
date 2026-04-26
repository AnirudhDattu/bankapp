package com.bank.izbank.MainScreen;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bank.izbank.Adapters.BankSelectionAdapter;
import com.bank.izbank.R;
import com.bank.izbank.Sign.SignIn;
import com.bank.izbank.UserInfo.BankAccount;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.util.ArrayList;

public class MyQRCodeActivity extends AppCompatActivity {

    private ImageView imgQrCode;
    private TextView txtAccountName, txtUpiId;
    private RecyclerView recyclerViewAccounts;
    private BankSelectionAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_qr_code);

        imgQrCode = findViewById(R.id.img_qr_code);
        txtAccountName = findViewById(R.id.txt_account_name);
        txtUpiId = findViewById(R.id.txt_upi_id);
        recyclerViewAccounts = findViewById(R.id.recycler_bank_accounts);
        ImageButton btnBack = findViewById(R.id.btn_back);

        btnBack.setOnClickListener(v -> finish());

        if (SignIn.mainUser != null) {
            txtAccountName.setText(SignIn.mainUser.getName());
            
            ArrayList<BankAccount> accounts = SignIn.mainUser.getBankAccounts();
            if (accounts != null && !accounts.isEmpty()) {
                setupRecyclerView(accounts);
                generateQRCode(accounts.get(0));
            }
        }
    }

    private void setupRecyclerView(ArrayList<BankAccount> accounts) {
        recyclerViewAccounts.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BankSelectionAdapter(accounts, this::generateQRCode);
        recyclerViewAccounts.setAdapter(adapter);
    }

    private void generateQRCode(BankAccount account) {
        txtUpiId.setText(account.getUpiId());
        String upiUri = account.getUpiUri(SignIn.mainUser.getName());
        
        QRCodeWriter writer = new QRCodeWriter();
        try {
            BitMatrix bitMatrix = writer.encode(upiUri, BarcodeFormat.QR_CODE, 512, 512);
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            imgQrCode.setImageBitmap(bmp);
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }
}
