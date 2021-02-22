package com.amine.mealmanager;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;

import static com.amine.mealmanager.MainActivity.IS_MANAGER;

public class DetailsActivity extends AppCompatActivity {
    private ArrayList<Boarder> boarders, stoppedBoarders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        initialize();
    }

    private void initialize(){
        boarders = MainActivity.boarders;
        stoppedBoarders = MainActivity.stoppedBoarders;

        setDetailsToFrame();
    }

    private void setDetailsToFrame(){


        int width = 0;
        for(int i = 0; i < boarders.size(); i++){
            if(boarders.get(i).getMealD().size() > width)
                width = boarders.get(i).getMealD().size();
        }
        for(int i = 0; i < stoppedBoarders.size(); i++){
            if(stoppedBoarders.get(i).getMealD().size() > width)
                width = stoppedBoarders.get(i).getMealD().size();
        }
        width += 2;
        width *= 300;

        Resources res = getResources();
        Drawable drawable = null;

        LinearLayout paymentD = findViewById(R.id.paymentD), mealD = findViewById(R.id.mealD);

        paymentD.removeAllViews();
        mealD.removeAllViews();

        try {
            drawable = Drawable.createFromXml(res,
                    res.getXml(R.xml.rectangular_shape_details_meal));
        }
        catch (IOException | XmlPullParserException e) {
            e.printStackTrace();
        }

        for(int i = 0; i < boarders.size(); i++){
            LinearLayout ll = new LinearLayout(DetailsActivity.this),
                    fake = new LinearLayout(DetailsActivity.this);
            mealD.addView(ll);
            mealD.addView(fake);
            ll.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams paramsLayout = new LinearLayout.LayoutParams(
                    width, ViewGroup.LayoutParams.WRAP_CONTENT),
                    paramsFake = new LinearLayout.LayoutParams(ll.getWidth(), 10),

                    paramsName = new LinearLayout.LayoutParams(
                            300, ViewGroup.LayoutParams.MATCH_PARENT),
                    paramsFakeH = new LinearLayout.LayoutParams(10, 150);

            ll.setLayoutParams(paramsLayout);
            fake.setLayoutParams(paramsFake);

            fake.setBackgroundColor(Color.WHITE);

            ll.setBackgroundColor(getResources().getColor(R.color.meal_details));

            for(int j = 0; boarders.get(i).getMealD() != null && j < boarders.get(i).getMealD().size(); j++){
                TextView det = new TextView(DetailsActivity.this),
                        nameView = new TextView(DetailsActivity.this);
                LinearLayout h = new LinearLayout(DetailsActivity.this);

                if(j == 0) ll.addView(nameView);
                ll.addView(h);
                ll.addView(det);
                h.setLayoutParams(paramsFakeH);
                h.setBackgroundColor(Color.WHITE);

                nameView.setLayoutParams(paramsName);
                nameView.setGravity(Gravity.CENTER);
                nameView.setTextColor(Color.WHITE);
                det.setLayoutParams(paramsName);
                det.setTextColor(Color.WHITE);
                det.setGravity(Gravity.CENTER);
                String nm = (i + 1) + ". " + boarders.get(i).getName(),
                        m = boarders.get(i).getMealD().get(j).getDate() + "\n" +
                                boarders.get(i).getMealD().get(j).getMeal();
                if(j == 0) nameView.setText(nm);
                det.setText(m);
            }

            if(IS_MANAGER){
                final int finalI = i;
                ll.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(DetailsActivity.this, EditInterfaceActivity.class);
                        intent.putExtra("TYPE", "MEAL");
                        intent.putExtra("INDEX", finalI);
                        startActivity(intent);
                    }
                });
            }
        }

        for(int i = 0; i < stoppedBoarders.size(); i++){
            LinearLayout ll = new LinearLayout(DetailsActivity.this),
                    fake = new LinearLayout(DetailsActivity.this);
            mealD.addView(ll);
            mealD.addView(fake);
            ll.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams paramsLayout = new LinearLayout.LayoutParams(
                    width, ViewGroup.LayoutParams.WRAP_CONTENT),
                    paramsFake = new LinearLayout.LayoutParams(ll.getWidth(), 10),

                    paramsName = new LinearLayout.LayoutParams(300, ViewGroup.LayoutParams.MATCH_PARENT),
                    paramsFakeH = new LinearLayout.LayoutParams(10, 150);

            ll.setLayoutParams(paramsLayout);
            fake.setLayoutParams(paramsFake);

            fake.setBackgroundColor(Color.WHITE);

            //if(drawable != null) ll.setBackground(drawable);
            ll.setBackgroundColor(getResources().getColor(R.color.meal_details));

            int index = boarders.size();

            for(int j = 0;stoppedBoarders.get(i).getMealD() != null &&
                    j < stoppedBoarders.get(i).getMealD().size(); j++){
                TextView det = new TextView(DetailsActivity.this),
                        nameView = new TextView(DetailsActivity.this);
                LinearLayout h = new LinearLayout(DetailsActivity.this);

                if(j == 0) ll.addView(nameView);
                ll.addView(h);
                ll.addView(det);
                h.setLayoutParams(paramsFakeH);
                h.setBackgroundColor(Color.WHITE);

                nameView.setLayoutParams(paramsName);
                det.setLayoutParams(paramsName);
                nameView.setTextColor(Color.WHITE);
                det.setTextColor(Color.WHITE);
                nameView.setGravity(Gravity.CENTER);
                det.setGravity(Gravity.CENTER);
                String nm = (index + i + 1) + ". " + stoppedBoarders.get(i).getName() + " (Previous sessions)",
                        m = "   " + stoppedBoarders.get(i).getMealD().get(j).getDate() + "\n" +
                                stoppedBoarders.get(i).getMealD().get(j).getMeal();
                if(j == 0) nameView.setText(nm);
                det.setText(m);
            }
        }

        width = 0;

        for(int i = 0; i < boarders.size(); i++){
            if(boarders.get(i).getPaymentD().size() > width)
                width = boarders.get(i).getPaymentD().size();
        }
        for(int i = 0; i < stoppedBoarders.size(); i++){
            if(stoppedBoarders.get(i).getPaymentD().size() > width)
                width = stoppedBoarders.get(i).getPaymentD().size();
        }
        width += 2;
        width *= 300;

        try {
            drawable = Drawable.createFromXml(res,
                    res.getXml(R.xml.rectangular_shape_details_payment));
        }
        catch (IOException | XmlPullParserException e) {
            e.printStackTrace();
        }

        for(int i = 0; i < boarders.size(); i++){
            LinearLayout ll = new LinearLayout(DetailsActivity.this),
                    fake = new LinearLayout(DetailsActivity.this);
            paymentD.addView(ll);
            paymentD.addView(fake);
            ll.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams paramsLayout = new LinearLayout.LayoutParams(
                    width, ViewGroup.LayoutParams.WRAP_CONTENT),
                    paramsFake = new LinearLayout.LayoutParams(width, 10),
                    paramsFakeH = new LinearLayout.LayoutParams(10, 150),
                    paramsName = new LinearLayout.LayoutParams(300, ViewGroup.LayoutParams.MATCH_PARENT);

            ll.setLayoutParams(paramsLayout);
            fake.setLayoutParams(paramsFake);

            fake.setBackgroundColor(Color.WHITE);

            if(drawable != null) ll.setBackground(drawable);

            for(int j = 0; boarders.get(i).getPaymentD() != null &&
                    j < boarders.get(i).getPaymentD().size(); j++){
                TextView det = new TextView(DetailsActivity.this),
                        nameView = new TextView(DetailsActivity.this);
                LinearLayout h = new LinearLayout(DetailsActivity.this);

                if(j == 0) ll.addView(nameView);
                ll.addView(h);
                ll.addView(det);

                h.setBackgroundColor(Color.WHITE);
                h.setLayoutParams(paramsFakeH);
                nameView.setLayoutParams(paramsName);
                det.setLayoutParams(paramsName);
                nameView.setTextColor(Color.WHITE);
                det.setTextColor(Color.WHITE);
                nameView.setGravity(Gravity.CENTER);
                det.setGravity(Gravity.CENTER);

                String nm = (i + 1) + ". " + boarders.get(i).getName(),
                        m = "   " + boarders.get(i).getPaymentD().get(j).getDate() + "\n" +
                                boarders.get(i).getPaymentD().get(j).getMeal();
                if(j == 0) nameView.setText(nm);
                det.setText(m);
            }

            if(IS_MANAGER){
                final int finalI = i;
                ll.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(DetailsActivity.this, EditInterfaceActivity.class);
                        intent.putExtra("TYPE", "PAYMENT");
                        intent.putExtra("INDEX", finalI);
                        startActivity(intent);
                    }
                });
            }
        }

        for(int i = 0; i < stoppedBoarders.size(); i++){
            LinearLayout ll = new LinearLayout(DetailsActivity.this),
                    fake = new LinearLayout(DetailsActivity.this);
            paymentD.addView(ll);
            paymentD.addView(fake);
            ll.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams paramsLayout = new LinearLayout.LayoutParams(
                    width, ViewGroup.LayoutParams.WRAP_CONTENT),
                    paramsFake = new LinearLayout.LayoutParams(width, 10),
                    paramsFakeH = new LinearLayout.LayoutParams(10, 150),
                    paramsName = new LinearLayout.LayoutParams(300, ViewGroup.LayoutParams.MATCH_PARENT);

            ll.setLayoutParams(paramsLayout);
            fake.setLayoutParams(paramsFake);

            fake.setBackgroundColor(Color.WHITE);

            if(drawable != null) ll.setBackground(drawable);

            int index = boarders.size();

            for(int j = 0; stoppedBoarders.get(i).getPaymentD() != null &&
                    j < stoppedBoarders.get(i).getPaymentD().size(); j++){
                TextView det = new TextView(DetailsActivity.this),
                        nameView = new TextView(DetailsActivity.this);
                LinearLayout h = new LinearLayout(DetailsActivity.this);

                if(j == 0) ll.addView(nameView);
                ll.addView(h);
                ll.addView(det);

                h.setBackgroundColor(Color.WHITE);
                h.setLayoutParams(paramsFakeH);
                nameView.setLayoutParams(paramsName);
                det.setLayoutParams(paramsName);

                nameView.setTextColor(Color.WHITE);
                det.setTextColor(Color.WHITE);
                nameView.setGravity(Gravity.CENTER);
                det.setGravity(Gravity.CENTER);
                String nm = (index + i + 1)  + ". " + stoppedBoarders.get(i).getName() + " (Previous sessions)",
                        m = "   " + stoppedBoarders.get(i).getPaymentD().get(j).getDate() + "\n" +
                                stoppedBoarders.get(i).getPaymentD().get(j).getMeal();
                if(j == 0) nameView.setText(nm);
                det.setText(m);
            }
        }

    }
}