package com.amine.mealmanager;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.ArrayList;

import static com.amine.mealmanager.MainActivity.df;
import static com.amine.mealmanager.MainActivity.fakeTotalCost;
import static com.amine.mealmanager.MainActivity.fakeTotalMeal;
import static com.amine.mealmanager.MainActivity.mealRate;
import static com.amine.mealmanager.MainActivity.stoppedCost;
import static com.amine.mealmanager.MainActivity.stoppedMeal;
import static com.amine.mealmanager.MainActivity.totalCost;
import static com.amine.mealmanager.MainActivity.totalMeal;
import static com.amine.mealmanager.MainActivity.totalPaid;

public class DetailMealCalcActivity extends AppCompatActivity {

    private ArrayList<Boarder> boarders, sBoarders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_meal_calc);
        ActionBar a = getSupportActionBar();
        if(a != null) a.setTitle("  Meal Calculations");
        initialize();
    }

    private void initialize(){
        boarders = new ArrayList<>();
        sBoarders = new ArrayList<>();

        boarders.addAll(MainActivity.boarders);
        sBoarders.addAll(MainActivity.stoppedBoarders);

        TextView tv = findViewById(R.id.mealCalc_txtDetails);
        tv.setText(getString());
    }

    private String getString(){
        String s = "1. Currently active members in your meal are: " + boarders.size() +
                "\nTotal inactive members are(some of them might be active by now): "
                + sBoarders.size() + ".\nTotal Meal: " + df.format(totalMeal) +
                ".\nTotal Paid: " + df.format(totalPaid) + ".\nTotal Cost: " + df.format(totalCost) + ".\n";

        if(sBoarders.size() > 0){
            s = s + "\n\n2. But " + sBoarders.size() +
                    " Member's meal has been closed for for few(or at least once) times.\n" +
                    "We closed their calculations also on 'meal rate' at the moment of closing.\n" +
                    "Since closed member(s) are out of calculation, we calculate the " +
                    "'meal rate' from only active members.\n" +
                    "When a closed member's meal is reactivated," +
                    " his meal is reactivated under new 'meal rate'. " +
                    "But his previous session(s) is kept different.\n" +
                    "Total closed members in your meal are: " + sBoarders.size() + " and their " +
                    "'Closed Cost' is: " + df.format(stoppedCost) + ".\nSo, 'Current Cost' is: " +
                    df.format(totalCost) + " - " + df.format(stoppedCost) + "(total cost - closed cost) = "
                    + df.format(fakeTotalCost) + ".\n" +
                    "Similarly, 'Current Meal' is: " + df.format(totalMeal) + " - " + df.format(stoppedMeal)+
                    "(total meal - closed meal) = " + df.format(fakeTotalMeal)+ ".\nSo 'meal rate' is: " +
                    df.format(fakeTotalCost) + "÷" + df.format(fakeTotalMeal) +
                    "('current cost' ÷ 'current meal')" + " = " + df.format(mealRate) + ".\n";

        }
        else{
            s = s + "\n\nSo, 'meal rate' is: " + df.format(totalCost) + " ÷ " + df.format(totalMeal) +
                    "(Total cost ÷ total meal) = "
                    + df.format(mealRate) + ".\n";

        }
        return s;
    }
}