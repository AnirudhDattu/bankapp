package com.bank.izbank.Adapters;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bank.izbank.R;
import com.bank.izbank.UserInfo.BankAccount;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;

public class MyBankAccountAdapter extends RecyclerView.Adapter<MyBankAccountAdapter.ViewHolder> {

    ArrayList<BankAccount> MyBankAccounts;
    Activity context;

    public MyBankAccountAdapter(ArrayList<BankAccount> myMovieData, Activity activity) {
        this.MyBankAccounts = myMovieData;
        this.context = activity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.bank_account_cardview, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final BankAccount account = MyBankAccounts.get(position);
        holder.textviewmoney.setText("₹" + String.valueOf(account.getCash()));
        holder.textviewbankno.setText(account.getAccountno());
        holder.textviewupi.setText(account.getUpiId());

        holder.itemView.setOnClickListener(v -> showAccountDetailsDialog(account));
    }

    private void showAccountDetailsDialog(BankAccount account) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_account_details, null);
        builder.setView(dialogView);

        TextView typeTv = dialogView.findViewById(R.id.detail_account_type);
        TextView noTv = dialogView.findViewById(R.id.detail_account_number);
        TextView upiTv = dialogView.findViewById(R.id.detail_upi_id);
        TextView balanceTv = dialogView.findViewById(R.id.detail_balance);
        ImageView copyNoBtn = dialogView.findViewById(R.id.btn_copy_acc_no);
        ImageView copyUpiBtn = dialogView.findViewById(R.id.btn_copy_upi_id);
        MaterialButton closeBtn = dialogView.findViewById(R.id.btn_close_details);

        // Setting data
        noTv.setText(account.getAccountno());
        upiTv.setText(account.getUpiId());
        balanceTv.setText("₹" + account.getCash());
        typeTv.setText("Current Account Account"); // Matching the text in user's image

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        copyNoBtn.setOnClickListener(v -> copyToClipboard("Account Number", account.getAccountno()));
        copyUpiBtn.setOnClickListener(v -> copyToClipboard("UPI ID", account.getUpiId()));

        closeBtn.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void copyToClipboard(String label, String text) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(label, text);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(context, label + " copied to clipboard", Toast.LENGTH_SHORT).show();
    }

    @Override
    public int getItemCount() {
        return MyBankAccounts.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView textviewbankno;
        TextView textviewmoney;
        TextView textviewupi;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textviewbankno = itemView.findViewById(R.id.text_view_bank_account_no);
            textviewmoney = itemView.findViewById(R.id.text_view_bank_account_money);
            textviewupi = itemView.findViewById(R.id.text_view_upi_id);
        }
    }
}
