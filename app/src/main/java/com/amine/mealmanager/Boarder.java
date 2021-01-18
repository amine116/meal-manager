package com.amine.mealmanager;

import java.util.ArrayList;

public class Boarder {
    private String name, memberPassword;
    private double paidMoney, meals, due, overHead;
    private ArrayList<MealOrPaymentDetails> paymentD, mealD;
    private boolean isMealOn;

    public Boarder(){}

    public Boarder(String name, String memberPassword, double paidMoney, double meals, double due, double overHead,
                   ArrayList<MealOrPaymentDetails> paymentD, ArrayList<MealOrPaymentDetails> mealD,
                   boolean isMealOn) {
        this.name = name;
        this.memberPassword = memberPassword;
        this.paidMoney = paidMoney;
        this.meals = meals;
        this.due = due;
        this.overHead = overHead;
        this.paymentD = paymentD;
        this.mealD = mealD;
        this.isMealOn = isMealOn;
    }


    public String getName() {
        return name;
    }

    public String getMemberPassword() {
        return memberPassword;
    }

    public double getPaidMoney() {
        return paidMoney;
    }

    public void setPaidMoney(double paidMoney) {
        this.paidMoney = paidMoney;
    }

    public double getMeals() {
        return meals;
    }

    public void setMeals(double meals) {
        this.meals = meals;
    }

    public double getDue() {
        return due;
    }

    public void setDue(double due) {
        this.due = due;
    }

    public double getOverHead() {
        return overHead;
    }

    public void setOverHead(double overHead) {
        this.overHead = overHead;
    }

    public ArrayList<MealOrPaymentDetails> getPaymentD() {
        return paymentD;
    }

    public ArrayList<MealOrPaymentDetails> getMealD() {
        return mealD;
    }

    public boolean isMealOn() {
        return isMealOn;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setMealOn(boolean mealOn) {
        isMealOn = mealOn;
    }

    public void setMemberPassword(String memberPassword) {
        this.memberPassword = memberPassword;
    }

    public void setPaymentD(ArrayList<MealOrPaymentDetails> paymentD) {
        this.paymentD = paymentD;
    }

    public void setMealD(ArrayList<MealOrPaymentDetails> mealD) {
        this.mealD = mealD;
    }
}
