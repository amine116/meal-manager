package com.amine.mealmanager;

public class MarketItem {

    private String itemName, amount, price;

    public MarketItem(){}

    public MarketItem(String itemName, String amount, String price) {
        this.itemName = itemName;
        this.amount = amount;
        this.price = price;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }
}
