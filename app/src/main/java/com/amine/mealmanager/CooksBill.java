package com.amine.mealmanager;

public class CooksBill {
    private String name, paid;

    public CooksBill(){}

    public CooksBill(String name, String paid) {
        this.name = name;
        this.paid = paid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPaid() {
        return paid;
    }

    public void setPaid(String paid) {
        this.paid = paid;
    }


}
