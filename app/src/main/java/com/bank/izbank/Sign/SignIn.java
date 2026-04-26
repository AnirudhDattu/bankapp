package com.bank.izbank.Sign;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.bank.izbank.Bill.Bill;
import com.bank.izbank.Bill.Date;
import com.bank.izbank.Credit.Credit;
import com.bank.izbank.Job.Job;
import com.bank.izbank.MainScreen.AdminPanelActivity;
import com.bank.izbank.MainScreen.MainScreenActivity;
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
import java.util.Calendar;
import java.util.List;
import java.util.Stack;

public class SignIn extends AppCompatActivity {
    // --- SPECIAL ACCOUNTS CONFIGURATION ---
    private static final String ADMIN_ID = "9999";
    private static final String DEMO_ID = "0000";
    // --------------------------------------

    private EditText userName,userPass;
    public static User mainUser;
    public static ArrayList<User> allUsers = new ArrayList<>();
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
        if (SignIn.mainUser == null) return;
        ParseQuery<ParseObject> queryBill=ParseQuery.getQuery("Bill");
        queryBill.whereEqualTo("username",SignIn.mainUser.getId());
        queryBill.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e!=null){
                    e.printStackTrace();
                }else{
                    bills = new ArrayList<>();
                    if(objects != null && objects.size()>0){
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
                    SignIn.mainUser.setUserbills(bills);
                }
            }
        });
    }

    public void getUserCredits(){
        if (SignIn.mainUser == null) return;
        ParseQuery<ParseObject> queryCredit=ParseQuery.getQuery("Credit");
        queryCredit.whereEqualTo("username",SignIn.mainUser.getId());
        queryCredit.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e!=null){
                    e.printStackTrace();
                }else{
                    credits = new ArrayList<>();
                    if(objects != null && objects.size()>0){
                        for(ParseObject object:objects){

                            creditAmount=object.getString("amount");
                            creditInstallment=object.getString("installment");
                            creditInterestRate=object.getString("interestRate");
                            creditPayAmount=object.getString("payAmount");

                            Credit tempCredit = new Credit(Integer.parseInt(creditAmount),Integer.parseInt(creditInstallment),Integer.parseInt(creditInterestRate),Integer.parseInt(creditPayAmount));

                            credits.add(tempCredit);
                        }
                    }
                    SignIn.mainUser.setCredits(credits);
                }
            }
        });
    }

    public void getBankAccounts(User user){
        if (user == null) return;
        ParseQuery<ParseObject> queryBankAccount=ParseQuery.getQuery("BankAccount");
        queryBankAccount.whereEqualTo("userId",user.getId());
        queryBankAccount.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e!=null){
                    e.printStackTrace();
                }else{
                    ArrayList<BankAccount> accounts = new ArrayList<>();
                    if(objects != null && objects.size()>0){
                        for(ParseObject object:objects){

                            String cash=object.getString("cash");
                            String accountNo=object.getString("accountNo");
                            String upiId = object.getString("upiId");

                            BankAccount tempAccount = new BankAccount(accountNo,Integer.parseInt(cash), upiId, user.getId());
                            
                            // Protocol Enforcement: Auto-generate and save UPI ID if missing or inconsistent
                            String expectedUpiId = tempAccount.generateUpiId();
                            if (upiId == null || !upiId.equals(expectedUpiId)) {
                                tempAccount.setUpiId(expectedUpiId);
                                object.put("upiId", expectedUpiId);
                                object.saveInBackground();
                                Log.d("SignIn", "Healed UPI ID for " + user.getId() + " to " + expectedUpiId);
                            }

                            accounts.add(tempAccount);
                        }
                        user.setBankAccounts(accounts);
                    }
                }
            }
        });
    }

    public void getCreditCards(User user){
        if (user == null) return;
        ParseQuery<ParseObject> queryCreditCard=ParseQuery.getQuery("CreditCard");
        queryCreditCard.whereEqualTo("userId",user.getId());
        queryCreditCard.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e!=null){
                    e.printStackTrace();
                }else{
                    ArrayList<CreditCard> cards = new ArrayList<>();
                    if(objects != null && objects.size()>0){
                        for(ParseObject object:objects){

                            String cardNo=object.getString("creditCardNo");
                            String limit=object.getString("limit");

                            CreditCard tempCard = new CreditCard(cardNo,Integer.parseInt(limit));

                            cards.add(tempCard);
                        }
                        user.setCreditCards(cards);
                    }
                }
            }
        });
    }

    public void getHistory(){
        if (SignIn.mainUser == null) return;
        ParseQuery<ParseObject> queryHistory=ParseQuery.getQuery("History");
        queryHistory.whereEqualTo("userId",SignIn.mainUser.getId());
        queryHistory.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e!=null){
                    e.printStackTrace();
                }else{
                    Stack<History> historyStack = new Stack<>();
                    if(objects != null && objects.size()>0){
                        for(ParseObject object:objects){

                            String historyProcess=object.getString("process");
                            java.util.Date historyDate=object.getDate("date");
                            boolean isIncome = object.getBoolean("isIncome");

                            History tempHistory = new History(SignIn.mainUser.getId(), historyProcess, historyDate, isIncome);

                            historyStack.push(tempHistory);
                        }
                        SignIn.mainUser.setHistory(historyStack);
                    }
                }
            }
        });
    }

    public void getAllUsers(Runnable onComplete){
        allUsers.clear();
        ParseQuery<ParseObject> query = ParseQuery.getQuery("UserInfo");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e == null){
                    Log.d("SignIn", "Users found: " + objects.size());
                    for(ParseObject object : objects){
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
                                    }
                                });
                            }
                            getBankAccounts(user);
                            getCreditCards(user);
                            allUsers.add(user);
                        } catch (Exception ex) {
                            Log.e("SignIn", "Error parsing user: " + ex.getMessage());
                        }
                    }
                    if (onComplete != null) onComplete.run();
                } else {
                    Log.e("SignIn", "Query error: " + e.getMessage());
                    Toast.makeText(SignIn.this, "Error fetching users: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    if (onComplete != null) onComplete.run();
                }
            }
        });
    }

    public void signIn(View view){
        String user_name = userName.getText().toString();
        String user_pass = userPass.getText().toString();

        if (user_name.isEmpty() || user_pass.isEmpty()) {
            Toast.makeText(this, "Please enter ID and Password", Toast.LENGTH_SHORT).show();
            return;
        }

        if (user_name.equals(ADMIN_ID)) {
            mainUser = new User();
            mainUser.setName("Admin");
            mainUser.setId(ADMIN_ID);
            mainUser.setHistory(new Stack<>());
            getAllUsers(() -> {
                Intent intent = new Intent(SignIn.this, AdminPanelActivity.class);
                startActivity(intent);
                finish();
            });
            return;
        }

        if (user_name.equals(DEMO_ID)) {
            setupDemoUser();
            intent = new Intent(SignIn.this, splashScreen.class);
            startActivity(intent);
            finish();
            return;
        }

        ParseUser.logInInBackground(user_name, user_pass, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                if(e !=null){
                    Toast.makeText(getApplicationContext(),e.getLocalizedMessage(),Toast.LENGTH_LONG).show();
                }else{
                    ParseQuery<ParseObject> query=ParseQuery.getQuery("UserInfo");
                    query.whereEqualTo("username",user_name);
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
                                        if (address_string == null) address_string = "";
                                        String[] str = address_string.split(" ");
                                        Address address;
                                        if (str.length >= 8) {
                                            address = new Address(str[0],str[1],Integer.parseInt(str[2]),Integer.parseInt(str[3]),Integer.parseInt(str[4]),str[5],str[6],str[7]);
                                        } else {
                                            address = new Address("", "", 0, 0, 0, "", "", "");
                                        }
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

                                        intent = new Intent(SignIn.this, splashScreen.class);
                                        startActivity(intent);
                                        finish();
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

    private void setupDemoUser() {
        mainUser = new User("Demo User", DEMO_ID, "9876543210", new Address("Demo St", "Tech Park", 1, 1, 1, "Bangalore", "KA", "India"), new Job("Software Engineer", "1000000", "24", "8.5"));
        
        ArrayList<BankAccount> accounts = new ArrayList<>();
        accounts.add(new BankAccount("DEMO123456", 75000));
        accounts.add(new BankAccount("SAVING9876", 125000));
        mainUser.setBankAccounts(accounts);

        ArrayList<CreditCard> cards = new ArrayList<>();
        cards.add(new CreditCard("4532 1234 5678 9012", 250000));
        mainUser.setCreditCards(cards);

        Stack<History> historyStack = new Stack<>();
        Calendar cal = Calendar.getInstance();
        historyStack.push(new History(DEMO_ID, "Salary Received: ₹150000", cal.getTime()));
        cal.add(Calendar.DAY_OF_YEAR, -1);
        historyStack.push(new History(DEMO_ID, "Power Bill Paid: ₹4500", cal.getTime()));
        cal.add(Calendar.DAY_OF_YEAR, -1);
        historyStack.push(new History(DEMO_ID, "Sent ₹5000 via UPI Scan", cal.getTime()));
        cal.add(Calendar.DAY_OF_YEAR, -2);
        historyStack.push(new History(DEMO_ID, "Credit Loan Paid: ₹25000", cal.getTime()));
        cal.add(Calendar.DAY_OF_YEAR, -1);
        historyStack.push(new History(DEMO_ID, "Fuel Station: ₹3200", cal.getTime()));
        mainUser.setHistory(historyStack);
        
        Toast.makeText(this, "Logged in as Demo User", Toast.LENGTH_SHORT).show();
    }

    public void adminPanel(View view){
        Intent intent = new Intent(SignIn.this, AdminPanelActivity.class);
        startActivity(intent);
    }

}
