package com.amine.mealmanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AlignmentSpan;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;

import static com.amine.mealmanager.MainActivity.IS_MANAGER;
import static com.amine.mealmanager.MainActivity.isBreakfastOn;
import static com.amine.mealmanager.MainActivity.isDinnerOn;
import static com.amine.mealmanager.MainActivity.isLunchOn;
import static com.amine.mealmanager.MainActivity.paybacks;
import static com.amine.mealmanager.MainActivity.readFromDatabase;
import static com.amine.mealmanager.MainActivity.rootRef;
import static com.amine.mealmanager.PaybackActivity.getMeal;
import static com.amine.mealmanager.PaybackActivity.getOverHead;
import static com.amine.mealmanager.PaybackActivity.getPaid;

public class ActivityOverView extends AppCompatActivity {

    private ArrayList<Boarder> boarders, stoppedBoarders;
    private LinearLayout rootLayout;
    private static final DecimalFormat df =  new DecimalFormat("0.#");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_over_view);
        ActionBar a = getSupportActionBar();
        if(a != null) a.setTitle("    Overview");
        initialize();
    }

    private void initialize(){
        boarders = MainActivity.boarders;
        stoppedBoarders = MainActivity.stoppedBoarders;
        rootLayout = findViewById(R.id.rootLayout);
        setDataToAppFrame();
    }

    private void setDataToAppFrame(){
        rootLayout.removeAllViews();

        Resources res = getResources();
        Drawable drawable = null;
        try {
            drawable = Drawable.createFromXml(res,
                    res.getXml(R.xml.rectangular_shape_main_activity_data));
        }
        catch (IOException | XmlPullParserException e) {
            e.printStackTrace();
        }

        for(int i = 0; i < boarders.size(); i++){
            LinearLayout ll = new LinearLayout(ActivityOverView.this),
                    fake = new LinearLayout(ActivityOverView.this),
                    fakeH1 = new LinearLayout(ActivityOverView.this),
                    fakeH2 = new LinearLayout(ActivityOverView.this),
                    fakeH3 = new LinearLayout(ActivityOverView.this),
                    fakeH4 = new LinearLayout(ActivityOverView.this);
            LinearLayout.LayoutParams paramsFakeH = new LinearLayout.LayoutParams(10, 150);
            fakeH1.setLayoutParams(paramsFakeH);
            fakeH2.setLayoutParams(paramsFakeH);
            fakeH3.setLayoutParams(paramsFakeH);
            fakeH4.setLayoutParams(paramsFakeH);

            fakeH1.setBackgroundColor(Color.WHITE);
            fakeH2.setBackgroundColor(Color.WHITE);
            fakeH3.setBackgroundColor(Color.WHITE);
            fakeH4.setBackgroundColor(Color.WHITE);

            if(drawable != null){
                ll.setBackground(drawable);
            }

            Payback p = paybacks.get(boarders.get(i).getName());

            double over = getOverHead(boarders.get(i), stoppedBoarders);
            if(p != null){
                over -= p.getAmount();
            }
            /*
            Resources res = getResources();
            Drawable drawable = null;
            try {
                drawable = Drawable.createFromXml(res,
                        res.getXml(R.xml.rectangular_shape_main_activity_data));
            }
            catch (IOException | XmlPullParserException e) {
                e.printStackTrace();
            }

            for(int i = 0; i < boarders.size(); i++){
                LinearLayout ll = new LinearLayout(ActivityOverView.this),
                        fake = new LinearLayout(ActivityOverView.this),
                        fakeH1 = new LinearLayout(ActivityOverView.this),
                        fakeH2 = new LinearLayout(ActivityOverView.this),
                        fakeH3 = new LinearLayout(ActivityOverView.this),
                        fakeH4 = new LinearLayout(ActivityOverView.this);
                LinearLayout.LayoutParams paramsFakeH = new LinearLayout.LayoutParams(10, 150);
                fakeH1.setLayoutParams(paramsFakeH);
                fakeH2.setLayoutParams(paramsFakeH);
                fakeH3.setLayoutParams(paramsFakeH);
                fakeH4.setLayoutParams(paramsFakeH);

                fakeH1.setBackgroundColor(Color.WHITE);
                fakeH2.setBackgroundColor(Color.WHITE);
                fakeH3.setBackgroundColor(Color.WHITE);
                fakeH4.setBackgroundColor(Color.WHITE);

                if(drawable != null){
                    ll.setBackground(drawable);
                }

                Payback p = paybacks.get(boarders.get(i).getName());

                int pos = -1;
                double d1, ov1, d2, ov2, d = 0, ov = 0;
                for(int j = 0; j < stoppedBoarders.size(); j++){
                    if(boarders.get(i).getName().equals(stoppedBoarders.get(j).getName())){
                        d1 = boarders.get(i).getDue();
                        d2 = stoppedBoarders.get(j).getDue();
                        ov1 = boarders.get(i).getOverHead();
                        ov2 = stoppedBoarders.get(j).getOverHead();
                        d = d1 +  d2;
                        ov = ov1 + ov2;
                        if(d > ov){
                            d -= ov;
                            ov = 0;
                        }else{
                            ov -= d;
                            d = 0;
                        }

                        pos = j;
                        break;
                    }
                }

                String s;

                TextView name = new TextView(this);
                s = (i + 1) + ". " + boarders.get(i).getName();
                name.setText(s);
                name.setGravity(Gravity.CENTER);
                name.setTypeface(Typeface.DEFAULT_BOLD);
                name.setTextColor(Color.DKGRAY);
                //.rgb(245, 247, 246)
                //name.setTextSize(17);

                TextView paid = new TextView(this);
                if(pos == -1) s = df.format(boarders.get(i).getPaidMoney()) + "";
                else s = df.format((boarders.get(i).getPaidMoney() + stoppedBoarders.get(pos).getPaidMoney())) + "";
                paid.setText(s);
                paid.setGravity(Gravity.CENTER);
                paid.setTypeface(Typeface.DEFAULT_BOLD);
                //paid.setTextSize(17);
                paid.setTextColor(Color.DKGRAY);

                TextView meal = new TextView(this);
                if(pos == -1) s = df.format(boarders.get(i).getMeals()) + "";
                else s = df.format((boarders.get(i).getMeals() + stoppedBoarders.get(pos).getMeals())) + "";
                meal.setText(s);
                meal.setGravity(Gravity.CENTER);
                meal.setTypeface(Typeface.DEFAULT_BOLD);
                //meal.setTextSize(17);
                meal.setTextColor(Color.DKGRAY);

                TextView due = new TextView(this);
                double t = 0;
                if(pos == -1) {
                    if(p != null) t = boarders.get(i).getOverHead() - p.getAmount();
                    if(t < 0) s = df.format(boarders.get(i).getDue() + Math.abs(t));
                    else s = df.format(boarders.get(i).getDue());
                }
                else {
                    if(p != null) t = ov - p.getAmount();
                    if(t < 0) s = df.format(d + Math.abs(t));
                    else s = df.format(d);
                }
                due.setText(s);
                due.setGravity(Gravity.CENTER);
                due.setTypeface(Typeface.DEFAULT_BOLD);
                //due.setTextSize(17);
                due.setTextColor(Color.DKGRAY);

                TextView overHead = new TextView(this);
                if(pos == -1) {
                    t = boarders.get(i).getOverHead();
                    if(p != null) t = boarders.get(i).getOverHead() - p.getAmount();
                    if(t > 0) s = df.format(t);
                    else s = "0";
                }
                else {
                    t = ov;
                    if(p != null) t = ov - p.getAmount();
                    if(t > 0) df.format(t);
                    else s = "0";
                }
                overHead.setText(s);
                overHead.setGravity(Gravity.CENTER);
                overHead.setTextColor(Color.DKGRAY);
                //overHead.setTextSize(18);
                overHead.setTypeface(Typeface.DEFAULT_BOLD);

                ll.setOrientation(LinearLayout.HORIZONTAL);

                ll.addView(name);
                ll.addView(fakeH1);
                ll.addView(paid);
                ll.addView(fakeH2);
                ll.addView(meal);
                ll.addView(fakeH3);
                ll.addView(due);
                ll.addView(fakeH4);
                ll.addView(overHead);

                LinearLayout.LayoutParams params30 = new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        35
                );
                LinearLayout.LayoutParams params15 = new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        17
                );
                LinearLayout.LayoutParams params20 = new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        25
                );
                name.setLayoutParams(params30);
                paid.setLayoutParams(params15);
                meal.setLayoutParams(params15);
                due.setLayoutParams(params15);
                overHead.setLayoutParams(params20);

                rootLayout.addView(ll);
                if(i != boarders.size() - 1) rootLayout.addView(fake);

                LinearLayout.LayoutParams paramsLl = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 150),
                        paramsFake = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 10);
                ll.setLayoutParams(paramsLl);

                fake.setLayoutParams(paramsFake);
                fake.setBackgroundColor(Color.WHITE);
            }

            try {
                drawable = Drawable.createFromXml(res,
                        res.getXml(R.xml.rectangular_shape_main_activity_data2));
            }
            catch (IOException | XmlPullParserException e) {
                e.printStackTrace();
            }

            int index = boarders.size() + 1;
            for(int i = 0; i < stoppedBoarders.size(); i++){

                boolean isExist = false;

                for(int j = 0; j < boarders.size(); j++){
                    if(boarders.get(j).getName().equals(stoppedBoarders.get(i).getName())){
                        isExist = true;
                        break;
                    }
                }

                if(!isExist){
                    LinearLayout ll = new LinearLayout(ActivityOverView.this),
                            fake = new LinearLayout(ActivityOverView.this),
                            fakeH1 = new LinearLayout(ActivityOverView.this),
                            fakeH2 = new LinearLayout(ActivityOverView.this),
                            fakeH3 = new LinearLayout(ActivityOverView.this),
                            fakeH4 = new LinearLayout(ActivityOverView.this);
                    LinearLayout.LayoutParams paramsFakeH = new LinearLayout.LayoutParams(10, 150);
                    fakeH1.setLayoutParams(paramsFakeH);
                    fakeH2.setLayoutParams(paramsFakeH);
                    fakeH3.setLayoutParams(paramsFakeH);
                    fakeH4.setLayoutParams(paramsFakeH);

                    fakeH1.setBackgroundColor(Color.WHITE);
                    fakeH2.setBackgroundColor(Color.WHITE);
                    fakeH3.setBackgroundColor(Color.WHITE);
                    fakeH4.setBackgroundColor(Color.WHITE);


                    String s;
                    Payback p = paybacks.get(stoppedBoarders.get(i).getName());

                    final TextView name = new TextView(this);
                    s = index + ". " + stoppedBoarders.get(i).getName();
                    name.setText(s);
                    name.setGravity(Gravity.CENTER);
                    name.setTextColor(Color.rgb(245, 247, 246));
                    name.setTextSize(17);

                    TextView paid = new TextView(this);
                    s = df.format(stoppedBoarders.get(i).getPaidMoney()) + "";
                    paid.setText(s);
                    paid.setGravity(Gravity.CENTER);
                    paid.setTextSize(17);
                    paid.setTextColor(Color.rgb(245, 247, 246));

                    TextView meal = new TextView(this);
                    s = df.format(stoppedBoarders.get(i).getMeals()) + "";
                    meal.setText(s);
                    meal.setGravity(Gravity.CENTER);
                    meal.setTextSize(17);
                    meal.setTextColor(Color.rgb(245, 247, 246));

                    TextView due = new TextView(this);
                    double ov = stoppedBoarders.get(i).getOverHead();
                    if(p != null){
                        ov -= p.getAmount();
                    }
                    if(ov < 0) s = df.format(stoppedBoarders.get(i).getDue() + Math.abs(ov)) + "";
                    else s = df.format(stoppedBoarders.get(i).getDue()) + "";
                    due.setText(s);
                    due.setGravity(Gravity.CENTER);
                    due.setTextColor(Color.rgb(250, 245, 150));
                    due.setTextSize(17);
                    due.setTextColor(Color.rgb(245, 247, 246));

                    TextView overHead = new TextView(this);
                    if(ov > 0) s = df.format(ov) + "";
                    else s = "0";
                    overHead.setText(s);
                    overHead.setGravity(Gravity.CENTER);
                    overHead.setTextColor(Color.rgb(245, 247, 246));
                    overHead.setTextSize(18);

                    ll.setOrientation(LinearLayout.HORIZONTAL);

                    ll.addView(name);
                    ll.addView(fakeH1);
                    ll.addView(paid);
                    ll.addView(fakeH2);
                    ll.addView(meal);
                    ll.addView(fakeH3);
                    ll.addView(due);
                    ll.addView(fakeH4);
                    ll.addView(overHead);

                    LinearLayout.LayoutParams params30 = new LinearLayout.LayoutParams(
                            0,
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            35
                    );
                    LinearLayout.LayoutParams params15 = new LinearLayout.LayoutParams(
                            0,
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            17
                    );
                    LinearLayout.LayoutParams params20 = new LinearLayout.LayoutParams(
                            0,
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            25
                    );
                    name.setLayoutParams(params30);
                    paid.setLayoutParams(params15);
                    meal.setLayoutParams(params15);
                    due.setLayoutParams(params15);
                    overHead.setLayoutParams(params20);

                    rootLayout.addView(ll);
                    rootLayout.addView(fake);

                    LinearLayout.LayoutParams paramsLl = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, 150),
                            paramsFake = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                    10);
                    ll.setLayoutParams(paramsLl);
                    if(drawable != null){
                        ll.setBackground(drawable);
                    }

                    fake.setLayoutParams(paramsFake);
                    fake.setBackgroundColor(Color.WHITE);

                    final int finalI = i;
                    ll.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Toast.makeText(ActivityOverView.this, stoppedBoarders.get(finalI).getName() +
                                    " Has deactivated his/her meal", Toast.LENGTH_LONG).show();
                        }
                    });


                    if(IS_MANAGER){
                        ll.setOnLongClickListener(new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View v) {

                                class ActivateHisMeal extends Dialog implements View.OnClickListener{

                                    TextView br, ln, dn;

                                    public ActivateHisMeal(@NonNull Context context) {
                                        super(context);
                                    }

                                    @Override
                                    protected void onCreate(Bundle savedInstanceState) {
                                        super.onCreate(savedInstanceState);
                                        setContentView(R.layout.reactivate_meal);
                                        initialize();
                                    }

                                    private void initialize(){
                                        TextView textView = findViewById(R.id.reactivateUserName);
                                        br = findViewById(R.id.brTxt);
                                        ln = findViewById(R.id.lTxt);
                                        dn = findViewById(R.id.dTxt);
                                        textView.setText(stoppedBoarders.get(finalI).getName());

                                        findViewById(R.id.reactivate_moveUnderCalc).setVisibility(View.VISIBLE);


                                        findViewById(R.id.reactivateMeal).setOnClickListener(this);
                                        findViewById(R.id.cancelReactivation).setOnClickListener(this);
                                        findViewById(R.id.reactivate_moveUnderCalc).setOnClickListener(this);
                                        findViewById(R.id.brMinus).setOnClickListener(this);
                                        findViewById(R.id.brPlus).setOnClickListener(this);
                                        findViewById(R.id.lMinus).setOnClickListener(this);
                                        findViewById(R.id.lPlus).setOnClickListener(this);
                                        findViewById(R.id.dMinus).setOnClickListener(this);
                                        findViewById(R.id.dPlus).setOnClickListener(this);


                                    }

                                    @Override
                                    public void onClick(View v) {
                                        int id = v.getId();

                                        if(id == R.id.brMinus){
                                            if(isBreakfastOn) {
                                                double prev = Double.parseDouble(br.getText().toString());
                                                if (prev > 0) prev -= 0.5;
                                                String fin = df.format(prev) + "";
                                                br.setText(fin);
                                            }else{
                                                Toast.makeText(ActivityOverView.this,
                                                        "Breakfast meal off!", Toast.LENGTH_LONG).show();
                                            }
                                        }
                                        else if(id == R.id.brPlus){
                                            if(isBreakfastOn){
                                                double prev = Double.parseDouble(br.getText().toString());
                                                prev += 0.5;
                                                String fin = df.format(prev) + "";
                                                br.setText(fin);
                                            }else{
                                                Toast.makeText(ActivityOverView.this,
                                                        "Breakfast meal off!", Toast.LENGTH_LONG).show();
                                            }
                                        }
                                        else if(id == R.id.lMinus){
                                            if(isLunchOn){
                                                double prev = Double.parseDouble(ln.getText().toString());
                                                if(prev > 0) prev -= 0.5;
                                                String fin = df.format(prev) + "";
                                                ln.setText(fin);
                                            }else{
                                                Toast.makeText(ActivityOverView.this,
                                                        "Lunch meal off!", Toast.LENGTH_LONG).show();
                                            }
                                        }
                                        else if(id == R.id.lPlus){
                                            if(isLunchOn){
                                                double prev = Double.parseDouble(ln.getText().toString());
                                                prev += 0.5;
                                                String fin = df.format(prev) + "";
                                                ln.setText(fin);
                                            }else{
                                                Toast.makeText(ActivityOverView.this,
                                                        "Lunch meal off!", Toast.LENGTH_LONG).show();
                                            }
                                        }
                                        else if(id == R.id.dMinus){
                                            if(isDinnerOn){
                                                double prev = Double.parseDouble(dn.getText().toString());
                                                if(prev > 0) prev -= 0.5;
                                                String fin = df.format(prev) + "";
                                                dn.setText(fin);
                                            }else{
                                                Toast.makeText(ActivityOverView.this,
                                                        "Dinner meal off!", Toast.LENGTH_LONG).show();
                                            }
                                        }
                                        else if(id == R.id.dPlus){
                                            if(isDinnerOn){
                                                double prev = Double.parseDouble(dn.getText().toString());
                                                prev += 0.5;
                                                String fin = df.format(prev) + "";
                                                dn.setText(fin);
                                            }else{
                                                Toast.makeText(ActivityOverView.this,
                                                        "Dinner meal off!", Toast.LENGTH_LONG).show();
                                            }
                                        }
                                        else if(id == R.id.reactivateMeal){
                                            final double b = Double.parseDouble(br.getText().toString()),
                                                    l = Double.parseDouble(ln.getText().toString()),
                                                    d = Double.parseDouble(dn.getText().toString()),
                                                    total = b + l + d;
                                            if(total == 0){
                                                Toast.makeText(ActivityOverView.this, "Can't Reactivate" +
                                                        " with 0 meal", Toast.LENGTH_LONG).show();
                                            }
                                            else{
                                                final String nm = stoppedBoarders.get(finalI).getName();
                                                final ArrayList<MealOrPaymentDetails> temp = new ArrayList<>();
                                                temp.add(new MealOrPaymentDetails(
                                                        MainActivity.getTodayDate(), "0"));

                                                final DatabaseReference r = rootRef.child("members").child(nm),
                                                        r2 = rootRef.child("Meal Status").child(nm),
                                                        r3 = rootRef.child("those who stopped meal/members")
                                                                .child(nm);

                                                r3.addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                        Boarder boarder = snapshot.getValue(Boarder.class);
                                                        Boarder boarder1 = new Boarder(nm,
                                                                boarder.getMemberPassword(),
                                                                0, 0, 0,
                                                                0, temp, temp, true);
                                                        r.setValue(boarder1);
                                                        TodayMealStatus t = new TodayMealStatus(nm, b, l,
                                                                d, total);
                                                        r2.setValue(t);

                                                        readFromDatabase(new MainActivity.CallBack() {
                                                            @Override
                                                            public void onCallback() {
                                                                setDataToAppFrame();
                                                            }
                                                        });

                                                        dismiss();
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {

                                                    }
                                                });
                                            }
                                        }
                                        else if(id == R.id.cancelReactivation){
                                            dismiss();
                                        }
                                        else if(id == R.id.reactivate_moveUnderCalc){
                                            final double b = Double.parseDouble(br.getText().toString()),
                                                    l = Double.parseDouble(ln.getText().toString()),
                                                    d = Double.parseDouble(dn.getText().toString()),
                                                    total = b + l + d;

                                            PermToMoveUnderCurCalc p = new PermToMoveUnderCurCalc(
                                                    ActivityOverView.this, stoppedBoarders.get(finalI),
                                                    df.format(b), df.format(l), df.format(d), df.format(total));
                                            p.show();
                                            dismiss();
                                        }
                                    }
                                }

                                ActivateHisMeal activateHisMeal = new ActivateHisMeal(ActivityOverView.this);
                                activateHisMeal.show();
                                return true;
                            }
                        });
                    }



                    index++;
                }
            }

            LinearLayout ll = new LinearLayout(ActivityOverView.this);
            LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 10);
            rootLayout.addView(ll);
            ll.setLayoutParams(params);
            ll.setBackgroundColor(Color.BLACK);
             */

            String s;

            TextView name = new TextView(this);
            s = (i + 1) + ". " + boarders.get(i).getName();
            name.setText(s);
            name.setGravity(Gravity.CENTER);
            name.setTypeface(Typeface.DEFAULT_BOLD);
            name.setTextColor(Color.DKGRAY);
            //.rgb(245, 247, 246)
            //name.setTextSize(17);

            TextView paid = new TextView(this);
            s = df.format(getPaid(boarders.get(i), stoppedBoarders));
            paid.setText(s);
            paid.setGravity(Gravity.CENTER);
            paid.setTypeface(Typeface.DEFAULT_BOLD);
            //paid.setTextSize(17);
            paid.setTextColor(Color.DKGRAY);

            TextView meal = new TextView(this);
            s = df.format(getMeal(boarders.get(i), stoppedBoarders));
            meal.setText(s);
            meal.setGravity(Gravity.CENTER);
            meal.setTypeface(Typeface.DEFAULT_BOLD);
            //meal.setTextSize(17);
            meal.setTextColor(Color.DKGRAY);

            TextView due = new TextView(this);
            if(over < 0){
                s = df.format(Math.abs(over));
            }
            else{
                s = "0";
            }
            due.setText(s);
            due.setGravity(Gravity.CENTER);
            due.setTypeface(Typeface.DEFAULT_BOLD);
            //due.setTextSize(17);
            due.setTextColor(Color.DKGRAY);

            TextView overHead = new TextView(this);
            if(over > 0){
                s = df.format(over);
            }
            else s = "0";
            overHead.setText(s);
            overHead.setGravity(Gravity.CENTER);
            overHead.setTextColor(Color.DKGRAY);
            //overHead.setTextSize(18);
            overHead.setTypeface(Typeface.DEFAULT_BOLD);

            ll.setOrientation(LinearLayout.HORIZONTAL);

            ll.addView(name);
            ll.addView(fakeH1);
            ll.addView(paid);
            ll.addView(fakeH2);
            ll.addView(meal);
            ll.addView(fakeH3);
            ll.addView(due);
            ll.addView(fakeH4);
            ll.addView(overHead);

            LinearLayout.LayoutParams params30 = new LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    35
            );
            LinearLayout.LayoutParams params15 = new LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    17
            );
            LinearLayout.LayoutParams params20 = new LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    25
            );
            name.setLayoutParams(params30);
            paid.setLayoutParams(params15);
            meal.setLayoutParams(params15);
            due.setLayoutParams(params15);
            overHead.setLayoutParams(params20);

            rootLayout.addView(ll);
            if(i != boarders.size() - 1) rootLayout.addView(fake);

            LinearLayout.LayoutParams paramsLl = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 150),
                    paramsFake = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 10);
            ll.setLayoutParams(paramsLl);

            fake.setLayoutParams(paramsFake);
            fake.setBackgroundColor(Color.WHITE);
        }

        try {
            drawable = Drawable.createFromXml(res,
                    res.getXml(R.xml.rectangular_shape_main_activity_data2));
        }
        catch (IOException | XmlPullParserException e) {
            e.printStackTrace();
        }

        int index = boarders.size() + 1;
        for(int i = 0; i < stoppedBoarders.size(); i++){

            boolean isExist = false;

            for(int j = 0; j < boarders.size(); j++){
                if(boarders.get(j).getName().equals(stoppedBoarders.get(i).getName())){
                    isExist = true;
                    break;
                }
            }

            if(!isExist){
                LinearLayout ll = new LinearLayout(ActivityOverView.this),
                        fake = new LinearLayout(ActivityOverView.this),
                        fakeH1 = new LinearLayout(ActivityOverView.this),
                        fakeH2 = new LinearLayout(ActivityOverView.this),
                        fakeH3 = new LinearLayout(ActivityOverView.this),
                        fakeH4 = new LinearLayout(ActivityOverView.this);
                LinearLayout.LayoutParams paramsFakeH = new LinearLayout.LayoutParams(10, 150);
                fakeH1.setLayoutParams(paramsFakeH);
                fakeH2.setLayoutParams(paramsFakeH);
                fakeH3.setLayoutParams(paramsFakeH);
                fakeH4.setLayoutParams(paramsFakeH);

                fakeH1.setBackgroundColor(Color.WHITE);
                fakeH2.setBackgroundColor(Color.WHITE);
                fakeH3.setBackgroundColor(Color.WHITE);
                fakeH4.setBackgroundColor(Color.WHITE);


                String s;
                Payback p = paybacks.get(stoppedBoarders.get(i).getName());

                final TextView name = new TextView(this);
                s = index + ". " + stoppedBoarders.get(i).getName();
                name.setText(s);
                name.setGravity(Gravity.CENTER);
                name.setTextColor(Color.rgb(245, 247, 246));
                name.setTextSize(17);

                TextView paid = new TextView(this);
                s = df.format(stoppedBoarders.get(i).getPaidMoney());
                paid.setText(s);
                paid.setGravity(Gravity.CENTER);
                paid.setTextSize(17);
                paid.setTextColor(Color.rgb(245, 247, 246));

                TextView meal = new TextView(this);
                s = df.format(stoppedBoarders.get(i).getMeals());
                meal.setText(s);
                meal.setGravity(Gravity.CENTER);
                meal.setTextSize(17);
                meal.setTextColor(Color.rgb(245, 247, 246));

                TextView due = new TextView(this);
                double ov = stoppedBoarders.get(i).getOverHead() - stoppedBoarders.get(i).getDue();
                if(p != null){
                    ov -= p.getAmount();
                }
                if(ov < 0) s = df.format(Math.abs(ov));
                else s = "0";
                due.setText(s);
                due.setGravity(Gravity.CENTER);
                due.setTextColor(Color.rgb(250, 245, 150));
                due.setTextSize(17);
                due.setTextColor(Color.rgb(245, 247, 246));

                TextView overHead = new TextView(this);
                if(ov > 0) s = df.format(ov);
                else s = "0";
                overHead.setText(s);
                overHead.setGravity(Gravity.CENTER);
                overHead.setTextColor(Color.rgb(245, 247, 246));
                overHead.setTextSize(18);

                ll.setOrientation(LinearLayout.HORIZONTAL);

                ll.addView(name);
                ll.addView(fakeH1);
                ll.addView(paid);
                ll.addView(fakeH2);
                ll.addView(meal);
                ll.addView(fakeH3);
                ll.addView(due);
                ll.addView(fakeH4);
                ll.addView(overHead);

                LinearLayout.LayoutParams params30 = new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        35
                );
                LinearLayout.LayoutParams params15 = new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        17
                );
                LinearLayout.LayoutParams params20 = new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        25
                );
                name.setLayoutParams(params30);
                paid.setLayoutParams(params15);
                meal.setLayoutParams(params15);
                due.setLayoutParams(params15);
                overHead.setLayoutParams(params20);

                rootLayout.addView(ll);
                rootLayout.addView(fake);

                LinearLayout.LayoutParams paramsLl = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 150),
                        paramsFake = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                10);
                ll.setLayoutParams(paramsLl);
                if(drawable != null){
                    ll.setBackground(drawable);
                }

                fake.setLayoutParams(paramsFake);
                fake.setBackgroundColor(Color.WHITE);

                final int finalI = i;
                ll.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(ActivityOverView.this, stoppedBoarders.get(finalI).getName() +
                                " Has deactivated his/her meal", Toast.LENGTH_LONG).show();
                    }
                });


                if(IS_MANAGER){
                    ll.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {

                            class ActivateHisMeal extends Dialog implements View.OnClickListener{

                                TextView br, ln, dn;

                                public ActivateHisMeal(@NonNull Context context) {
                                    super(context);
                                }

                                @Override
                                protected void onCreate(Bundle savedInstanceState) {
                                    super.onCreate(savedInstanceState);
                                    setContentView(R.layout.reactivate_meal);
                                    initialize();
                                }

                                private void initialize(){
                                    TextView textView = findViewById(R.id.reactivateUserName);
                                    br = findViewById(R.id.brTxt);
                                    ln = findViewById(R.id.lTxt);
                                    dn = findViewById(R.id.dTxt);
                                    textView.setText(stoppedBoarders.get(finalI).getName());

                                    findViewById(R.id.reactivate_moveUnderCalc).setVisibility(View.VISIBLE);


                                    findViewById(R.id.reactivateMeal).setOnClickListener(this);
                                    findViewById(R.id.cancelReactivation).setOnClickListener(this);
                                    findViewById(R.id.reactivate_moveUnderCalc).setOnClickListener(this);
                                    findViewById(R.id.brMinus).setOnClickListener(this);
                                    findViewById(R.id.brPlus).setOnClickListener(this);
                                    findViewById(R.id.lMinus).setOnClickListener(this);
                                    findViewById(R.id.lPlus).setOnClickListener(this);
                                    findViewById(R.id.dMinus).setOnClickListener(this);
                                    findViewById(R.id.dPlus).setOnClickListener(this);


                                }

                                @Override
                                public void onClick(View v) {
                                    int id = v.getId();

                                    if(id == R.id.brMinus){
                                        if(isBreakfastOn) {
                                            double prev = Double.parseDouble(br.getText().toString());
                                            if (prev > 0) prev -= 0.5;
                                            String fin = df.format(prev) + "";
                                            br.setText(fin);
                                        }else{
                                            Toast.makeText(ActivityOverView.this,
                                                    "Breakfast meal off!", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                    else if(id == R.id.brPlus){
                                        if(isBreakfastOn){
                                            double prev = Double.parseDouble(br.getText().toString());
                                            prev += 0.5;
                                            String fin = df.format(prev) + "";
                                            br.setText(fin);
                                        }else{
                                            Toast.makeText(ActivityOverView.this,
                                                    "Breakfast meal off!", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                    else if(id == R.id.lMinus){
                                        if(isLunchOn){
                                            double prev = Double.parseDouble(ln.getText().toString());
                                            if(prev > 0) prev -= 0.5;
                                            String fin = df.format(prev) + "";
                                            ln.setText(fin);
                                        }else{
                                            Toast.makeText(ActivityOverView.this,
                                                    "Lunch meal off!", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                    else if(id == R.id.lPlus){
                                        if(isLunchOn){
                                            double prev = Double.parseDouble(ln.getText().toString());
                                            prev += 0.5;
                                            String fin = df.format(prev) + "";
                                            ln.setText(fin);
                                        }else{
                                            Toast.makeText(ActivityOverView.this,
                                                    "Lunch meal off!", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                    else if(id == R.id.dMinus){
                                        if(isDinnerOn){
                                            double prev = Double.parseDouble(dn.getText().toString());
                                            if(prev > 0) prev -= 0.5;
                                            String fin = df.format(prev) + "";
                                            dn.setText(fin);
                                        }else{
                                            Toast.makeText(ActivityOverView.this,
                                                    "Dinner meal off!", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                    else if(id == R.id.dPlus){
                                        if(isDinnerOn){
                                            double prev = Double.parseDouble(dn.getText().toString());
                                            prev += 0.5;
                                            String fin = df.format(prev) + "";
                                            dn.setText(fin);
                                        }else{
                                            Toast.makeText(ActivityOverView.this,
                                                    "Dinner meal off!", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                    else if(id == R.id.reactivateMeal){
                                        final double b = Double.parseDouble(br.getText().toString()),
                                                l = Double.parseDouble(ln.getText().toString()),
                                                d = Double.parseDouble(dn.getText().toString()),
                                                total = b + l + d;
                                        if(total == 0){
                                            Toast.makeText(ActivityOverView.this, "Can't Reactivate" +
                                                    " with 0 meal", Toast.LENGTH_LONG).show();
                                        }
                                        else{
                                            final String nm = stoppedBoarders.get(finalI).getName();
                                            final ArrayList<MealOrPaymentDetails> temp = new ArrayList<>();
                                            temp.add(new MealOrPaymentDetails(
                                                    MainActivity.getTodayDate(), "0"));

                                            final DatabaseReference r = rootRef.child("members").child(nm),
                                                    r2 = rootRef.child("Meal Status").child(nm),
                                                    r3 = rootRef.child("those who stopped meal/members")
                                                            .child(nm);

                                            r3.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    Boarder boarder = snapshot.getValue(Boarder.class);
                                                    Boarder boarder1 = new Boarder(nm,
                                                            boarder.getMemberPassword(),
                                                            0, 0, 0,
                                                            0, temp, temp, true);
                                                    r.setValue(boarder1);
                                                    TodayMealStatus t = new TodayMealStatus(nm, b, l,
                                                            d, total);
                                                    r2.setValue(t);

                                                    readFromDatabase(new MainActivity.CallBack() {
                                                        @Override
                                                        public void onCallback() {
                                                            setDataToAppFrame();
                                                        }
                                                    });

                                                    dismiss();
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {

                                                }
                                            });
                                        }
                                    }
                                    else if(id == R.id.cancelReactivation){
                                        dismiss();
                                    }
                                    else if(id == R.id.reactivate_moveUnderCalc){
                                        final double b = Double.parseDouble(br.getText().toString()),
                                                l = Double.parseDouble(ln.getText().toString()),
                                                d = Double.parseDouble(dn.getText().toString()),
                                                total = b + l + d;

                                        PermToMoveUnderCurCalc p = new PermToMoveUnderCurCalc(
                                                ActivityOverView.this, stoppedBoarders.get(finalI),
                                                df.format(b), df.format(l), df.format(d), df.format(total));
                                        p.show();
                                        dismiss();
                                    }
                                }
                            }

                            ActivateHisMeal activateHisMeal = new ActivateHisMeal(ActivityOverView.this);
                            activateHisMeal.show();
                            return true;
                        }
                    });
                }



                index++;
            }
        }

        LinearLayout ll = new LinearLayout(ActivityOverView.this);
        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 10);
        rootLayout.addView(ll);
        ll.setLayoutParams(params);
        ll.setBackgroundColor(Color.BLACK);
    }

    private class PermToMoveUnderCurCalc extends Dialog implements View.OnClickListener{
        private final Boarder boarder;
        private final String br, ln, dn, total;
        private TextView txtConsent;
        private EditText edtConsentText;
        public PermToMoveUnderCurCalc(@NonNull Context context,
                                      Boarder boarder, String b, String l, String d, String total) {
            super(context);
            this.boarder = boarder;
            this.br = b;
            this.ln = l;
            this.dn = d;
            this.total = total;
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.want_parmisstion);
            initialize();
        }

        private void initialize(){
            TextView tv = findViewById(R.id.wantToLogOut);
            Button b = findViewById(R.id.yesToDelete);
            txtConsent = findViewById(R.id.permission_txtConsent);
            edtConsentText = findViewById(R.id.permission_edtWriteMove);


            txtConsent.setVisibility(View.VISIBLE);
            findViewById(R.id.permission_writeMoveLayout).setVisibility(View.VISIBLE);

            String first = "Moving members under current calculation will",
                    tobeChange = " AFFECT ALL MEMBER's",
                    last = " statistics.\n\n" +
                            "Do you want to move with total meal " + total + "(" + br + "-" + ln + "-" + dn + ")?";

            String finalString = first + tobeChange + last;
            Spannable spannable = new SpannableString(finalString);

            spannable.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.announcement)),
                    first.length(),
                    first.length() + tobeChange.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            tv.setText(spannable, TextView.BufferType.SPANNABLE);
            String s = "Yes, Move";
            b.setText(s);

            String firstPart = "Type",
                    toBoldPart = " move",
                    lastPart = " to confirm.";
            finalString = firstPart + toBoldPart + lastPart;

            spannable = new SpannableString(finalString);
            spannable.setSpan(new BackgroundColorSpan(getResources().getColor(R.color.announcement)),
                    firstPart.length() + 1,
                    firstPart.length() + toBoldPart.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            spannable.setSpan(new ForegroundColorSpan(Color.BLACK),
                    firstPart.length() + 1,
                    firstPart.length() + toBoldPart.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            txtConsent.setText(spannable, TextView.BufferType.SPANNABLE);

            findViewById(R.id.wantToLogOut).setOnClickListener(this);
            findViewById(R.id.yesToDelete).setOnClickListener(this);
            findViewById(R.id.noToDelete).setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int id = v.getId();
            if(id == R.id.yesToDelete){
                String strConsentText = edtConsentText.getText().toString();
                if(strConsentText.equals("") || strConsentText.isEmpty() || !strConsentText.equals("move")){
                    txtConsent.setTextColor(Color.RED);

                    return;
                }

                TodayMealStatus m = new TodayMealStatus(boarder.getName(), Double.parseDouble(br),
                        Double.parseDouble(ln),
                        Double.parseDouble(dn),
                        Double.parseDouble(total));
                MainActivity.getMealStatusRef().child(boarder.getName()).setValue(m);
                MainActivity.getMembersRef().child(boarder.getName()).setValue(boarder);
                rootRef.child("those who stopped meal").child("members").
                        child(boarder.getName()).removeValue();

                readFromDatabase(new MainActivity.CallBack() {
                    @Override
                    public void onCallback() {
                        setDataToAppFrame();
                    }
                });

                dismiss();
            }
            else if(id == R.id.noToDelete){
                dismiss();
            }
        }
    }
}