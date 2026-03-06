package com.bank.izbank.MainScreen;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.bank.izbank.Adapters.HistoryAdapter;
import com.bank.izbank.Adapters.UserAdapter;
import com.bank.izbank.Job.Job;
import com.bank.izbank.R;
import com.bank.izbank.Sign.SignIn;
import com.bank.izbank.UserInfo.Address;
import com.bank.izbank.UserInfo.BankAccount;
import com.bank.izbank.UserInfo.CreditCard;
import com.bank.izbank.UserInfo.History;
import com.bank.izbank.UserInfo.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.parse.FindCallback;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;


import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Stack;
import java.util.ArrayList;
import java.util.List;

import static com.bank.izbank.Sign.SignIn.mainUser;

public class AdminPanelActivity extends AppCompatActivity {

    private MaterialButton linear_layout_history, linear_layout_log_out;
    RecyclerView recyclerViewHistory;
    private HistoryAdapter historyAdapter;
    private UserAdapter userAdapter;
    private TextView date;
    private RecyclerView recyclerViewUser;
    private ArrayList<User> userList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_panel);
        define();
        
        recyclerViewUser.setHasFixedSize(true);
        recyclerViewUser.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewUser.setNestedScrollingEnabled(false);

        userAdapter = new UserAdapter(userList, this);
        recyclerViewUser.setAdapter(userAdapter);

        setDate();
        click();
        
        fetchUsers();
    }

    public void define(){
        linear_layout_history = findViewById(R.id.linear_layout_history);
        linear_layout_log_out = findViewById(R.id.linear_layout_log_out);
        date = findViewById(R.id.text_view_date_admin);
        recyclerViewUser = findViewById(R.id.recyclerview_user);
    }

    private void fetchUsers() {
        userList.clear();
        ParseQuery<ParseObject> query = ParseQuery.getQuery("UserInfo");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null && objects != null) {
                    Log.d("AdminPanel", "Fetched " + objects.size() + " users");
                    for (ParseObject object : objects) {
                        try {
                            String name = object.getString("userRealName");
                            String phone = object.getString("phone");
                            String userId = object.getString("username");
                            String address_string = object.getString("address");
                            if (address_string == null) address_string = "";
                            String[] str = address_string.split(" ");
                            Address address;
                            if (str.length >= 8) {
                                address = new Address(str[0], str[1], Integer.parseInt(str[2]), Integer.parseInt(str[3]), Integer.parseInt(str[4]), str[5], str[6], str[7]);
                            } else {
                                address = new Address("", "", 0, 0, 0, "", "", "");
                            }
                            String jobName = object.getString("job");
                            String maxCreditAmount = object.getString("maxCreditAmount");
                            String maxCreditInstallment = object.getString("maxCreditInstallment");
                            String interestRate = object.getString("interestRate");
                            Job tempJob = new Job(jobName, maxCreditAmount, maxCreditInstallment, interestRate);

                            User user = new User(name, userId, phone, address, tempJob);
                            ParseFile parseFile = (ParseFile) object.get("images");
                            if (parseFile != null) {
                                parseFile.getDataInBackground((data, e1) -> {
                                    if (data != null && e1 == null) {
                                        Bitmap downloadedImage = BitmapFactory.decodeByteArray(data, 0, data.length);
                                        user.setPhoto(downloadedImage);
                                        userAdapter.notifyDataSetChanged();
                                    }
                                });
                            }
                            
                            fetchBankAccounts(user);
                            fetchCreditCards(user);
                            
                            userList.add(user);
                        } catch (Exception ex) {
                            Log.e("AdminPanel", "Error parsing user: " + ex.getMessage());
                        }
                    }
                    userAdapter.notifyDataSetChanged();
                } else {
                    String error = (e != null) ? e.getMessage() : "Unknown error";
                    Log.e("AdminPanel", "Query error: " + error);
                }
            }
        });
    }

    private void fetchBankAccounts(User user) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("BankAccount");
        query.whereEqualTo("userId", user.getId());
        query.findInBackground((objects, e) -> {
            if (e == null && objects != null) {
                ArrayList<BankAccount> accounts = new ArrayList<>();
                for (ParseObject object : objects) {
                    String cash = object.getString("cash");
                    String accountNo = object.getString("accountNo");
                    if (cash != null && accountNo != null) {
                        accounts.add(new BankAccount(accountNo, Integer.parseInt(cash)));
                    }
                }
                user.setBankAccounts(accounts);
            }
        });
    }

    private void fetchCreditCards(User user) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("CreditCard");
        query.whereEqualTo("userId", user.getId());
        query.findInBackground((objects, e) -> {
            if (e == null && objects != null) {
                ArrayList<CreditCard> cards = new ArrayList<>();
                for (ParseObject object : objects) {
                    String cardNo = object.getString("creditCardNo");
                    String limit = object.getString("limit");
                    if (cardNo != null && limit != null) {
                        cards.add(new CreditCard(cardNo, Integer.parseInt(limit)));
                    }
                }
                user.setCreditCards(cards);
            }
        });
    }

    public void click(){
        linear_layout_history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Fetch admin history
                fetchAdminHistory();
            }
        });
    }

    private void fetchAdminHistory() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("History");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null && objects != null) {
                    ArrayList<History> historyList = new ArrayList<>();
                    for (ParseObject object : objects) {
                        String process = object.getString("process");
                        String userId = object.getString("userId");
                        Date date = object.getDate("date");
                        historyList.add(new History(userId, process, date));
                    }
                    showHistoryDialog(historyList);
                } else {
                    Toast.makeText(AdminPanelActivity.this, "No global history found", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showHistoryDialog(ArrayList<History> historyList) {
        AlertDialog.Builder history_popup=new AlertDialog.Builder(AdminPanelActivity.this);
        history_popup.setTitle("GLOBAL SYSTEM HISTORY");
        LayoutInflater inflater = getLayoutInflater();
        View dialogView= inflater.inflate(R.layout.history_popup, null);
        history_popup.setView(dialogView);
        recyclerViewHistory = dialogView.findViewById(R.id.history_recycler_view);
        recyclerViewHistory.setLayoutManager(new LinearLayoutManager(AdminPanelActivity.this));

        historyAdapter = new HistoryAdapter(historyList, AdminPanelActivity.this, getApplicationContext());
        recyclerViewHistory.setAdapter(historyAdapter);
        history_popup.create().show();
    }

    public void setDate(){
        SimpleDateFormat format = new SimpleDateFormat("EEEE, dd MMMM");
        Date currentTime = Calendar.getInstance().getTime();
        date.setText(format.format(currentTime));
    }

    public void logOut(View view){
        ParseUser.logOutInBackground(new LogOutCallback() {
            @Override
            public void done(ParseException e) {
                if(e !=null){
                    Toast.makeText(getApplicationContext(),e.getLocalizedMessage(),Toast.LENGTH_LONG).show();
                }else{
                    Intent intent=new Intent(AdminPanelActivity.this, SignIn.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }
}
