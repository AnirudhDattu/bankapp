package com.bank.izbank.UserInfo;

import java.text.SimpleDateFormat;
import java.util.Date;

public class History {
    private String userId;
    private String Process;
    private Date date;
    private boolean isIncome; // true for received, false for sent

    public History(String userId, String process, Date date) {
        this.userId = userId;
        this.Process = process;
        this.date = date;
        this.isIncome = process.contains("+₹") || process.toLowerCase().contains("received");
    }

    public History(String userId, String process, Date date, boolean isIncome) {
        this.userId = userId;
        this.Process = process;
        this.date = date;
        this.isIncome = isIncome;
    }

    public History(String userId, String process, String dateString) {
        this.userId = userId;
        this.Process = process;
        this.isIncome = process.contains("+₹") || process.toLowerCase().contains("received");
        try {
            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
            this.date = format.parse(dateString);
        } catch (Exception e) {
            this.date = new Date();
        }
    }

    public Date getDateDate() {
        return date;
    }

    public String getDateString() {
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        return format.format(date);
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getProcess() {
        return Process;
    }

    public void setProcess(String process) {
        Process = process;
    }

    public boolean isIncome() {
        return isIncome;
    }

    public void setIncome(boolean income) {
        isIncome = income;
    }
}
