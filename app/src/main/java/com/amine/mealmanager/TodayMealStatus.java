package com.amine.mealmanager;

public class TodayMealStatus {
    private double breakfast, lunch, dinner, total;
    private String name;

    public TodayMealStatus(){}

    public TodayMealStatus(String name, double breakFirst, double lunch, double dinner, double total) {
        this.name = name;
        this.breakfast = breakFirst;
        this.lunch = lunch;
        this.dinner = dinner;
        this.total = total;
    }

    public double getBreakFirst() {
        return breakfast;
    }

    public void setBreakFirst(double breakFirst) {
        this.breakfast = breakFirst;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
