package com.amine.mealmanager;

public class StoppedHistory {
    private Boarder boarder;
    double mealRate;

    public StoppedHistory(){}

    public StoppedHistory(Boarder boarder, double mealRate) {
        this.boarder = boarder;
        this.mealRate = mealRate;
    }

    public Boarder getBoarder() {
        return boarder;
    }

    public void setBoarder(Boarder boarder) {
        this.boarder = boarder;
    }

    public double getMealRate() {
        return mealRate;
    }

    public void setMealRate(double mealRate) {
        this.mealRate = mealRate;
    }
}
