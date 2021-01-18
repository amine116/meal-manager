package com.amine.mealmanager;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;

import static com.amine.mealmanager.MainActivity.getTodayDate;

public class MaintainTodaysMeal extends AppCompatActivity implements View.OnClickListener, ValueEventListener{

    private ArrayList<Boarder> boarders;
    private ArrayList<TodayMealStatus> mealStatuses, todayMealStatuses;
    private DatabaseReference rootRef;
    private String nameOfManager;
    private int selectedRow = -1;
    private String selectedName = "";
    private int lastTimeOfBreakfast, lastTimeOfLunch, lastTimeOfDinner;
    private boolean isBreakfastOn, isLunchOn, isDinnerOn;

    private final TextView[] textViewsB = new TextView[MainActivity.getMaxBoarder()],
            textViewsL = new TextView[MainActivity.getMaxBoarder()],
            textViewsD = new TextView[MainActivity.getMaxBoarder()];

    private final DecimalFormat df =  new DecimalFormat("0.#");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maintain_todays_meal);
        initialize();
    }
    private void initialize(){
        rootRef = FirebaseDatabase.getInstance().getReference();
        nameOfManager = MainActivity.getManagerName();
        boarders = new ArrayList<>();
        mealStatuses = new ArrayList<>();
        todayMealStatuses = new ArrayList<>();

        rootRef.child(nameOfManager).addValueEventListener(MaintainTodaysMeal.this);
    }

    private void getLastTimes(final LastTimeCallback lastTimeCallback){
        DatabaseReference r = rootRef.child(nameOfManager);
        r.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boarders.clear();
                mealStatuses.clear();
                todayMealStatuses.clear();
                if(snapshot.child("members").exists()){
                    for(DataSnapshot items : snapshot.child("members").getChildren()){
                        Boarder boarder = items.getValue(Boarder.class);
                        boarders.add(boarder);
                    }
                }

                if(snapshot.child("Last time Of changing meals").exists()){
                    lastTimeOfBreakfast = snapshot.child("Last time Of changing meals")
                            .child("Breakfast").getValue(Integer.class);
                    lastTimeOfLunch = snapshot.child("Last time Of changing meals")
                            .child("Lunch").getValue(Integer.class);
                    lastTimeOfDinner = snapshot.child("Last time Of changing meals")
                            .child("Dinner").getValue(Integer.class);
                }else{
                    lastTimeOfBreakfast = 7*60;
                    lastTimeOfLunch = 12*60;
                    lastTimeOfDinner = 16*60;
                }
                if(snapshot.child("Meal Period").exists()){
                    isBreakfastOn = snapshot.child("Meal Period").child("Breakfast").getValue(Boolean.class);
                    isLunchOn = snapshot.child("Meal Period").child("Lunch").getValue(Boolean.class);
                    isDinnerOn = snapshot.child("Meal Period").child("Dinner").getValue(Boolean.class);
                }else{
                    isBreakfastOn = false;
                    isLunchOn = true;
                    isDinnerOn = true;
                }

                if(snapshot.child("Meal Status").exists()){

                    for(DataSnapshot items : snapshot.child("Meal Status").getChildren()){
                        TodayMealStatus t = items.getValue(TodayMealStatus.class);
                        mealStatuses.add(t);
                    }
                }
                String date = getTodayDate();
                if(snapshot.child("Today's Meal Status").exists()){

                    for(DataSnapshot items : snapshot.child("Today's Meal Status").getChildren()){

                        if(items.child(date).exists()){
                            TodayMealStatus todayMealStatus = items.child(date).getValue(TodayMealStatus.class);
                            todayMealStatuses.add(todayMealStatus);
                        }
                    }
                }

                lastTimeCallback.onCallback();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onDataChange(@NonNull DataSnapshot snapshot) {
        getLastTimes(new LastTimeCallback() {
            @Override
            public void onCallback() {
                prepareFrame();
            }
        });
    }

    @Override
    public void onCancelled(@NonNull DatabaseError error) {

    }

    private interface LastTimeCallback{
        void onCallback();
    }

    private void prepareFrame(){

        setMealStatuseToFrame();
        setTodaysMealStatusToFrame();
    }

    private void setMealStatuseToFrame(){
        LinearLayout rootLayoutS = findViewById(R.id.maintainRegularLayout);
        rootLayoutS.removeAllViews();
        for(int i = 0; i < boarders.size() && i < mealStatuses.size(); i++){
            final LinearLayout ll = new LinearLayout(MaintainTodaysMeal.this),
                    ll2 = new LinearLayout(MaintainTodaysMeal.this),
                    fake = new LinearLayout(MaintainTodaysMeal.this);

            rootLayoutS.addView(ll);
            rootLayoutS.addView(ll2);
            rootLayoutS.addView(fake);
            ll.setOrientation(LinearLayout.HORIZONTAL);
            ll2.setOrientation(LinearLayout.HORIZONTAL);

            LinearLayout.LayoutParams paramsLayout = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, 100),
                    paramsFake = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, 5),

                    paramsName = new LinearLayout.LayoutParams(
                            0,
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            100),
                    params3 = new LinearLayout.LayoutParams(
                            0,
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            33);
            ll.setLayoutParams(paramsLayout);
            ll2.setLayoutParams(paramsLayout);
            fake.setLayoutParams(paramsFake);
            fake.setBackgroundColor(Color.WHITE);
            ll.setBackgroundColor(Color.rgb(86, 94, 88));
            ll2.setBackgroundColor(Color.rgb(86, 94, 88));

            TextView nameView = new TextView(MaintainTodaysMeal.this),
                    brView = new TextView(MaintainTodaysMeal.this),
                    lnView = new TextView(MaintainTodaysMeal.this),
                    dnView = new TextView(MaintainTodaysMeal.this);



            ll.addView(nameView);
            ll2.addView(brView);
            ll2.addView(lnView);
            ll2.addView(dnView);


            nameView.setLayoutParams(paramsName);
            nameView.setGravity(Gravity.START);
            brView.setLayoutParams(params3);
            lnView.setLayoutParams(params3);
            dnView.setLayoutParams(params3);


            String s = "" + boarders.get(i).getName();
            nameView.setText(s);
            nameView.setTextSize(20);
            nameView.setTextColor(Color.rgb(144, 232, 220));

            s = "Breakfast: " + df.format(mealStatuses.get(i).getBreakFirst());
            brView.setText(s);
            brView.setTextSize(17);
            brView.setTextColor(Color.WHITE);


            s = "Lunch: " + df.format(mealStatuses.get(i).getLunch());
            lnView.setText(s);
            lnView.setTextSize(17);
            lnView.setTextColor(Color.WHITE);

            s = "Dinner: " + df.format(mealStatuses.get(i).getDinner());
            dnView.setText(s);
            dnView.setTextSize(17);
            dnView.setTextColor(Color.WHITE);

        }

    }

    private void setTodaysMealStatusToFrame(){
        LinearLayout rootLayoutS = findViewById(R.id.maintainTodayLayout);
        rootLayoutS.removeAllViews();

        Button[] button = new Button[MainActivity.getMaxBoarder()];
        final TextView[] nameView = new TextView[MainActivity.getMaxBoarder()];

        for(int i = 0; i < boarders.size() && i < mealStatuses.size(); i++){
            final LinearLayout ll = new LinearLayout(MaintainTodaysMeal.this),
                    ll2 = new LinearLayout(MaintainTodaysMeal.this),
                    fake = new LinearLayout(MaintainTodaysMeal.this);

            rootLayoutS.addView(ll);
            rootLayoutS.addView(ll2);
            rootLayoutS.addView(fake);
            ll.setOrientation(LinearLayout.HORIZONTAL);
            ll2.setOrientation(LinearLayout.HORIZONTAL);

            LinearLayout.LayoutParams paramsLayout = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT),
                    paramsFake = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, 5),

                    paramsName = new LinearLayout.LayoutParams(
                            0,
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            70),
                    paramsButton = new LinearLayout.LayoutParams(
                            0,
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            30),
                    params3 = new LinearLayout.LayoutParams(
                            0,
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            10),
                    paramsLabel = new LinearLayout.LayoutParams(
                            0,
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            24);
            ll.setLayoutParams(paramsLayout);
            ll2.setLayoutParams(paramsLayout);
            fake.setLayoutParams(paramsFake);
            fake.setBackgroundColor(Color.WHITE);
            ll.setBackgroundColor(Color.rgb(62, 66, 63));
            ll2.setBackgroundColor(Color.rgb(62, 66, 63));

            nameView[i] = new TextView(MaintainTodaysMeal.this);
            button[i] = new Button(MaintainTodaysMeal.this);

            textViewsB[i] = new TextView(MaintainTodaysMeal.this);
            textViewsL[i] = new TextView(MaintainTodaysMeal.this);
            textViewsD[i] = new TextView(MaintainTodaysMeal.this);

            TextView labelB = new TextView(MaintainTodaysMeal.this),
                    labelL = new TextView(MaintainTodaysMeal.this),
                    labelD = new TextView(MaintainTodaysMeal.this);



            ll.addView(nameView[i]);
            ll.addView(button[i]);

            ll2.addView(labelB);
            ll2.addView(textViewsB[i]);
            ll2.addView(labelL);
            ll2.addView(textViewsL[i]);
            ll2.addView(labelD);
            ll2.addView(textViewsD[i]);



            nameView[i].setLayoutParams(paramsName);
            nameView[i].setGravity(Gravity.START);
            button[i].setLayoutParams(paramsButton);
            textViewsB[i].setLayoutParams(params3);
            textViewsL[i].setLayoutParams(params3);
            textViewsD[i].setLayoutParams(params3);
            labelB.setLayoutParams(paramsLabel);
            labelL.setLayoutParams(paramsLabel);
            labelD.setLayoutParams(paramsLabel);

            labelB.setGravity(Gravity.END);
            textViewsB[i].setGravity(Gravity.START);
            labelL.setGravity(Gravity.END);
            textViewsL[i].setGravity(Gravity.START);
            labelD.setGravity(Gravity.END);
            textViewsD[i].setGravity(Gravity.START);


            String s = "" + boarders.get(i).getName();
            nameView[i].setText(s);
            nameView[i].setTextSize(20);
            nameView[i].setTextColor(Color.rgb(144, 232, 220));

            s = "Manage";
            button[i].setText(s);
            button[i].setBackgroundColor(Color.rgb(74, 72, 72));
            button[i].setTextColor(Color.WHITE);

            s = "Breakfast: ";
            labelB.setText(s);
            labelB.setTextColor(Color.WHITE);

            boolean isChanged = false;
            TodayMealStatus m = mealStatuses.get(i), tm;
            for(int j = 0; j < todayMealStatuses.size(); j++){
                tm = todayMealStatuses.get(j);
                if(m.getName().equals(tm.getName())){

                    textViewsB[i].setText(df.format(tm.getBreakFirst()));
                    textViewsL[i].setText(df.format(tm.getLunch()));
                    textViewsD[i].setText(df.format(tm.getDinner()));
                    isChanged = true;
                    break;
                }
            }
            if(!isChanged) {
                textViewsB[i].setText(df.format(m.getBreakFirst()));
                textViewsL[i].setText(df.format(m.getLunch()));
                textViewsD[i].setText(df.format(m.getDinner()));
            }

            textViewsB[i].setTextColor(Color.WHITE);
            labelB.setTextSize(17);
            textViewsB[i].setTextSize(17);

            s = "Lunch: ";
            labelL.setText(s);
            labelL.setTextColor(Color.WHITE);

            labelL.setTextSize(17);
            textViewsL[i].setTextSize(17);
            textViewsL[i].setTextColor(Color.WHITE);

            s = "Dinner: ";
            labelD.setText(s);
            labelD.setTextColor(Color.WHITE);

            labelD.setTextSize(17);
            textViewsD[i].setTextSize(17);
            textViewsD[i].setTextColor(Color.WHITE);

            final int finalI1 = i;
            button[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedName = nameView[finalI1].getText().toString();
                    selectedRow = finalI1;
                    CheckPassword checkPassword = new CheckPassword(MaintainTodaysMeal.this);
                    checkPassword.show();

                    DisplayMetrics displayMetrics = new DisplayMetrics();
                    getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                    int displayWidth = displayMetrics.widthPixels;
                    int displayHeight = displayMetrics.heightPixels;
                    WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
                    layoutParams.copyFrom(checkPassword.getWindow().getAttributes());
                    int dialogWindowWidth = (int) (displayWidth * 0.6f);
                    int dialogWindowHeight = (int) (displayHeight * 0.3f);
                    layoutParams.width = dialogWindowWidth;
                    layoutParams.height = dialogWindowHeight;
                    checkPassword.getWindow().setAttributes(layoutParams);

                }
            });
        }
    }

    @Override
    public void onClick(View v) {

    }

    public class CheckPassword extends Dialog implements View.OnClickListener{

        EditText pass;
        Button verify, cancel;

        public CheckPassword(@NonNull Context context) {
            super(context);
        }


        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.announcemnet);
            initialize();
        }

        private void initialize(){
            pass = findViewById(R.id.edtEnterAnnouncement);
            verify = findViewById(R.id.btnMakeAnnouncement);
            cancel = findViewById(R.id.btnCancelAnnouncement);

            verify.setOnClickListener(this);
            cancel.setOnClickListener(this);

            pass.setHint("Enter Your Password");
            String s = "Verify";
            verify.setText(s);
        }

        @Override
        public void onClick(View v) {
            if(v.getId() == R.id.btnMakeAnnouncement){
                DatabaseReference r = rootRef.child(nameOfManager).child("members")
                        .child(selectedName).child("memberPassword");
                r.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String memberPass = snapshot.getValue(String.class);
                        if(memberPass != null){
                            memberPass = memberPass.trim();
                        }
                        if(pass.getText().toString().equals(memberPass)){

                            ActivateHisMeal activateHisMeal = new ActivateHisMeal(MaintainTodaysMeal.this);
                            activateHisMeal.show();

                            DisplayMetrics displayMetrics = new DisplayMetrics();
                            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                            int displayWidth = displayMetrics.widthPixels;
                            int displayHeight = displayMetrics.heightPixels;
                            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
                            layoutParams.copyFrom(activateHisMeal.getWindow().getAttributes());
                            int dialogWindowWidth = (int) (displayWidth * 0.6f);
                            int dialogWindowHeight = (int) (displayHeight * 0.4f);
                            layoutParams.width = dialogWindowWidth;
                            layoutParams.height = dialogWindowHeight;
                            activateHisMeal.getWindow().setAttributes(layoutParams);

                            dismiss();
                        }else{
                            Toast.makeText(MaintainTodaysMeal.this, "Incorrect Password",
                                    Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
            if(v.getId() == R.id.btnCancelAnnouncement){
                dismiss();
            }
        }
    }

    private class ActivateHisMeal extends Dialog implements View.OnClickListener{

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
            textView.setText(selectedName);
            br.setText(textViewsB[selectedRow].getText());
            ln.setText(textViewsL[selectedRow].getText());
            dn.setText(textViewsD[selectedRow].getText());


            Button b = findViewById(R.id.reactivateMeal);
            String s = "Update";
            b.setText(s);
            b.setOnClickListener(this);
            findViewById(R.id.cancelReactivation).setOnClickListener(this);
            findViewById(R.id.brMinus).setOnClickListener(this);
            findViewById(R.id.brPlus).setOnClickListener(this);
            findViewById(R.id.lMinus).setOnClickListener(this);
            findViewById(R.id.lPlus).setOnClickListener(this);
            findViewById(R.id.dMinus).setOnClickListener(this);
            findViewById(R.id.dPlus).setOnClickListener(this);


        }

        @Override
        public void onClick(View v) {
            if(v.getId() == R.id.brMinus){
                if(isBreakfastOn){
                    int now = getInstantTime();
                    if(now < lastTimeOfBreakfast){
                        double prev = Double.parseDouble(br.getText().toString());
                        if(prev > 0) prev -= 0.5;
                        String fin = df.format(prev) + "";
                        br.setText(fin);
                    }else{
                        Toast.makeText(MaintainTodaysMeal.this, "Time up",
                                Toast.LENGTH_LONG).show();
                    }
                }else{
                    Toast.makeText(MaintainTodaysMeal.this, "Breakfast meal off",
                            Toast.LENGTH_LONG).show();
                }
            }
            if(v.getId() == R.id.brPlus){
                if(isBreakfastOn){
                    int now = getInstantTime();
                    if(now < lastTimeOfBreakfast){
                        double prev = Double.parseDouble(br.getText().toString());
                        prev += 0.5;
                        String fin = df.format(prev) + "";
                        br.setText(fin);
                    }else{
                        Toast.makeText(MaintainTodaysMeal.this, "Time up",
                                Toast.LENGTH_LONG).show();
                    }

                }else{
                    Toast.makeText(MaintainTodaysMeal.this, "Breakfast meal off",
                            Toast.LENGTH_LONG).show();
                }
            }
            if(v.getId() == R.id.lMinus){
                if(isLunchOn){
                    int now = getInstantTime();
                    if(now < lastTimeOfLunch){
                        double prev = Double.parseDouble(ln.getText().toString());
                        if(prev > 0) prev -= 0.5;
                        String fin = df.format(prev) + "";
                        ln.setText(fin);
                    }else{
                        Toast.makeText(MaintainTodaysMeal.this, "Time up",
                                Toast.LENGTH_LONG).show();
                    }

                }else{
                    Toast.makeText(MaintainTodaysMeal.this, "Lunch meal off",
                            Toast.LENGTH_LONG).show();
                }
            }
            if(v.getId() == R.id.lPlus){
                if(isLunchOn){
                    int now = getInstantTime();
                    if(now < lastTimeOfLunch){
                        double prev = Double.parseDouble(ln.getText().toString());
                        prev += 0.5;
                        String fin = df.format(prev) + "";
                        ln.setText(fin);
                    }else{
                        Toast.makeText(MaintainTodaysMeal.this, "Time up",
                                Toast.LENGTH_LONG).show();
                    }

                }else{
                    Toast.makeText(MaintainTodaysMeal.this, "Lunch meal off",
                            Toast.LENGTH_LONG).show();
                }
            }
            if(v.getId() == R.id.dMinus){
                if(isDinnerOn){
                    int now = getInstantTime();
                    if(now < lastTimeOfDinner){
                        double prev = Double.parseDouble(dn.getText().toString());
                        if(prev > 0) prev -= 0.5;
                        String fin = df.format(prev) + "";
                        dn.setText(fin);
                    }else{
                        Toast.makeText(MaintainTodaysMeal.this, "Time up",
                                Toast.LENGTH_LONG).show();
                    }

                }else{
                    Toast.makeText(MaintainTodaysMeal.this, "Dinner meal off",
                            Toast.LENGTH_LONG).show();
                }
            }
            if(v.getId() == R.id.dPlus){
                if(isDinnerOn){
                    int now = getInstantTime();
                    if(now < lastTimeOfDinner){
                        double prev = Double.parseDouble(dn.getText().toString());
                        prev += 0.5;
                        String fin = df.format(prev) + "";
                        dn.setText(fin);
                    }else{
                        Toast.makeText(MaintainTodaysMeal.this, "Time up",
                                Toast.LENGTH_LONG).show();
                    }

                }else{
                    Toast.makeText(MaintainTodaysMeal.this, "Dinner meal off",
                            Toast.LENGTH_LONG).show();
                }
            }
            if(v.getId() == R.id.reactivateMeal){

                double b = Double.parseDouble(br.getText().toString()),
                        l = Double.parseDouble(ln.getText().toString()),
                        d = Double.parseDouble(dn.getText().toString());
                TodayMealStatus todayMealStatus = new TodayMealStatus(selectedName, b, l, d,
                        b + l + d);
                rootRef.child(nameOfManager).child("Today's Meal Status").child(selectedName)
                        .child(getTodayDate()).setValue(todayMealStatus);

                dismiss();

            }
            if(v.getId() == R.id.cancelReactivation){
                dismiss();
            }
        }
    }

    private int getInstantTime(){
        String calender = Calendar.getInstance().getTime().toString();

        String h = calender.substring(11, 13),
                m = calender.substring(14, 16);
                //s = calender.substring(17, 19);

        return Integer.parseInt(h)*60 + Integer.parseInt(m);

    }

}