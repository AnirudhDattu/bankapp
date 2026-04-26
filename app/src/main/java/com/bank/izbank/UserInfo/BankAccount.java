package com.bank.izbank.UserInfo;

import com.bank.izbank.Sign.SignIn;
import java.util.Random;

public class BankAccount {

    private int cash;
    private String accountno;
    private String upiId;
    private String ownerId;

    public BankAccount(int cash) {
        this.cash = cash;
        this.accountno = setBankAccountNo();
        this.ownerId = (SignIn.mainUser != null) ? SignIn.mainUser.getId() : null;
        this.upiId = generateUpiId();
    }
    
    public BankAccount(String no, int cash) {
        this.cash = cash;
        this.accountno = no;
        this.ownerId = (SignIn.mainUser != null) ? SignIn.mainUser.getId() : null;
        this.upiId = generateUpiId();
    }

    public BankAccount(String no, int cash, String upiId) {
        this.cash = cash;
        this.accountno = no;
        this.upiId = upiId;
        // ownerId will be inferred from upiId if possible, or set later
    }

    public BankAccount(String no, int cash, String upiId, String ownerId) {
        this.cash = cash;
        this.accountno = no;
        this.ownerId = ownerId;
        this.upiId = (upiId == null || upiId.isEmpty()) ? generateUpiId() : upiId;
    }

    public String generateUpiId() {
        String idToUse = ownerId;
        if (idToUse == null && SignIn.mainUser != null) {
            idToUse = SignIn.mainUser.getId();
        }
        
        if (idToUse != null && accountno != null) {
            String suffix = accountno.length() > 4 ? accountno.substring(accountno.length() - 4) : accountno;
            return idToUse + "." + suffix + "@izbank";
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

    public String getUpiUri(String name) {
        return "upi://pay?pa=" + upiId + "&pn=" + name.replace(" ", "%20") + "&cu=INR";
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

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
        // If ownerId changes, we might want to regenerate UPI ID if it was default
        if (upiId == null || upiId.contains("@izbank")) {
            this.upiId = generateUpiId();
        }
    }
}
