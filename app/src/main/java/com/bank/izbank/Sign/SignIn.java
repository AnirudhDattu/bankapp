package com.bank.izbank.Sign;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.bank.izbank.Bill.Bill;
import com.bank.izbank.Bill.Date;
import com.bank.izbank.Credit.Credit;
import com.bank.izbank.Job.Job;
import com.bank.izbank.MainScreen.AdminPanelActivity;
import com.bank.izbank.R;
import com.bank.izbank.UserInfo.Address;
import com.bank.izbank.UserInfo.BankAccount;
import com.bank.izbank.UserInfo.CreditCard;
import com.bank.izbank.UserInfo.History;
import com.bank.izbank.UserInfo.User;
import com.bank.izbank.splashScreen.splashScreen;
import com.parse.FindCallback;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class SignIn extends AppCompatActivity {
    private EditText userName,userPass;
    public static User mainUser;
    public static ArrayList<User> allUsers;
    private String billType;
    private String billAmount;
    private String billDate;
    private ArrayList<Bill> bills;
    private ArrayList<BankAccount> bankAccounts;
    private Stack<History> history;
    private ArrayList<CreditCard> creditCards;
    private String bankCash,bankAccountNo;
    private String cardNo, cardLimit;
    private Intent intent ;
    private ArrayList<Credit> credits;
    private String creditAmount;
    private String creditInstallment;
    private String creditInterestRate;
    private String creditPayAmount;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
           setContentView(R.layout.activity_sign_in);//load screen
           userName=findViewById(R.id.edittext_id_number_sign_in);
           userPass=findViewById(R.id.edittext_user_password_sign_in);
    }
    public void signUp(View view){
        Intent signUp=new Intent(SignIn.this, SignUpActivity.class);
        startActivity(signUp);

    }


    public void getUserBills(){
        ParseQuery<ParseObject> queryBill=ParseQuery.getQuery("Bill");
        queryBill.whereEqualTo("username",SignIn.mainUser.getId());
        queryBill.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e!=null){
                    e.printStackTrace();
                }else{
                    bills = new ArrayList<>();
                    if(objects.size()>0){
                        for(ParseObject object:objects){

                            billType=object.getString("type");
                            billAmount=object.getString("amount");
                            billDate=object.getString("date");

                            String [] date = billDate.split("/");
                            if (date.length == 3) {
                                Date tempdate = new Date(date[0], date[1], date[2]);
                                Bill tempBill = new Bill(billType, Integer.parseInt(billAmount), tempdate);
                                bills.add(tempBill);
                            }
                        }
                    }
                }
            }
        });
    }

    public void getUserCredits(){
        ParseQuery<ParseObject> queryCredit=ParseQuery.getQuery("Credit");
        queryCredit.whereEqualTo("username",SignIn.mainUser.getId());
        queryCredit.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e!=null){
                    e.printStackTrace();
                }else{
                    credits = new ArrayList<>();
                    if(objects.size()>0){
                        for(ParseObject object:objects){

                            creditAmount=object.getString("amount");
                            creditInstallment=object.getString("installment");
                            creditInterestRate=object.getString("interestRate");
                            creditPayAmount=object.getString("payAmount");

                            Credit tempCredit = new Credit(Integer.parseInt(creditAmount),Integer.parseInt(creditInstallment),Integer.parseInt(creditInterestRate),Integer.parseInt(creditPayAmount));

                            credits.add(tempCredit);
                        }
                    }
                }
            }
        });
    }

    public void getBankAccounts(User user){
        ParseQuery<ParseObject> queryBankAccount=ParseQuery.getQuery("BankAccount");
        queryBankAccount.whereEqualTo("userId",user.getId());
        queryBankAccount.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e!=null){
                    e.printStackTrace();
                }else{
                    bankAccounts = new ArrayList<>();
                    if(objects.size()>0){
                        for(ParseObject object:objects){

                            bankCash=object.getString("cash");
                            bankAccountNo=object.getString("accountNo");

                            BankAccount tempAccount = new BankAccount(bankAccountNo,Integer.parseInt(bankCash));

                            bankAccounts.add(tempAccount);
                        }
                        user.setBankAccounts(bankAccounts);
                    }
                }
            }
        });
    }

    public void getCreditCards(User user){
        ParseQuery<ParseObject> queryCreditCard=ParseQuery.getQuery("CreditCard");
        queryCreditCard.whereEqualTo("userId",user.getId());
        queryCreditCard.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e!=null){
                    e.printStackTrace();
                }else{
                    creditCards = new ArrayList<>();
                    if(objects.size()>0){
                        for(ParseObject object:objects){

                            cardNo=object.getString("creditCardNo");
                            cardLimit=object.getString("limit");

                            CreditCard tempCard = new CreditCard(cardNo,Integer.parseInt(cardLimit));

                            creditCards.add(tempCard);
                        }
                        user.setCreditCards(creditCards);
                    }
                }
            }
        });
    }

    public void getHistory(){
        ParseQuery<ParseObject> queryHistory=ParseQuery.getQuery("History");
        queryHistory.whereEqualTo("userId",SignIn.mainUser.getId());
        queryHistory.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e!=null){
                    e.printStackTrace();
                }else{
                    history = new Stack<>();
                    if(objects.size()>0){
                        for(ParseObject object:objects){

                            String historyProcess=object.getString("process");
                            java.util.Date historyDate=object.getDate("date");

                            History tempHistory = new History(SignIn.mainUser.getId(), historyProcess, historyDate);

                            history.push(tempHistory);
                        }
                        SignIn.mainUser.setHistory(history);
                    }
                }
            }
        });
    }

    public void signIn(View view){
        //loading screen

        ParseUser.logInInBackground(userName.getText().toString(), userPass.getText().toString(), new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                if(e !=null){
                    Toast.makeText(getApplicationContext(),e.getLocalizedMessage(),Toast.LENGTH_LONG).show();
                }else{

                    ParseQuery<ParseObject> query=ParseQuery.getQuery("UserInfo");
                    query.whereEqualTo("username",userName.getText().toString());
                    query.findInBackground(new FindCallback<ParseObject>() {
                        @Override
                        public void done(List<ParseObject> objects, ParseException e) {
                            if(e!=null){
                                e.printStackTrace();
                            }else{

                                if(objects.size()>0){
                                    for(ParseObject object:objects){

                                        String name=object.getString("userRealName");
                                        String phone=object.getString("phone");
                                        String userId=object.getString("username");
                                        String address_string= object.getString("address");
                                        String[] str = address_string.split(" ");
                                        Address address = new Address(str[0],str[1],Integer.parseInt(str[2]),Integer.parseInt(str[3]),Integer.parseInt(str[4]),str[5],str[6],str[7]);
                                        String jobName = object.getString("job");
                                        String maxCreditAmount = object.getString("maxCreditAmount");
                                        String maxCreditInstallment = object.getString("maxCreditInstallment");
                                        String interestRate = object.getString("interestRate");

                                        Job tempJob = new Job(jobName,maxCreditAmount,maxCreditInstallment,interestRate);

                                        mainUser = new User(name,userId, phone,address,tempJob);
                                        ParseFile parseFile=(ParseFile)object.get("images");
                                       if( parseFile!=null){
                                           parseFile.getDataInBackground((data, e1) -> {
                                               if(data!=null && e1 ==null){
                                                   Bitmap downloadedImage= BitmapFactory.decodeByteArray(data,0,data.length);
                                                   mainUser.setPhoto(downloadedImage);

                                               }
                                           });
                                       }


                                        Toast.makeText(getApplicationContext(),"Welcome "+name,Toast.LENGTH_LONG).show();

                                        getBankAccounts(mainUser);
                                        getCreditCards(mainUser);
                                        getHistory();
                                        getUserBills();
                                        getUserCredits();

                                        // Start splashScreen AFTER initializing data
                                        intent = new Intent(SignIn.this, splashScreen.class);
                                        startActivity(intent);
                                        finish(); // Finish SignIn activity so user can't go back to it
                                    }
                                } else {
                                    Toast.makeText(getApplicationContext(), "User info not found", Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                    });

                }
            }
        });
    }

    public void adminPanel(View view){
        Intent intent = new Intent(SignIn.this, AdminPanelActivity.class);
        startActivity(intent);
    }

}
