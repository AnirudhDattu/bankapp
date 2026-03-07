package com.bank.izbank.MainScreen;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bank.izbank.Adapters.HistoryAdapter;
import com.bank.izbank.Adapters.MyBankAccountAdapter;
import com.bank.izbank.Adapters.MyCreditCardAdapter;
import com.bank.izbank.R;
import com.bank.izbank.UserInfo.BankAccount;
import com.bank.izbank.UserInfo.CreditCard;
import com.bank.izbank.Sign.SignIn;
import com.bank.izbank.UserInfo.History;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.material.card.MaterialCardView;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.bank.izbank.Sign.SignIn.mainUser;
import static com.parse.Parse.getApplicationContext;

public class AccountFragment extends Fragment {
    MaterialCardView linear_layout_request_money,linear_layout_send_money, linear_layout_history;
    ImageView add_bank_account, add_credit_card;
    RecyclerView recyclerView;
    RecyclerView recyclerViewbankaccount, recyclerViewHistory;
    TextView text_view_name, date,text_view_total_money;
    TextView textIncomeTotal, textExpenseTotal, textLoanTotal;
    ArrayList<CreditCard> myCreditCard;
    ArrayList<BankAccount> myBankAccount;
    BankAccount sendUser = null;
    String bankAccountAnother = null;
    String anotherUserid;
    private HistoryAdapter historyAdapter;
    private LineChart balanceChart;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_1,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        myCreditCard = mainUser.getCreditCards();
        myBankAccount = mainUser.getBankAccounts();

        define();
        setDate();
        click();
        setTotalMoney(myBankAccount);
        setupChart();
        updateAnalysisSummary();

        text_view_name.setText("Hello, " + mainUser.getName());

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        recyclerViewbankaccount.setHasFixedSize(true);
        recyclerViewbankaccount.setLayoutManager(new LinearLayoutManager(getActivity()));

        MyCreditCardAdapter myCreditCardAdapter = new MyCreditCardAdapter(myCreditCard,getActivity(),myBankAccount ,recyclerViewbankaccount);
        recyclerView.setAdapter(myCreditCardAdapter);

