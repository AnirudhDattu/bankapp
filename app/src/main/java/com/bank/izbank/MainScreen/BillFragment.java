package com.bank.izbank.MainScreen;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import com.bank.izbank.Bill.Bill;
import com.bank.izbank.Adapters.BillAdapter;
import com.bank.izbank.Bill.ElectricBill;
import com.bank.izbank.Bill.GasBill;
import com.bank.izbank.Bill.InternetBill;
import com.bank.izbank.Bill.PhoneBill;
import com.bank.izbank.Bill.WaterBill;
import com.bank.izbank.R;
import com.bank.izbank.Sign.SignIn;
import com.bank.izbank.UserInfo.BankAccount;
import com.bank.izbank.UserInfo.History;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
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

public class BillFragment extends Fragment{

    private RecyclerView recyclerView;
    private ArrayList<Bill> list;
    private BillAdapter billAdapter;
    private ExtendedFloatingActionButton floatingActionButtonBill;
    private TextView textViewDate;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_4,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        floatingActionButtonBill = view.findViewById(R.id.floatingActionButton_bill);
        textViewDate = view.findViewById(R.id.text_view_date_bill);
        recyclerView = view.findViewById(R.id.recyclerView_bill);

        setDate();

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        list = SignIn.mainUser.getUserbills();
        if (list == null) list = new ArrayList<>();

        billAdapter = new BillAdapter(getContext(),list);
        recyclerView.setAdapter(billAdapter);

        floatingActionButtonBill.setOnClickListener(v -> showBillPopup());
    }

    private void setDate() {
        SimpleDateFormat format = new SimpleDateFormat("EEEE, dd MMMM");
        Date currentTime = Calendar.getInstance().getTime();
        textViewDate.setText(format.format(currentTime));
    }

    private void showBillPopup() {
        if (SignIn.mainUser.getBankAccounts() == null || SignIn.mainUser.getBankAccounts().isEmpty()) {
            Toast.makeText(getContext(), "You need a bank account to pay bills.", Toast.LENGTH_LONG).show();
            return;
        }

        AlertDialog.Builder ad = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.bill_payment_popup, null);
        ad.setView(dialogView);

        Spinner spinnerBillType = dialogView.findViewById(R.id.spinner_bill_type);
        EditText editTextAmount = dialogView.findViewById(R.id.editText_bill_amount);
        Spinner spinnerSourceAccount = dialogView.findViewById(R.id.spinner_source_account);

        // Populate Bill Types
        String[] billTypes = {"Electric", "Gas", "Internet", "Phone", "Water"};
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, billTypes);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBillType.setAdapter(typeAdapter);

        // Populate Bank Accounts
        ArrayList<String> accountNumbers = new ArrayList<>();
        for (BankAccount acc : SignIn.mainUser.getBankAccounts()) {
            accountNumbers.add(acc.getAccountno() + " (₹" + acc.getCash() + ")");
        }
        ArrayAdapter<String> accAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, accountNumbers);
        accAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSourceAccount.setAdapter(accAdapter);

        ad.setPositiveButton("Pay Now", (dialog, which) -> {
            String amountStr = editTextAmount.getText().toString();
            if (!amountStr.isEmpty()) {
                int amount = Integer.parseInt(amountStr);
                int typeIndex = spinnerBillType.getSelectedItemPosition();
                int accountIndex = spinnerSourceAccount.getSelectedItemPosition();
                
                processBillPayment(typeIndex, amount, accountIndex);
            } else {
                Toast.makeText(getContext(), "Please enter an amount.", Toast.LENGTH_SHORT).show();
            }
        });

        ad.setNegativeButton("Cancel", null);
        ad.create().show();
    }

    private void processBillPayment(int typeIndex, int amount, int accountIndex) {
        BankAccount selectedAccount = SignIn.mainUser.getBankAccounts().get(accountIndex);

        if (amount <= selectedAccount.getCash()) {
            // Deduct money
            selectedAccount.setCash(selectedAccount.getCash() - amount);
            updateBankAccount(selectedAccount);

            // Create Bill Object
            Bill bill;
            switch (typeIndex) {
                case 0: bill = new ElectricBill(); break;
                case 1: bill = new GasBill(); break;
                case 2: bill = new InternetBill(); break;
                case 3: bill = new PhoneBill(); break;
                default: bill = new WaterBill(); break;
            }
            bill.setAmount(amount);
            setBillDate(bill);

            // Save to DB and UI
            billToDatabase(bill);
            list.add(bill);
            billAdapter.notifyDataSetChanged();

            Toast.makeText(getContext(), bill.getType() + " Bill Paid!", Toast.LENGTH_LONG).show();

            // History
            History hs = new History(mainUser.getId(), "Paid " + bill.getType() + " Bill: ₹" + amount, Calendar.getInstance().getTime());
            mainUser.getHistory().push(hs);
            historyToDatabase(hs);
        } else {
            Toast.makeText(getContext(), "Insufficient funds in selected account!", Toast.LENGTH_LONG).show();
        }
    }

    private void setBillDate(Bill newBill){
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        String str = format.format(Calendar.getInstance().getTime());
        String[] dateParts = str.split("/");
        if (newBill.getDate() == null) newBill.setDate(new com.bank.izbank.Bill.Date(dateParts[0], dateParts[1], dateParts[2]));
        else {
            newBill.getDate().setDay(dateParts[0]);
            newBill.getDate().setMonth(dateParts[1]);
            newBill.getDate().setYear(dateParts[2]);
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

    private void billToDatabase(Bill bill){
        ParseObject object=new ParseObject("Bill");
        object.put("type",bill.getType());
        object.put("username", SignIn.mainUser.getId());
        object.put("amount",String.valueOf(bill.getAmount()));
        object.put("date",bill.getDate().getDay()+"/"+bill.getDate().getMonth()+"/"+bill.getDate().getYear());
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
