package com.bank.izbank.Adapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bank.izbank.R;
import com.bank.izbank.UserInfo.BankAccount;
import com.bank.izbank.UserInfo.User;
import com.google.android.material.button.MaterialButton;
import com.parse.ParseObject;
import com.parse.ParseQuery;

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

        holder.itemView.setOnClickListener(v -> showUserDetails(account, position));
    }

    private void showUserDetails(User user, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = context.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.admin_screen_user_popup, null);
        builder.setView(dialogView);

        TextView phone = dialogView.findViewById(R.id.text_view_phone_number);
        TextView adres = dialogView.findViewById(R.id.text_view_adres);
        TextView job = dialogView.findViewById(R.id.text_view_job);
        LinearLayout bankAccountsList = dialogView.findViewById(R.id.layout_bank_accounts_list);
        MaterialButton btnAddAccount = dialogView.findViewById(R.id.btn_add_bank_account);
        MaterialButton btnDeleteUser = dialogView.findViewById(R.id.btn_delete_user);

        phone.setText("+" + user.getPhoneNumber());
        adres.setText(user.addressWrite());
        job.setText(user.getJob().getName());

        refreshBankAccountsList(user, bankAccountsList);

        btnAddAccount.setOnClickListener(v -> {
            showAddAccountDialog(user, bankAccountsList);
        });

        btnDeleteUser.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Delete User")
                    .setMessage("Are you sure you want to delete " + user.getName() + " and all their data?")
                    .setPositiveButton("DELETE", (dialog, which) -> deleteUserCompletely(user, position))
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        dialog.show();
    }

    private void refreshBankAccountsList(User user, LinearLayout container) {
        container.removeAllViews();
        if (user.getBankAccounts() != null) {
            for (BankAccount account : user.getBankAccounts()) {
                View row = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_2, null);
                TextView t1 = row.findViewById(android.R.id.text1);
                TextView t2 = row.findViewById(android.R.id.text2);

                t1.setText("Acc: " + account.getAccountno());
                t1.setTextColor(Color.BLACK);
                t1.setTypeface(null, Typeface.BOLD);
                
                t2.setText("Balance: ₹" + account.getCash() + " | UPI: " + account.getUpiId());
                t2.setTextColor(Color.GRAY);

                row.setPadding(0, 15, 0, 15);
                row.setOnClickListener(v -> showEditAccountDialog(user, account, container));
                container.addView(row);
            }
        }
    }

    private void showEditAccountDialog(User user, BankAccount account, LinearLayout container) {
        String[] options = {"Edit Balance", "Delete Account"};
        new AlertDialog.Builder(context)
                .setTitle("Manage Account: " + account.getAccountno())
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        showEditBalanceDialog(user, account, container);
                    } else {
                        deleteAccount(user, account, container);
                    }
                }).show();
    }

    private void showEditBalanceDialog(User user, BankAccount account, LinearLayout container) {
        EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setText(String.valueOf(account.getCash()));
        
        new AlertDialog.Builder(context)
                .setTitle("Edit Balance")
                .setView(input)
                .setPositiveButton("UPDATE", (dialog, which) -> {
                    int newBalance = Integer.parseInt(input.getText().toString());
                    updateAccountBalanceInDb(user, account, newBalance, container);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateAccountBalanceInDb(User user, BankAccount account, int newBalance, LinearLayout container) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("BankAccount");
        query.whereEqualTo("userId", user.getId());
        query.whereEqualTo("accountNo", account.getAccountno());
        query.getFirstInBackground((object, e) -> {
            if (e == null && object != null) {
                object.put("cash", String.valueOf(newBalance));
                object.saveInBackground(e1 -> {
                    if (e1 == null) {
                        account.setCash(newBalance);
                        refreshBankAccountsList(user, container);
                        Toast.makeText(context, "Balance updated", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void deleteAccount(User user, BankAccount account, LinearLayout container) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("BankAccount");
        query.whereEqualTo("userId", user.getId());
        query.whereEqualTo("accountNo", account.getAccountno());
        query.getFirstInBackground((object, e) -> {
            if (e == null && object != null) {
                object.deleteInBackground(e1 -> {
                    if (e1 == null) {
                        user.getBankAccounts().remove(account);
                        refreshBankAccountsList(user, container);
                        Toast.makeText(context, "Account deleted", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void showAddAccountDialog(User user, LinearLayout container) {
        EditText input = new EditText(context);
        input.setHint("Initial Deposit Amount");
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        
        new AlertDialog.Builder(context)
                .setTitle("Add New Account")
                .setView(input)
                .setPositiveButton("CREATE", (dialog, which) -> {
                    int amount = input.getText().toString().isEmpty() ? 0 : Integer.parseInt(input.getText().toString());
                    createNewAccount(user, amount, container);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void createNewAccount(User user, int amount, LinearLayout container) {
        BankAccount newAcc = new BankAccount(amount); // This uses our consistent UPI generator
        ParseObject obj = new ParseObject("BankAccount");
        obj.put("accountNo", newAcc.getAccountno());
        obj.put("userId", user.getId());
        obj.put("cash", String.valueOf(newAcc.getCash()));
        obj.put("upiId", newAcc.getUpiId());
        
        obj.saveInBackground(e -> {
            if (e == null) {
                if (user.getBankAccounts() == null) user.setBankAccounts(new ArrayList<>());
                user.getBankAccounts().add(newAcc);
                refreshBankAccountsList(user, container);
                Toast.makeText(context, "Account created successfully", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteUserCompletely(User user, int position) {
        // 1. Delete from UserInfo
        ParseQuery<ParseObject> userQuery = ParseQuery.getQuery("UserInfo");
        userQuery.whereEqualTo("username", user.getId());
        userQuery.getFirstInBackground((obj, e) -> {
            if (e == null && obj != null) {
                obj.deleteInBackground();
            }
        });

        // 2. Delete all BankAccounts
        ParseQuery<ParseObject> bankQuery = ParseQuery.getQuery("BankAccount");
        bankQuery.whereEqualTo("userId", user.getId());
        bankQuery.findInBackground((objects, e) -> {
            if (e == null && objects != null) {
                for (ParseObject o : objects) o.deleteInBackground();
            }
        });

        // 3. Delete from Local List
        users.remove(position);
        notifyItemRemoved(position);
        Toast.makeText(context, "User deleted from system", Toast.LENGTH_LONG).show();
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
