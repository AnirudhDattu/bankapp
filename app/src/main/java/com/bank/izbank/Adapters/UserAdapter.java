package com.bank.izbank.Adapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bank.izbank.R;
import com.bank.izbank.UserInfo.User;

import java.util.ArrayList;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    ArrayList<User> users;
    Activity context;

    public UserAdapter(ArrayList<User> myMovieData, Activity activity) {
        this.users = myMovieData;
        this.context = activity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.user_admin_card_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final User account = users.get(position);
        holder.textviewmoney.setText(account.getName());
        holder.textviewbankno.setText("User ID: " + account.getId());

        if (account.getPhoto() != null) {
            holder.photo.setImageBitmap(account.getPhoto());
        } else {
            holder.photo.setImageResource(R.drawable.ic_user_def);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showUserDetails(account);
            }
        });
    }

    private void showUserDetails(User account) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = context.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.admin_screen_user_popup, null);
        builder.setView(dialogView);

        TextView phone = dialogView.findViewById(R.id.text_view_phone_number);
        TextView adres = dialogView.findViewById(R.id.text_view_adres);
        TextView job = dialogView.findViewById(R.id.text_view_job);
        TextView bankcount = dialogView.findViewById(R.id.text_view_bank_account_counter);
        TextView creditcount = dialogView.findViewById(R.id.text_view_credit_card_counter);
        TextView interestRate = dialogView.findViewById(R.id.text_view_interest_rate);

        phone.setText("+" + account.getPhoneNumber());
        adres.setText(account.addressWrite());
        job.setText(account.getJob().getName());

        StringBuilder bankStr = new StringBuilder("BANK ACCOUNTS:\n");
        if (account.getBankAccounts() != null && !account.getBankAccounts().isEmpty()) {
            for (int i = 0; i < account.getBankAccounts().size(); i++) {
                bankStr.append(account.getBankAccounts().get(i).getAccountno())
                        .append(": ₹")
                        .append(account.getBankAccounts().get(i).getCash());
                if (i != account.getBankAccounts().size() - 1) bankStr.append("\n");
            }
        } else {
            bankStr.append("No active accounts");
        }
        bankcount.setText(bankStr.toString());

        StringBuilder creditStr = new StringBuilder("CREDIT CARDS:\n");
        if (account.getCreditCards() != null && !account.getCreditCards().isEmpty()) {
            for (int i = 0; i < account.getCreditCards().size(); i++) {
                creditStr.append(account.getCreditCards().get(i).getCreditCardNo())
                        .append(": ₹")
                        .append(account.getCreditCards().get(i).getLimit());
                if (i != account.getCreditCards().size() - 1) creditStr.append("\n");
            }
        } else {
            creditStr.append("No active cards");
        }
        creditcount.setText(creditStr.toString());

        interestRate.setText("Interest Rate: " + account.getJob().getInterestRate() + "% | Max Credit: ₹" + account.getJob().getMaxCreditAmount());

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        dialog.show();
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView textviewbankno;
        TextView textviewmoney;
        ImageView photo;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textviewbankno = itemView.findViewById(R.id.text_view_bank_account_no);
            textviewmoney = itemView.findViewById(R.id.text_view_bank_account_money);
            photo = itemView.findViewById(R.id.bank_account_ImageView);
        }
    }
}
