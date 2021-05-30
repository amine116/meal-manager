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

public class EditCostActivity extends AppCompatActivity {

    private LinearLayout editCostLayout;
    private ArrayList<MarketerHistory> marketerHistories;
    private Drawable drawable, drawable1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_cost);
        ActionBar a = getSupportActionBar();
        if(a != null) a.setTitle("  Edit Info");
        initialize();
        setOption();
    }

    private void initialize(){
        editCostLayout = findViewById(R.id.editCost_editLayout);
        marketerHistories = MainActivity.marketerHistories;

        Resources res = getResources();
        try {
            drawable = Drawable.createFromXml(res,
                    res.getXml(R.xml.rectangular_shape_main_activity_data));
            drawable1 = Drawable.createFromXml(res, res.getXml(R.xml.custom_ractangle_shape));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void setOption(){
        editCostLayout.removeAllViews();

        for(int i = 0; i < marketerHistories.size(); i++){
            LinearLayout ll = new LinearLayout(this),
                    fakeV = new LinearLayout(this);

            TextView txtName = new TextView(this),
                    txtFakeH = new TextView(this);

            Button btnEdit = new Button(this);

            editCostLayout.addView(ll);
            editCostLayout.addView(fakeV);

            fakeV.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, 10));

            ll.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            ll.setOrientation(LinearLayout.HORIZONTAL);
            if(drawable != null) ll.setBackground(drawable);

            ll.addView(txtName);
            ll.addView(txtFakeH);
            ll.addView(btnEdit);

            txtName.setLayoutParams(new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.MATCH_PARENT, 60));
            txtFakeH.setLayoutParams(new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.MATCH_PARENT, 1));
            txtFakeH.setBackgroundColor(Color.BLACK);

            btnEdit.setLayoutParams(new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.MATCH_PARENT, 39));
            if(drawable1 != null) btnEdit.setBackground(drawable1);

            txtName.setText(marketerHistories.get(i).getName());
            txtName.setTextColor(Color.WHITE);
            txtName.setGravity(Gravity.CENTER);
            String s = "Edit";
            btnEdit.setText(s);

            final int finalI = i;
            btnEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(EditCostActivity.this,
                            PersonWiseEditCostActivity.class);
                    intent.putExtra("INDEX", finalI);
                    startActivity(intent);
                }
            });


        }
    }
}