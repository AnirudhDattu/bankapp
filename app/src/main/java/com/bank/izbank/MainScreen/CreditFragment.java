package com.bank.izbank.MainScreen;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bank.izbank.Credit.Credit;
import com.bank.izbank.Adapters.CreditAdapter;
import com.bank.izbank.Credit.CustomEventListener;
import com.bank.izbank.R;
import com.bank.izbank.Sign.SignIn;
import com.bank.izbank.UserInfo.BankAccount;
import com.bank.izbank.UserInfo.History;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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

import static com.bank.izbank.Sign.SignIn.mainUser;
import static com.parse.Parse.getApplicationContext;

public class CreditFragment extends Fragment {

    private RecyclerView recyclerViewCredit;
    private ArrayList<Credit> list;
    private CreditAdapter creditAdapter;
    private ExtendedFloatingActionButton floatingActionButtonCredit;
    private TextView textViewDate;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_2,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        floatingActionButtonCredit = view.findViewById(R.id.floatingActionButton_credit);
        textViewDate = view.findViewById(R.id.text_view_date_credit);
        recyclerViewCredit = view.findViewById(R.id.recyclerView_credit);

        setDate();

        recyclerViewCredit.setHasFixedSize(true);
        recyclerViewCredit.setLayoutManager(new LinearLayoutManager(getContext()));

        list = SignIn.mainUser.getCredits();
        if (list == null) list = new ArrayList<>();

        creditAdapter = new CreditAdapter(getContext(),list);
        creditAdapter.setListener(() -> {
            list = SignIn.mainUser.getCredits();
            creditAdapter = new CreditAdapter(getContext(),list);
            recyclerViewCredit.setAdapter(creditAdapter);
        });

        recyclerViewCredit.setAdapter(creditAdapter);

