package com.bank.izbank.MainScreen;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
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
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

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
    ImageView add_bank_account, add_credit_card, image_view_profile_small;
    RecyclerView recyclerView;
    RecyclerView recyclerViewbankaccount, recyclerViewHistory;
    TextView text_view_name, date;
    ArrayList<CreditCard> myCreditCard;
    ArrayList<BankAccount> myBankAccount;
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
        refreshDataFromServer();
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshDataFromServer();
    }

    private void refreshDataFromServer() {
        if (mainUser == null) return;

        // Fetch latest Bank Accounts from DB
        ParseQuery<ParseObject> queryBank = ParseQuery.getQuery("BankAccount");
        queryBank.whereEqualTo("userId", mainUser.getId());
        queryBank.findInBackground((objects, e) -> {
            if (e == null && objects != null) {
                ArrayList<BankAccount> accounts = new ArrayList<>();
                for (ParseObject obj : objects) {
                    accounts.add(new BankAccount(
                            obj.getString("accountNo"),
                            Integer.parseInt(obj.getString("cash")),
                            obj.getString("upiId")
                    ));
                }
                mainUser.setBankAccounts(accounts);
                updateUI();
            }
        });

        // Fetch latest History from DB
        ParseQuery<ParseObject> queryHistory = ParseQuery.getQuery("History");
        queryHistory.whereEqualTo("userId", mainUser.getId());
        queryHistory.orderByDescending("date");
        queryHistory.findInBackground((objects, e) -> {
            if (e == null && objects != null) {
                Stack<History> historyStack = new Stack<>();
                for (ParseObject obj : objects) {
                    historyStack.push(new History(
                            mainUser.getId(),
                            obj.getString("process"),
                            obj.getDate("date"),
                            obj.getBoolean("isIncome")
                    ));
                }
                mainUser.setHistory(historyStack);
                updateUI();
            }
        });
        
        // Refresh User Info (for Profile Photo)
        ParseQuery<ParseObject> queryInfo = ParseQuery.getQuery("UserInfo");
        queryInfo.whereEqualTo("username", mainUser.getId());
        queryInfo.findInBackground((objects, e) -> {
            if (e == null && !objects.isEmpty()) {
                com.parse.ParseFile file = (com.parse.ParseFile) objects.get(0).get("images");
                if (file != null) {
                    file.getDataInBackground((data, e1) -> {
                        if (e1 == null && data != null) {
                            android.graphics.Bitmap bmp = android.graphics.BitmapFactory.decodeByteArray(data, 0, data.length);
                            mainUser.setPhoto(bmp);
                            if (image_view_profile_small != null) {
                                image_view_profile_small.setImageBitmap(bmp);
                            }
                        }
                    });
                }
            }
        });
    }

    private void updateUI() {
        if (!isAdded() || getView() == null) return;
        
        myCreditCard = mainUser.getCreditCards();
        myBankAccount = mainUser.getBankAccounts();

        define();
        setDate();
        click();
        setupCarousel();

        text_view_name.setText("Hello, " + mainUser.getName());
        
        if (mainUser.getPhoto() != null) {
            image_view_profile_small.setImageBitmap(mainUser.getPhoto());
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerViewbankaccount.setLayoutManager(new LinearLayoutManager(getActivity()));

        MyCreditCardAdapter myCreditCardAdapter = new MyCreditCardAdapter(myCreditCard, getActivity(), myBankAccount, recyclerViewbankaccount);
        recyclerView.setAdapter(myCreditCardAdapter);

        MyBankAccountAdapter myBankAccountAdapter = new MyBankAccountAdapter(myBankAccount, getActivity());
        recyclerViewbankaccount.setAdapter(myBankAccountAdapter);
    }

    public void define(){
        if (getView() == null) return;
        text_view_name = getView().findViewById(R.id.text_view_name);
        date = getView().findViewById(R.id.text_view_date_main);
        recyclerView = getView().findViewById(R.id.recyclerview_credit_card);
        recyclerViewbankaccount = getView().findViewById(R.id.recyclerview_bank_account);
        add_bank_account = getView().findViewById(R.id.image_view_add_bank_account);
        add_credit_card = getView().findViewById(R.id.image_view_add_credit_card);
        linear_layout_request_money = getView().findViewById(R.id.linear_layout_request_money);
        linear_layout_send_money = getView().findViewById(R.id.linear_layout_send_money);
        linear_layout_history = getView().findViewById(R.id.linear_layout_history);
        image_view_profile_small = getView().findViewById(R.id.image_view_profile_small);
        
        viewPagerAnalysis = getView().findViewById(R.id.viewPager_analysis);
        tabLayoutIndicator = getView().findViewById(R.id.tabLayout_indicator);
    }

    private void setupCarousel() {
        if (viewPagerAnalysis == null) return;

        List<AnalysisCarouselAdapter.AnalysisPage> pages = new ArrayList<>();
        ArrayList<History> fullHistory = stackToArrayList(mainUser.getHistory());
        
        AnalysisCarouselAdapter.AnalysisPage summaryPage = new AnalysisCarouselAdapter.AnalysisPage("Net Summary", 2);
        int cash = 0;
        if (myBankAccount != null) {
            for (BankAccount acc : myBankAccount) cash += acc.getCash();
        }
        summaryPage.mainStat = String.format("₹%,d", cash);
        summaryPage.statLabel = "Total Account Value";
        pages.add(summaryPage);

        carouselAdapter = new AnalysisCarouselAdapter(pages);
        viewPagerAnalysis.setAdapter(carouselAdapter);
    }

    public void click(){
        if (linear_layout_history == null) return;
        
        linear_layout_history.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), android.R.style.Theme_Material_Light_NoActionBar_Fullscreen);
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.history_popup, null);
            builder.setView(dialogView);
            AlertDialog dialog = builder.create();
            dialogView.findViewById(R.id.btn_close_history).setOnClickListener(view -> dialog.dismiss());
            recyclerViewHistory = dialogView.findViewById(R.id.history_recycler_view);
            recyclerViewHistory.setLayoutManager(new LinearLayoutManager(getActivity()));
            historyAdapter = new HistoryAdapter(stackToArrayList(mainUser.getHistory()), getActivity(), getContext());
            recyclerViewHistory.setAdapter(historyAdapter);
            dialog.show();
        });

        add_bank_account.setOnClickListener(v -> {
            showModernInputDialog("Initial Deposit", "Enter amount for new account (Min: ₹1000).", "ADD", amountStr -> {
                int amount = amountStr.isEmpty() ? 0 : Integer.parseInt(amountStr);
                if (amount < 1000) {
                    Toast.makeText(getContext(), "Can't create account below min balance (₹1000)", Toast.LENGTH_LONG).show();
                } else {
                    BankAccount newAcc = new BankAccount(amount);
                    myBankAccount.add(newAcc);
                    saveBankAccountToDb(newAcc);
                    updateUI();
                    Toast.makeText(getContext(), "Account created successfully!", Toast.LENGTH_SHORT).show();
                }
            });
        });

        image_view_profile_small.setOnClickListener(v -> {
            if (getActivity() != null) {
                BottomNavigationView bottomNavigationView = getActivity().findViewById(R.id.bottom_navigation);
                if (bottomNavigationView != null) {
                    bottomNavigationView.setSelectedItemId(R.id.menu5);
                }
            }
        });

        linear_layout_send_money.setOnClickListener(v -> {
            if (getActivity() != null) {
                BottomNavigationView bottomNavigationView = getActivity().findViewById(R.id.bottom_navigation);
                if (bottomNavigationView != null) {
                    bottomNavigationView.setSelectedItemId(R.id.menu_upi);
                }
            }
        });
        
        linear_layout_request_money.setOnClickListener(v -> {
             if (getActivity() != null) {
                BottomNavigationView bottomNavigationView = getActivity().findViewById(R.id.bottom_navigation);
                if (bottomNavigationView != null) {
                    bottomNavigationView.setSelectedItemId(R.id.menu_upi);
                    // Optionally trigger a specific state in UPIFragment if needed
                }
            }
        });
    }

    private void saveBankAccountToDb(BankAccount acc) {
        ParseObject obj = new ParseObject("BankAccount");
        obj.put("accountNo", acc.getAccountno());
        obj.put("userId", mainUser.getId());
        obj.put("cash", String.valueOf(acc.getCash()));
        obj.put("upiId", acc.getUpiId());
        obj.saveInBackground();
    }

    private void showModernInputDialog(String title, String message, String actionText, ModernInputListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.ModernDialog);
        View view = getLayoutInflater().inflate(R.layout.dialog_modern_input, null);
        builder.setView(view);
        ((TextView)view.findViewById(R.id.dialog_title)).setText(title);
        ((TextView)view.findViewById(R.id.dialog_message)).setText(message);
        EditText input = view.findViewById(R.id.dialog_input);
        MaterialButton btn = view.findViewById(R.id.dialog_btn_action);
        btn.setText(actionText);
        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        btn.setOnClickListener(v -> {
            listener.onInput(input.getText().toString());
            dialog.dismiss();
        });
        dialog.show();
    }

    public ArrayList<History> stackToArrayList(Stack<History> stack){
        ArrayList<History> list = new ArrayList<>();
        Stack<History> temp = new Stack<>();
        while (!stack.isEmpty()) {
            History h = stack.pop();
            list.add(h);
            temp.push(h);
        }
        while (!temp.isEmpty()) stack.push(temp.pop());
        return list;
    }

    public void setDate(){
        if (date == null) return;
        date.setText(new SimpleDateFormat("EEEE, dd MMMM").format(new Date()));
    }

    interface ModernInputListener { void onInput(String input); }
}