        MyBankAccountAdapter myBankAccountAdapter = new MyBankAccountAdapter(myBankAccount,getActivity() );
        recyclerViewbankaccount.setAdapter(myBankAccountAdapter);
    }

    public void define(){
        text_view_name = getView().findViewById(R.id.text_view_name);
        date = getView().findViewById(R.id.text_view_date_main);
        recyclerView = getView().findViewById(R.id.recyclerview_credit_card);
        recyclerViewbankaccount = getView().findViewById(R.id.recyclerview_bank_account);
        add_bank_account = getView().findViewById(R.id.image_view_add_bank_account);
        add_credit_card = getView().findViewById(R.id.image_view_add_credit_card);
        linear_layout_request_money = getView().findViewById(R.id.linear_layout_request_money);
        text_view_total_money = getView().findViewById(R.id.text_view_total_money);
        linear_layout_send_money = getView().findViewById(R.id.linear_layout_send_money);
        linear_layout_history = getView().findViewById(R.id.linear_layout_history);
        balanceChart = getView().findViewById(R.id.balance_chart);
        
        textIncomeTotal = getView().findViewById(R.id.text_income_total);
        textExpenseTotal = getView().findViewById(R.id.text_expense_total);
        textLoanTotal = getView().findViewById(R.id.text_loan_total);
    }

    private void updateAnalysisSummary() {
        ArrayList<History> fullHistory = stackToArrayList(mainUser.getHistory());
        int income = 0;
        int expense = 0;
        int loans = 0;

        Pattern pattern = Pattern.compile("₹(\\d+)");
        for (History h : fullHistory) {
            String process = h.getProcess();
            Matcher matcher = pattern.matcher(process);
            if (matcher.find()) {
                int amount = Integer.parseInt(matcher.group(1));
                if (process.toLowerCase().contains("requested") || process.toLowerCase().contains("received")) {
                    income += amount;
                    if (process.toLowerCase().contains("credit")) loans += amount;
                } else if (process.toLowerCase().contains("paid") || process.toLowerCase().contains("sent")) {
                    expense += amount;
                }
            }
        }

        if (textIncomeTotal != null) textIncomeTotal.setText("₹" + income);
        if (textExpenseTotal != null) textExpenseTotal.setText("₹" + expense);
        if (textLoanTotal != null) textLoanTotal.setText("₹" + loans);
    }

    private void setupChart() {
        if (balanceChart == null) return;

        ArrayList<History> fullHistory = stackToArrayList(mainUser.getHistory());
        List<Entry> entries = new ArrayList<>();
        
        int currentTotal = 0;
        if (myBankAccount != null) {
            for (BankAccount acc : myBankAccount) currentTotal += acc.getCash();
        }

        int runningBalance = currentTotal;
        entries.add(new Entry(fullHistory.size(), runningBalance));

        Pattern pattern = Pattern.compile("₹(\\d+)");
        
        for (int i = 0; i < fullHistory.size(); i++) {
            History h = fullHistory.get(i);
            String process = h.getProcess();
            Matcher matcher = pattern.matcher(process);
            
            if (matcher.find()) {
                int amount = Integer.parseInt(matcher.group(1));
                if (process.toLowerCase().contains("paid") || process.toLowerCase().contains("sent")) {
                    runningBalance += amount;
                } else if (process.toLowerCase().contains("requested") || process.toLowerCase().contains("received")) {
                    runningBalance -= amount;
                }
            }
            entries.add(0, new Entry(fullHistory.size() - 1 - i, runningBalance));
        }

        if (entries.isEmpty()) {
            entries.add(new Entry(0, currentTotal));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Spending Analysis");
        dataSet.setColor(Color.parseColor("#0071E3"));
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(Color.parseColor("#0071E3"));
        dataSet.setFillAlpha(30);
        dataSet.setDrawCircles(false);
        dataSet.setDrawValues(false);
        dataSet.setLineWidth(2.5f);

        LineData lineData = new LineData(dataSet);
        balanceChart.setData(lineData);
        balanceChart.getDescription().setEnabled(false);
        balanceChart.getLegend().setEnabled(false);
        balanceChart.getXAxis().setEnabled(false);
        balanceChart.getAxisLeft().setEnabled(false);
        balanceChart.getAxisRight().setEnabled(false);
        balanceChart.setTouchEnabled(true);
        balanceChart.invalidate();
    }

    public void setTotalMoney(ArrayList<BankAccount> MyBankAccounts){
        int totalmoney = 0;
        if (MyBankAccounts != null) {
            for (int i = 0; i<MyBankAccounts.size();i++){
                totalmoney += MyBankAccounts.get(i).getCash();
            }
        }
        text_view_total_money.setText(String.format("%,d.00", totalmoney));
    }

    public void accountsToDatabase(BankAccount bankAc){
        ParseObject object=new ParseObject("BankAccount");
        object.put("accountNo",bankAc.getAccountno());
        object.put("userId", mainUser.getId());
        object.put("cash",String.valueOf(bankAc.getCash()));
        object.saveInBackground(e -> {
            if(e != null){
                Toast.makeText(getApplicationContext(),e.getLocalizedMessage(),Toast.LENGTH_LONG).show();
            }
        });
    }

    public void cardsToDatabase(CreditCard card){
        ParseObject object=new ParseObject("CreditCard");
        object.put("creditCardNo",card.getCreditCardNo());
        object.put("userId", mainUser.getId());
        object.put("limit",String.valueOf(card.getLimit()));
        object.saveInBackground(e -> {
            if(e != null){
                Toast.makeText(getApplicationContext(),e.getLocalizedMessage(),Toast.LENGTH_LONG).show();
            }
        });
    }

    public void historyToDatabase(History history){
        ParseObject object=new ParseObject("History");
        object.put("process",history.getProcess());
        object.put("userId", mainUser.getId());
        object.put("date",history.getDateDate());
        object.saveInBackground(e -> {
            if(e != null){
                Toast.makeText(getApplicationContext(),e.getLocalizedMessage(),Toast.LENGTH_LONG).show();
            }
        });
    }

    public void updateBankAccount(BankAccount bankac){
        ParseQuery<ParseObject> queryBankAccount=ParseQuery.getQuery("BankAccount");
        queryBankAccount.whereEqualTo("accountNo", bankac.getAccountno());
        queryBankAccount.findInBackground((objects, e) -> {
            if(e!=null){
                e.printStackTrace();
            }else{
                if(objects.size()>0){
                    for(ParseObject object:objects){
                        object.deleteInBackground();
                        accountsToDatabase(bankac);
                    }
                }
            }
        });
    }

    public void updateBankAccountAnotherUser(BankAccount bankac, String userId){
        ParseQuery<ParseObject> queryBankAccount=ParseQuery.getQuery("BankAccount");
        queryBankAccount.whereEqualTo("accountNo", bankac.getAccountno());
        queryBankAccount.findInBackground((objects, e) -> {
            if(e!=null){
                e.printStackTrace();
            }else{
                if(objects.size()>0){
                    for(ParseObject object:objects){
                        object.deleteInBackground();
                        accountsToDatabaseAnotherUser(bankac,userId);
                    }
                }
            }
        });
    }

    public void accountsToDatabaseAnotherUser(BankAccount bankAc,String userId){
        ParseObject object=new ParseObject("BankAccount");
        object.put("accountNo",bankAc.getAccountno());
        object.put("userId", userId);
        object.put("cash",String.valueOf(bankAc.getCash()));
        object.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e != null){
                    Toast.makeText(getApplicationContext(),e.getLocalizedMessage(),Toast.LENGTH_LONG).show();
                }
            }
        });
    }


    public void click(){
        linear_layout_history.setOnClickListener(v -> {
            AlertDialog.Builder history_popup=new AlertDialog.Builder(getContext());
            history_popup.setTitle("HISTORY");
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View dialogView= inflater.inflate(R.layout.history_popup, null);
            history_popup.setView(dialogView);
            recyclerViewHistory = dialogView.findViewById(R.id.history_recycler_view);
            recyclerViewHistory.setLayoutManager(new LinearLayoutManager(getActivity()));

            historyAdapter=new HistoryAdapter(stackToArrayList(mainUser.getHistory()),getActivity(),getContext());
            recyclerViewHistory.setAdapter(historyAdapter);
            historyAdapter.notifyDataSetChanged();
            history_popup.create().show();
        });

        add_bank_account.setOnClickListener(v -> {
            if (myBankAccount.size()>=5){
                Toast.makeText(getContext(), "YOU CANT ADD MORE THAN 5 BANK ACCOUNT", Toast.LENGTH_LONG).show();
            }
            else{
                final EditText editText = new EditText(getContext());
                editText.setHint("0");
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                AlertDialog.Builder ad = new AlertDialog.Builder(getContext());
                ad.setTitle("Initial Deposit");
                ad.setIcon(R.drawable.icon_save_money);
                ad.setView(editText);
                ad.setNegativeButton("ADD", (dialogInterface, i) -> {
                    try {
                        int amount = Integer.parseInt(editText.getText().toString());
                        myBankAccount.add(new BankAccount(amount));
                    }catch (NumberFormatException e){
                        myBankAccount.add(new BankAccount(0));
                    }
                    MyBankAccountAdapter myBankAccountAdapter = new MyBankAccountAdapter(myBankAccount,getActivity() );
                    recyclerViewbankaccount.setAdapter(myBankAccountAdapter);
                    setTotalMoney(myBankAccount);
                    
                    accountsToDatabase(myBankAccount.get(myBankAccount.size()-1));
                    History hs = new History(mainUser.getId(),"New Bank Account Added.", Calendar.getInstance().getTime());
                    mainUser.getHistory().push(hs);
                    historyToDatabase(hs);
                    setupChart();
                    updateAnalysisSummary();
                });
                ad.create().show();
            }
        });

        add_credit_card.setOnClickListener(v -> {
            if (myCreditCard.size()>=5){
                Toast.makeText(getContext(), "YOU CANT ADD MORE THAN 5 CREDIT CARD", Toast.LENGTH_LONG).show();
            }
            else{
                final EditText editText = new EditText(getContext());
                editText.setHint("0");
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                AlertDialog.Builder ad = new AlertDialog.Builder(getContext());
                ad.setTitle("Credit Card Limit");
                ad.setIcon(R.drawable.icon_credit_card);
                ad.setView(editText);
                ad.setNegativeButton("ADD", (dialogInterface, i) -> {
                    try {
                        int limit = Integer.parseInt(editText.getText().toString());
                        myCreditCard.add(new CreditCard(limit));
                    }catch (NumberFormatException e){
                        myCreditCard.add(new CreditCard(0));
                    }
                    MyCreditCardAdapter myCreditCardAdapter = new MyCreditCardAdapter(myCreditCard,getActivity(),myBankAccount ,recyclerViewbankaccount);
                    recyclerView.setAdapter(myCreditCardAdapter);

                    cardsToDatabase(myCreditCard.get(myCreditCard.size()-1));
                    History hs = new History(mainUser.getId(),"New Credit Card Added.", Calendar.getInstance().getTime());
                    mainUser.getHistory().push(hs);
                    historyToDatabase(hs);
                });
                ad.create().show();
            }
        });

        linear_layout_request_money.setOnClickListener(v -> {
            if (myBankAccount.size()==0){
                AlertDialog.Builder ad = new AlertDialog.Builder(getContext());
                ad.setTitle("You dont have any bank account.");
                ad.setNegativeButton("CLOSE", null);
                ad.create().show();
            }
            else{
                final EditText editText = new EditText(getContext());
                editText.setHint("Amount to Request");
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);

                AlertDialog.Builder ad = new AlertDialog.Builder(getContext());
                ad.setTitle("Select Target Account");
                ad.setView(editText);
                String[] items = new String[myBankAccount.size()];
                for (int i =0; i<myBankAccount.size();i++){
                    items[i] = myBankAccount.get(i).getAccountno() + " (Balance: ₹" + myBankAccount.get(i).getCash() + ")";
                }
                final int[] checkedItem = {0};
                ad.setSingleChoiceItems(items, checkedItem[0], (dialogInterface, i) -> checkedItem[0] = i);
                ad.setNegativeButton("Request", (dialogInterface, i) -> {
                    int pos = checkedItem[0];
                    try {
                        int amount = Integer.parseInt(editText.getText().toString());
                        myBankAccount.get(pos).setCash(myBankAccount.get(pos).getCash() + amount);
                        updateBankAccount(myBankAccount.get(pos));
                        MyBankAccountAdapter myBankAccountAdapter = new MyBankAccountAdapter(myBankAccount,getActivity() );
                        recyclerViewbankaccount.setAdapter(myBankAccountAdapter);
                        setTotalMoney(myBankAccount);
                        History hs = new History(mainUser.getId(),"Money Requested: ₹" + amount, Calendar.getInstance().getTime());
                        mainUser.getHistory().push(hs);
                        historyToDatabase(hs);
                        setupChart();
                        updateAnalysisSummary();
                    } catch (Exception e) {}
                });
                ad.create().show();
            }
        });

        linear_layout_send_money.setOnClickListener(v -> {
            AlertDialog.Builder ad = new AlertDialog.Builder(getContext());
            ad.setTitle("Transfer Money");
            String arr[] = {"Between My Accounts", "To Another Person"};
            ad.setItems(arr, (dialog, which) -> {
                if (which == 0) transferBetweenOwn();
                else transferToAnother();
            });
            ad.create().show();
        });
    }

    private void transferBetweenOwn() {
        AlertDialog.Builder ad2 = new AlertDialog.Builder(getContext());
        ad2.setTitle("Source Account");
        String[] items = new String[myBankAccount.size()];
        for (int i =0; i<myBankAccount.size();i++){
            items[i] = myBankAccount.get(i).getAccountno() + " (₹" + myBankAccount.get(i).getCash() + ")";
        }
        final int[] from = {0};
        ad2.setSingleChoiceItems(items, from[0], (dialogInterface, i) -> from[0] = i);
        ad2.setNegativeButton("CONTINUE", (dialog, which) -> {
            AlertDialog.Builder ad3 = new AlertDialog.Builder(getContext());
            ad3.setTitle("Destination Account");
            final int[] to = {0};
            ad3.setSingleChoiceItems(items, to[0], (dialogInterface, i) -> to[0] = i);
            ad3.setNegativeButton("CONTINUE", (dialog1, which1) -> {
                final EditText editText = new EditText(getContext());
                editText.setHint("Amount");
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                AlertDialog.Builder ad4 = new AlertDialog.Builder(getContext());
                ad4.setTitle("Enter Amount");
                ad4.setView(editText);
                ad4.setNegativeButton("SEND", (dialog2, which2) -> {
                    try {
                        int amount = Integer.parseInt(editText.getText().toString());
                        if (amount <= myBankAccount.get(from[0]).getCash()) {
                            myBankAccount.get(to[0]).setCash(myBankAccount.get(to[0]).getCash() + amount);
                            myBankAccount.get(from[0]).setCash(myBankAccount.get(from[0]).getCash() - amount);
                            updateBankAccount(myBankAccount.get(from[0]));
                            updateBankAccount(myBankAccount.get(to[0]));
                            setTotalMoney(myBankAccount);
                            MyBankAccountAdapter adapter = new MyBankAccountAdapter(myBankAccount, getActivity());
                            recyclerViewbankaccount.setAdapter(adapter);
                            History hs = new History(mainUser.getId(),"Internal Transfer: ₹" + amount, Calendar.getInstance().getTime());
                            mainUser.getHistory().push(hs);
                            historyToDatabase(hs);
                            setupChart();
                            updateAnalysisSummary();
                        } else {
                            Toast.makeText(getApplicationContext(), "Insufficient funds", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {}
                });
                ad4.create().show();
            });
            ad3.create().show();
        });
        ad2.create().show();
    }

    private void transferToAnother() {
        final EditText editText = new EditText(getContext());
        editText.setHint("Recipient Account Number");
        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        AlertDialog.Builder typeAccountNo = new AlertDialog.Builder(getContext());
        typeAccountNo.setTitle("External Transfer");
        typeAccountNo.setView(editText);
        typeAccountNo.setNegativeButton("CONTINUE", (dialog, which) -> {
            String accNo = editText.getText().toString();
            ParseQuery<ParseObject> query = ParseQuery.getQuery("BankAccount");
            query.whereEqualTo("accountNo", accNo);
            query.findInBackground((objects, e) -> {
                if (e == null && objects.size() > 0) {
                    ParseObject obj = objects.get(0);
                    String recipientId = obj.getString("userId");
                    int recipientCash = Integer.parseInt(obj.getString("cash"));
                    
                    AlertDialog.Builder adFrom = new AlertDialog.Builder(getContext());
                    adFrom.setTitle("Select Source Account");
                    String[] items = new String[myBankAccount.size()];
                    for (int i =0; i<myBankAccount.size();i++){
                        items[i] = myBankAccount.get(i).getAccountno() + " (₹" + myBankAccount.get(i).getCash() + ")";
                    }
                    final int[] from = {0};
                    adFrom.setSingleChoiceItems(items, from[0], (dialog1, which1) -> from[0] = which1);
                    adFrom.setNegativeButton("CONTINUE", (dialog1, which1) -> {
                        final EditText amtEdit = new EditText(getContext());
                        amtEdit.setHint("Amount");
                        amtEdit.setInputType(InputType.TYPE_CLASS_NUMBER);
                        AlertDialog.Builder adAmt = new AlertDialog.Builder(getContext());
                        adAmt.setTitle("Enter Amount");
                        adAmt.setView(amtEdit);
                        adAmt.setNegativeButton("SEND", (dialog2, which2) -> {
                            try {
                                int amount = Integer.parseInt(amtEdit.getText().toString());
                                if (amount <= myBankAccount.get(from[0]).getCash()) {
                                    myBankAccount.get(from[0]).setCash(myBankAccount.get(from[0]).getCash() - amount);
                                    updateBankAccount(myBankAccount.get(from[0]));
                                    
                                    BankAccount recipientAcc = new BankAccount(accNo, recipientCash + amount);
                                    updateBankAccountAnotherUser(recipientAcc, recipientId);
                                    
                                    setTotalMoney(myBankAccount);
                                    MyBankAccountAdapter adapter = new MyBankAccountAdapter(myBankAccount, getActivity());
                                    recyclerViewbankaccount.setAdapter(adapter);
                                    
                                    History hs = new History(mainUser.getId(),"Sent ₹" + amount + " to " + accNo, Calendar.getInstance().getTime());
                                    mainUser.getHistory().push(hs);
                                    historyToDatabase(hs);
                                    setupChart();
                                    updateAnalysisSummary();
                                    Toast.makeText(getApplicationContext(), "Sent Successfully", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getApplicationContext(), "Insufficient funds", Toast.LENGTH_SHORT).show();
                                }
                            } catch (Exception ex) {}
                        });
                        adAmt.create().show();
                    });
                    adFrom.create().show();
                } else {
                    Toast.makeText(getApplicationContext(), "Invalid Account Number", Toast.LENGTH_SHORT).show();
                }
            });
        });
        typeAccountNo.create().show();
    }

    public ArrayList<History> stackToArrayList(Stack<History> stack){
        ArrayList<History> arraylistHistory = new ArrayList<>();
        Stack<History> temp = new Stack<>();
        while (!stack.isEmpty()){
            History h = stack.pop();
            arraylistHistory.add(h);
            temp.push(h);
        }
        while (!temp.isEmpty()) {
            stack.push(temp.pop());
        }
        return arraylistHistory;
    }

    public void setDate(){
        SimpleDateFormat format = new SimpleDateFormat("EEEE, dd MMMM");
        date.setText(format.format(new Date()));
    }
}
