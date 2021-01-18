package com.amine.mealmanager;

public class MarketerHistory {
    private String name, expenseHistory, totalAmount;

    public MarketerHistory(){}

    public MarketerHistory(String name, String expenseHistory, String totalAmount) {
        this.name = name;
        this.expenseHistory = expenseHistory;
        this.totalAmount = totalAmount;
    }

    public String getExpenseHistory() {
        return expenseHistory;
    }

    public void setExpenseHistory(String expenseHistory) {
        this.expenseHistory = expenseHistory;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(String totalAmount) {
        this.totalAmount = totalAmount;
    }
}
