package com.bank.izbank.MainScreen;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bank.izbank.Adapters.BankSelectionAdapter;
import com.bank.izbank.R;
import com.bank.izbank.Sign.SignIn;
import com.bank.izbank.UserInfo.BankAccount;
import com.google.android.material.button.MaterialButton;
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
    private ImageView btnCopyUpi;
    private MaterialButton btnShareQr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_qr_code);

        imgQrCode = findViewById(R.id.img_qr_code);
        txtAccountName = findViewById(R.id.txt_account_name);
        txtUpiId = findViewById(R.id.txt_upi_id);
        recyclerViewAccounts = findViewById(R.id.recycler_bank_accounts);
        ImageButton btnBack = findViewById(R.id.btn_back);
        btnCopyUpi = findViewById(R.id.btn_copy_upi);
        btnShareQr = findViewById(R.id.btn_share_qr);

        btnBack.setOnClickListener(v -> finish());

        btnCopyUpi.setOnClickListener(v -> {
            String upi = txtUpiId.getText().toString();
            copyToClipboard("UPI ID", upi);
        });

        btnShareQr.setOnClickListener(v -> {
            saveAndShareQR();
        });

        if (SignIn.mainUser != null) {
            txtAccountName.setText(SignIn.mainUser.getName());
            
            ArrayList<BankAccount> accounts = SignIn.mainUser.getBankAccounts();
            if (accounts != null && !accounts.isEmpty()) {
                setupRecyclerView(accounts);
                generateQRCode(accounts.get(0));
            }
        }
    }

    private void copyToClipboard(String label, String text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(label, text);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, label + " copied to clipboard", Toast.LENGTH_SHORT).show();
    }

    private void saveAndShareQR() {
        if (imgQrCode.getDrawable() == null || SignIn.mainUser == null) return;
        
        // Inflate the custom share layout
        View shareView = LayoutInflater.from(this).inflate(R.layout.layout_qr_share, null);
        
        TextView initialTv = shareView.findViewById(R.id.share_initial);
        TextView nameTv = shareView.findViewById(R.id.share_name);
        ImageView qrIv = shareView.findViewById(R.id.share_qr);
        TextView upiTv = shareView.findViewById(R.id.share_upi);

        String name = SignIn.mainUser.getName();
        nameTv.setText(name);
        if (name != null && !name.isEmpty()) {
            initialTv.setText(name.substring(0, 1).toUpperCase());
        }
        
        upiTv.setText("UPI ID: " + txtUpiId.getText().toString());
        qrIv.setImageDrawable(imgQrCode.getDrawable());

        // Convert the view to a Bitmap
        Bitmap bitmap = getBitmapFromView(shareView);
        
        // Save the composed image to the gallery
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "My_QR_Code_" + System.currentTimeMillis(), "Izbank UPI QR");
        
        if (path != null) {
            Toast.makeText(this, "Professional QR Code saved to Gallery", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Failed to save QR Code", Toast.LENGTH_SHORT).show();
        }
    }

    private Bitmap getBitmapFromView(View view) {
        view.measure(View.MeasureSpec.makeMeasureSpec(1000, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
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
