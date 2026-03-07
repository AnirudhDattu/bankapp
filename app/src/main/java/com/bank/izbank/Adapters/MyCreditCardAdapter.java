package com.bank.izbank.Adapters;

import android.app.Activity;
import android.content.DialogInterface;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bank.izbank.R;
import com.bank.izbank.Sign.SignIn;
import com.bank.izbank.UserInfo.BankAccount;
import com.bank.izbank.UserInfo.CreditCard;
import com.bank.izbank.UserInfo.History;
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

import static com.bank.izbank.Sign.SignIn.mainUser;
import static com.parse.Parse.getApplicationContext;

public class MyCreditCardAdapter extends RecyclerView.Adapter<MyCreditCardAdapter.ViewHolder> {
    ArrayList<BankAccount> MyBankAccounts;
    ArrayList<CreditCard> MyCreditCards;
    Activity context;
    RecyclerView recyclerViewbankaccount;

    public MyCreditCardAdapter(ArrayList<CreditCard> myCreditCardData,Activity activity,ArrayList<BankAccount> MyBankAccounts,RecyclerView recyclerViewbankaccount) {
        this.MyCreditCards = myCreditCardData;
        this.context = activity;
        this.MyBankAccounts = MyBankAccounts;
        this.recyclerViewbankaccount = recyclerViewbankaccount;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.credit_car_cardview,parent,false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @NonNull


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final CreditCard creditCard = MyCreditCards.get(position);
        
        // Fix alignment: Format card number with spaces every 4 digits
        String rawNo = creditCard.getCreditCardNo();
        if (rawNo != null && rawNo.length() == 16) {
            String formatted = rawNo.substring(0, 4) + " " + rawNo.substring(4, 8) + " " + rawNo.substring(8, 12) + " " + rawNo.substring(12, 16);
            holder.textCreditCardNo.setText(formatted);
        } else {
            holder.textCreditCardNo.setText(rawNo);
        }

        holder.textCreditCardLimit.setText("₹" + String.valueOf(creditCard.getLimit()));
        
        // Show real user name
        if (SignIn.mainUser != null) {
            holder.textCardHolderName.setText(SignIn.mainUser.getName().toUpperCase());
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MyBankAccounts.size()==0){
                    AlertDialog.Builder ad = new AlertDialog.Builder(context);
                    ad.setTitle("You dont have any bank account. Please add one before pay off credit card debt.");
                    ad.setNegativeButton("CLOSE", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogInterface, int i){

                        }
                    });
                    ad.create().show();
                }
                else{
                    final EditText editText = new EditText(context);
                    editText.setHint("How much do you want to pay?");
                    editText.setInputType(InputType.TYPE_CLASS_NUMBER);

                    AlertDialog.Builder ad = new AlertDialog.Builder(context);

                    ad.setTitle("Which Bank Account Do You Want to Pay with?");
                    ad.setIcon(R.drawable.icon_credit_card);
                    ad.setView(editText);
                    String[] items = new String[MyBankAccounts.size()];
                    for (int i =0; i<MyBankAccounts.size();i++){
                        String data= MyBankAccounts.get(i).getAccountno() + "  ₹" + Integer.toString(MyBankAccounts.get(i).getCash());
                        items[i] = data;
                    }
                    final int[] checkedItem = {0};
                    ad.setSingleChoiceItems(items, checkedItem[0], new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            checkedItem[0] = i;
                        }
                    });
                    ad.setNegativeButton("Pay", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            i= checkedItem[0];
                            try {
                                int payAmount = Integer.parseInt(editText.getText().toString());
                                if (payAmount > 0) {
                                    MyCreditCards.get(position).setLimit(MyCreditCards.get(position).getLimit() + payAmount);
                                    holder.textCreditCardLimit.setText("₹" + String.valueOf(creditCard.getLimit()));
                                    MyBankAccounts.get(i).setCash(MyBankAccounts.get(i).getCash() - payAmount);
                                    updateBankAccount(MyBankAccounts.get(i));
                                    updateCreditCards(MyCreditCards.get(position));
                                    setTotalMoney(MyBankAccounts);
                                    MyBankAccountAdapter myBankAccountAdapter = new MyBankAccountAdapter(MyBankAccounts,context );
                                    recyclerViewbankaccount.setAdapter(myBankAccountAdapter);
                                    History hs = new History(mainUser.getId(),"Credit Card Paid: ₹" + payAmount, getDate() );
                                    mainUser.getHistory().push(hs);
                                    historyToDatabase(hs);
                                }
                            } catch (NumberFormatException e) {
                                Toast.makeText(context, "Invalid amount", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    ad.create().show();
                }

            }
        });

    }

    @Override
    public int getItemCount() {
        return MyCreditCards.size();
    }
    public void setTotalMoney(ArrayList<BankAccount> MyBankAccounts){
        // Total money UI update is handled by AccountFragment's setupCarousel()
    }

    public void accountsToDatabase(BankAccount bankAc){
        ParseObject object=new ParseObject("BankAccount");
        object.put("accountNo",bankAc.getAccountno());
        object.put("userId", SignIn.mainUser.getId());
        object.put("cash",String.valueOf(bankAc.getCash()));

        object.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e != null){
                    Toast.makeText(getApplicationContext(),e.getLocalizedMessage().toString(),Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void cardsToDatabase(CreditCard card){
        ParseObject object=new ParseObject("CreditCard");
        object.put("creditCardNo",card.getCreditCardNo());
        object.put("userId", SignIn.mainUser.getId());
        object.put("limit",String.valueOf(card.getLimit()));

        object.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e != null){
                    Toast.makeText(getApplicationContext(),e.getLocalizedMessage().toString(),Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void updateCreditCards(CreditCard card){
        ParseQuery<ParseObject> query = ParseQuery.getQuery("CreditCard");
        query.whereEqualTo("creditCardNo", card.getCreditCardNo());
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e == null && objects != null && objects.size() > 0){
                    for(ParseObject object : objects){
                        object.deleteInBackground();
                        cardsToDatabase(card);
                    }
                }
            }
        });
    }

    public void updateBankAccount(BankAccount bankac){
        ParseQuery<ParseObject> query = ParseQuery.getQuery("BankAccount");
        query.whereEqualTo("accountNo", bankac.getAccountno());
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e == null && objects != null && objects.size() > 0){
                    for(ParseObject object : objects){
                        object.deleteInBackground();
                        accountsToDatabase(bankac);
                    }
                }
            }
        });
    }


    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView textCreditCardLimit;
        TextView textCreditCardNo;
        TextView textCardHolderName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textCreditCardLimit = itemView.findViewById(R.id.text_view_credit_card_limit);
            textCreditCardNo = itemView.findViewById(R.id.text_view_credit_card_no);
            textCardHolderName = itemView.findViewById(R.id.text_view_card_holder_name);
        }
    }
    
    public void historyToDatabase(History history){
        ParseObject object=new ParseObject("History");
        object.put("process",history.getProcess());
        object.put("userId", mainUser.getId());
        object.put("date",history.getDateDate());

        object.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e != null){
                    Toast.makeText(getApplicationContext(),e.getLocalizedMessage().toString(),Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public Date getDate(){
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        Date currentTime = Calendar.getInstance().getTime();
        return currentTime;
    }

}
