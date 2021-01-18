package com.amine.mealmanager;

public class MealStatus {
    private double breakfast, lunch, dinner, total;

    public MealStatus(){}

    public MealStatus(double breakfast, double lunch, double dinner, double total) {
        this.breakfast = breakfast;
        this.lunch = lunch;
        this.dinner = dinner;
        this.total = total;
    }

    public double getBreakfast() {
        return breakfast;
    }

    public void setBreakfast(double breakfast) {
        this.breakfast = breakfast;
    }

    public double getLunch() {
        return lunch;
    }

    public void setLunch(double lunch) {
        this.lunch = lunch;
    }

    public double getDinner() {
        return dinner;
    }

    public void setDinner(double dinner) {
        this.dinner = dinner;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }
}
