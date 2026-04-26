package com.bank.izbank.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bank.izbank.R;
import com.bank.izbank.UserInfo.BankAccount;

import java.util.List;

public class BankSelectionAdapter extends RecyclerView.Adapter<BankSelectionAdapter.ViewHolder> {

    private final List<BankAccount> accounts;
    private final OnAccountSelectedListener listener;
    private int selectedPosition = 0;

    public interface OnAccountSelectedListener {
        void onAccountSelected(BankAccount account);
    }

    public BankSelectionAdapter(List<BankAccount> accounts, OnAccountSelectedListener listener) {
        this.accounts = accounts;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bank_selection, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BankAccount account = accounts.get(position);
        holder.txtBankName.setText("IzBank - " + account.getAccountno().substring(account.getAccountno().length() - 4));
        holder.txtUpiId.setText(account.getUpiId());
        
        holder.imgSelected.setVisibility(position == selectedPosition ? View.VISIBLE : View.GONE);
        
        holder.itemView.setOnClickListener(v -> {
            int previousSelected = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(previousSelected);
            notifyItemChanged(selectedPosition);
            listener.onAccountSelected(account);
        });
    }

    @Override
    public int getItemCount() {
        return accounts.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtBankName, txtUpiId;
        ImageView imgSelected;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtBankName = itemView.findViewById(R.id.txt_item_bank_name);
            txtUpiId = itemView.findViewById(R.id.txt_item_upi_id);
            imgSelected = itemView.findViewById(R.id.img_selected_check);
        }
    }
}
