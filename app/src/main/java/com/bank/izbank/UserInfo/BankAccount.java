package com.bank.izbank.UserInfo;

import com.bank.izbank.Sign.SignIn;
import java.util.Random;

public class BankAccount {

    private int cash;
    private String accountno;
    private String upiId;

    public BankAccount(int cash) {
        this.cash = cash;
        this.accountno = setBankAccountNo();
        this.upiId = generateUpiId();
    }
    
    public BankAccount(String no, int cash) {
        this.cash = cash;
        this.accountno = no;
        this.upiId = generateUpiId();
    }
    
    public BankAccount(String no, int cash, String upiId) {
        this.cash = cash;
        this.accountno = no;
        this.upiId = upiId;
    }

    private String generateUpiId() {
        if (SignIn.mainUser != null && SignIn.mainUser.getPhoneNumber() != null) {
            // For multiple accounts, append the last 4 digits of account number
            String suffix = accountno.length() > 4 ? accountno.substring(accountno.length() - 4) : accountno;
            return SignIn.mainUser.getPhoneNumber() + "." + suffix + "@izbank";
        }
        return accountno + "@izbank";
    }

    public String setBankAccountNo(){
        String no= "";
        Random rnd = new Random();
        for (int i = 0; i < 10; i++) {
            int radnomint = rnd.nextInt(10);
            no = no + String.valueOf(radnomint);
        }
        return no;
    }

    public String getAccountno() {
        return accountno;
    }

    public int getCash() {
        return cash;
    }

    public void setCash(int cash) {
        this.cash = cash;
    }

    public String getUpiId() {
        return upiId;
    }

    public void setUpiId(String upiId) {
        this.upiId = upiId;
    }
}