        floatingActionButtonCredit.setOnClickListener(v -> showCreditPopup());
    }

    private void setDate() {
        SimpleDateFormat format = new SimpleDateFormat("EEEE, dd MMMM");
        Date currentTime = Calendar.getInstance().getTime();
        textViewDate.setText(format.format(currentTime));
    }

    private void showCreditPopup() {
        if (SignIn.mainUser.getBankAccounts() == null || SignIn.mainUser.getBankAccounts().isEmpty()) {
            Toast.makeText(getContext(), "You need at least one bank account to receive a loan.", Toast.LENGTH_LONG).show();
            return;
        }

        AlertDialog.Builder creditPopup = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView= inflater.inflate(R.layout.credit_screen_credit_first_step_popup, null);
        creditPopup.setView(dialogView);
        
        EditText creditAmount = dialogView.findViewById(R.id.editText_credit_amount) ;
        TextView interestRate = dialogView.findViewById(R.id.textView_credit_interestRate);
        EditText installment = dialogView.findViewById(R.id.editText_credit_installment);

        TextView staticMaxAmount = dialogView.findViewById(R.id.textView_static_credit_max_amount);
        TextView staticMaxInstallment = dialogView.findViewById(R.id.textView_static_credit_max_installment);

        interestRate.setText("Your Interest Rate: %" + SignIn.mainUser.getJob().getInterestRate());
        staticMaxAmount.setText("₹" + SignIn.mainUser.getJob().getMaxCreditAmount());
        staticMaxInstallment.setText(SignIn.mainUser.getJob().getMaxCreditInstallment());

        creditPopup.setPositiveButton("Calculate", (dialog, which) -> {
            String amountStr = creditAmount.getText().toString();
            String installmentStr = installment.getText().toString();

            if (!amountStr.isEmpty() && !installmentStr.isEmpty()) {
                try {
                    int maxAmount = Integer.parseInt(SignIn.mainUser.getJob().getMaxCreditAmount());
                    int maxInstallment = Integer.parseInt(SignIn.mainUser.getJob().getMaxCreditInstallment());
                    int currentAmount = Integer.parseInt(amountStr);
                    int currentInstallment = Integer.parseInt(installmentStr);

                    if (currentAmount > 0 && currentAmount <= maxAmount && currentInstallment > 0 && currentInstallment <= maxInstallment) {
                        showConfirmPopup(currentAmount, currentInstallment);
                    } else {
                        Toast.makeText(getContext(), "Please enter valid values within limits!", Toast.LENGTH_SHORT).show();
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(getContext(), "Invalid input format.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "Fields cannot be empty!", Toast.LENGTH_SHORT).show();
            }
        });
        
        creditPopup.setNegativeButton("Cancel", null);
        creditPopup.create().show();
    }

    private void showConfirmPopup(int currentAmount, int currentInstallment) {
        AlertDialog.Builder creditPopupSecond = new AlertDialog.Builder(getContext());
        LayoutInflater inflaterSecond = getActivity().getLayoutInflater();
        View dialogViewSecond = inflaterSecond.inflate(R.layout.credit_screen_credit_second_step_popup, null);
        creditPopupSecond.setView(dialogViewSecond);

        TextView amountSecond = dialogViewSecond.findViewById(R.id.textView_taken_amount);
        TextView payAmountSecond = dialogViewSecond.findViewById(R.id.textView_pay_amount);
        TextView monthlyInstallmentSecond = dialogViewSecond.findViewById(R.id.textView_monthly_installment);
        Spinner spinnerAccounts = dialogViewSecond.findViewById(R.id.spinner_accounts);

        int interest = Integer.parseInt(SignIn.mainUser.getJob().getInterestRate());
        // Simple Interest Math: Total = Principal + (Principal * Rate * Time / 1200)
        int payTotal = currentAmount + ((currentAmount * interest) * currentInstallment) / 1200;
        int monthly = payTotal / currentInstallment;

        amountSecond.setText("₹" + currentAmount);
        payAmountSecond.setText("₹" + payTotal);
        monthlyInstallmentSecond.setText("₹" + monthly + "/mo");

        // Populate Spinner with Bank Accounts
        ArrayList<String> accountNumbers = new ArrayList<>();
        for (BankAccount acc : SignIn.mainUser.getBankAccounts()) {
            accountNumbers.add(acc.getAccountno() + " (Balance: ₹" + acc.getCash() + ")");
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, accountNumbers);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAccounts.setAdapter(adapter);

        creditPopupSecond.setPositiveButton("Confirm & Receive", (dialog, which) -> {
            int selectedIndex = spinnerAccounts.getSelectedItemPosition();
            Credit tempCredit = new Credit(currentAmount, currentInstallment, interest, payTotal);
            receiveCredit(tempCredit, selectedIndex);
        });
        
        creditPopupSecond.setNegativeButton("Back", (dialog, which) -> showCreditPopup());
        creditPopupSecond.create().show();
    }

    public void receiveCredit(Credit tempCredit, int accountIndex){
        if(accountIndex >= 0 && accountIndex < SignIn.mainUser.getBankAccounts().size()){
            BankAccount selectedAccount = SignIn.mainUser.getBankAccounts().get(accountIndex);
            selectedAccount.setCash(selectedAccount.getCash() + tempCredit.getAmount());
            updateBankAccount(selectedAccount);

            Toast.makeText(getContext(), "₹" + tempCredit.getAmount() + " deposited to account " + selectedAccount.getAccountno(), Toast.LENGTH_LONG).show();

            creditToDatabase(tempCredit);
            list.add(tempCredit);
            creditAdapter.notifyDataSetChanged();

            History hs = new History(mainUser.getId(),"Credit Received: ₹" + tempCredit.getAmount() + " to " + selectedAccount.getAccountno(), Calendar.getInstance().getTime());
            mainUser.getHistory().push(hs);
            historyToDatabase(hs);
        } else {
            Toast.makeText(getContext(), "Error: Invalid account selection.", Toast.LENGTH_LONG).show();
        }
    }

    private void updateBankAccount(BankAccount bankac){
        ParseQuery<ParseObject> queryBankAccount=ParseQuery.getQuery("BankAccount");
        queryBankAccount.whereEqualTo("accountNo", bankac.getAccountno());
        queryBankAccount.findInBackground((objects, e) -> {
            if(e == null && objects.size() > 0){
                for(ParseObject object : objects){
                    object.put("cash", String.valueOf(bankac.getCash()));
                    object.saveInBackground();
                }
            }
        });
    }

    private void creditToDatabase(Credit tempCredit){
        ParseObject object=new ParseObject("Credit");
        object.put("amount",String.valueOf(tempCredit.getAmount()));
        object.put("username", SignIn.mainUser.getId());
        object.put("installment",String.valueOf(tempCredit.getInstallment()));
        object.put("interestRate",String.valueOf(tempCredit.getInterestRate()));
        object.put("payAmount",String.valueOf(tempCredit.getPayAmount()));
        object.saveInBackground();
    }

    private void historyToDatabase(History history){
        ParseObject object=new ParseObject("History");
        object.put("process",history.getProcess());
        object.put("userId", mainUser.getId());
        object.put("date",history.getDateDate());
        object.saveInBackground();
    }
}
