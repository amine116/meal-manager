package com.amine.mealmanager;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class EditInfo extends AppCompatActivity implements View.OnClickListener {

    private ArrayList<Boarder> boarders;
    private LinearLayout optionsLayout;
    private final TextView[] names = new TextView[MainActivity.getMaxBoarder()];
    private final Button[] btnEditMeal = new Button[MainActivity.getMaxBoarder()],
            btnEditPayment = new Button[MainActivity.getMaxBoarder()];
    private Drawable drawable, drawable1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_info);
        ActionBar a = getSupportActionBar();
        if(a != null) a.setTitle("  Edit");
        initialize();
        setOptions();
    }

    private void initialize(){
        boarders = MainActivity.getBoarders();
        optionsLayout = findViewById(R.id.edit_optionsLayout);
        findViewById(R.id.edit_btnEditCost).setOnClickListener(this);
        Resources res = getResources();
        try {
            drawable = Drawable.createFromXml(res,
                    res.getXml(R.xml.rectangular_shape_main_activity_data));
            drawable1 = Drawable.createFromXml(res, res.getXml(R.xml.custom_ractangle_shape));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void setOptions() {
        optionsLayout.removeAllViews();
        for(int i = 0; i < boarders.size(); i++){
            LinearLayout ll = new LinearLayout(this),
                    fakeH = new LinearLayout(this);
            names[i] = new TextView(this);
            btnEditMeal[i] = new Button(this);
            btnEditPayment[i] = new Button(this);
            TextView tv = new TextView(this),
                    tv1 = new TextView(this);

            optionsLayout.addView(ll);
            optionsLayout.addView(fakeH);
            ll.setOrientation(LinearLayout.HORIZONTAL);
            ll.setGravity(Gravity.CENTER);
            ll.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            fakeH.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, 10));
            fakeH.setBackgroundColor(Color.BLACK);

            if(drawable != null) ll.setBackground(drawable);
            if(drawable1 != null){
                btnEditPayment[i].setBackground(drawable1);
                btnEditMeal[i].setBackground(drawable1);
            }

            ll.addView(names[i]);
            ll.addView(tv);
            ll.addView(btnEditMeal[i]);
            ll.addView(tv1);
            ll.addView(btnEditPayment[i]);

            names[i].setGravity(Gravity.CENTER);
            names[i].setLayoutParams(new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.MATCH_PARENT, 40));
            String tempString = (i + 1) + ". " + boarders.get(i).getName();
            names[i].setText(tempString);
            names[i].setTextColor(Color.WHITE);

            tv.setLayoutParams(new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.MATCH_PARENT, 1));
            tv.setBackgroundColor(Color.BLACK);

            btnEditMeal[i].setGravity(Gravity.CENTER);
            btnEditMeal[i].setLayoutParams(new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.MATCH_PARENT, 30));
            tempString = "Edit Meal";
            btnEditMeal[i].setText(tempString);
            btnEditMeal[i].setAllCaps(false);

            tv1.setLayoutParams(new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.MATCH_PARENT, 1));
            tv1.setBackgroundColor(Color.BLACK);

            btnEditPayment[i].setGravity(Gravity.CENTER);
            btnEditPayment[i].setLayoutParams(new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.MATCH_PARENT, 30));
            tempString = "Edit Payment";
            btnEditPayment[i].setText(tempString);
            btnEditPayment[i].setAllCaps(false);

            final int finalI = i;
            btnEditMeal[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(EditInfo.this, EditInterfaceActivity.class);
                    intent.putExtra("TYPE", "MEAL");
                    intent.putExtra("INDEX", finalI);
                    startActivity(intent);
                }
            });

            btnEditPayment[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(EditInfo.this, EditInterfaceActivity.class);
                    intent.putExtra("TYPE", "PAYMENT");
                    intent.putExtra("INDEX", finalI);
                    startActivity(intent);
                }
            });

        }

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if(id == R.id.edit_btnEditCost){
            Intent intent = new Intent(this, EditCostActivity.class);
            startActivity(intent);
        }
    }
}