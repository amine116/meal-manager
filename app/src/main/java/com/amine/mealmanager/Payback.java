package com.amine.mealmanager;

public class Payback {
    private double amount;
    private String date, name;

    public Payback(){}

    public Payback(String name, double amount, String date) {
        this.amount = amount;
        this.date = date;
        this.name = name;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
