package com.bank.izbank.MainScreen;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
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
import androidx.viewpager2.widget.ViewPager2;

import com.bank.izbank.Adapters.AnalysisCarouselAdapter;
import com.bank.izbank.Adapters.HistoryAdapter;
import com.bank.izbank.Adapters.MyBankAccountAdapter;
import com.bank.izbank.Adapters.MyCreditCardAdapter;
import com.bank.izbank.R;
import com.bank.izbank.UserInfo.BankAccount;
import com.bank.izbank.UserInfo.CreditCard;
import com.bank.izbank.Sign.SignIn;
import com.bank.izbank.UserInfo.History;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieEntry;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
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
    TextView text_view_name, date;
    ArrayList<CreditCard> myCreditCard;
    ArrayList<BankAccount> myBankAccount;
    BankAccount sendUser = null;
    String bankAccountAnother = null;
    String anotherUserid;
    private HistoryAdapter historyAdapter;
    
    private ViewPager2 viewPagerAnalysis;
    private TabLayout tabLayoutIndicator;
    private AnalysisCarouselAdapter carouselAdapter;

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
        setupCarousel();

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
        linear_layout_send_money = getView().findViewById(R.id.linear_layout_send_money);
        linear_layout_history = getView().findViewById(R.id.linear_layout_history);
        
        viewPagerAnalysis = getView().findViewById(R.id.viewPager_analysis);
        tabLayoutIndicator = getView().findViewById(R.id.tabLayout_indicator);
    }

    private void setupCarousel() {
        if (viewPagerAnalysis == null) return;

        List<AnalysisCarouselAdapter.AnalysisPage> pages = new ArrayList<>();
        ArrayList<History> fullHistory = stackToArrayList(mainUser.getHistory());
        
        // --- Page 1: Balance Trend (Line Chart) ---
        AnalysisCarouselAdapter.AnalysisPage trendPage = new AnalysisCarouselAdapter.AnalysisPage("Balance Activity", 0);
        trendPage.lineEntries = calculateTrendEntries(fullHistory);
        pages.add(trendPage);

        // --- Page 2: Spend Analysis (Pie Chart) ---
        AnalysisCarouselAdapter.AnalysisPage spendPage = new AnalysisCarouselAdapter.AnalysisPage("Spend Analysis", 1);
        spendPage.pieEntries = calculateSpendDistribution(fullHistory);
        pages.add(spendPage);

        // --- Page 3: Summary Stats (Text) ---
        AnalysisCarouselAdapter.AnalysisPage summaryPage = new AnalysisCarouselAdapter.AnalysisPage("Net Summary", 2);
        
        int cash = 0;
        if (myBankAccount != null) {
            for (BankAccount acc : myBankAccount) cash += acc.getCash();
        }
        
        int debt = 0;
        if (myCreditCard != null) {
            for (CreditCard card : myCreditCard) debt += card.getLimit(); 
        }

        summaryPage.mainStat = String.format("₹%,d", cash);
        summaryPage.statLabel = "Total Account Value";
        summaryPage.subDetails = String.format("Cash: ₹%,d | Credit: ₹%,d", cash, debt);
        pages.add(summaryPage);

        carouselAdapter = new AnalysisCarouselAdapter(pages);
        viewPagerAnalysis.setAdapter(carouselAdapter);

        new TabLayoutMediator(tabLayoutIndicator, viewPagerAnalysis, (tab, position) -> {}).attach();
    }

    private List<Entry> calculateTrendEntries(ArrayList<History> fullHistory) {
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
                if (process.toLowerCase().contains("paid") || process.toLowerCase().contains("sent")) runningBalance += amount;
                else if (process.toLowerCase().contains("requested") || process.toLowerCase().contains("received")) runningBalance -= amount;
            }
            entries.add(0, new Entry(fullHistory.size() - 1 - i, runningBalance));
        }
        if (entries.isEmpty()) entries.add(new Entry(0, currentTotal));
        return entries;
    }

    private List<PieEntry> calculateSpendDistribution(ArrayList<History> fullHistory) {
        List<PieEntry> entries = new ArrayList<>();
        int power = 0, gas = 0, internet = 0, phone = 0, water = 0, transfers = 0, loans = 0, other = 0;

        Pattern pattern = Pattern.compile("₹(\\d+)");
        for (History h : fullHistory) {
            String process = h.getProcess().toLowerCase();
            Matcher matcher = pattern.matcher(process);
            if (matcher.find()) {
                int amount = Integer.parseInt(matcher.group(1));
                if (process.contains("electric") || process.contains("power")) power += amount;
                else if (process.contains("gas") || process.contains("fuel")) gas += amount;
                else if (process.contains("internet")) internet += amount;
                else if (process.contains("phone")) phone += amount;
                else if (process.contains("water")) water += amount;
                else if (process.contains("sent") || process.contains("transfer")) transfers += amount;
                else if (process.contains("credit") || process.contains("loan")) loans += amount;
                else if (process.contains("paid")) other += amount;
            }
        }

        if (power > 0) entries.add(new PieEntry(power, "Power"));
        if (gas > 0) entries.add(new PieEntry(gas, "Fuel"));
        if (internet > 0) entries.add(new PieEntry(internet, "Internet"));
        if (phone > 0) entries.add(new PieEntry(phone, "Phone"));
        if (water > 0) entries.add(new PieEntry(water, "Water"));
        if (transfers > 0) entries.add(new PieEntry(transfers, "Transfers"));
        if (loans > 0) entries.add(new PieEntry(loans, "Loans"));
        if (other > 0) entries.add(new PieEntry(other, "Misc"));
        
        if (entries.isEmpty()) entries.add(new PieEntry(1, "No Activity"));
        return entries;
    }

    private String calculateNetWorth() {
        int total = 0;
        if (myBankAccount != null) {
            for (BankAccount acc : myBankAccount) total += acc.getCash();
        }
        return String.format("₹%,d", total);
    }

    public void setTotalMoney(ArrayList<BankAccount> MyBankAccounts){
        setupCarousel();
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
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), android.R.style.Theme_Material_Light_NoActionBar_Fullscreen);
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.history_popup, null);
            builder.setView(dialogView);
            
            AlertDialog dialog = builder.create();
            
            dialogView.findViewById(R.id.btn_close_history).setOnClickListener(view -> dialog.dismiss());
            
            recyclerViewHistory = dialogView.findViewById(R.id.history_recycler_view);
            recyclerViewHistory.setLayoutManager(new LinearLayoutManager(getActivity()));

            historyAdapter=new HistoryAdapter(stackToArrayList(mainUser.getHistory()),getActivity(),getContext());
            recyclerViewHistory.setAdapter(historyAdapter);
            
            dialog.show();
        });

        add_bank_account.setOnClickListener(v -> {
            if (myBankAccount.size()>=5){
                Toast.makeText(getContext(), "Limit Reached: Max 5 accounts.", Toast.LENGTH_LONG).show();
            }
            else{
                showModernInputDialog("Initial Deposit", "Enter the amount for your new account.", "ADD", amountStr -> {
                    try {
                        int amount = Integer.parseInt(amountStr);
                        myBankAccount.add(new BankAccount(amount));
                    } catch (NumberFormatException e){
                        myBankAccount.add(new BankAccount(0));
                    }
                    updateBankAccountList();
                    accountsToDatabase(myBankAccount.get(myBankAccount.size()-1));
                    recordHistory("New Bank Account Added.");
                });
            }
        });

        add_credit_card.setOnClickListener(v -> {
            if (myCreditCard.size()>=5){
                Toast.makeText(getContext(), "Limit Reached: Max 5 cards.", Toast.LENGTH_LONG).show();
            }
            else{
                showModernInputDialog("Credit Limit", "Set the initial limit for your new card.", "ADD", limitStr -> {
                    try {
                        int limit = Integer.parseInt(limitStr);
                        myCreditCard.add(new CreditCard(limit));
                    } catch (NumberFormatException e){
                        myCreditCard.add(new CreditCard(0));
                    }
                    updateCreditCardList();
                    cardsToDatabase(myCreditCard.get(myCreditCard.size()-1));
                    recordHistory("New Credit Card Added.");
                });
            }
        });

        linear_layout_request_money.setOnClickListener(v -> {
            if (myBankAccount.size()==0){
                showModernMessageDialog("No Accounts", "Please add a bank account first.");
            }
            else{
                showModernAccountSelectionDialog("Request Money", pos -> {
                    showModernInputDialog("Request Amount", "How much would you like to request?", "REQUEST", amountStr -> {
                        try {
                            int amount = Integer.parseInt(amountStr);
                            myBankAccount.get(pos).setCash(myBankAccount.get(pos).getCash() + amount);
                            updateBankAccount(myBankAccount.get(pos));
                            updateBankAccountList();
                            recordHistory("Money Requested: ₹" + amount);
                        } catch (Exception e) {}
                    });
                });
            }
        });

        linear_layout_send_money.setOnClickListener(v -> {
            showModernSendMoneySelection();
        });
    }

    private void showModernInputDialog(String title, String message, String actionText, ModernInputListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.ModernDialog);
        View view = getLayoutInflater().inflate(R.layout.dialog_modern_input, null);
        builder.setView(view);

        TextView titleView = view.findViewById(R.id.dialog_title);
        TextView messageView = view.findViewById(R.id.dialog_message);
        EditText input = view.findViewById(R.id.dialog_input);
        MaterialButton btnAction = view.findViewById(R.id.dialog_btn_action);

        titleView.setText(title);
        messageView.setText(message);
        btnAction.setText(actionText);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        btnAction.setOnClickListener(v -> {
            listener.onInput(input.getText().toString());
            dialog.dismiss();
        });

        dialog.show();
        
        // Make input dialog wider
        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(window.getAttributes());
            lp.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
            window.setAttributes(lp);
        }
    }

    private void showModernAccountSelectionDialog(String title, ModernSelectionListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.ModernDialog);
        View view = getLayoutInflater().inflate(R.layout.dialog_modern_selection, null);
        builder.setView(view);

        TextView titleView = view.findViewById(R.id.dialog_title);
        titleView.setText(title);
        
        RecyclerView rv = view.findViewById(R.id.dialog_recycler);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        
        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        rv.setAdapter(new ModernAccountAdapter(myBankAccount, pos -> {
            listener.onSelected(pos);
            dialog.dismiss();
        }));

        dialog.show();
        
        // Make selection dialog wider
        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(window.getAttributes());
            lp.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
            window.setAttributes(lp);
        }
    }

    private void showModernSendMoneySelection() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.ModernDialog);
        View view = getLayoutInflater().inflate(R.layout.dialog_modern_transfer_type, null);
        builder.setView(view);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        view.findViewById(R.id.btn_transfer_internal).setOnClickListener(v -> {
            dialog.dismiss();
            transferBetweenOwn();
        });
        view.findViewById(R.id.btn_transfer_external).setOnClickListener(v -> {
            dialog.dismiss();
            transferToAnother();
        });

        dialog.show();
        
        // Make transfer type dialog wider
        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(window.getAttributes());
            lp.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
            window.setAttributes(lp);
        }
    }

    private void showModernMessageDialog(String title, String message) {
        new AlertDialog.Builder(getContext(), R.style.ModernDialog)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    private void updateBankAccountList() {
        MyBankAccountAdapter adapter = new MyBankAccountAdapter(myBankAccount, getActivity());
        recyclerViewbankaccount.setAdapter(adapter);
        setTotalMoney(myBankAccount);
    }

    private void updateCreditCardList() {
        MyCreditCardAdapter adapter = new MyCreditCardAdapter(myCreditCard, getActivity(), myBankAccount, recyclerViewbankaccount);
        recyclerView.setAdapter(adapter);
    }

    private void recordHistory(String process) {
        History hs = new History(mainUser.getId(), process, Calendar.getInstance().getTime());
        mainUser.getHistory().push(hs);
        historyToDatabase(hs);
        setupCarousel();
    }

    private void transferBetweenOwn() {
        showModernAccountSelectionDialog("Source Account", from -> {
            showModernAccountSelectionDialog("Destination Account", to -> {
                showModernInputDialog("Transfer Amount", "Enter amount to move.", "TRANSFER", amountStr -> {
                    try {
                        int amount = Integer.parseInt(amountStr);
                        if (amount <= myBankAccount.get(from).getCash()) {
                            myBankAccount.get(to).setCash(myBankAccount.get(to).getCash() + amount);
                            myBankAccount.get(from).setCash(myBankAccount.get(from).getCash() - amount);
                            updateBankAccount(myBankAccount.get(from));
                            updateBankAccount(myBankAccount.get(to));
                            updateBankAccountList();
                            recordHistory("Internal Transfer: ₹" + amount);
                        } else {
                            Toast.makeText(getApplicationContext(), "Insufficient funds", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {}
                });
            });
        });
    }

    private void transferToAnother() {
        showModernInputDialog("External Transfer", "Enter recipient account number.", "CONTINUE", accNo -> {
            ParseQuery<ParseObject> query = ParseQuery.getQuery("BankAccount");
            query.whereEqualTo("accountNo", accNo);
            query.findInBackground((objects, e) -> {
                if (e == null && objects.size() > 0) {
                    ParseObject obj = objects.get(0);
                    String recipientId = obj.getString("userId");
                    int recipientCash = Integer.parseInt(obj.getString("cash"));
                    
                    showModernAccountSelectionDialog("Select Source Account", from -> {
                        showModernInputDialog("Enter Amount", "How much to send to " + accNo + "?", "SEND", amtStr -> {
                            try {
                                int amount = Integer.parseInt(amtStr);
                                if (amount <= myBankAccount.get(from).getCash()) {
                                    myBankAccount.get(from).setCash(myBankAccount.get(from).getCash() - amount);
                                    updateBankAccount(myBankAccount.get(from));
                                    
                                    BankAccount recipientAcc = new BankAccount(accNo, recipientCash + amount);
                                    updateBankAccountAnotherUser(recipientAcc, recipientId);
                                    
                                    updateBankAccountList();
                                    recordHistory("Sent ₹" + amount + " to " + accNo);
                                    Toast.makeText(getApplicationContext(), "Sent Successfully", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getApplicationContext(), "Insufficient funds", Toast.LENGTH_SHORT).show();
                                }
                            } catch (Exception ex) {}
                        });
                    });
                } else {
                    Toast.makeText(getApplicationContext(), "Invalid Account Number", Toast.LENGTH_SHORT).show();
                }
            });
        });
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

    interface ModernInputListener { void onInput(String input); }
    interface ModernSelectionListener { void onSelected(int position); }

    private class ModernAccountAdapter extends RecyclerView.Adapter<ModernAccountAdapter.Holder> {
        List<BankAccount> list;
        ModernSelectionListener listener;
        ModernAccountAdapter(List<BankAccount> list, ModernSelectionListener listener) { this.list = list; this.listener = listener; }
        @NonNull @Override public Holder onCreateViewHolder(@NonNull ViewGroup p, int t) {
            View v = getLayoutInflater().inflate(R.layout.item_modern_selection, p, false);
            return new Holder(v);
        }
        @Override public void onBindViewHolder(@NonNull Holder h, int p) {
            BankAccount acc = list.get(p);
            h.t1.setText(acc.getAccountno());
            h.t2.setText("Balance: ₹" + acc.getCash());
            h.itemView.setOnClickListener(v -> listener.onSelected(p));
        }
        @Override public int getItemCount() { return list.size(); }
        class Holder extends RecyclerView.ViewHolder {
            TextView t1, t2;
            Holder(View v) { super(v); t1 = v.findViewById(R.id.text1); t2 = v.findViewById(R.id.text2); }
        }
    }
}
