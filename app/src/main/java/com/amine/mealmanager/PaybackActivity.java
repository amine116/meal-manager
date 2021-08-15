package com.amine.mealmanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.amine.mealmanager.MainActivity.df;
import static com.amine.mealmanager.MainActivity.getReminderMoney;
import static com.amine.mealmanager.MainActivity.getTodayDate;
import static com.amine.mealmanager.MainActivity.isAnimationAlive;
import static com.amine.mealmanager.MainActivity.readFromDatabase;
import static com.amine.mealmanager.MainActivity.reminderMoney;

public class PaybackActivity extends AppCompatActivity {

    private ArrayList<Boarder> boarders, sBoarders;
    private Map<String, Payback> paybacks;
    private LinearLayout paybackLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payback);

        ActionBar a = getSupportActionBar();
        if(a != null) a.setTitle("  Refund");

        initialize();
        setFrame();
    }

    private void initialize(){
        boarders = MainActivity.boarders;
        sBoarders = MainActivity.stoppedBoarders;
        paybacks = MainActivity.paybacks;
        paybackLayout = findViewById(R.id.payback_layout);
    }

    private void setFrame(){
        paybackLayout.removeAllViews();
        Drawable drawable = null, drBtnRefundAccent = null, drBtnRefundBlack_50 = null;
        Resources res = getResources();

        try {
            drawable = Drawable.createFromXml(res,
                    res.getXml(R.xml.rectangular_shape_cook_bill));

            drBtnRefundAccent = Drawable.createFromXml(res,
                    res.getXml(R.xml.shape_button));
            drBtnRefundBlack_50 = Drawable.createFromXml(res, res.getXml(R.xml.shape_button_black_50));
        }
        catch (IOException | XmlPullParserException e) {
            e.printStackTrace();
        }

        int index = 0;
        double td = 0, to = 0;
        for(int i = 0; i < boarders.size(); i++){
            index = i;
            double overHead = getOverHead(boarders.get(i), sBoarders);
            Payback p = paybacks.get(boarders.get(i).getName());

            String s = "";
            if(p != null){
                overHead -= p.getAmount();
            }

            LinearLayout ll = new LinearLayout(this),
                    fake = new LinearLayout(this);
            ll.setGravity(Gravity.CENTER);
            ll.setOrientation(LinearLayout.HORIZONTAL);

            paybackLayout.addView(ll);
            paybackLayout.addView(fake);
            fake.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 10));
            ll.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, 100));

            TextView txtName = new TextView(this),
                    txtOverHead = new TextView(this),
                    txtDue = new TextView(this);
            Button btnPayback = new Button(this);

            ll.addView(txtName);
            ll.addView(txtDue);
            ll.addView(txtOverHead);
            ll.addView(btnPayback);

            if(drawable != null){
                ll.setBackground(drawable);
            }


            s = (i + 1) + ". " + boarders.get(i).getName();
            txtName.setText(s);
            if(overHead > 0){
                to += overHead;
                txtDue.setText("0");
                txtOverHead.setText(MainActivity.df.format(overHead));
                if(drBtnRefundAccent != null) btnPayback.setBackground(drBtnRefundAccent);
            }
            else{
                td += -overHead;
                txtDue.setText(df.format(-overHead));
                txtOverHead.setText("0");
                if(drBtnRefundBlack_50 != null) btnPayback.setBackground(drBtnRefundBlack_50);
            }

            s = "Refund"; btnPayback.setText(s);

            txtName.setLayoutParams(new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.MATCH_PARENT, 1));
            txtOverHead.setLayoutParams(new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.MATCH_PARENT, 1));
            txtDue.setLayoutParams(new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.MATCH_PARENT, 1));
            btnPayback.setLayoutParams(new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.MATCH_PARENT, 1));

            txtName.setGravity(Gravity.CENTER);
            txtDue.setGravity(Gravity.CENTER);
            txtOverHead.setGravity(Gravity.CENTER);
            btnPayback.setAllCaps(false);

            final int finalI = i;
            btnPayback.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    RefundDialog r =
                            new RefundDialog(PaybackActivity.this, boarders.get(finalI).getName());
                    r.show();
                }
            });


        }
        index++;
        for(int i = 0; i < sBoarders.size(); i++, index++){
            double overHead = getSOverHead(sBoarders.get(i), boarders);
            Payback p = paybacks.get(sBoarders.get(i).getName());

            if(overHead != -1){
                String s = "";
                if(p != null){
                    overHead -= p.getAmount();
                }

                LinearLayout ll = new LinearLayout(this),
                        fake = new LinearLayout(this);
                ll.setGravity(Gravity.CENTER);
                ll.setOrientation(LinearLayout.HORIZONTAL);

                paybackLayout.addView(ll);
                paybackLayout.addView(fake);
                ll.setLayoutParams(new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, 100));
                fake.setLayoutParams(
                        new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 10));

                TextView txtName = new TextView(this),
                        txtOverHead = new TextView(this),
                        txtDue = new TextView(this);
                Button btnPayback = new Button(this);

                ll.addView(txtName);
                ll.addView(txtDue);
                ll.addView(txtOverHead);
                ll.addView(btnPayback);
                if(drawable != null) ll.setBackground(drawable);

                s = (index + 1) + ". " + sBoarders.get(i).getName();

                txtName.setText(s);
                if(overHead > 0){
                    to += overHead;
                    txtDue.setText("0");
                    txtOverHead.setText(MainActivity.df.format(overHead));
                    if(drBtnRefundAccent != null) btnPayback.setBackground(drBtnRefundAccent);
                }
                else{
                    td += -overHead;
                    txtDue.setText(df.format(-overHead));
                    txtOverHead.setText("0");
                    if(drBtnRefundBlack_50 != null) btnPayback.setBackground(drBtnRefundBlack_50);
                }

                s = "Refund"; btnPayback.setText(s);

                txtName.setLayoutParams(new LinearLayout.LayoutParams(
                        0, ViewGroup.LayoutParams.MATCH_PARENT, 1));
                txtOverHead.setLayoutParams(new LinearLayout.LayoutParams(
                        0, ViewGroup.LayoutParams.MATCH_PARENT, 1));
                txtDue.setLayoutParams(new LinearLayout.LayoutParams(
                        0, ViewGroup.LayoutParams.MATCH_PARENT, 1));
                btnPayback.setLayoutParams(new LinearLayout.LayoutParams(
                        0, ViewGroup.LayoutParams.MATCH_PARENT, 1));

                txtName.setGravity(Gravity.CENTER);
                txtDue.setGravity(Gravity.CENTER);
                txtOverHead.setGravity(Gravity.CENTER);
                btnPayback.setAllCaps(false);

                final int finalI = i;
                btnPayback.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        RefundDialog r =
                                new RefundDialog(PaybackActivity.this,
                                        sBoarders.get(finalI).getName());
                        r.show();
                    }
                });

            }

        }

        TextView txtTotalDue = findViewById(R.id.txtTotalDue),
                txtTotalOv = findViewById(R.id.txtTotalOverHead),
                txtReminderMoney = findViewById(R.id.txtReminderMoney);
        String s = "Total Due: " + df.format(td);
        txtTotalDue.setText(s);
        s = "Total Overhead: " + df.format(to);
        txtTotalOv.setText(s);
        reminderMoney = getReminderMoney();
        s = "Reminder Money: " + df.format(reminderMoney);
        txtReminderMoney.setText(s);
    }

    private class RefundDialog extends Dialog implements View.OnClickListener{

        String name;
        public RefundDialog(@NonNull Context context, String name) {
            super(context);
            this.name = name;
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.payback_dialog);
            initialize();
        }

        @Override
        public void onClick(View v) {
            int id = v.getId();
            if(id == R.id.paybackDialog_btnPayback){
                EditText ed = findViewById(R.id.paybackDialog_edtAmount);
                String s = ed.getText().toString();
                if(s.isEmpty()){
                    ed.setError("Enter amount");
                    ed.requestFocus();
                    return;
                }
                double amount = Double.parseDouble(s);
                final Payback newP = new Payback(name, amount, getTodayDate());
                final DatabaseReference r = MainActivity.rootRef.child("Payback").child(name);
                r.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Payback p = newP;
                        if(snapshot.exists()){
                            Payback oldP = snapshot.getValue(Payback.class);
                            if(oldP != null){
                                p = addTwoPayback(newP, oldP);
                            }
                        }
                        r.setValue(p);

                        makeInvisible();

                        readFromDatabase(new MainActivity.CallBack() {
                            @Override
                            public void onCallback() {

                                makeVisible();
                                setFrame();
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
            dismiss();

        }

        private void initialize(){
            findViewById(R.id.paybackDialog_btnCancel).setOnClickListener(this);
            findViewById(R.id.paybackDialog_btnPayback).setOnClickListener(this);
            EditText edt = findViewById(R.id.paybackDialog_edtAmount);
            String s = "Amount for " + name;
            edt.setHint(s);

        }
    }

    private Payback addTwoPayback(Payback newP, Payback oldP){
        Payback p = new Payback();
        p.setName(newP.getName());
        p.setDate(newP.getDate() + "\n" + oldP.getDate());
        p.setAmount(newP.getAmount() + oldP.getAmount());

        return p;
    }

    private void makeVisible(){
        isAnimationAlive = false;
        findViewById(R.id.payback_progress).setVisibility(View.GONE);
        findViewById(R.id.layout_payback_info).setVisibility(View.VISIBLE);
        //findViewById(R.id.layout_totalDueOve).setVisibility(View.VISIBLE);
    }

    private void makeInvisible(){
        isAnimationAlive = true;
        findViewById(R.id.payback_progress).setVisibility(View.VISIBLE);
        animate();
        findViewById(R.id.layout_payback_info).setVisibility(View.GONE);
    }

    private void animate(){
        final Handler handler = new Handler(getApplicationContext().getMainLooper());
        final ImageView imgAnimate = findViewById(R.id.payback_progress);
        final int sleepTime = 50;

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (isAnimationAlive){

                    handler.post(new Runnable() {
                        @Override
                        public void run() {

                            /*
                            imgAnimate.setImageBitmap(decodeSampledBitmapFromResource(getResources(),
                                            R.drawable.i1, imageWidthInPixel, imageHeightInPixels));
                            Log.i("test", "Image: 1");*/

                            imgAnimate.setImageResource(R.drawable.i1);
                        }
                    });

                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        Toast.makeText(PaybackActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            /*imgAnimate.setImageBitmap(decodeSampledBitmapFromResource(getResources(),
                                    R.drawable.i2, imageWidthInPixel, imageHeightInPixels));
                           // Log.i("test", "Image: 2");*/
                            imgAnimate.setImageResource(R.drawable.i2);
                        }
                    });

                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        Toast.makeText(PaybackActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }


                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            /*imgAnimate.setImageBitmap(decodeSampledBitmapFromResource(getResources(),
                                    R.drawable.i3, imageWidthInPixel, imageHeightInPixels));
                            //Log.i("test", "Image: 3");*/
                            imgAnimate.setImageResource(R.drawable.i3);
                        }
                    });

                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        Toast.makeText(PaybackActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }



                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            /*imgAnimate.setImageBitmap(decodeSampledBitmapFromResource(getResources(),
                                    R.drawable.i4, imageWidthInPixel, imageHeightInPixels));
                            //Log.i("test", "Image: 4");*/
                            imgAnimate.setImageResource(R.drawable.i4);
                        }
                    });

                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        Toast.makeText(PaybackActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }


                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            /*imgAnimate.setImageBitmap(decodeSampledBitmapFromResource(getResources(),
                                    R.drawable.i5, imageWidthInPixel, imageHeightInPixels));
                            //Log.i("test", "Image: 5");*/
                            imgAnimate.setImageResource(R.drawable.i5);
                        }
                    });

                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        Toast.makeText(PaybackActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }


                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            /*imgAnimate.setImageBitmap(decodeSampledBitmapFromResource(getResources(),
                                    R.drawable.i6, imageWidthInPixel, imageHeightInPixels));
                            //Log.i("test", "Image: 6");*/
                            imgAnimate.setImageResource(R.drawable.i6);
                        }
                    });

                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        Toast.makeText(PaybackActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }


                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            /*imgAnimate.setImageBitmap(decodeSampledBitmapFromResource(getResources(),
                                    R.drawable.i7, imageWidthInPixel, imageHeightInPixels));
                            //Log.i("test", "Image: 7");*/
                            imgAnimate.setImageResource(R.drawable.i7);
                        }
                    });

                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        Toast.makeText(PaybackActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }


                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            /*imgAnimate.setImageBitmap(decodeSampledBitmapFromResource(getResources(),
                                    R.drawable.i8, imageWidthInPixel, imageHeightInPixels));
                            //Log.i("test", "Image: 8");*/
                            imgAnimate.setImageResource(R.drawable.i8);
                        }
                    });

                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        Toast.makeText(PaybackActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }


                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            /*imgAnimate.setImageBitmap(decodeSampledBitmapFromResource(getResources(),
                                    R.drawable.i9, imageWidthInPixel, imageHeightInPixels));
                            //Log.i("test", "Image: 9");*/
                            imgAnimate.setImageResource(R.drawable.i9);
                        }
                    });

                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        Toast.makeText(PaybackActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }


                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            /*imgAnimate.setImageBitmap(decodeSampledBitmapFromResource(getResources(),
                                    R.drawable.i10, imageWidthInPixel, imageHeightInPixels));
                            //Log.i("test", "Image: 10");*/
                            imgAnimate.setImageResource(R.drawable.i10);
                        }
                    });

                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        Toast.makeText(PaybackActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }


                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            /*imgAnimate.setImageBitmap(decodeSampledBitmapFromResource(getResources(),
                                    R.drawable.i11, imageWidthInPixel, imageHeightInPixels));
                            //Log.i("test", "Image: 11");*/
                            imgAnimate.setImageResource(R.drawable.i11);
                        }
                    });

                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        Toast.makeText(PaybackActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }


                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            /*imgAnimate.setImageBitmap(decodeSampledBitmapFromResource(getResources(),
                                    R.drawable.i12, imageWidthInPixel, imageHeightInPixels));
                            //Log.i("test", "Image: 12");*/
                            imgAnimate.setImageResource(R.drawable.i12);
                        }
                    });

                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        Toast.makeText(PaybackActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }



                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            /*imgAnimate.setImageBitmap(decodeSampledBitmapFromResource(getResources(),
                                    R.drawable.i13, imageWidthInPixel, imageHeightInPixels));
                            //Log.i("test", "Image: 13");*/
                            imgAnimate.setImageResource(R.drawable.i13);
                        }
                    });

                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        Toast.makeText(PaybackActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }


                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            /*imgAnimate.setImageBitmap(decodeSampledBitmapFromResource(getResources(),
                                    R.drawable.i14, imageWidthInPixel, imageHeightInPixels));
                           // Log.i("test", "Image: 14");*/
                            imgAnimate.setImageResource(R.drawable.i14);
                        }
                    });

                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        Toast.makeText(PaybackActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            }
        }).start();
    }

    public static double getPaid(Boarder b, ArrayList<Boarder> sB){

        for(int i = 0; i < sB.size(); i++){
            if(b.getName().equals(sB.get(i).getName())){
                return (b.getPaidMoney() + sB.get(i).getPaidMoney());
            }
        }

        return b.getPaidMoney();
    }

    public static double getMeal(Boarder b, ArrayList<Boarder> sB){

        for(int i = 0; i < sB.size(); i++){
            if(b.getName().equals(sB.get(i).getName())){
                return (b.getMeals() + sB.get(i).getMeals());
            }
        }

        return b.getMeals();
    }

    public static double getOverHead(Boarder boarder, ArrayList<Boarder> sB){
        double total = 0,
                bd = boarder.getDue(),
                bo = boarder.getOverHead();

        boolean exist = false;

        for(int i = 0; i < sB.size(); i++){
            if(boarder.getName().equals(sB.get(i).getName())){
                exist = true;
                double d = bd + sB.get(i).getDue(),
                        o = bo + sB.get(i).getOverHead();
                total = o - d;
                break;
            }
        }
        if(!exist) total = bo - bd;
        return total;
    }

    public static double getSOverHead(Boarder sB, ArrayList<Boarder> boarders){
        for(int i = 0; i < boarders.size(); i++){
            if(boarders.get(i).getName().equals(sB.getName())) return -1;
        }
        return (sB.getOverHead() - sB.getDue());
      }
}