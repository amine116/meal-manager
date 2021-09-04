package com.amine.mealmanager;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.Lifecycle;

import android.os.Handler;
import android.text.InputType;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AlignmentSpan;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.SubscriptSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import org.xmlpull.v1.XmlPullParserException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Scanner;
import static com.amine.mealmanager.DiscussionActivity.getProfileName;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, ValueEventListener {

    public static ArrayList<Boarder> boarders, stoppedBoarders;
    public static ArrayList<MarketerHistory> marketerHistories;
    public static ArrayList<CooksBill> cooksBills;
    public static ArrayList<MemberSuggestion> memSug;
    public static Map<String, Payback> paybacks;
    public static ArrayList<UNotifications> unSeenNot, seenNot;
    private static ArrayList<TodayMealStatus> todayMealStatuses, mealStatuses;
    private static ArrayList<String> monthNames;
    private File rootFolder, evidence, loggedInPersonFile, selectedMonth;
    private TextView txtTotalPaid, txtTotalMeal, txtMealRate, txtTotalCost,
            txtLastUpdate, txtReminderMoney;
    public static final int MAX_BOARDER = 100;

    public static boolean IS_MANAGER = false, LOGGED_OUT = true,
            isBreakfastOn, isLunchOn, isDinnerOn, readingPermissionAccepted = false,
            isAnimationAlive = false;
    private boolean isAddingBoarder = false, isMonthIndThreadRunning = false;

    public static double totalMeal = 0.0, totalPaid = 0.0, mealRate = 0.0, totalCost = 0.0,
            extraMoney = 0, stoppedMeal = 0.0, stoppedCost = 0.0, fakeTotalCost, fakeTotalMeal,
            reminderMoney;
    public static final DecimalFormat df =  new DecimalFormat("0.#");
    public static DatabaseReference rootRef, lastUpdateRef, lastChangingTimeRef, marketHistoryRef,
            mealPeriodRef, mealStatusRef, postRef, todayMealStatusRef, cookBillRef, membersRef,
            readingRef, monthRef;
    private static String lastUpdate = "", nameOfManager = "", announcement = "",
            strMonth = "", curProfileName = "";
    private String updatableMealString = "", newVersionName = "";
    private static final String INFO_FILE = "Info.txt", MEMBER_NAME_FILE = "Member Name.txt",
            FILE_MONTH = "selected month";
    private FirebaseAuth fAuth;
    private DrawerLayout drawerLayout;
    public static int selectedBoarderIndex = -1,
            selectedStoppedBoarderIndex = -1, selectedCookBillIndex = -1;
    private boolean isMainActivityRunning = false;
    private final Dialog[] dialogs = new Dialog[5];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) actionBar.hide();
        initialize();
        makeManagerOrMemberView();
    }

    private void makeManagerOrMemberView() {
        if(!IS_MANAGER){
            findViewById(R.id.updateData).setVisibility(View.GONE);
            findViewById(R.id.updatePayment).setVisibility(View.GONE);
            findViewById(R.id.eraseAll).setVisibility(View.GONE);
            findViewById(R.id.layout_goToPayBack).setVisibility(View.GONE);
            //findViewById(R.id.addBoarder).setVisibility(View.GONE);

            TextView b = findViewById(R.id.addMemberBtnText);
            String s = "Suggest Member";
            b.setText(s);

        }
    }

    private void countOfTodaysMeal(){
        double b = 0, l = 0, d = 0;
        for(int i = 0; i < mealStatuses.size(); i++){
            int ind = foundInTodaysMeal(mealStatuses.get(i).getName());
            if(ind > -1){
                b += todayMealStatuses.get(ind).getBreakFirst();
                l += todayMealStatuses.get(ind).getLunch();
                d += todayMealStatuses.get(ind).getDinner();
            }else{
                b += mealStatuses.get(i).getBreakFirst();
                l += mealStatuses.get(i).getLunch();
                d += mealStatuses.get(i).getDinner();
            }
        }
        String todayMeal = "Today's Meals: " + df.format(b) + " + " + df.format(l)
                + " + " + df.format(d) + " = " + df.format((b + l + d));
        updatableMealString = todayMeal;
        TextView tv = findViewById(R.id.txtNumberOfTodayMeal);
        tv.setText(todayMeal);
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MaintainTodaysMeal.class);
                startActivity(intent);
            }
        });

    }

    private int foundInTodaysMeal(String name){
        for(int i = 0; i < todayMealStatuses.size(); i++){
            if(name.equals(todayMealStatuses.get(i).getName())) return i;
        }
        return -1;
    }

    private void checkAndSetSecurity(){

        if(fAuth.getCurrentUser() == null){
            if(evidence.exists()){
                try {
                    Scanner scanner = new Scanner(evidence);
                    if(scanner.hasNextLine()){
                        nameOfManager = scanner.nextLine();
                        LOGGED_OUT = false;
                        IS_MANAGER = false;
                        if(strMonth.equals("Default"))
                            rootRef = FirebaseDatabase.getInstance().getReference().child(nameOfManager);
                        else rootRef = FirebaseDatabase.getInstance().getReference().child(nameOfManager)
                                .child(strMonth);
                        readingRef = rootRef.child("readingNode");
                        readingPermissionAccepted = true;
                        readingRef.addValueEventListener(this);

                        //readSelectedBoarder();

                        setReferences();

                    }else{
                        setLogInPage();
                        IS_MANAGER = false;
                        LOGGED_OUT = true;
                    }

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
            else{
                setLogInPage();
                IS_MANAGER = false;
                LOGGED_OUT = true;
            }
        }
        else{
            try {

                //readSelectedBoarder();

                nameOfManager = getUsernameFromEmail(fAuth.getCurrentUser().getEmail());
                IS_MANAGER = true;
                LOGGED_OUT = false;
                if(strMonth.equals("Default"))
                    rootRef = FirebaseDatabase.getInstance().getReference().child(nameOfManager);
                else rootRef = FirebaseDatabase.getInstance().getReference().child(nameOfManager)
                        .child(strMonth);
                readingRef = rootRef.child("readingNode");
                readingPermissionAccepted = true;
                readingRef.addValueEventListener(this);
                setReferences();
                FirebaseDatabase.getInstance().getReference().child("change request")
                        .child("users").child(nameOfManager)
                        .child("lastActivity").setValue(getTodayDate());
            }catch (Exception e){
                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
        //Log.i("test", rootRef.toString());
    }

    private void readSelectedBoarder(){
        try {
            if(loggedInPersonFile.exists()){
                Scanner scanner1 = new Scanner(loggedInPersonFile);
                if(scanner1.hasNextLine()){
                    selectedBoarderIndex = Integer.parseInt(scanner1.nextLine());
                    selectedStoppedBoarderIndex = Integer.parseInt(scanner1.nextLine());
                    selectedCookBillIndex = Integer.parseInt(scanner1.nextLine());

                    if(selectedBoarderIndex >= boarders.size())
                        selectedBoarderIndex = -1;
                    if(selectedStoppedBoarderIndex >= stoppedBoarders.size())
                        selectedStoppedBoarderIndex = -1;
                    if(selectedCookBillIndex >= cooksBills.size())
                        selectedCookBillIndex = -1;


                }
                scanner1.close();
            }
        }
        catch (Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void setSelectedMonth(String strMonth){
        if(selectedMonth.exists()){
            try {
                if(strMonth.isEmpty()){
                    Toast.makeText(this, "Need month name", Toast.LENGTH_SHORT).show();
                }
                else{
                    PrintWriter pr = new PrintWriter(selectedMonth);
                    pr.println(strMonth);
                    pr.close();
                }
            }catch (Exception e){
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
        else{
            createNeededFiles();
        }
    }
    private void getSelectedMonth(){
        if(selectedMonth.exists()){
            try {
                Scanner sc = new Scanner(selectedMonth);
                if(sc.hasNextLine()){
                    strMonth = sc.nextLine();
                }
                else strMonth = "Default";
                sc.close();
            } catch (FileNotFoundException e) {
                strMonth = "Default";
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
        else strMonth = "Default";
    }

    private void setReferences(){

        lastUpdateRef = rootRef.child("lastUpdate");
        lastChangingTimeRef = rootRef.child("Last time Of changing meals");
        marketHistoryRef = rootRef.child("Marketer History");
        mealPeriodRef = rootRef.child("Meal Period");
        mealStatusRef = rootRef.child("Meal Status");
        postRef = rootRef.child("Post");
        todayMealStatusRef = rootRef.child("Today's Meal Status");
        cookBillRef = rootRef.child("cooksBill");
        membersRef = rootRef.child("members");
        monthRef = FirebaseDatabase.getInstance().getReference().child(nameOfManager)
                .child("--month-names-");

    }

    private String getUsernameFromEmail(String email){
        int i = 0;
        while (i < email.length() && email.charAt(i) != '@') i++;

        return email.substring(0, i);
    }

    private void makeViewsInvisible(){
        findViewById(R.id.infoLayout).setVisibility(View.INVISIBLE);
        findViewById(R.id.imgAnimate).setVisibility(View.VISIBLE);
        findViewById(R.id.monthNamesLayout).setVisibility(View.GONE);
        isAnimationAlive = true;
        animate();
    }

    private void makeViewsVisible(){
        findViewById(R.id.infoLayout).setVisibility(View.VISIBLE);
        isAnimationAlive = false;
        findViewById(R.id.imgAnimate).setVisibility(View.GONE);
        findViewById(R.id.monthNamesLayout).setVisibility(View.VISIBLE);

    }

    private void animate(){
        final Handler handler = new Handler(getApplicationContext().getMainLooper());
        final ImageView imgAnimate = findViewById(R.id.imgAnimate);
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
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
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
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
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
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
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
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
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
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
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
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
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
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
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
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
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
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
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
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
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
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
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
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
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
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
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
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            }
        }).start();
    }

    private void setStatistics(){
        fakeTotalCost = totalCost - stoppedCost;
        fakeTotalMeal = totalMeal - stoppedMeal;


        String s = "Total meal: " + df.format(totalMeal) + ",\nCurrent Meal: " + df.format(fakeTotalMeal);
        txtTotalMeal.setText(s);
        s = "Total Cost: " + df.format(totalCost) + ",\nCurrent cost: " + df.format(fakeTotalCost);
        txtTotalCost.setText(s);
        reminderMoney = getReminderMoney();
        if(fakeTotalMeal > 0) mealRate = fakeTotalCost/fakeTotalMeal;
        else{
            if(totalMeal > 0) mealRate = totalCost/totalMeal;
            else mealRate = 0;
        }

        s = "Total Members: " + boarders.size();
        TextView tv = findViewById(R.id.txtTotalMember);
        tv.setText(s);
        s = "Reminder Money: " + df.format(reminderMoney);
        txtReminderMoney.setText(s);
        s = "Current meal rate: " + df.format(mealRate) +
                " (" + df.format(fakeTotalCost) + "÷" + df.format(fakeTotalMeal) + ")";
        txtMealRate.setText(s);
        s = "Total Paid: " + df.format(totalPaid);
        txtTotalPaid.setText(s);
        TextView editText = findViewById(R.id.txtExtraMoney);
        s = "Extra Money: " + df.format(extraMoney);
        editText.setText(s);
    }

    public static double getReminderMoney(){
        return (totalPaid - totalCost - getTotalPayback());
    }

    private static double getTotalPayback() {
        double total = 0;
        for(Map.Entry<String, Payback> paybackEntry : paybacks.entrySet()){
            total += paybackEntry.getValue().getAmount();
        }
        return total;
    }

    private void createDialogs(){
        SelectNamesForLogIn d1 = new SelectNamesForLogIn(MainActivity.this);
        BazaarNoticeForUsers d2 = new BazaarNoticeForUsers(this);
        dialogs[0] = d1; dialogs[1] = d2;
        dialogs[3] = new SelectMonth(this);

        readVersionName(new Wait() {
            @Override
            public void onCallback() {
                readReleaseNote(new ReadRNote() {
                    @Override
                    public void onCallback(String note) {

                        String info = "New version " + newVersionName + " available!\n" +
                                "Having new: \n" + note;

                        AppUpdateNoticeForUsers d3 =
                                new AppUpdateNoticeForUsers(MainActivity.this, info);

                        dialogs[2] = d3;
                    }
                });
            }
        });
    }

    private void readVersionName(final Wait wait){
        FirebaseDatabase.getInstance().getReference().child("change request")
                .child("versionName").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                newVersionName = "";
                if(snapshot.exists()){
                    newVersionName = snapshot.getValue(String.class);
                }
                if(newVersionName == null) newVersionName = BuildConfig.VERSION_NAME;
                if(newVersionName.equals("")) newVersionName = BuildConfig.VERSION_NAME;

                wait.onCallback();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void readReleaseNote(final ReadRNote wait){
        FirebaseDatabase.getInstance().getReference().child("change request").child("version-details")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String versionDetails = "";
                        if(snapshot.exists()){
                            versionDetails = snapshot.getValue(String.class);
                        }
                        wait.onCallback(versionDetails);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private interface ReadRNote{
        void onCallback(String note);
    }

    private void dismissDialogs(){
        if(dialogs[0] != null) dialogs[0].dismiss();
        if(dialogs[1] != null) dialogs[1].dismiss();
        if(dialogs[2] != null) dialogs[2].dismiss();
        if(dialogs[3] != null) dialogs[3].dismiss();
    }

    private void profileSelectionDialog(Wait wait){

        if(selectedBoarderIndex == -1 && selectedStoppedBoarderIndex == -1 &&
                selectedCookBillIndex == -1 && (boarders.size() != 0 ||
                stoppedBoarders.size() != 0) && isMainActivityRunning && !isAddingBoarder){

            if(dialogs[0] != null) dialogs[0].dismiss();
            dialogs[0] = null;
            dialogs[0] = new SelectNamesForLogIn(MainActivity.this);

            dialogs[0].show();
        }

        wait.onCallback();
    }

    private void showNotice(Wait wait) {
        boolean isShown = false;
        Random random = new Random();
        int randomInt = random.nextInt(10)%2;

        if((boarders.size() > 0 || stoppedBoarders.size() > 0) &&
                totalCost == 0 && IS_MANAGER && !isAddingBoarder &&
                isMainActivityRunning && dialogs[1] != null){

            isShown = true;
            dialogs[1].show();
        }

        if (!isShown && !newVersionName.equals(BuildConfig.VERSION_NAME) &&
                !isAddingBoarder && isMainActivityRunning && randomInt == 1 && dialogs[2] != null){

            dialogs[2].show();
        }

        wait.onCallback();

    }

    @Override
    public void onDataChange(@NonNull DataSnapshot snapshot) {

        if(readingPermissionAccepted){
            makeViewsInvisible();
            disableNavItems();
            readMonths(new Wait() {
                @Override
                public void onCallback() {
                    readFromDatabase(new CallBack() {
                        @Override
                        public void onCallback() {

                            readSelectedBoarder();
                            fillMonths();
                            setRadioButtonMonthSelection(getMonthIndex(strMonth));
                            readingPermissionAccepted = false;
                            makeViewsVisible();
                            enableNavItems();
                            setMemberApproval();

                            if(isAddingBoarder && !curProfileName.equals("")){
                                saveSelectedName(curProfileName);
                            }

                            showNotice(new Wait() {
                                @Override
                                public void onCallback() {

                                }
                            });

                            profileSelectionDialog(new Wait() {
                                @Override
                                public void onCallback() {

                                }
                            });

                            showSelectMonthDialog(new Wait() {
                                @Override
                                public void onCallback() {

                                }
                            });

                            if(boarders.size() == 0 && stoppedBoarders.size() == 0){
                                setHadith();
                            }
                            else {

                                findViewById(R.id.infoLayout).setVisibility(View.VISIBLE);
                                findViewById(R.id.layout_hadith).setVisibility(View.GONE);

                                setStatistics();
                                recalculate();
                                countOfTodaysMeal();
                                announce();
                                txtLastUpdate.setText(lastUpdate);

                                setProfile(selectedBoarderIndex, selectedStoppedBoarderIndex,
                                        selectedCookBillIndex);

                            }
                        }
                    });
                }
            });
        }
    }

    private void readMonths(final Wait wait){
        monthRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                monthNames.clear();
                monthNames.add("Default");
                if(snapshot.exists()){
                    for(DataSnapshot months : snapshot.getChildren()){
                        String month = months.getValue(String.class);
                        if(month != null) monthNames.add(month);
                    }
                }
                wait.onCallback();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setHadith(){
        findViewById(R.id.infoLayout).setVisibility(View.GONE);
        findViewById(R.id.layout_hadith).setVisibility(View.VISIBLE);
        if(IS_MANAGER){
            findViewById(R.id.addBoarder2).setVisibility(View.VISIBLE);
            findViewById(R.id.addBoarder2).setOnClickListener(this);
        }
        else{
            findViewById(R.id.addBoarder2).setVisibility(View.GONE);
            TextView b = findViewById(R.id.addMemberBtnText2);
            String s = "Suggest Member";
            b.setText(s);
        }
        if(boarders.size() == 0 && stoppedBoarders.size() == 0){

            TextView txtHadith = findViewById(R.id.txtHadith),
                    txtInstructions = findViewById(R.id.txtInstruction);
            String hadith = "\"Give food to the hungry,\n" +
                    "pay a visit to the sick and release (set free) \n" +
                    "the one in captivity (by paying his ransom)\".\n",

                    referenceText =
                            "----------------------------------------------------------------- Muhammad(ﷺ)\n" +
                                    "Sahih Al Bukhari 1.\n" +
                                    "Chapter 71.\n" +
                                    "Hadith: 5373\n\n\n",

                    welcomeText = "Welcome to the new session of eating \uD83D\uDE0E.\n",
                    addMemberText = "Add members ",
                    lastText = "to get your meal start. You can see Instructions for better understanding!",
                    s, s1, s2,

                    mainText = hadith + referenceText + welcomeText + addMemberText + lastText;

            Spannable spannable = new SpannableString(mainText);

            // TODO
            //  HADITH
            spannable.setSpan(new RelativeSizeSpan(1f), 0,
                    hadith.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            spannable.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), 0,
                    hadith.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            // TODO
            //  Reference
            s = hadith + referenceText;
            spannable.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_OPPOSITE),
                    hadith.length(), s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            spannable.setSpan(new RelativeSizeSpan(1f),
                    hadith.length(), s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            // TODO
            //  welcome text
            s1 = hadith + referenceText;
            s = hadith + referenceText + welcomeText;
            spannable.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER),
                    s1.length(),
                    s.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            spannable.setSpan(new RelativeSizeSpan(1.5f),
                    s1.length(),
                    s.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            // TODO
            //  add member
            s1 = hadith + referenceText + welcomeText;
            s = hadith + referenceText + welcomeText + addMemberText;
            spannable.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER),
                    s1.length(),
                    s.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            spannable.setSpan(new RelativeSizeSpan(1.5f),
                    s1.length(),
                    s.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);


            spannable.setSpan(new ForegroundColorSpan(Color.RED),
                    s1.length(),
                    s.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);


            // TODO
            //  welcome text
            s1 = hadith + referenceText + welcomeText + addMemberText;
            s = hadith + referenceText + welcomeText + addMemberText + lastText;
            spannable.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER),
                    s1.length(),
                    s.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            spannable.setSpan(new RelativeSizeSpan(1.5f),
                    s1.length(),
                    s.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);


            txtHadith.setText(spannable, TextView.BufferType.SPANNABLE);


            s2 = "\nSee Instructions";
            txtInstructions.setText(s2);
            txtInstructions.setTextColor(Color.BLACK);
            txtInstructions.setTypeface(null, Typeface.BOLD);
            txtInstructions.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, 150));
            txtInstructions.setGravity(Gravity.CENTER);
            txtInstructions.setTextSize(20);
            txtInstructions.setPaintFlags(txtInstructions.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            txtInstructions.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, ActivityInstructions.class);
                    startActivity(intent);
                }
            });



        }
    }

    private void disableNavItems() {
        findViewById(R.id.layout_goOverView).setEnabled(false);
        findViewById(R.id.layout_goDetails).setEnabled(false);
        findViewById(R.id.layout_goCookBill).setEnabled(false);
        findViewById(R.id.layout_goBazaarList).setEnabled(false);
        findViewById(R.id.layout_goToPayBack).setEnabled(false);
        findViewById(R.id.updateData).setEnabled(false);
        findViewById(R.id.updatePayment).setEnabled(false);
        findViewById(R.id.btnDiscussion).setEnabled(false);
        findViewById(R.id.eraseAll).setEnabled(false);
    }

    private void enableNavItems() {
        findViewById(R.id.layout_goOverView).setEnabled(true);
        findViewById(R.id.layout_goDetails).setEnabled(true);
        findViewById(R.id.layout_goCookBill).setEnabled(true);
        findViewById(R.id.layout_goBazaarList).setEnabled(true);
        findViewById(R.id.layout_goToPayBack).setEnabled(true);
        findViewById(R.id.updateData).setEnabled(true);
        findViewById(R.id.updatePayment).setEnabled(true);
        findViewById(R.id.btnDiscussion).setEnabled(true);
        findViewById(R.id.eraseAll).setEnabled(true);
    }

    private void announce() {
        ImageView iv = findViewById(R.id.imgAnnouncement);
        iv.setOnClickListener(this);
        if(!announcement.equals("")){
            getAnnouncementThread(iv).start();
        }
    }

    private Thread getAnnouncementThread(final ImageView iv){
        final Handler handler = new Handler();
        return new Thread(new Runnable() {
            @Override
            public void run() {
                for(int i = 1; i < 60; i++){
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            iv.setBackgroundColor(Color.WHITE);
                        }
                    });

                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }


                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            iv.setBackgroundColor(getResources().getColor(R.color.discussion_write));
                        }
                    });

                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void setProfile(final int bI, final int sbI, final int cbI){
        final TextView txtProfileName = findViewById(R.id.txtProfileName),
                txtProfileConsumedMeal = findViewById(R.id.txtProfileConsumedMeal),
                txtProfilePaid = findViewById(R.id.txtProfilePaid),
                txtProfileCost = findViewById(R.id.txtProfileCost),
                txtProfileMelDueOrOverHead = findViewById(R.id.txtProfileMelDueOrOverHead),
                txtProfileCookBill = findViewById(R.id.txtProfileCookBill),
                txtMealState = findViewById(R.id.txtProfileMealState),
                txtProfileTotalDueOrOverHead = findViewById(R.id.txtProfileTotalDueOrOverHead);

        makeViewsInvisible();
        final String profileName = getProfileName(bI, sbI, cbI);
        final Payback p = paybacks.get(profileName);
        readNotific(profileName, new Wait() {
            @Override
            public void onCallback() {
                makeViewsVisible();
                setUnseenNotToFrame(getResources().getColor(R.color.pure_red));
                double bMeal = 0, bPaid = 0, bCost = 0, bDue = 0, bOve = 0, cBill = 0,
                        due = bDue, ove = bOve, sbMeal = 0, sbPaid = 0;


                if(bI != -1 && bI < boarders.size()){
                    bMeal = boarders.get(bI).getMeals();
                    bPaid = boarders.get(bI).getPaidMoney();
                    bCost = boarders.get(bI).getMeals() * mealRate;
                    bDue = boarders.get(bI).getDue();
                    bOve = boarders.get(bI).getOverHead();
                    cBill = 0;
                    due = bDue;
                    ove = bOve;
                    sbMeal = 0;
                    sbPaid = 0;
                }

                if(cbI != -1 && cbI < cooksBills.size()){
                    cBill = Double.parseDouble(cooksBills.get(cbI).getPaid());
                }
                if(sbI != -1 && sbI < stoppedBoarders.size()){

                    Boarder boarder = stoppedBoarders.get(sbI);

                    due += boarder.getDue();
                    ove += boarder.getOverHead();
                    sbMeal += boarder.getMeals();
                    sbPaid += boarder.getPaidMoney();

                    if(boarder.getDue() == 0)
                        bCost += (boarder.getPaidMoney() - boarder.getOverHead());
                    else bCost += (boarder.getPaidMoney() + boarder.getDue());
                }

                txtProfileName.setText(profileName);

                String s = "Consumed Meal: " + df.format(bMeal + sbMeal);
                txtProfileConsumedMeal.setText(s);

                s = "Paid: " + df.format(bPaid + sbPaid);
                txtProfilePaid.setText(s);

                s = "Cost: " + df.format(bCost);
                txtProfileCost.setText(s);

                if(p != null){
                   ove -= p.getAmount();

                    s = "Refunded: " + p.getAmount();
                }else{
                    s = "Refunded: 0";
                }
                txtProfileTotalDueOrOverHead.setText(s);

                s = ove > due ?
                        "Overhead: " + df.format(ove - due): "Due: "
                        + df.format(due - ove);

                txtProfileMelDueOrOverHead.setText(s);
                if(ove > due) txtProfileMelDueOrOverHead.setTextColor(Color.GREEN);
                else txtProfileMelDueOrOverHead.setTextColor(Color.RED);

                s = "Cook Bill Paid: " + df.format(cBill);
                txtProfileCookBill.setText(s);

                if(selectedBoarderIndex == -1) s = "Status: Off";
                else s = "Status: On";
                txtMealState.setText(s);


            }
        });
    }

    private void setUnseenNotToFrame(int color) {
        TextView textView = findViewById(R.id.txtNotification);
        String s = unSeenNot.size() + "";
        if(unSeenNot.size() > 0){
            textView.setText(s);
            textView.setTextColor(color);
        }
        else{
            textView.setText("");
            textView.setTextColor(Color.WHITE);
        }
    }

    private void readNotific(String profileName, final Wait wait) {
        if(IS_MANAGER) profileName = nameOfManager + "-Manager-";
        final DatabaseReference notR = rootRef.child("notifications").child(profileName);

        notR.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                unSeenNot.clear();
                seenNot.clear();
                if(snapshot.child("unseen").exists()){
                    /*
                    GenericTypeIndicator<ArrayList<UNotifications>> t =
                            new GenericTypeIndicator<ArrayList<UNotifications>>() {};
                    unSeenNot = snapshot.child("unseen").getValue(t);

                     */
                    for(DataSnapshot ids : snapshot.child("unseen").getChildren()){
                        UNotifications not = ids.getValue(UNotifications.class);
                        if(not != null){
                            unSeenNot.add(not);
                        }

                    }

                }
                if(snapshot.child("seen").exists()){
                    for(DataSnapshot ids : snapshot.child("seen").getChildren()){
                        UNotifications not = ids.getValue(UNotifications.class);
                        if(not != null){
                            seenNot.add(not);
                        }

                    }
                }
                wait.onCallback();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onCancelled(@NonNull DatabaseError error) {

    }


    private int getMonthIndex(String month){
        for(int i = 0; i < monthNames.size(); i++) {
            if (monthNames.get(i).equals(month)) return i;
        }

        return -1;
    }

    private class SelectNamesForLogIn extends Dialog{

        public SelectNamesForLogIn(@NonNull Context context) {
            super(context);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.select_name_page);
            initialize();

        }

        private void initialize(){

            findViewById(R.id.layoutBtnSelectNamePage).setVisibility(View.GONE);
            int gapH = 30, textSize = 20;

            LinearLayout selectName = findViewById(R.id.layout_selectName);
            for(int i = 0; i < boarders.size(); i++){

                LinearLayout ll = new LinearLayout(MainActivity.this);
                selectName.addView(ll);
                ll.setLayoutParams(new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                ll.setPadding(3, 3, 3,3);
                ll.setBackgroundColor(getResources().getColor(R.color.light_green));
                final TextView txtName = new TextView(MainActivity.this);
                TextView txtGap = new TextView(MainActivity.this);
                ll.addView(txtName);
                selectName.addView(txtGap);


                txtName.setLayoutParams(new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                txtName.setBackgroundColor(getResources().getColor(R.color.white));
                txtName.setTextSize(textSize);

                txtGap.setLayoutParams(new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, gapH));

                String s = boarders.get(i).getName();
                txtName.setGravity(Gravity.CENTER);
                txtName.setText(s);

                txtName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        saveSelectedName(txtName.getText().toString());
                        dismiss();
                    }
                });
            }

            for(int i = 0; i < stoppedBoarders.size(); i++){

                String s = stoppedBoarders.get(i).getName();
                if(isBoarderOn(s) == 0) {

                    LinearLayout ll = new LinearLayout(MainActivity.this);
                    selectName.addView(ll);
                    ll.setLayoutParams(new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    ll.setPadding(3, 3, 3,3);
                    ll.setBackgroundColor(getResources().getColor(R.color.light_green));

                    final TextView txtName = new TextView(MainActivity.this),
                            txtGap = new TextView(MainActivity.this);

                    ll.addView(txtName);
                    txtName.setBackgroundColor(getResources().getColor(R.color.white));
                    txtName.setTextSize(textSize);

                    selectName.addView(txtGap);

                    txtName.setLayoutParams(new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                    txtGap.setLayoutParams(new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, gapH));

                    txtName.setGravity(Gravity.CENTER);
                    txtName.setText(s);

                    txtName.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            saveSelectedName(txtName.getText().toString());
                            dismiss();
                        }
                    });
                }
            }
        }

    }

    private void saveSelectedName(String name){

        selectedBoarderIndex = -1;
        selectedStoppedBoarderIndex = -1;
        selectedCookBillIndex = -1;


        for(int i = 0; i < boarders.size(); i++){
            if(boarders.get(i).getName().equals(name)){
                selectedBoarderIndex = i;
                break;
            }
        }

        for(int i = 0; i < stoppedBoarders.size(); i++){
            if(stoppedBoarders.get(i).getName().equals(name)){
                selectedStoppedBoarderIndex = i;
                break;
            }
        }

        for(int i = 0; i < cooksBills.size(); i++){
            if(cooksBills.get(i).getName().equals(name)){
                selectedCookBillIndex = i;
                break;
            }
        }

        saveSelectedIndexes();
    }

    private void saveSelectedIndexes() {
        if(loggedInPersonFile.exists()){
            try {
                PrintWriter pr = new PrintWriter(loggedInPersonFile);
                pr.println(selectedBoarderIndex);
                pr.println(selectedStoppedBoarderIndex);
                pr.println(selectedCookBillIndex);

                pr.close();

                setProfile(selectedBoarderIndex, selectedStoppedBoarderIndex, selectedCookBillIndex);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private int isBoarderOn(String name){
        for(int i = 0; i < boarders.size(); i++){
            if(boarders.get(i).getName().equals(name)) return (i + 1);
        }

        return 0;
    }

    public static class MakeAnnouncement extends Dialog implements View.OnClickListener{

        private EditText editText;

        public MakeAnnouncement(@NonNull Context context) {
            super(context);
        }

        @Override
        public void onClick(View v) {
            if(v.getId() == R.id.btnMakeAnnouncement){

                editText = findViewById(R.id.edtEnterAnnouncement);
                String announcement = editText.getText().toString();

                DatabaseReference r = rootRef.child("announcement").child(getTodayDate());
                r.setValue(announcement);

                readingRef.setValue("");
                readingPermissionAccepted = true;
                readingRef.setValue("read");

                dismiss();
            }
            if(v.getId() == R.id.btnCancelAnnouncement){
                dismiss();
            }
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.announcemnet);
            initialize();
        }

        private void initialize(){
            findViewById(R.id.btnMakeAnnouncement).setOnClickListener(this);
            findViewById(R.id.btnCancelAnnouncement).setOnClickListener(this);

            editText = findViewById(R.id.edtEnterAnnouncement);
            if(!announcement.isEmpty()) editText.setText(announcement);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
    public void initialize(){
        txtTotalPaid = findViewById(R.id.txtTotalPaid);
        txtTotalMeal = findViewById(R.id.txtTotalMeal);
        txtMealRate = findViewById(R.id.txtMealRate);
        txtReminderMoney = findViewById(R.id.txtReminderMoney);
        txtTotalCost = findViewById(R.id.txtTotalCost);
        txtLastUpdate = findViewById(R.id.lastUpdate);

        boarders = new ArrayList<>();
        marketerHistories = new ArrayList<>();
        cooksBills = new ArrayList<>();
        mealStatuses = new ArrayList<>();
        todayMealStatuses = new ArrayList<>();
        stoppedBoarders = new ArrayList<>();
        memSug = new ArrayList<>();
        paybacks = new HashMap<>();
        unSeenNot = new ArrayList<>();
        seenNot = new ArrayList<>();
        monthNames = new ArrayList<>();

        fAuth = FirebaseAuth.getInstance();

        rootFolder = getCacheDir();
        evidence = new File(rootFolder, INFO_FILE);
        loggedInPersonFile = new File(rootFolder, MEMBER_NAME_FILE);
        selectedMonth = new File(rootFolder, FILE_MONTH);

        drawerLayout = findViewById(R.id.drawer_main);

        findViewById(R.id.menu_more).setOnClickListener(this);
        findViewById(R.id.img_navigation_menu).setOnClickListener(this);
        findViewById(R.id.layout_goOverView).setOnClickListener(this);
        findViewById(R.id.layout_goDetails).setOnClickListener(this);
        findViewById(R.id.layout_goCookBill).setOnClickListener(this);
        findViewById(R.id.layout_goBazaarList).setOnClickListener(this);
        findViewById(R.id.layout_goToPayBack).setOnClickListener(this);
        findViewById(R.id.addBoarder).setOnClickListener(MainActivity.this);
        findViewById(R.id.updateData).setOnClickListener(MainActivity.this);
        findViewById(R.id.btnDiscussion).setOnClickListener(MainActivity.this);
        findViewById(R.id.updatePayment).setOnClickListener(MainActivity.this);
        findViewById(R.id.eraseAll).setOnClickListener(MainActivity.this);
        findViewById(R.id.imgProfileChange).setOnClickListener(this);
        findViewById(R.id.txtSeeProfileDetails).setOnClickListener(this);
        findViewById(R.id.txtNotification).setOnClickListener(this);
        findViewById(R.id.txtSeeMealDetails).setOnClickListener(this);
        findViewById(R.id.imgRefresh).setOnClickListener(this);
        findViewById(R.id.navLayoutFeedback).setOnClickListener(this);

        setOnClickToRadioMonths();

        createNeededFiles();
        getSelectedMonth();
        checkAndSetSecurity();
        setDrawerHome();
        getMonthIndThread();
    }

    private void setDrawerHome() {
        TextView tv = findViewById(R.id.txtHomeManagerName);
        tv.setText(nameOfManager);
    }

    private void createNeededFiles(){
        if(!rootFolder.exists()){
            if(!rootFolder.mkdir()){
                Toast.makeText(MainActivity.this, rootFolder.toString() + "\nCan't be created",
                        Toast.LENGTH_LONG).show();
            }
        }

        if(!evidence.exists()){
            try {
                if(!evidence.createNewFile()){
                    Toast.makeText(MainActivity.this,
                            "Problem with creating evidence file", Toast.LENGTH_LONG).show();
                }
            } catch (IOException e) {
                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
        if(!loggedInPersonFile.exists()){
            try {
                if(!loggedInPersonFile.createNewFile()){
                    Toast.makeText(MainActivity.this,
                            "Problem with creating login file", Toast.LENGTH_LONG).show();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(!selectedMonth.exists()){
            try {
                if(!selectedMonth.createNewFile()){
                    Toast.makeText(this,
                            "problem with creating month file", Toast.LENGTH_SHORT).show();
                }
            }catch (Exception e){
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }

    }

    private void disableView(int second, final View view){
        final int sec = second*1000;
        view.setEnabled(false);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(sec);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            view.setEnabled(true);
                        }
                    });
                } catch (InterruptedException e) {
                    Toast.makeText(MainActivity.this, e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
            }
        }).start();
    }

    @Override
    public void onClick(View v) {

        int viewDisabledTime = 2;

        if(v.getId() == R.id.addBoarder ||
            v.getId() == R.id.addBoarder2){

            if(v.getId() == R.id.addBoarder2)
                disableView(viewDisabledTime, findViewById(R.id.addBoarder2));
            else if(v.getId() == R.id.addBoarder)
                disableView(viewDisabledTime, findViewById(R.id.addBoarder));

            if(boarders.size() + 1 == MAX_BOARDER){

                Toast.makeText(MainActivity.this,
                        "Member more than " + MAX_BOARDER + " isn't allowed!", Toast.LENGTH_LONG).show();
                return;
            }
            AddMemberDialog addMemberDialog = new AddMemberDialog(MainActivity.this);
            addMemberDialog.show();
        }

        if(v.getId() == R.id.layoutSuggestion){
            ApproveMember d = new ApproveMember(this);
            d.show();
        }
        if(v.getId() == R.id.updateData){

            disableView(viewDisabledTime, findViewById(R.id.updateData));

            if(stoppedBoarders.size() == 0 && boarders.size() == 0){
                Toast.makeText(this, "You need to add " +
                        "members to use this feature", Toast.LENGTH_SHORT).show();
                return;
            }

            UpdateMealDialog updateMealDialog = new UpdateMealDialog(MainActivity.this);
            updateMealDialog.show();

        }
        if(v.getId() == R.id.btnDiscussion){
            Intent intent = new Intent(MainActivity.this, DiscussionActivity.class);
            startActivity(intent);
        }
        if(v.getId() == R.id.updatePayment){

            disableView(viewDisabledTime, findViewById(R.id.updatePayment));

            if(stoppedBoarders.size() == 0 && boarders.size() == 0){
                Toast.makeText(this, "You need to add " +
                        "members to use this feature", Toast.LENGTH_SHORT).show();
                return;
            }

            UpdatePaymentDialog updatePaymentDialog = new UpdatePaymentDialog(MainActivity.this);
            updatePaymentDialog.show();
        }
        if(v.getId() == R.id.eraseAll){

            EraseAll eraseAll = new EraseAll(MainActivity.this);
            eraseAll.show();
        }
        if(v.getId() == R.id.img_navigation_menu){
            openDrawer(drawerLayout);
        }
        if(v.getId() == R.id.imgRefresh){
            finish();
            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
        }
        if(v.getId() == R.id.menu_more){

            PopupMenu menu = new PopupMenu(this, v);
            MenuInflater inflater = menu.getMenuInflater();
            inflater.inflate(R.menu.menu_main, menu.getMenu());

            if(IS_MANAGER){
                menu.getMenu().findItem(R.id.updateMPDPP).setVisible(true);
                menu.getMenu().findItem(R.id.makeAnnouncement).setVisible(true);
                menu.getMenu().findItem(R.id.manageTodayMeal).setVisible(true);
                menu.getMenu().findItem(R.id.lastTimeOfChangingMeal).setVisible(true);
                menu.getMenu().findItem(R.id.changePassword).setVisible(true);
                menu.getMenu().findItem(R.id.btnLogOut).setVisible(true);
                menu.getMenu().findItem(R.id.memberPasswords).setVisible(true);
                menu.getMenu().findItem(R.id.menu_editInfo).setVisible(true);
                menu.getMenu().findItem(R.id.menu_newMonth).setVisible(true);
            }
            else if(LOGGED_OUT){
                menu.getMenu().findItem(R.id.updateMPDPP).setVisible(false);
                menu.getMenu().findItem(R.id.makeAnnouncement).setVisible(false);
                menu.getMenu().findItem(R.id.manageTodayMeal).setVisible(false);
                menu.getMenu().findItem(R.id.changePassword).setVisible(false);
                menu.getMenu().findItem(R.id.btnLogOut).setVisible(false);
                menu.getMenu().findItem(R.id.memberPasswords).setVisible(false);
                menu.getMenu().findItem(R.id.lastTimeOfChangingMeal).setVisible(false);
                menu.getMenu().findItem(R.id.menu_editInfo).setVisible(false);
            }
            else{
                menu.getMenu().findItem(R.id.updateMPDPP).setVisible(false);
                menu.getMenu().findItem(R.id.makeAnnouncement).setVisible(false);
                menu.getMenu().findItem(R.id.manageTodayMeal).setVisible(true);
                menu.getMenu().findItem(R.id.lastTimeOfChangingMeal).setVisible(false);
                menu.getMenu().findItem(R.id.memberPasswords).setVisible(false);
                menu.getMenu().findItem(R.id.changePassword).setVisible(true);
                menu.getMenu().findItem(R.id.btnLogOut).setVisible(true);
                menu.getMenu().findItem(R.id.menu_editInfo).setVisible(false);
                menu.getMenu().findItem(R.id.menu_newMonth).setVisible(false);
            }
            menu.getMenu().findItem(R.id.itemAbout).setVisible(true);
            menu.getMenu().findItem(R.id.btnInstructions).setVisible(true);

            setOnClickListenerForPopUpMenu(menu);

            menu.show();
        }
        if(v.getId() == R.id.layout_goOverView){

            if(stoppedBoarders.size() == 0 && boarders.size() == 0){
                Toast.makeText(this, "You need to add " +
                        "members to use this feature", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent i = new Intent(MainActivity.this, ActivityOverView.class);
            startActivity(i);
        }
        if(v.getId() == R.id.layout_goDetails){

            if(stoppedBoarders.size() == 0 && boarders.size() == 0){
                Toast.makeText(this, "You need to add " +
                        "members to use this feature", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent i = new Intent(MainActivity.this, DetailsActivity.class);
            startActivity(i);
        }
        if(v.getId() == R.id.layout_goCookBill){

            if(stoppedBoarders.size() == 0 && boarders.size() == 0){
                Toast.makeText(this, "You need to add " +
                        "members to use this feature", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent i = new Intent(MainActivity.this, CookBillActivity.class);
            startActivity(i);
        }
        if(v.getId() == R.id.layout_goBazaarList){

            if(stoppedBoarders.size() == 0 && boarders.size() == 0){
                Toast.makeText(this, "You need to add " +
                        "members to use this feature", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent i = new Intent(MainActivity.this, BazaarActivity.class);
            startActivity(i);
        }
        if(v.getId() == R.id.imgProfileChange){

            if(isMainActivityRunning){

                if(dialogs[0] != null) dialogs[0].dismiss();
                dialogs[0] = null;
                dialogs[0] = new SelectNamesForLogIn(MainActivity.this);
                dialogs[0].show();
                
            }
        }
        if(v.getId() == R.id.txtSeeProfileDetails){
            Intent i = new Intent(this, ProfileDetailsActivity.class);
            i.putExtra("BI", selectedBoarderIndex);
            i.putExtra("SBI", selectedStoppedBoarderIndex);
            i.putExtra("CBI", selectedCookBillIndex);
            startActivity(i);
        }
        if(v.getId() == R.id.txtNotification){

            Intent i = new Intent(this, NotificationActivity.class);
            if(IS_MANAGER){
                i.putExtra("NAME", nameOfManager + "-Manager-");
            }
            else{
                i.putExtra("NAME", getProfileName(selectedBoarderIndex,
                        selectedStoppedBoarderIndex, selectedCookBillIndex));
            }
            startActivity(i);
        }
        if(v.getId() == R.id.txtSeeMealDetails){

            if(stoppedBoarders.size() == 0 && boarders.size() == 0){
                Toast.makeText(this, "You need to add " +
                        "members to use this feature", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent i = new Intent(this, DetailMealCalcActivity.class);
            startActivity(i);
        }
        if(v.getId() == R.id.imgAnnouncement){
            Intent i = new Intent(this, ActivitySeeAnnouncement.class);
            i.putExtra("ANNOUNCEMENT_TEXT", announcement);
            startActivity(i);
        }
        if(v.getId() == R.id.layout_goToPayBack){

            if(stoppedBoarders.size() == 0 && boarders.size() == 0){
                Toast.makeText(this, "You need to add " +
                        "members to use this feature", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent i = new Intent(this, PaybackActivity.class);
            startActivity(i);
        }
        if(v.getId() == R.id.navLayoutFeedback){

            GiveFeedback d = new GiveFeedback(this);
            if(!this.isFinishing() && !d.isShowing()) d.show();
        }

    }

    private void getMonthIndThread(){
        final TextView img1 = findViewById(R.id.txtMonthIndicator);
        final Handler handler = new Handler();
        final int timeDif = 100;
        isMonthIndThreadRunning = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                int tempTime = 60000;
                while (isMonthIndThreadRunning && tempTime > 0){
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            img1.setCompoundDrawablesWithIntrinsicBounds(
                                    0, 0, R.drawable.ic__arrow_right_green, 0);

                        }
                    });
                    try {
                        Thread.sleep(timeDif);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            img1.setCompoundDrawablesWithIntrinsicBounds(
                                    0, 0, R.drawable.ic_arrow_right_megenta, 0);
                        }
                    });
                    try {
                        Thread.sleep(timeDif);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            img1.setCompoundDrawablesWithIntrinsicBounds(
                                    0, 0, R.drawable.ic_arrow_right_orange, 0);
                        }
                    });
                    try {
                        Thread.sleep(timeDif);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    tempTime -= 3*timeDif;
                }
            }
        }).start();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        isMonthIndThreadRunning = false;
    }

    private void setRadioButtonMonthSelection(int ind){

        RadioButton rb0 = findViewById(R.id.radioDefault);
        RadioButton rb1 = findViewById(R.id.radioSecondMonth);
        RadioButton rb2 = findViewById(R.id.radioThirdMonth);

        if(ind == 0){
            rb0.setChecked(true);
            rb1.setChecked(false);
            rb2.setChecked(false);
        }
        else if(ind == 1){
            rb0.setChecked(false);
            rb1.setChecked(true);
            rb2.setChecked(false);
        }
        else if(ind == 2){
            rb0.setChecked(false);
            rb1.setChecked(false);
            rb2.setChecked(true);
        }
    }

    private void setOnClickToRadioMonths(){
        final RadioButton rb0 = findViewById(R.id.radioDefault);
        final RadioButton rb1 = findViewById(R.id.radioSecondMonth);
        final RadioButton rb2 = findViewById(R.id.radioThirdMonth);

        rb0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                strMonth = rb0.getText().toString();
                setSelectedMonth(strMonth);
                checkAndSetSecurity();
            }
        });
        rb1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                strMonth = rb1.getText().toString();
                setSelectedMonth(strMonth);
                checkAndSetSecurity();
            }
        });
        rb2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                strMonth = rb2.getText().toString();
                setSelectedMonth(strMonth);
                checkAndSetSecurity();
            }
        });
    }

    private void setAllRadioButtonGone(){

        RadioButton rb1 = findViewById(R.id.radioSecondMonth);
        RadioButton rb2 = findViewById(R.id.radioThirdMonth);

        rb1.setVisibility(View.GONE);
        rb2.setVisibility(View.GONE);
    }

    private void fillMonths(){
        for(int i = 0; i < monthNames.size(); i++){
            if(i == 1){
                RadioButton rb = findViewById(R.id.radioSecondMonth);
                rb.setVisibility(View.VISIBLE);
                rb.setText(monthNames.get(i));
            }
            else if(i == 2){
                RadioButton rb = findViewById(R.id.radioThirdMonth);
                rb.setVisibility(View.VISIBLE);
                rb.setText(monthNames.get(i));
            }
        }
    }

    private class NewMonth extends Dialog implements View.OnClickListener{

        public NewMonth(@NonNull Context context) {
            super(context);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.new_month_name);
            initialize();
        }

        private void initialize(){
            findViewById(R.id.btnNewMonthSave).setOnClickListener(this);
            findViewById(R.id.btnNewMonthCancel).setOnClickListener(this);

            EditText edtMonth = findViewById(R.id.edtNewMonth);
            edtMonth.setText(createNewMonth());
        }

        @Override
        public void onClick(View v) {
            int id = v.getId();
            if(id == R.id.btnNewMonthSave){
                EditText edtMonth = findViewById(R.id.edtNewMonth);
                String temp = edtMonth.getText().toString().trim();

                if(temp.isEmpty() || isMonthNameExists(temp)){
                    edtMonth.setError("Need month name");
                    edtMonth.requestFocus();
                }
                else{
                    setSelectedMonth(temp);
                    monthRef.child(temp).setValue(temp);
                    startActivity(new Intent(MainActivity.this, MainActivity.class));
                    dismiss();
                    finish();
                }
            }
            else if(id == R.id.btnNewMonthCancel) dismiss();
        }
    }

    private String createNewMonth(){
        String date = Calendar.getInstance().getTime().toString(),
                day = date.substring(8, 10),
                month = getFullMonthName(date.substring(4, 7)),
                year = date.substring(date.length() - 4);

        return month + ", " + year;
    }

    private void showSelectMonthDialog(Wait wait){
        if(monthNames.size() > 0 && isMainActivityRunning){
            String s = getSelectedMonth("dummy");
            if(s.isEmpty()){
                if(dialogs[3] != null) dialogs[3].dismiss();
                dialogs[3] = null;
                dialogs[3] = new SelectMonth(this);
                dialogs[3].show();
            }
        }
        wait.onCallback();
    }

    private String getSelectedMonth(String dummy){
        String s = "";
        if(selectedMonth.exists()){
            try {
                Scanner sc = new Scanner(selectedMonth);
                if(sc.hasNextLine()){
                    s = sc.nextLine();
                }
                else s = "";
                sc.close();
            } catch (FileNotFoundException e) {
                s = "";
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
        else s = "";

        return s;
    }

    private class SelectMonth extends Dialog{

        public SelectMonth(@NonNull Context context) {
            super(context);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.select_month);
            initialize();
        }

        private void initialize(){
            final TextView txtMonthDefault = findViewById(R.id.txtMonthDefault),
                    txtMonth2 = findViewById(R.id.txtMonth2),
                    txtMonth3 = findViewById(R.id.txtMonth3);
            String s = "Default";
            for(int i = 0; i < 3; i++){
                if(i == 0){
                    findViewById(R.id.layoutMonthDefault).setVisibility(View.VISIBLE);
                    txtMonthDefault.setText(s);
                    final int finalI = i;
                    txtMonthDefault.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            strMonth = txtMonthDefault.getText().toString();
                            setRadioButtonMonthSelection(finalI);
                            setSelectedMonth(strMonth);
                            checkAndSetSecurity();
                            dismiss();
                        }
                    });
                }

                if(i == 1 && i < monthNames.size()){
                    findViewById(R.id.layoutMonth2).setVisibility(View.VISIBLE);
                    txtMonth2.setText(monthNames.get(i));

                    final int finalI = i;
                    txtMonth2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            strMonth = txtMonth2.getText().toString();
                            setRadioButtonMonthSelection(finalI);
                            setSelectedMonth(strMonth);
                            checkAndSetSecurity();
                            dismiss();
                        }
                    });
                }

                if(i == 2 && i < monthNames.size()){
                    findViewById(R.id.layoutMonth3).setVisibility(View.VISIBLE);
                    txtMonth3.setText(monthNames.get(i));

                    final int finalI = i;
                    txtMonth3.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            strMonth = txtMonth3.getText().toString();
                            setRadioButtonMonthSelection(finalI);
                            setSelectedMonth(strMonth);
                            checkAndSetSecurity();
                            dismiss();
                        }
                    });
                }
            }
        }
    }

    private static String getFullMonthName(String month){
        switch (month) {
            case "Jan":
                return "January";
            case "Feb":
                return "February";
            case "Mar":
                return "March";
            case "Apr":
                return "April";
            case "May":
                return "May";
            case "Jun":
                return "June";
            case "Jul":
                return "July";
            case "Aug":
                return "August";
            case "Sep":
                return "September";
            case "Oct":
                return "October";
            case "Nov":
                return "November";
            default:
                return "December";
        }

    }

    private boolean isMonthNameExists(String month){
        for(int i = 0; i < monthNames.size(); i++){
            if(monthNames.get(i).equals(month)) return true;
        }

        return false;
    }

    private class GiveFeedback extends Dialog implements View.OnClickListener {

        public GiveFeedback(@NonNull Context context) {
            super(context);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.feedback_to_server);

            initialize();
        }

        private void initialize(){
            //EditText edtDate = findViewById(R.id.edtFeedbackDate);
            findViewById(R.id.btnFeedbackSend).setOnClickListener(this);
            findViewById(R.id.btnFeedbackCancel).setOnClickListener(this);
            findViewById(R.id.btnFeedbackSendEmail).setOnClickListener(this);
            //edtDate.setText(getTodayDate());
        }


        @Override
        public void onClick(View v) {
            int id = v.getId();
            if(id == R.id.btnFeedbackSend){
                EditText //edtName = findViewById(R.id.edtFeedbackName),
                        //edtEmail = findViewById(R.id.edtFeedbackEmail),
                        //edtDate = findViewById(R.id.edtFeedbackDate),
                        edtText = findViewById(R.id.edtFeedbackText);
                String //strName = edtName.getText().toString(),
                        //strEmail = edtEmail.getText().toString(),
                        //strDate = edtDate.getText().toString(),
                        strText = edtText.getText().toString();
                /*
                if(!isDateFormatCorrect(strDate)){
                    edtDate.setError(getDateFormatSugg());
                    edtDate.requestFocus();
                    return;
                }

                 */
                if(strText.isEmpty()){
                    edtText.setError("Enter your feedback");
                    edtText.requestFocus();
                    return;
                }

                strText = "V-" + BuildConfig.VERSION_NAME + "\n\n" + strText;
                String name = IS_MANAGER ? getManagerName() + " : manager" : getManagerName() + " : member";

                //if(strEmail.isEmpty()) strEmail = "Anonymous";

                sendFeedbackToServer(new Feedback(name, strText, getTodayDate(), "Option hidden"));

                dismiss();

            }
            else if(id == R.id.btnFeedbackSendEmail){
                EditText //edtName = findViewById(R.id.edtFeedbackName),
                        //edtEmail = findViewById(R.id.edtFeedbackEmail),
                        //edtDate = findViewById(R.id.edtFeedbackDate),
                        edtText = findViewById(R.id.edtFeedbackText);
                String //strName = edtName.getText().toString(),
                        //strEmail = edtEmail.getText().toString(),
                        //strDate = edtDate.getText().toString(),
                        strText = edtText.getText().toString();
                /*
                if(!isDateFormatCorrect(strDate)){
                    edtDate.setError(getDateFormatSugg());
                    edtDate.requestFocus();
                    return;
                }

                 */
                if(strText.isEmpty()){
                    edtText.setError("Enter your feedback");
                    edtText.requestFocus();
                    return;
                }

                String name = IS_MANAGER ? getManagerName() + " : manager" : getManagerName() + " : member";

                String body = "Feedback from " + name + "\nV-" + BuildConfig.VERSION_NAME + "\n\n" + strText;
                sendFeedbackMail(body);
                dismiss();
            }
            else if(id == R.id.btnFeedbackCancel){
                dismiss();
            }
        }
    }

    private void sendFeedbackToServer(Feedback feedback){
        DatabaseReference fr = FirebaseDatabase.getInstance().getReference().child("feedback-user-manager")
                .push();
        if(feedback != null) fr.setValue(feedback);
    }

    private void sendFeedbackMail(String message){
        /*
        Intent email = new Intent(Intent.ACTION_SEND);
        email.putExtra(Intent.EXTRA_EMAIL, new String[]{"iaminul237@gmail.com"});
        email.putExtra(Intent.EXTRA_SUBJECT, "Feedback-Meal Manager");
        email.putExtra(Intent.EXTRA_TEXT, message);

        //need this to prompts email client only
        email.setType("message/rfc822");
        //email.setData(Uri.parse("mailto:"));

        //startActivity(Intent.createChooser(email, "Choose an Email client :"));
        startActivity(Intent.createChooser(email, "Send mail..."));

         */
        Intent emailSelectorIntent = new Intent(Intent.ACTION_SENDTO);
        emailSelectorIntent.setData(Uri.parse("mailto:"));

        final Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"iaminul237@gmail.com"});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Feedback-Meal Manager");
        emailIntent.putExtra(Intent.EXTRA_TEXT, message);
        emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        emailIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        emailIntent.setSelector(emailSelectorIntent);

        if( emailIntent.resolveActivity(getPackageManager()) != null )
            startActivity(emailIntent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        isMainActivityRunning = true;
        createDialogs();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isMainActivityRunning = true;
        dismissDialogs();
    }

    @Override
    protected void onPause() {
        super.onPause();
        isMainActivityRunning = false;
        dismissDialogs();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isMainActivityRunning = false;
        dismissDialogs();
    }

    private static class BazaarNoticeForUsers extends Dialog implements View.OnClickListener {

        public BazaarNoticeForUsers(@NonNull Context context) {
            super(context);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.notice);
            initialize();
            showNotice();

        }

        private void initialize(){
            findViewById(R.id.notice_txtOk).setOnClickListener(this);
        }

        private void showNotice(){
            TextView tv = findViewById(R.id.txtNotice);
            String bazaarCostNotice = "You didn't add Bazaar/Expense history yet." +
                    " Click navigation bar on top-left and " +
                    "go to 'Bazaar/Expense history' to add bazaar cost. Otherwise calculation can" +
                    " not be shown correctly! ";
            tv.setText(bazaarCostNotice);
        }

        @Override
        public void onClick(View v) {
            int id = v.getId();
            if(id == R.id.notice_txtOk){
                dismiss();
            }
        }
    }

    private static class AppUpdateNoticeForUsers extends Dialog implements View.OnClickListener {
        private final String info;

        public AppUpdateNoticeForUsers(@NonNull Context context, String info) {
            super(context);
            this.info = info;
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.notice);
            initialize();
            showNotice();

        }

        private void initialize(){
            findViewById(R.id.notice_txtOk).setOnClickListener(this);
        }

        private void showNotice(){
            TextView tv = findViewById(R.id.txtNotice);
            String updateNotice = info + "\n\nPlease visit play store to update " +
                    "and experience better functionality";
            tv.setText(updateNotice);
        }

        @Override
        public void onClick(View v) {
            int id = v.getId();
            if(id == R.id.notice_txtOk){
                dismiss();
            }
        }
    }

    private void setOnClickListenerForPopUpMenu(PopupMenu menu) {
        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                int id = item.getItemId();

                if (id == R.id.lastTimeOfChangingMeal) {

                    SetLastTimeOfChangingMeals setLastTimeOfChangingMeals =
                            new SetLastTimeOfChangingMeals(MainActivity.this);
                    setLastTimeOfChangingMeals.show();
                }
                if(id == R.id.changePassword){

                    UpdatePassword updatePassword =
                            new UpdatePassword(MainActivity.this);
                    updatePassword.show();
                }

                if(id == R.id.btnLogOut){
                    PermissionToLogOut permissionToLogOut =
                            new PermissionToLogOut(MainActivity.this);
                    permissionToLogOut.show();
                }

                if(id == R.id.updateMPDPP){
                    UpdateMPDPP updateMPDPP = new UpdateMPDPP(MainActivity.this);
                    updateMPDPP.show();
                }

                if(id == R.id.makeAnnouncement){

                    MakeAnnouncement makeAnnouncement =
                            new MakeAnnouncement(MainActivity.this);
                    makeAnnouncement.show();
                }

                if(id == R.id.manageTodayMeal){
                    Intent intent = new Intent(MainActivity.this, MaintainTodaysMeal.class);
                    startActivity(intent);
                }

                if(id == R.id.btnInstructions){
                    Intent intent =
                            new Intent(MainActivity.this, ActivityInstructions.class);
                    startActivity(intent);
                }

                if(id == R.id.itemAbout){
                    Intent intent = new Intent(MainActivity.this, ActivityAbout.class);
                    startActivity(intent);
                }

                if(id == R.id.memberPasswords){
                    Intent intent = new Intent(MainActivity.this, MemberPasswordsActivity.class);
                    startActivity(intent);
                }

                if(id == R.id.menu_editInfo){
                    Intent i = new Intent(MainActivity.this, EditInfo.class);
                    startActivity(i);
                }

                if(id == R.id.menu_newMonth){
                    if(monthNames.size() < 3){
                        NewMonth d = new NewMonth(MainActivity.this);
                        if(!MainActivity.this.isFinishing() && !d.isShowing()) d.show();
                    }
                    else{
                        String s = "You can't add more than 3 months. " +
                                "Please 'Erase' the months of which calculations are done!";
                        Toast.makeText(MainActivity.this, s, Toast.LENGTH_LONG).show();
                    }
                }

                return false;
            }
        });
    }

    private static void openDrawer(DrawerLayout drawerLayout) {
        drawerLayout.openDrawer(GravityCompat.START);
    }

    private class EraseAll extends Dialog implements View.OnClickListener{

        private TextView txtConsent;
        private EditText edtConsentText;

        public EraseAll(@NonNull Context context) {
            super(context);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.want_parmisstion);
            initialize();
        }

        @Override
        public void onClick(View v) {
            if(v.getId() == R.id.yesToDelete){

                String strConsentText = edtConsentText.getText().toString();
                if(strConsentText.equals("") || strConsentText.isEmpty() ||
                        !strConsentText.equals("erase")){
                    txtConsent.setTextColor(Color.RED);

                    return;
                }


                if(strMonth.equals("Default")){
                    rootRef.child("members").removeValue();
                    rootRef.child("Marketer History").removeValue();
                    rootRef.child("Meal Period").removeValue();
                    rootRef.child("Meal Status").removeValue();
                    rootRef.child("lastUpdate").removeValue();
                    rootRef.child("those who stopped meal").removeValue();
                    rootRef.child("Extra Money").removeValue();
                    rootRef.child("cooksBill").removeValue();
                    rootRef.child("Discussion").removeValue();
                    rootRef.child("Last time Of changing meals").removeValue();
                    rootRef.child("Post").removeValue();
                    rootRef.child("Today's Meal Status").removeValue();
                    rootRef.child("numOfBoarders").removeValue();
                    rootRef.child("notifications").removeValue();
                    rootRef.child("Payback").removeValue();
                    rootRef.child("announcement").removeValue();
                    rootRef.child("cookBillPaid").removeValue();
                    rootRef.child("member-suggestions").removeValue();
                }
                else if(!strMonth.isEmpty()){
                    rootRef.removeValue();
                    monthRef.child(strMonth).removeValue();
                }
                removeThisMonth();
                if(loggedInPersonFile.exists()){
                    if(!loggedInPersonFile.delete()){
                        Toast.makeText(MainActivity.this,
                                "Logged In File couldn't be found", Toast.LENGTH_SHORT).show();
                    }
                    selectedBoarderIndex = -1;
                    selectedCookBillIndex = -1;
                    selectedStoppedBoarderIndex = -1;
                }

                if(selectedMonth.exists()){
                    if(!selectedMonth.delete()){
                        Toast.makeText(MainActivity.this,
                                "Selected-month-file cant' be found", Toast.LENGTH_SHORT).show();
                    }
                }

                strMonth = "Default";
                checkAndSetSecurity();
            }
            dismiss();
        }

        private void removeThisMonth(){
            int ind = -1;
            for(int i = 0; i < monthNames.size(); i++){
                if(strMonth.equals(monthNames.get(i))) {
                    ind = i;
                    break;
                }
            }
            if(ind != -1){
                monthNames.remove(ind);
            }

            setAllRadioButtonGone();
            fillMonths();
        }

        private void initialize(){
            TextView textView = findViewById(R.id.wantToLogOut);
            Button button = findViewById(R.id.yesToDelete);
            txtConsent = findViewById(R.id.permission_txtConsent);
            edtConsentText = findViewById(R.id.permission_edtWriteMove);

            txtConsent.setVisibility(View.VISIBLE);
            findViewById(R.id.permission_writeMoveLayout).setVisibility(View.VISIBLE);

            String s = "Do you want to erase all data of " + strMonth + " month?\n" +
                    "\u26A0 This action is irreversible.";
            textView.setText(s);
            s = "Erase";
            button.setText(s);

            String firstPart = "Type",
                    toBoldPart = " erase",
                    lastPart = " to confirm.";
            String finalString = firstPart + toBoldPart + lastPart;

            Spannable spannable = new SpannableString(finalString);
            spannable.setSpan(new BackgroundColorSpan(getResources().getColor(R.color.announcement)),
                    firstPart.length() + 1,
                    firstPart.length() + toBoldPart.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            spannable.setSpan(new ForegroundColorSpan(Color.BLACK),
                    firstPart.length() + 1,
                    firstPart.length() + toBoldPart.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            txtConsent.setText(spannable, TextView.BufferType.SPANNABLE);

            button.setOnClickListener(this);
            findViewById(R.id.noToDelete).setOnClickListener(this);
        }
    }

    public interface CallBack{
        void onCallback();
    }
    public interface Wait{
        void onCallback();
    }

    private void setLogInPage(){

        finish();
        Intent intent = new Intent(MainActivity.this, ActivityLogIn.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private class UpdatePassword extends Dialog implements View.OnClickListener{
        private EditText edtName, edtPrev, edtNew;
        public UpdatePassword(@NonNull Context context) {
            super(context);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.change_password_layout);
            initialize();
        }

        private void initialize(){
            edtName = findViewById(R.id.edtEnterNameToChange);
            edtPrev = findViewById(R.id.edtEnterPrevPassToChange);
            edtNew = findViewById(R.id.edtEnterNewPassToChange);

            findViewById(R.id.btnSavePass).setOnClickListener(this);
            findViewById(R.id.btnCancelPass).setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(v.getId() == R.id.btnSavePass){
                final String givenPass = edtPrev.getText().toString(),
                        name = edtName.getText().toString(),
                        newPass = edtNew.getText().toString();

                final DatabaseReference r = rootRef.child("members").child(name).child("memberPassword");
                r.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            String pass = snapshot.getValue(String.class);
                            if(pass.equals(givenPass)){
                                r.setValue(newPass);

                                readingRef.setValue("");
                                readingPermissionAccepted = true;
                                readingRef.setValue("read");

                                dismiss();
                            }else{
                                Toast.makeText(MainActivity.this, "Incorrect password",
                                        Toast.LENGTH_LONG).show();
                            }
                        }else {
                            Toast.makeText(MainActivity.this, "Name doesn't exist",
                                    Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
            if(v.getId() == R.id.btnCancelPass){
                dismiss();
            }
        }
    }

    private static class SetLastTimeOfChangingMeals extends Dialog implements View.OnClickListener{

        private TimePicker b, l, d;
        final DatabaseReference r = rootRef.child("Last time Of changing meals");

        public SetLastTimeOfChangingMeals(@NonNull Context context) {
            super(context);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.last_time_of_change_meal);
            initialize();
        }

        private void initialize(){
            b = findViewById(R.id.setBreakfastTime);
            l = findViewById(R.id.setLunchTime);
            d = findViewById(R.id.setDinnerTime);

            findViewById(R.id.lastTimeSave).setOnClickListener(this);
            findViewById(R.id.lastTimeCancel).setOnClickListener(this);

            r.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        int breakfast = snapshot.child("Breakfast").getValue(Integer.class),
                                lunch = snapshot.child("Lunch").getValue(Integer.class),
                                dinner = snapshot.child("Dinner").getValue(Integer.class);

                        b.setCurrentHour(breakfast/60);
                        b.setCurrentMinute(breakfast%60);
                        l.setCurrentHour(lunch/60);
                        l.setCurrentMinute(lunch%60);
                        d.setCurrentHour(dinner/60);
                        d.setCurrentMinute(dinner%60);

                    }else{
                        b.setCurrentHour(7);
                        b.setCurrentMinute(0);
                        l.setCurrentHour(12);
                        l.setCurrentMinute(0);
                        d.setCurrentHour(16);
                        d.setCurrentMinute(0);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }

        @Override
        public void onClick(View v) {
            if(v.getId() == R.id.lastTimeSave){
                int brH = b.getCurrentHour(),
                        brM = b.getCurrentMinute(),
                        lH = l.getCurrentHour(),
                        lM = l.getCurrentMinute(),
                        dH = d.getCurrentHour(),
                        dM = d.getCurrentMinute();


                r.child("Breakfast").setValue(brH*60 + brM);
                r.child("Lunch").setValue(lH*60 + lM);
                r.child("Dinner").setValue(dH*60 + dM);

                readingRef.setValue("");
                readingPermissionAccepted = true;
                readingRef.setValue("read");

                dismiss();
            }
            if(v.getId() == R.id.lastTimeCancel){
                dismiss();
            }
        }

    }

    private interface UpdateEditText{
        void onUpdate(String s, int ID);
    }

    private class CalculatorInterface extends Dialog implements View.OnClickListener{
        //private static final int MAX_NUMBER = 60;
        //private TextView[] txtNumbers = new TextView[MAX_NUMBER], tempTxtNumbers = new TextView[MAX_NUMBER];
        //private LinearLayout displayInputs, displayResult;
        //private LinearLayout.LayoutParams params;
        //private ArrayList<Integer> divInd, mulInd, tDiv, tMul;
        //private int index = 0;
        private TextView txtDisplayInputs, txtDisplayResult;
        private final int ID;
        private double result = 0;
        private String  prevOp = "", lastOp = "";
        private boolean equalClicked = false;
        private final UpdateEditText updateEditText;

        public CalculatorInterface(@NonNull Context context, int ID, UpdateEditText updateEditText) {
            super(context);
            this.updateEditText = updateEditText;
            this.ID = ID;
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.calculator_interface);
            initialize();
        }

        @Override
        public void onClick(View v) {

            //if(index >= MAX_NUMBER) return;

            if(v.getId() == R.id.btn0){
                if(equalClicked){
                    Toast.makeText(MainActivity.this, "Click 'AC' to clear", Toast.LENGTH_LONG).show();
                    return;
                }
                String s = txtDisplayResult.getText().toString();
                if(!s.equals("")) s = s + "0";
                txtDisplayResult.setText(s);
            }
            if(v.getId() == R.id.btn1){
                //addDigit("1");
                addDigit("1");
            }
            if(v.getId() == R.id.btn2){
                addDigit("2");
            }
            if(v.getId() == R.id.btn3){
                addDigit("3");
            }
            if(v.getId() == R.id.btn4){
                addDigit("4");
            }
            if(v.getId() == R.id.btn5){
                addDigit("5");
            }
            if(v.getId() == R.id.btn6){
                addDigit("6");
            }
            if(v.getId() == R.id.btn7){
                addDigit("7");
            }
            if(v.getId() == R.id.btn8){
                addDigit("8");
            }
            if(v.getId() == R.id.btn9){
                addDigit("9");
            }
            if(v.getId() == R.id.btnAc){
                //displayInputs.removeAllViews();
                //initializeViews();
                result = 0;
                txtDisplayResult.setText("");
                txtDisplayInputs.setText("");
                equalClicked = false;
                lastOp = "";
                prevOp = "";
            }
            if(v.getId() == R.id.btnX){
                /*
                String s = txtNumbers[index].getText().toString();
                if(s.equals("÷")){
                    if(divInd.size() > 0) divInd.remove(divInd.size() - 1);
                    txtNumbers[index].setText("");
                    displayInputs.removeView(txtNumbers[index]);
                    if(index > 0) index--;
                }
                else if(s.equals("X")){
                    if(mulInd.size() > 0) mulInd.remove(mulInd.size() - 1);
                    txtNumbers[index].setText("");
                    displayInputs.removeView(txtNumbers[index]);
                    if(index > 0) index--;
                }
                else if(s.length() > 1){
                    s = s.substring(0, s.length() - 1);
                    txtNumbers[index].setText(s);
                    displayInputs.removeView(txtNumbers[index]);
                    displayInputs.addView(txtNumbers[index]);
                    txtNumbers[index].setLayoutParams(params);
                }
                else if(s.length() == 1){
                    txtNumbers[index].setText("");
                    displayInputs.removeView(txtNumbers[index]);
                    if(index > 0) index--;
                    String divMul = txtNumbers[index].getText().toString();
                    if(divMul.equals("÷")){
                        if(divInd.size() > 0) divInd.remove(divInd.size() - 1);
                        txtNumbers[index].setText("");
                        displayInputs.removeView(txtNumbers[index]);
                        if(index > 0) index--;
                    }
                    else if(divMul.equals("X")){
                        if(mulInd.size() > 0) mulInd.remove(mulInd.size() - 1);
                        txtNumbers[index].setText("");
                        displayInputs.removeView(txtNumbers[index]);
                        if(index > 0) index--;
                    }

                }
                else if(s.equals("")){
                    if(index > 0) index--;

                    String divMul = txtNumbers[index].getText().toString();
                    if(divMul.equals("÷")){
                        if(divInd.size() > 0) divInd.remove(divInd.size() - 1);
                    }
                    else if(divMul.equals("X")){
                        if(mulInd.size() > 0) mulInd.remove(mulInd.size() - 1);
                    }

                    txtNumbers[index].setText("");
                    displayInputs.removeView(txtNumbers[index]);
                }

                 */

                String s = txtDisplayResult.getText().toString();
                if(s.length() > 1) s = s.substring(0, s.length() - 1);
                else s = "";
                txtDisplayResult.setText(s);
            }
            if(v.getId() == R.id.btnDiv){
                /*
                String s = txtNumbers[index].getText().toString(), s1 = "";
                if(!s.equals("+") && !s.equals("-"))
                    addDivMul("÷");

                 */
                operation("÷");
            }
            if(v.getId() == R.id.btnMul){
                /*
                String s = txtNumbers[index].getText().toString(), s1 = "";
                if(!s.equals("+") && !s.equals("-"))
                    addDivMul("X");

                 */
                operation("X");
            }
            if(v.getId() == R.id.btnMin){
                //addPlusMin("-");
                operation("-");
            }
            if(v.getId() == R.id.btnPlus){
                //addPlusMin("+");
                operation("+");
            }
            if(v.getId() == R.id.btnEqual){
                /*
                String ss = txtNumbers[index].getText().toString();
                //Log.i("test", txtNumbers[index].getText().toString());
                if(ss.equals("")) return;
                if(ss.equals("÷")) return;
                if(ss.equals("X")) return;
                if(ss.equals("+")) return;
                if(ss.equals("-")) return;

                saveTemp();

                //printViews();

                while (divInd.size() > 0){
                    int left = getLeft(divInd.get(0));
                    int right = getRight(divInd.get(0));

                    if(left != -1 && right != -1){
                        //Log.i("test", " ÷ " + left + " " + right + " " + divInd.get(0));
                        double temp = Double.parseDouble(txtNumbers[left].getText().toString()) /
                                Double.parseDouble(txtNumbers[right].getText().toString());
                        String s = temp + "";
                        txtNumbers[left].setText(s);
                        txtNumbers[right].setText("#");
                        divInd.remove(0);
                    }
                    else{
                        //Log.i("test", "Negative: " + left + " " + right);
                    }

                    printViews();

                }
                //Log.i("test", "mulInd Size: " + mulInd.size());
                while (mulInd.size() > 0){
                    int left = getLeft(mulInd.get(0));
                    int right = getRight(mulInd.get(0));
                    if(left != -1 && right != -1){
                        //Log.i("test", "X " + left + " " + right + " " + mulInd.get(0));
                        double temp = Double.parseDouble(txtNumbers[left].getText().toString()) *
                                Double.parseDouble(txtNumbers[right].getText().toString());
                        String s = temp + "";
                        txtNumbers[left].setText(s);
                        txtNumbers[right].setText("#");
                        mulInd.remove(0);
                    }
                    else{
                        //Log.i("test", "Negative: " + left + " " + right);
                    }

                    printViews();
                }


                double res = 0.0;
                for(int i = 0; i <= index; i++){
                    String s = txtNumbers[i].getText().toString();
                    //Log.i("test", i + " --> " + s);
                    if(!s.equals("#") && !s.equals("") && !s.equals("÷") && !s.equals("X"))
                        res += Double.parseDouble(s);

                }

                //Log.i("test", res + "");
                result = res + "";


                TextView resultView = new TextView(MainActivity.this);
                displayResult.removeAllViews();
                displayResult.addView(resultView);
                LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.MATCH_PARENT);
                resultView.setLayoutParams(param);
                resultView.setText(result);
                resultView.setGravity(Gravity.CENTER);
                resultView.setTextSize(20);


                 */

                //Log.i("test", result + "");
                String prev = txtDisplayInputs.getText().toString(),
                        s = txtDisplayResult.getText().toString();
                if(s.equals("")) return;

                if(equalClicked) return;
                equalClicked = true;

                if(!prev.equals("")){
                    prev = "( " + prev + " " + s + " ) )";
                }
                else{
                    prev = "( " + s + " )";
                }
                txtDisplayInputs.setText(prev);


                switch (lastOp) {
                    case "÷":
                        result = result / Double.parseDouble(s);
                        break;
                    case "X":
                        result = result * Double.parseDouble(s);
                        break;
                    case "+":
                        result = result + Double.parseDouble(s);
                        break;
                    case "-":
                        result = result - Double.parseDouble(s);
                        break;
                    case "":
                        result = Double.parseDouble(s);
                        break;
                }

                String val = result + "";
                txtDisplayResult.setText(val);
                Log.i("test", result + "");
            }
            if(v.getId() == R.id.btnPoint){
                /*
                String numb = txtNumbers[index].getText().toString();
                if(!pointExistInNumber(numb)) numb = numb + ".";
                txtNumbers[index].setText(numb);
                displayInputs.removeView(txtNumbers[index]);
                displayInputs.addView(txtNumbers[index]);
                txtNumbers[index].setLayoutParams(params);

                 */
                String s = txtDisplayResult.getText().toString();
                if(!pointExistInNumber(s)) s = s + ".";
                txtDisplayResult.setText(s);
            }

            if(v.getId() == R.id.btnCalcInterfaceOk){

                String s = txtDisplayResult.getText().toString();
                if(s.equals("")) s = "0";
                int res = (int)Double.parseDouble(s);
                if(res < 0){
                    Toast.makeText(MainActivity.this, "Can't include negative numbers\n" +
                                    "Press cancel",
                            Toast.LENGTH_LONG).show();
                }else {
                    updateEditText.onUpdate(res + "", ID);
                    dismiss();
                }
            }
            if(v.getId() == R.id.btnCalcInterfaceCancel){
                dismiss();
            }

        }

        private void addDigit(String num){
            if(equalClicked){
                Toast.makeText(MainActivity.this, "Click 'AC' to clear", Toast.LENGTH_LONG).show();
                return;
            }
            String s = txtDisplayResult.getText().toString() + num;
            txtDisplayResult.setText(s);
        }
        private void operation(String op){

            if(equalClicked){
                Toast.makeText(MainActivity.this, "Click 'AC' to clear", Toast.LENGTH_LONG).show();
                return;
            }
            String curNum = txtDisplayResult.getText().toString();

            if(curNum.equals("") || curNum.equals("0")) return;
            lastOp = op;
            Log.i("test2", result + "");
            operate(prevOp, curNum);
            Log.i("test2", result + "");
        }
        private void operate(String op, String num){

            String prev = txtDisplayInputs.getText().toString();
            if(!prev.equals("")){
                prev = "( " + prev + " " + num + " ) " + lastOp;
            }
            else{
                prev = "( " + num + " " + lastOp + " ";
            }
            txtDisplayInputs.setText(prev);
            txtDisplayResult.setText("");

            switch (op) {
                case "÷":
                    result = result / Double.parseDouble(num);
                    break;
                case "X":
                    result = result * Double.parseDouble(num);
                    break;
                case "+":
                    result = result + Double.parseDouble(num);
                    break;
                case "-":
                    result = result - Double.parseDouble(num);
                    break;
                case "":
                    result = Double.parseDouble(num);
                    break;
            }
            prevOp = lastOp;

        }
        private boolean pointExistInNumber(String number){
            for(int i = 0; i < number.length(); i++){
                if(number.charAt(i) == '.'){
                    return true;
                }
            }
            return false;
        }
        private void initialize(){
            //divInd = new ArrayList<>();
            //mulInd  = new ArrayList<>();
            //tDiv = new ArrayList<>();

            //tMul = new ArrayList<>();

            //initializeViews();
            //displayInputs = findViewById(R.id.displayInputs);
            //displayResult = findViewById(R.id.displayResult);
            //params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            txtDisplayInputs = findViewById(R.id.txtDisplayInputs);
            txtDisplayResult = findViewById(R.id.txtDisplayResult);

            findViewById(R.id.btn0).setOnClickListener(this);
            findViewById(R.id.btn1).setOnClickListener(this);
            findViewById(R.id.btn2).setOnClickListener(this);
            findViewById(R.id.btn3).setOnClickListener(this);
            findViewById(R.id.btn4).setOnClickListener(this);
            findViewById(R.id.btn5).setOnClickListener(this);
            findViewById(R.id.btn6).setOnClickListener(this);
            findViewById(R.id.btn7).setOnClickListener(this);
            findViewById(R.id.btn8).setOnClickListener(this);
            findViewById(R.id.btn9).setOnClickListener(this);
            findViewById(R.id.btnAc).setOnClickListener(this);
            findViewById(R.id.btnPlus).setOnClickListener(this);
            findViewById(R.id.btnMin).setOnClickListener(this);
            findViewById(R.id.btnMul).setOnClickListener(this);
            findViewById(R.id.btnDiv).setOnClickListener(this);
            findViewById(R.id.btnEqual).setOnClickListener(this);
            findViewById(R.id.btnPoint).setOnClickListener(this);
            findViewById(R.id.btnX).setOnClickListener(this);
            findViewById(R.id.btnCalcInterfaceOk).setOnClickListener(this);
            findViewById(R.id.btnCalcInterfaceCancel).setOnClickListener(this);

        }
    }

    private class UpdateMealDialog extends Dialog implements View.OnClickListener{

        private final double[] mealB = new double[boarders.size() + 1],
                mealL = new double[boarders.size() + 1], mealD = new double[boarders.size() + 1];

        EditText edtCustomDate;
        boolean mealStatusThreadAlive = true;
        int tbg = 0, tlg = 0, tdg = 0;
        private final String[] changedNames = new String[boarders.size() + 1],
                normalNames = new String[boarders.size() + 1];

        public UpdateMealDialog(@NonNull Context context) {
            super(context);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.update_layout);
            initialize();
        }

        private void initialize(){

            edtCustomDate = findViewById(R.id.edtCustomDate);
            edtCustomDate.setText(getTodayDate());
            findViewById(R.id.btnSaveUpdate).setOnClickListener(this);
            findViewById(R.id.btnCancelUpdate).setOnClickListener(this);
            findViewById(R.id.btnUpdateMealCalc).setOnClickListener(this);
            findViewById(R.id.btnExtraMoneyLayout).setVisibility(View.GONE);
            findViewById(R.id.checkboxLayout).setVisibility(View.GONE);

            LinearLayout ll = findViewById(R.id.updateDataLayout);
            ll.removeAllViews();
            for(int i = 0; i < boarders.size(); i++){
                mealB[i] = mealStatuses.get(i).getBreakFirst();
                mealL[i] = mealStatuses.get(i).getLunch();
                mealD[i] = mealStatuses.get(i).getDinner();

                View view = getView(i);
                ll.addView(view);

                View v = new View(MainActivity.this); ll.addView(v);
                v.setLayoutParams(new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, 10));

                tbg += mealStatuses.get(i).getBreakFirst();
                tlg += mealStatuses.get(i).getLunch();
                tdg += mealStatuses.get(i).getDinner();
            }

            String s = "Regular meals: " + df.format(tbg) + " + " + df.format(tlg) + " + " +
                    df.format(tdg) + " = " + df.format((tbg + tlg + tdg)) + "\n" +
                    updatableMealString + "\n\n" + closedMemberString();

            TextView tv = findViewById(R.id.updateNameLabel);
            tv.setText(s);
        }

        private View getView(int position){
            LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.update_meal_format, null);

            TextView txtBNumOfMeal = view.findViewById(R.id.txtBrUpdateNumbOfMeal),
                    txtLNumOfMeal = view.findViewById(R.id.txtLUpdateNumbOfMeal),
                    txtDNumOfMeal = view.findViewById(R.id.txtDUpdateNumbOfMeal),
                    txtName = view.findViewById(R.id.txtUpdateMealName);

            int todayIndex = isMealChanged(new TextView[]{txtBNumOfMeal, txtLNumOfMeal, txtDNumOfMeal},
                    mealStatuses.get(position).getName(),
                    mealStatuses.get(position).getBreakFirst(),
                    mealStatuses.get(position).getLunch(), mealStatuses.get(position).getDinner());
            String s = (position + 1) + ". " + boarders.get(position).getName();
            normalNames[position] = s;
            if (todayIndex != -1) {
                s = normalNames[position] + "\n" + "(" +
                        df.format(todayMealStatuses.get(todayIndex).getBreakFirst()) + " + " +
                        df.format(todayMealStatuses.get(todayIndex).getLunch()) + " + " +
                        df.format(todayMealStatuses.get(todayIndex).getDinner()) + ")";
                txtName.setTextColor(Color.rgb(168, 109, 162));
            }
            else{
                s = normalNames[position] + "\n" + "(" +
                        df.format(mealStatuses.get(position).getBreakFirst()) + " + " +
                        df.format(mealStatuses.get(position).getLunch()) + " + " +
                        df.format(mealStatuses.get(position).getDinner()) + ")";

                txtName.setTextColor(Color.WHITE);
            }
            changedNames[position] = s;
            if(todayIndex == -1) txtName.setText(normalNames[position]);
            else txtName.setText(changedNames[position]);

            if(isBreakfastOn) txtBNumOfMeal.setText(df.format(mealStatuses.get(position).getBreakFirst()));
            else txtBNumOfMeal.setText(df.format(0.0));
            if(isLunchOn) txtLNumOfMeal.setText(df.format(mealStatuses.get(position).getLunch()));
            else txtLNumOfMeal.setText(df.format(0.0));
            if(isDinnerOn) txtDNumOfMeal.setText(df.format(mealStatuses.get(position).getDinner()));
            else txtDNumOfMeal.setText(df.format(0.0));

            Button btnBrMin = view.findViewById(R.id.btnBrUpdateMealMinus),
                    btnBrPl = view.findViewById(R.id.btnBrUpdatePlus),
                    btnLMin = view.findViewById(R.id.btnLUpdateMealMinus),
                    btnLPl = view.findViewById(R.id.btnLUpdatePlus),
                    btnDMin = view.findViewById(R.id.btnDUpdateMealMinus),
                    btnDPl = view.findViewById(R.id.btnDUpdatePlus);

            btnBrPl.setOnClickListener(getOnClickBPl(position, txtBNumOfMeal, txtName));
            btnBrMin.setOnClickListener(getOnClickBMin(position, txtBNumOfMeal, txtName));

            btnLPl.setOnClickListener(getOnClickLPl(position, txtLNumOfMeal, txtName));
            btnLMin.setOnClickListener(getOnClickLMin(position, txtLNumOfMeal, txtName));

            btnDPl.setOnClickListener(getOnClickDPl(position, txtDNumOfMeal, txtName));
            btnDMin.setOnClickListener(getOnClickDMin(position, txtDNumOfMeal, txtName));


            return view;
        }

        private void setName(int position, TextView nameView){

            TodayMealStatus t = mealStatuses.get(position);
            if(isAllMealEqual(t, mealB[position], mealL[position], mealD[position])){
                nameView.setText(normalNames[position]);
                nameView.setTextColor(Color.WHITE);
            }
            else{
                nameView.setText(changedNames[position]);
                nameView.setTextColor(Color.rgb(168, 109, 162));
            }
        }

        private int isMealChanged(TextView[] views, String name, double b, double l, double d){
            for(int i = 0; i < todayMealStatuses.size(); i++){
                if(name.equals(todayMealStatuses.get(i).getName())){
                    if(b != todayMealStatuses.get(i).getBreakFirst() ||
                            l != todayMealStatuses.get(i).getLunch() ||
                            d != todayMealStatuses.get(i).getDinner()){

                        if(b != todayMealStatuses.get(i).getBreakFirst()){
                            runChangedThread(views[0]).start();
                        }
                        if(l != todayMealStatuses.get(i).getLunch()){
                            runChangedThread(views[1]).start();
                        }
                        if(d != todayMealStatuses.get(i).getDinner()){
                            runChangedThread(views[2]).start();
                        }

                        return i;
                    }
                }
            }
            return -1;
        }

        private Thread runChangedThread(final TextView txtNumOfMeal){
            final Handler handler = new Handler(getApplicationContext().getMainLooper());
            return new Thread(new Runnable() {
                @Override
                public void run() {
                    while (mealStatusThreadAlive){
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                txtNumOfMeal.setBackgroundColor(Color.rgb(168, 109, 162));
                            }
                        });
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                txtNumOfMeal.setBackgroundColor(Color.rgb(179, 162, 177));
                            }
                        });
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }

        private View.OnClickListener getOnClickBPl(final int position,
                                                   final TextView numOfMeal, final TextView txtName){

            return new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(isBreakfastOn){
                        double increment = 0.5;
                        double d = (mealB[position] + increment); mealB[position] = d;
                        numOfMeal.setText(df.format(d));
                        setName(position, txtName);
                        countUpdatableMeal();
                    }
                    else{
                        Toast.makeText(MainActivity.this,
                                "Breakfast meal off", Toast.LENGTH_SHORT).show();
                    }
                }
            };
        }

        private View.OnClickListener getOnClickLPl(final int position,
                                                   final TextView numOfMeal, final TextView txtName){
            return new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(isLunchOn){
                        double increment = 0.5;
                        double d = mealL[position] + increment; mealL[position] = d;
                        numOfMeal.setText(df.format(d));
                        setName(position, txtName);
                        countUpdatableMeal();
                    }
                    else{
                        Toast.makeText(MainActivity.this,
                                "Lunch meal off", Toast.LENGTH_SHORT).show();
                    }
                }
            };
        }

        private View.OnClickListener getOnClickDPl(final int position,
                                                   final TextView numOfMeal, final TextView txtName){
            return new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(isDinnerOn){
                        double increment = 0.5;
                        double d = mealD[position] + increment; mealD[position] = d;
                        numOfMeal.setText(df.format(d));
                        setName(position, txtName);
                        countUpdatableMeal();
                    }
                    else{
                        Toast.makeText(MainActivity.this,
                                "Dinner meal off", Toast.LENGTH_SHORT).show();
                    }
                }
            };
        }

        private View.OnClickListener getOnClickBMin(final int position,
                                                   final TextView numOfMeal, final TextView txtName){
            return new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(isBreakfastOn){
                        double increment = 0.5;
                        if(mealB[position] > 0.0){
                            double d = mealB[position] - increment; mealB[position] = d;
                            numOfMeal.setText(df.format(d));
                            setName(position, txtName);
                            countUpdatableMeal();
                        }
                    }
                    else{
                        Toast.makeText(MainActivity.this,
                                "Breakfast meal off", Toast.LENGTH_SHORT).show();
                    }
                }
            };
        }

        private View.OnClickListener getOnClickLMin(final int position,
                                                    final TextView numOfMeal, final TextView txtName){
            return new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(isLunchOn){
                        double increment = 0.5;
                        if(mealL[position] > 0.0){
                            double d = mealL[position] - increment; mealL[position] = d;
                            numOfMeal.setText(df.format(d));
                            setName(position, txtName);
                            countUpdatableMeal();
                        }
                    }
                    else{
                        Toast.makeText(MainActivity.this,
                                "Lunch meal off", Toast.LENGTH_SHORT).show();
                    }
                }
            };
        }

        private View.OnClickListener getOnClickDMin(final int position,
                                                    final TextView numOfMeal, final TextView txtName){
            return new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(isDinnerOn){
                        double increment = 0.5;
                        if(mealD[position] > 0.0){
                            double d = mealD[position] - increment; mealD[position] = d;
                            numOfMeal.setText(df.format(d));
                            setName(position, txtName);
                            countUpdatableMeal();
                        }
                    }
                    else{
                        Toast.makeText(MainActivity.this,
                                "Dinner meal off", Toast.LENGTH_SHORT).show();
                    }
                }
            };
        }
        @Override
        public void onClick(View v) {
            if(v.getId() == R.id.btnSaveUpdate){
                String date = edtCustomDate.getText().toString();
                if(!isDateFormatCorrect(date)){
                    edtCustomDate.setError(getDateFormatSugg());
                    edtCustomDate.requestFocus();
                    TextView tv = findViewById(R.id.checkCustomDate);
                    tv.setText(getDateFormatSugg());
                    tv.setTextColor(Color.RED);
                    return;
                }
                for(int i = 0; i < boarders.size(); i++){

                    double meal = mealB[i] + mealL[i] + mealD[i];
                    double finMeal = boarders.get(i).getMeals() + meal;
                    boarders.get(i).setMeals(finMeal);

                    boolean exist = false;
                    for(int j = 0; j < boarders.get(i).getMealD().size(); j++){
                        String dt = boarders.get(i).getMealD().get(j).getDate(),
                                ml = boarders.get(i).getMealD().get(j).getMeal();
                        if(dt.equals(date)){
                            double finM = Double.parseDouble(ml) + meal;
                            boarders.get(i).getMealD().get(j).setMeal(finM + "");
                            exist = true;
                            break;
                        }
                    }
                    if(!exist){
                        boarders.get(i).getMealD().add(new MealOrPaymentDetails(date, meal + ""));
                    }
                }

                saveBoarderToStorage();

                rootRef.child("lastUpdate").setValue(date);

                readingRef.setValue("");
                readingPermissionAccepted = true;
                readingRef.setValue("read");
                mealStatusThreadAlive = false;
                dismiss();


            }
            if(v.getId() == R.id.btnCancelUpdate){
                dismiss();
                mealStatusThreadAlive = false;
            }
            if(v.getId() == R.id.btnUpdateMealCalc){

                CalculatorInterface calculator = new CalculatorInterface(MainActivity.this, 20,
                        new UpdateEditText() {
                    @Override
                    public void onUpdate(String s, int ID) {
                        EditText ed = findViewById(R.id.edtUpdateExtraMoney);
                        ed.setText(s);
                    }
                });
                calculator.show();
                WindowManager.LayoutParams layoutParams =
                        getWindowParams(calculator, 0.91f, 0.81f);
                calculator.getWindow().setAttributes(layoutParams);
            }
        }

        private String closedMemberString() {
            if (todayMealStatuses.size() > 0) {
                StringBuilder s = new StringBuilder();
                if(todayMealStatuses.size() == 1) {
                    s.append(todayMealStatuses.get(0).getName());
                    s.append(" has changed meal.\n");
                }
                else {
                    s.append(todayMealStatuses.size());
                    s.append(" Members have changed their meal.\n");
                }

                return s.toString();
            }
            else{
                return "";
            }
        }

        private void countUpdatableMeal(){
            double tbg = 0, tlg = 0, tdg = 0;
            for(int i = 0; i < boarders.size(); i++){
                tbg += mealB[i]; tlg += mealL[i]; tdg += mealD[i];
            }

            String s = "Regular meals: " + df.format(tbg) + " + " + df.format(tlg) + " + " +
                    df.format(tdg) + " = " + df.format((tbg + tlg + tdg)) + "\n" +
                    updatableMealString + "\n\n" + closedMemberString();

            TextView tv = findViewById(R.id.updateNameLabel);
            tv.setText(s);
        }

        private boolean isAllMealEqual(TodayMealStatus m, double b, double l, double d){

            boolean isExist = false;

            for(int i = 0; i < todayMealStatuses.size(); i++){
                if(m.getName().equals(todayMealStatuses.get(i).getName())){
                    isExist = true;

                    if(todayMealStatuses.get(i).getBreakFirst() == b){
                        if(todayMealStatuses.get(i).getLunch() == l){
                            if(todayMealStatuses.get(i).getDinner() == d){
                                return true;
                            }
                        }
                    }
                }
            }
            if(isExist) {
                return false;
            }
            else{
                return b == m.getBreakFirst() && l == m.getLunch() && d == m.getDinner();
            }
        }
    }

    public static String getDateFormatSugg() {
        return "Date format must be DD-MM-YYYY";
    }

    private class UpdatePaymentDialog extends Dialog implements View.OnClickListener{
        private final EditText[] editText = new EditText[MAX_BOARDER];
        private final TextView[] name = new TextView[MAX_BOARDER];
        private final ImageView[] calc = new ImageView[MAX_BOARDER];
        private int index = 0;
        private EditText txtDate;
        private String date;

        public UpdatePaymentDialog(@NonNull Context context) {
            super(context);
        }

        @Override
        public void onClick(View v) {
            if(v.getId() == R.id.btnSavePayment){

                date = txtDate.getText().toString();
                if(!isDateFormatCorrect(date)){
                    txtDate.setError(getDateFormatSugg());
                    txtDate.requestFocus();
                    return;
                }

                for(int i = 0; i < boarders.size(); i++){
                    String payment = editText[i].getText().toString().trim();
                    if(isCorrectInput(payment) == 0){
                        //if(payment.equals("")) payment = "0";
                        if(payment.equals("")) continue;

                        double finPay = boarders.get(i).getPaidMoney() + Double.parseDouble(payment);
                        boarders.get(i).setPaidMoney(finPay);

                        boolean exist = false;
                        for(int j = 0; j < boarders.get(i).getPaymentD().size(); j++){
                            String dt = boarders.get(i).getPaymentD().get(j).getDate(),
                                    ml = boarders.get(i).getPaymentD().get(j).getMeal();
                            if(dt.equals(date)){
                                double finM = Double.parseDouble(ml) + Double.parseDouble(payment);
                                boarders.get(i).getPaymentD().get(j).setMeal(finM + "");
                                exist = true;
                                break;
                            }
                        }
                        if(!exist){
                            boarders.get(i).getPaymentD().add(new MealOrPaymentDetails(date, payment));
                        }

                    }
                }

                for(int i = boarders.size(), k = 0; k < stoppedBoarders.size(); k++){

                    String payment;
                    boolean isExist = false;
                    for(int j = 0; j < boarders.size(); j++){
                        if(stoppedBoarders.get(k).getName().equals(boarders.get(j).getName())){
                            isExist = true;
                            break;
                        }
                    }

                    if(!isExist){
                        payment = editText[i].getText().toString().trim();
                        i++;
                        //if(payment.equals("")) payment = "0";
                        if(payment.equals("")) continue;
                        double paymentD = Double.parseDouble(payment);
                        double finPay = stoppedBoarders.get(k).getPaidMoney() + paymentD;
                        stoppedBoarders.get(k).setPaidMoney(finPay);

                        if(stoppedBoarders.get(k).getDue() == 0){
                            double prevOv = stoppedBoarders.get(k).getOverHead();
                            stoppedBoarders.get(k).setOverHead(prevOv + paymentD);
                        }
                        else{
                            double prevDue = stoppedBoarders.get(k).getDue();
                            stoppedBoarders.get(k).setDue(prevDue - paymentD);
                        }

                        boolean exist = false;
                        for(int c = 0; c < stoppedBoarders.get(k).getPaymentD().size(); c++){
                            String dt = stoppedBoarders.get(k).getPaymentD().get(c).getDate(),
                                    ml = stoppedBoarders.get(k).getPaymentD().get(c).getMeal();
                            if(dt.equals(date)){
                                double finM = Double.parseDouble(ml) + Double.parseDouble(payment);
                                stoppedBoarders.get(k).getPaymentD().get(c).setMeal(finM + "");
                                exist = true;
                                break;
                            }
                        }

                        if(!exist){
                            stoppedBoarders.get(k).getPaymentD().add(new MealOrPaymentDetails(date, payment));
                        }

                    }

                }

                saveStoppedBoarderToStorage();
                recalculate();
                readingRef.setValue("");
                readingPermissionAccepted = true;
                readingRef.setValue("read");
                dismiss();
            }
            if(v.getId() == R.id.btnCancelPayment){
                dismiss();
            }
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.update_payment_layout);

            initialize();
        }


        private void initialize(){
            findViewById(R.id.btnSavePayment).setOnClickListener(this);
            findViewById(R.id.btnCancelPayment).setOnClickListener(this);

            txtDate = findViewById(R.id.txtUpdatePaymentDate);
            date = getTodayDate();
            txtDate.setText(date);

            Resources res = getResources();
            Drawable drawable = null;
            try {
                drawable = Drawable.createFromXml(res,
                        res.getXml(R.xml.rectangular_shape_updates));
            }
            catch (IOException | XmlPullParserException e) {
                e.printStackTrace();
            }

            LinearLayout updatePaymentLayout = findViewById(R.id.updatePaymentLayout);
            for( ; index < boarders.size(); index++){
                LinearLayout ll = new LinearLayout(MainActivity.this),
                        fake = new LinearLayout(MainActivity.this),
                        llForEdt = new LinearLayout(MainActivity.this);

                calc[index] = new ImageView(MainActivity.this);
                name[index] = new TextView(MainActivity.this);
                editText[index] = new EditText(MainActivity.this);
                editText[index].setInputType(InputType.TYPE_CLASS_NUMBER);

                LinearLayout.LayoutParams paramsLl = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, 150),
                        paramsCalc = new LinearLayout.LayoutParams(0,
                                ViewGroup.LayoutParams.MATCH_PARENT, 10),
                        paramsFake = new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT, 10);

                updatePaymentLayout.addView(ll);
                updatePaymentLayout.addView(fake);

                ll.addView(name[index]);
                ll.addView(llForEdt);
                ll.addView(calc[index]);

                if(drawable != null){
                    ll.setBackground(drawable);
                }

                ll.setLayoutParams(paramsLl);
                ll.setGravity(Gravity.CENTER);
                fake.setLayoutParams(paramsFake);

                name[index].setLayoutParams(new LinearLayout.LayoutParams(
                        0, ViewGroup.LayoutParams.MATCH_PARENT, 50));
                llForEdt.setLayoutParams(new LinearLayout.LayoutParams(
                        0, ViewGroup.LayoutParams.MATCH_PARENT, 30));
                calc[index].setLayoutParams(paramsCalc);
                llForEdt.setBackgroundColor(Color.GREEN);
                llForEdt.setPadding(2, 2, 2, 2);
                llForEdt.addView(editText[index]);

                editText[index].setLayoutParams(new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                editText[index].setBackgroundColor(Color.BLACK);
                editText[index].setTextColor(Color.WHITE);
                editText[index].setGravity(Gravity.CENTER);
                editText[index].setHintTextColor(Color.WHITE);

                String s = (index + 1) + ". " + boarders.get(index).getName() + "(" +
                        df.format(boarders.get(index).getPaidMoney() +
                                getStoppedPaid(boarders.get(index).getName())) + "): ";
                name[index].setText(s);
                name[index].setTextSize(20);
                name[index].setTextColor(Color.WHITE);
                name[index].setGravity(Gravity.CENTER);
                editText[index].setHint("Enter");
                calc[index].setImageResource(R.drawable.calcimagee);
                calc[index].setId(index);

                fake.setBackgroundColor(Color.WHITE);

                calc[index].setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        CalculatorInterface calculator = new CalculatorInterface(MainActivity.this, v.getId(),
                                new UpdateEditText() {
                                    @Override
                                    public void onUpdate(String s, int ID) {
                                        editText[ID].setText(s);
                                    }
                                });
                        calculator.show();
                        DisplayMetrics displayMetrics = new DisplayMetrics();
                        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                        int displayWidth = displayMetrics.widthPixels;
                        int displayHeight = displayMetrics.heightPixels;
                        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
                        layoutParams.copyFrom(calculator.getWindow().getAttributes());
                        int dialogWindowWidth = (int) (displayWidth * 0.9f);
                        int dialogWindowHeight = (int) (displayHeight * 0.8f);
                        layoutParams.width = dialogWindowWidth;
                        layoutParams.height = dialogWindowHeight;
                        calculator.getWindow().setAttributes(layoutParams);
                    }
                });

            }

            for(int i = 0; i < stoppedBoarders.size(); i++){

                boolean isExist = false;

                for(int j = 0; j < boarders.size(); j++){
                    if(stoppedBoarders.get(i).getName().equals(boarders.get(j).getName())){
                        isExist = true;
                        break;
                    }
                }
                if(!isExist){
                    LinearLayout ll = new LinearLayout(MainActivity.this),
                            fake = new LinearLayout(MainActivity.this),
                            llForEdt = new LinearLayout(MainActivity.this);

                    calc[index] = new ImageView(MainActivity.this);
                    name[index] = new TextView(MainActivity.this);
                    editText[index] = new EditText(MainActivity.this);
                    editText[index].setInputType(InputType.TYPE_CLASS_NUMBER);

                    LinearLayout.LayoutParams paramsLl = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, 150),
                            paramsCalc = new LinearLayout.LayoutParams(0,
                                    ViewGroup.LayoutParams.MATCH_PARENT, 10),
                            paramsFake = new LinearLayout.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT, 10);

                    updatePaymentLayout.addView(ll);
                    updatePaymentLayout.addView(fake);

                    ll.setGravity(Gravity.CENTER);

                    ll.addView(name[index]);
                    ll.addView(llForEdt);
                    ll.addView(calc[index]);

                    ll.setLayoutParams(paramsLl);
                    fake.setLayoutParams(paramsFake);

                    llForEdt.setPadding(2, 2, 2, 2);
                    llForEdt.setBackgroundColor(Color.GREEN);
                    llForEdt.setLayoutParams(new LinearLayout.LayoutParams(
                            0, ViewGroup.LayoutParams.MATCH_PARENT, 30));
                    llForEdt.addView(editText[index]);

                    name[index].setLayoutParams(new LinearLayout.LayoutParams(
                            0, ViewGroup.LayoutParams.MATCH_PARENT, 50));
                    name[index].setGravity(Gravity.CENTER);
                    editText[index].setLayoutParams(new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                    calc[index].setLayoutParams(paramsCalc);

                    editText[index].setGravity(Gravity.CENTER);
                    editText[index].setBackgroundColor(Color.BLACK);
                    editText[index].setHintTextColor(Color.WHITE);
                    editText[index].setTextColor(Color.WHITE);

                    String s = (index + 1) + ". " + stoppedBoarders.get(i).getName() + "(" +
                            df.format(stoppedBoarders.get(i).getPaidMoney()) + "): ";
                    name[index].setText(s);
                    name[index].setTextColor(Color.WHITE);
                    name[index].setTextSize(20);
                    editText[index].setHint("Enter");
                    calc[index].setImageResource(R.drawable.calcimagee);
                    calc[index].setId(index);

                    if(drawable != null) ll.setBackground(drawable);
                    fake.setBackgroundColor(Color.WHITE);

                    calc[index].setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            CalculatorInterface calculator = new CalculatorInterface(MainActivity.this, v.getId(),
                                    new UpdateEditText() {
                                        @Override
                                        public void onUpdate(String s, int ID) {
                                            editText[ID].setText(s);
                                        }
                                    });
                            calculator.show();

                            DisplayMetrics displayMetrics = new DisplayMetrics();
                            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                            int displayWidth = displayMetrics.widthPixels;
                            int displayHeight = displayMetrics.heightPixels;
                            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
                            layoutParams.copyFrom(calculator.getWindow().getAttributes());
                            int dialogWindowWidth = (int) (displayWidth * 0.9f);
                            int dialogWindowHeight = (int) (displayHeight * 0.8f);
                            layoutParams.width = dialogWindowWidth;
                            layoutParams.height = dialogWindowHeight;
                            calculator.getWindow().setAttributes(layoutParams);
                        }
                    });

                    index++;
                }
            }
        }
    }

    private double getStoppedPaid(String name) {
        for(int i = 0; i < stoppedBoarders.size(); i++){
            if(stoppedBoarders.get(i).getName().equals(name)){
                return stoppedBoarders.get(i).getPaidMoney();
            }
        }
        return 0;
    }

    private class UpdateMPDPP extends Dialog implements View.OnClickListener{
        private final TextView[] textViewsB = new TextView[MAX_BOARDER], textViewsL = new TextView[MAX_BOARDER],
        textViewsD = new TextView[MAX_BOARDER];
        private final Button[] minusB = new Button[MAX_BOARDER], minusL = new Button[MAX_BOARDER],
                minusD = new Button[MAX_BOARDER],
        plusB = new Button[MAX_BOARDER], plusL = new Button[MAX_BOARDER], plusD = new Button[MAX_BOARDER];
        private final CheckBox[] isOn = new CheckBox[MAX_BOARDER];
        LinearLayout updateDataLayout;
        CheckBox rbB, rbL, rbD, cbExtraMoney;
        private EditText edtExtraMoney;

        public UpdateMPDPP(@NonNull Context context) {
            super(context);
        }

        @Override
        public void onClick(View v) {
            if(v.getId() == R.id.btnSaveUpdate){

                String newExtra = edtExtraMoney.getText().toString();
                if(!newExtra.isEmpty() && !cbExtraMoney.isChecked()){
                    edtExtraMoney.setError("You need to check the 'Extra money' box to add this extra money");
                    edtExtraMoney.requestFocus();
                    return;
                }

                DatabaseReference r2 = rootRef.child("Meal Period");
                for(int i = 0; i < boarders.size(); i++){
                    final DatabaseReference r = rootRef.child("Meal Status").child(boarders.get(i).getName()),
                            r3 = rootRef.child("those who stopped meal")
                                    .child("members").child(boarders.get(i).getName()),
                            r4 = rootRef.child("members").child(boarders.get(i).getName());

                    double b, l, d, total = 0;
                    b = Double.parseDouble(textViewsB[i].getText().toString());
                    total += b;
                    l = Double.parseDouble(textViewsL[i].getText().toString());
                    total += l;
                    d = Double.parseDouble(textViewsD[i].getText().toString());
                    total += d;

                    TodayMealStatus mealStatus = new TodayMealStatus(boarders.get(i).getName(),
                            b, l, d, total);

                    r.setValue(mealStatus);

                    if(!isOn[i].isChecked()){
                        int pos = -1;
                        for(int j = 0; j < stoppedBoarders.size(); j++){
                            if(stoppedBoarders.get(j).getName().equals(boarders.get(i).getName())){
                                pos = j;
                                break;
                            }
                        }
                        if(pos == -1){
                            r3.setValue(boarders.get(i));
                            r4.removeValue();
                            r.removeValue();
                        }
                        else{
                            double P = stoppedBoarders.get(pos).getPaidMoney() +
                                    boarders.get(i).getPaidMoney(),
                                    M = stoppedBoarders.get(pos).getMeals() + boarders.get(i).getMeals(),
                                    D = stoppedBoarders.get(pos).getDue() + boarders.get(i).getDue(),
                                    Ov = stoppedBoarders.get(pos).getOverHead() + boarders.get(i).getOverHead();

                            for(int j = 0; boarders.get(i).getPaymentD() != null &&
                                    j < boarders.get(i).getPaymentD().size(); j++){
                                stoppedBoarders.get(pos).getPaymentD().add(boarders.get(i).getPaymentD().get(j));
                            }
                            for(int j = 0; boarders.get(i).getMealD() != null &&
                                    j < boarders.get(i).getMealD().size(); j++){
                                stoppedBoarders.get(pos).getMealD().add(boarders.get(i).getMealD().get(j));
                            }
                            stoppedBoarders.get(pos).setPaidMoney(P);
                            stoppedBoarders.get(pos).setMeals(M);
                            if(D > Ov) {
                                stoppedBoarders.get(pos).setDue(D - Ov);
                                stoppedBoarders.get(pos).setOverHead(0);
                            }
                            else {
                                stoppedBoarders.get(pos).setOverHead(Ov - D);
                                stoppedBoarders.get(pos).setDue(0);
                            }

                            r3.setValue(stoppedBoarders.get(pos));

                            r.removeValue();
                            r4.removeValue();
                        }

                        deleteSelectedName();

                        saveMealRateHistory(boarders.get(i), mealRate);
                    }


                }

                if(cbExtraMoney.isChecked()){

                    if(!newExtra.isEmpty()) extraMoney += Double.parseDouble(newExtra);
                    rootRef.child("Extra Money").setValue(extraMoney);
                }

                r2.child("Breakfast").setValue(rbB.isChecked());
                r2.child("Lunch").setValue(rbL.isChecked());
                r2.child("Dinner").setValue(rbD.isChecked());

                readingRef.setValue("");
                readingPermissionAccepted = true;
                readingRef.setValue("read");

            }
            dismiss();
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.update_layout);
            initialize();
        }
        private void initialize(){

            rbB = findViewById(R.id.rbBreakFirstDecide);
            rbL = findViewById(R.id.rbLunchDecide);
            rbD = findViewById(R.id.rbDinnerDecide);
            cbExtraMoney = findViewById(R.id.checkUpdateExtraMoney);
            edtExtraMoney = findViewById(R.id.edtUpdateExtraMoney);

            findViewById(R.id.customDateLayout).setVisibility(View.GONE);

            rbB.setChecked(isBreakfastOn);
            rbL.setChecked(isLunchOn);
            rbD.setChecked(isDinnerOn);

            findViewById(R.id.btnSaveUpdate).setOnClickListener(this);
            findViewById(R.id.btnCancelUpdate).setOnClickListener(this);
            updateDataLayout = findViewById(R.id.updateDataLayout);
            updateDataLayout.setPadding(10, 10, 10, 0);
            TextView tv = findViewById(R.id.updateNameLabel);
            String s = "Meal Per Day Per Head(MPDPH)";
            tv.setText(s);

            findViewById(R.id.btnSaveUpdate).setOnClickListener(this);
            findViewById(R.id.btnCancelUpdate).setOnClickListener(this);

            Resources res = getResources();
            Drawable drawable = null;
            try {
                drawable = Drawable.createFromXml(res,
                        res.getXml(R.xml.rectangular_shape_updates));
            }
            catch (IOException | XmlPullParserException e) {
                e.printStackTrace();
            }

            for(int i = 0; i < boarders.size(); i++){
                LinearLayout ll = new LinearLayout(MainActivity.this),
                        ll2 = new LinearLayout(MainActivity.this),
                        ll3 = new LinearLayout(MainActivity.this),
                        fake = new LinearLayout(MainActivity.this),
                        baseL = new LinearLayout(MainActivity.this);

                updateDataLayout.addView(baseL);
                baseL.setOrientation(LinearLayout.VERTICAL);
                baseL.setLayoutParams(new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                baseL.addView(ll);
                baseL.addView(ll2);
                baseL.addView(ll3);

                updateDataLayout.addView(fake);
                ll.setOrientation(LinearLayout.HORIZONTAL);
                ll2.setOrientation(LinearLayout.HORIZONTAL);
                ll3.setOrientation(LinearLayout.HORIZONTAL);
                ll3.setGravity(Gravity.END);
                ll3.setPadding(0, 0, 16, 0);

                LinearLayout.LayoutParams paramsLayout = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT),
                        paramsFake = new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT, 5),

                        paramsName = new LinearLayout.LayoutParams(
                                0,
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                45),
                        paramsLabel = new LinearLayout.LayoutParams(0,
                                ViewGroup.LayoutParams.MATCH_PARENT,  20),
                        params3 = new LinearLayout.LayoutParams(
                                0,
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                10);

                ll.setLayoutParams(paramsLayout);
                ll2.setLayoutParams(paramsLayout);
                ll3.setLayoutParams(paramsLayout);
                fake.setLayoutParams(paramsFake);
                fake.setBackgroundColor(Color.WHITE);

                if(drawable != null) baseL.setBackground(drawable);

                final TextView nameView = new TextView(MainActivity.this),
                        typeLabelB = new TextView(MainActivity.this),
                        typeLabelL = new TextView(MainActivity.this),
                        typeLabelD = new TextView(MainActivity.this);
                minusB[i] = new Button(MainActivity.this);
                minusL[i] = new Button(MainActivity.this);
                minusD[i] = new Button(MainActivity.this);

                isOn[i] = new CheckBox(MainActivity.this);
                isOn[i].setTextColor(Color.WHITE);

                textViewsB[i] = new TextView(MainActivity.this);
                textViewsL[i] = new TextView(MainActivity.this);
                textViewsD[i] = new TextView(MainActivity.this);



                plusB[i] = new Button(MainActivity.this);
                plusL[i] = new Button(MainActivity.this);
                plusD[i] = new Button(MainActivity.this);



                ll.addView(nameView);
                ll.addView(typeLabelB);
                ll.addView(minusB[i]);
                ll.addView(textViewsB[i]);
                ll.addView(plusB[i]);


                ll2.addView(typeLabelL);
                ll2.addView(minusL[i]);
                ll2.addView(textViewsL[i]);
                ll2.addView(plusL[i]);

                ll2.addView(typeLabelD);
                ll2.addView(minusD[i]);
                ll2.addView(textViewsD[i]);
                ll2.addView(plusD[i]);

                ll3.addView(isOn[i]);


                nameView.setLayoutParams(paramsName);
                typeLabelB.setLayoutParams(paramsLabel);
                typeLabelL.setLayoutParams(paramsLabel);
                typeLabelD.setLayoutParams(paramsLabel);

                minusB[i].setLayoutParams(params3);
                textViewsB[i].setLayoutParams(params3);
                plusB[i].setLayoutParams(params3);

                isOn[i].setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));

                minusL[i].setLayoutParams(params3);
                textViewsL[i].setLayoutParams(params3);
                plusL[i].setLayoutParams(params3);

                minusD[i].setLayoutParams(params3);
                textViewsD[i].setLayoutParams(params3);
                plusD[i].setLayoutParams(params3);


                nameView.setGravity(Gravity.CENTER);
                nameView.setTextColor(Color.CYAN);

                typeLabelB.setGravity(Gravity.END);
                typeLabelL.setGravity(Gravity.END);
                typeLabelD.setGravity(Gravity.END);
                typeLabelB.setTextColor(Color.WHITE);
                typeLabelL.setTextColor(Color.WHITE);
                typeLabelD.setTextColor(Color.WHITE);

                textViewsB[i].setGravity(Gravity.CENTER);
                textViewsL[i].setGravity(Gravity.CENTER);
                textViewsD[i].setGravity(Gravity.CENTER);

                textViewsB[i].setTextColor(Color.WHITE);
                textViewsL[i].setTextColor(Color.WHITE);
                textViewsD[i].setTextColor(Color.WHITE);

                String ss = (i + 1) + ". " + boarders.get(i).getName() + ": ";

                nameView.setText(ss);
                nameView.setTextSize(20);
                ss = "                Breakfast:";
                typeLabelB.setText(ss);
                ss = "                Lunch:";
                typeLabelL.setText(ss);
                ss = "                Dinner:";
                typeLabelD.setText(ss);

                ss = "-";
                minusB[i].setText(ss);
                minusL[i].setText(ss);
                minusD[i].setText(ss);

                ss = "+";
                plusB[i].setText(ss);
                plusL[i].setText(ss);
                plusD[i].setText(ss);

                if(boarders.get(i).isMealOn()) {
                    ss = "Meal is ON";
                    isOn[i].setChecked(true);
                }
                else{
                    ss = "Meal is Off";
                    isOn[i].setChecked(false);
                }
                isOn[i].setText(ss);

                textViewsB[i].setText(df.format(mealStatuses.get(i).getBreakFirst()));
                textViewsL[i].setText(df.format(mealStatuses.get(i).getLunch()));
                textViewsD[i].setText(df.format(mealStatuses.get(i).getDinner()));


                final int finalI = i;
                minusB[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(rbB.isChecked()){
                            double prev = Double.parseDouble(textViewsB[finalI].getText().toString());
                            if(prev > 0){
                                double d = prev - 0.5;
                                textViewsB[finalI].setText(df.format(d));
                            }

                        }else{
                            Toast.makeText(MainActivity.this, "Breakfast Meal Off!", Toast.LENGTH_LONG)
                                    .show();
                        }
                    }
                });

                minusL[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(rbL.isChecked()){
                            double prev = Double.parseDouble(textViewsL[finalI].getText().toString());
                            if(prev > 0) {
                                double d = prev - 0.5;
                                textViewsL[finalI].setText(df.format(d));
                            }
                        }else{
                            Toast.makeText(MainActivity.this, "Lunch Meal Off!", Toast.LENGTH_LONG)
                                    .show();
                        }
                    }
                });

                minusD[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(rbD.isChecked()){
                            double prev = Double.parseDouble(textViewsD[finalI].getText().toString());
                            if(prev > 0){
                                double d = prev - 0.5;
                                textViewsD[finalI].setText(df.format(d));
                            }

                        }else{
                            Toast.makeText(MainActivity.this, "Dinner Meal Off!", Toast.LENGTH_LONG)
                                    .show();
                        }
                    }
                });


                plusB[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(rbB.isChecked()){
                            double prev = Double.parseDouble(textViewsB[finalI].getText().toString()),
                                    d = prev + 0.5;
                            textViewsB[finalI].setText(df.format(d));
                        }else{
                            Toast.makeText(MainActivity.this, "Breakfast Meal Off!", Toast.LENGTH_LONG)
                                    .show();
                        }
                    }
                });


                plusL[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(rbL.isChecked()){
                            double prev = Double.parseDouble(textViewsL[finalI].getText().toString()),
                                    d = prev + 0.5;

                            textViewsL[finalI].setText(df.format(d));
                        }else{
                            Toast.makeText(MainActivity.this, "Lunch Meal Off!", Toast.LENGTH_LONG)
                                    .show();
                        }
                    }
                });


                plusD[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(rbD.isChecked()){
                            double prev = Double.parseDouble(textViewsD[finalI].getText().toString()),
                                    d = prev + 0.5;
                            textViewsD[finalI].setText(df.format(d));
                        }else{
                            Toast.makeText(MainActivity.this, "Dinner Meal Off!", Toast.LENGTH_LONG)
                                    .show();
                        }
                    }
                });

                isOn[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        isOn[finalI].setChecked(!isOn[finalI].isChecked());

                        class MealOnOfPerm extends Dialog implements View.OnClickListener{

                            public MealOnOfPerm(@NonNull Context context) {
                                super(context);
                            }

                            @Override
                            protected void onCreate(Bundle savedInstanceState) {
                                super.onCreate(savedInstanceState);
                                setContentView(R.layout.want_parmisstion);
                                initialize();
                            }

                            @Override
                            public void onClick(View v) {
                                int id = v.getId();
                                if(id == R.id.yesToDelete){

                                    if(isOn[finalI].isChecked()){
                                        isOn[finalI].setChecked(false);
                                        String string = "Meal is Off";
                                        isOn[finalI].setText(string);
                                    }
                                    else{
                                        isOn[finalI].setChecked(true);
                                        String string = "Meal is On";
                                        isOn[finalI].setText(string);
                                    }

                                }
                                dismiss();

                            }

                            private void initialize(){
                                TextView tv = findViewById(R.id.wantToLogOut);
                                String s1 = "সতর্কতাঃ কারো মিল বন্ধ করলে তার হিসেব বর্তমান " +
                                        "মিল রেট অনুযায়ি আলাদা করে রাখা হবে। কাজেই, যদি বড়ধরনের বাজার করা " +
                                        "হয়ে যায় ইতোমধ্যেই(যেমন চালের বস্তা কেনা ইত্যাদি যেগুলা " +
                                        boarders.get(finalI).getName() + " এখনো শেষ করে যেতে পারেন নি তাহলে মিল " +
                                        "বন্ধ করা উচিৎ হবে না।বন্ধ না করে Update MPDPH থেকে তার প্রতেক " +
                                        "বেলার মিল 0 করে দিন। এতে করে তার হিসেব রেগুলার" +
                                        " পরিবর্তন হবে বর্তমান মিল রেট অনুযায়ী, কিন্তু মিল বাড়বে না।\n\n" +
                                        "Warning: If you close his/her meal, calculation of this member " +
                                        "will be kept differntly according to current meal rate." +
                                        "If big amount of bazaar is already brought(like big amount" +
                                        " of rice etc.), that is not finished yet, then you should" +
                                        " not close his/her meal." +
                                        "All you need to do is, set his/her all meal to zero(0)" +
                                        " from Update MPDPH.\n\n" + "**After reading that,\n" +
                                        "Do you still want to close meal of " +
                                        boarders.get(finalI).getName() + "?";
                                tv.setText(s1);

                                Button button = findViewById(R.id.yesToDelete);
                                s1 = "Yes";
                                button.setText(s1);

                                findViewById(R.id.yesToDelete).setOnClickListener(this);
                                findViewById(R.id.noToDelete).setOnClickListener(this);
                            }
                        }

                        MealOnOfPerm perm = new MealOnOfPerm(MainActivity.this);
                        perm.show();
                    }
                });

            }
        }

    }

    private void deleteSelectedName() {
        if(!(loggedInPersonFile.exists() && loggedInPersonFile.delete())) {
            Toast.makeText(this, "Can't find to delete " + loggedInPersonFile.toString(),
                    Toast.LENGTH_SHORT).show();
        }
        else{
            selectedBoarderIndex = -1;
            selectedCookBillIndex = -1;
            selectedStoppedBoarderIndex = -1;
        }
    }

    private void saveMealRateHistory(Boarder boarder, double mealRate) {
        DatabaseReference r = rootRef.child("those who stopped meal").child("stoppedHistory")
                .child(boarder.getName()).push();
        r.setValue(new StoppedHistory(boarder, mealRate));
    }

    public static boolean isDateFormatCorrect(String date){

        if(date.length() == 10){
            if(!(date.charAt(0) >= '0' && date.charAt(0) <= '9')) return false;
            if(!(date.charAt(1) >= '0' && date.charAt(1) <= '9')) return false;
            if(date.charAt(2) != '-') return false;
            if(!(date.charAt(3) >= '0' && date.charAt(3) <= '9')) return false;
            if(!(date.charAt(4) >= '0' && date.charAt(4) <= '9')) return false;
            if(date.charAt(5) != '-') return false;
            if(!(date.charAt(6) >= '0' && date.charAt(6) <= '9')) return false;
            if(!(date.charAt(7) >= '0' && date.charAt(7) <= '9')) return false;
            if(!(date.charAt(8) >= '0' && date.charAt(8) <= '9')) return false;
            return date.charAt(9) >= '0' && date.charAt(9) <= '9';
        }
        else return false;
    }

    private void saveStoppedBoarderToStorage(){
        for(int i = 0; i < stoppedBoarders.size(); i++){
            DatabaseReference r = rootRef.child("those who stopped meal").child("members")
                    .child(stoppedBoarders.get(i).getName());
            r.setValue(stoppedBoarders.get(i));
        }
    }

    private class PermissionToLogOut extends Dialog implements View.OnClickListener{

        public PermissionToLogOut(@NonNull Context context) {
            super(context);
        }

        @Override
        public void onClick(View v) {
            if(v.getId() == R.id.yesToDelete){
                try {

                    if(IS_MANAGER){
                        fAuth.signOut();
                    }
                    else{
                        PrintWriter printWriter2 = new PrintWriter(evidence);
                        printWriter2.close();
                    }
                    if(loggedInPersonFile.exists()){
                        if(!loggedInPersonFile.delete()){
                            Toast.makeText(MainActivity.this,
                                    "Logged In File couldn't be found", Toast.LENGTH_SHORT).show();
                        }
                        selectedBoarderIndex = -1;
                        selectedCookBillIndex = -1;
                        selectedStoppedBoarderIndex = -1;
                    }
                    if(selectedMonth.exists()){
                        if(!selectedMonth.delete()){
                            Toast.makeText(MainActivity.this,
                                    "Selected-month-file cant' be found", Toast.LENGTH_SHORT).show();
                        }
                    }
                    setLogInPage();

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                dismiss();
            }
            if(v.getId() == R.id.noToDelete){
                dismiss();
            }
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.want_parmisstion);
            initialize();
        }
        private void initialize(){

            findViewById(R.id.yesToDelete).setOnClickListener(this);
            findViewById(R.id.noToDelete).setOnClickListener(this);

        }
    }

    public static String getTodayDate(){
        String date = Calendar.getInstance().getTime().toString(),
                day = date.substring(8, 10),
                month = getMonth(date.substring(4, 7)),
                year = date.substring(date.length() - 4);

        return (day + "-" + month + "-" + year);
    }

    private class AddMemberDialog extends Dialog implements View.OnClickListener{

        private EditText edtName, edtPayment, edtMeal;

        public AddMemberDialog(@NonNull Context context) {
            super(context);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.layout);
            initialize();
        }

        @Override
        public void onClick(View v) {
            if(v.getId() == R.id.saveTheMember){
                final String name = edtName.getText().toString().toLowerCase().trim();
                String pd = edtPayment.getText().toString().trim();
                String ml = edtMeal.getText().toString().trim();

                if(IS_MANAGER){
                    int boarderExist = boarderExistsInArray(name);
                    saveMember(name, pd, ml, boarderExist);
                    if(boarderExist == 0){
                        dismiss();
                    }
                }
                else saveMemSugg(name, pd, ml);

            }
            else if(v.getId() == R.id.cancelSavingTheMember){
                dismiss();
            }
            else if(v.getId() == R.id.btnLayoutCalc2){
                CalculatorInterface calculator = new CalculatorInterface(MainActivity.this,
                        R.id.btnLayoutCalc1,
                        new UpdateEditText() {
                            @Override
                            public void onUpdate(String s, int ID) {
                                edtPayment.setText(s);
                            }
                        });
                calculator.show();

                WindowManager.LayoutParams layoutParams = getWindowParams(calculator, 0.9f, 0.8f);
                calculator.getWindow().setAttributes(layoutParams);
            }
            else if(v.getId() == R.id.btnLayoutCalc3){
                CalculatorInterface calculator = new CalculatorInterface(MainActivity.this,
                        R.id.btnLayoutCalc1,
                        new UpdateEditText() {
                            @Override
                            public void onUpdate(String s, int ID) {
                                edtMeal.setText(s);
                            }
                        });
                calculator.show();
                WindowManager.LayoutParams layoutParams = getWindowParams(calculator, 0.9f, 0.8f);
                calculator.getWindow().setAttributes(layoutParams);
            }
        }

        private void saveMemSugg(String name, String pd, String ml){

            if(!pd.isEmpty()){
                edtPayment.setError("You are not allowed to add payment");
                edtPayment.requestFocus();
                return;
            }
            if(!ml.isEmpty()){
                edtMeal.setError("You are not allowed to add meal");
                edtMeal.requestFocus();
                return;
            }
            int isExist = boarderExistsInArray(name), isExist2 = isNameExistInSuggestion(name);
            //Log.i("test", isExist + " " + isExist2);
            if(isExist == -1){
                edtName.setError("Enter Name \uD83D\uDE44");
                edtName.requestFocus();
            }
            else if(isExist > 0){
                edtName.setError("Member with this name exist\n" +
                        "Change name \uD83E\uDD0F");
                edtName.requestFocus();
            }
            else if(isExist2 > 0){
                edtName.setError("This member has already been suggested by " +
                        memSug.get(isExist2 - 1).getSuggestedBy() +
                        " Skip or Change name slightly. \uD83E\uDD0F");
                edtName.requestFocus();
            }
            else if(isExist == 0){
                DatabaseReference sugRef = rootRef.child("member-suggestions").push();
                String name2 = getProfileName(selectedBoarderIndex, selectedStoppedBoarderIndex,
                        selectedCookBillIndex);
                if(name2.equals("")) name2 = "Anonymous";
                MemberSuggestion sug = new MemberSuggestion(name, name2, getTodayDate(), sugRef.getKey());
                sugRef.setValue(sug);
                dismiss();

                readingRef.setValue("");
                readingPermissionAccepted = true;
                readingRef.setValue("read");

            }
        }

        private void initialize(){
            Button save, cancel;
            save = findViewById(R.id.saveTheMember);
            cancel = findViewById(R.id.cancelSavingTheMember);
            edtName = findViewById(R.id.edtName);
            edtPayment = findViewById(R.id.edtAddPayment);
            edtMeal = findViewById(R.id.edtAddMeal);

            if(!IS_MANAGER){
                findViewById(R.id.bazaar_nameScroll).setVisibility(View.VISIBLE);
                findViewById(R.id.layout_nameScroll).setVisibility(View.GONE);
                findViewById(R.id.addPaymentLayout).setVisibility(View.GONE);
                findViewById(R.id.addMealLayout).setVisibility(View.GONE);

                TextView tv = findViewById(R.id.layout_txtHint);
                String s = "Manager must approve your suggestion. Otherwise member will not be included to meal";
                tv.setText(s);
            }

            save.setOnClickListener(this);
            cancel.setOnClickListener(this);
            findViewById(R.id.btnLayoutCalc1).setVisibility(View.INVISIBLE);
            findViewById(R.id.btnLayoutCalc2).setOnClickListener(this);
            findViewById(R.id.btnLayoutCalc3).setOnClickListener(this);
        }
    }

    private void saveMember(String name, String pd, String ml, int boarderExist){

        if(pd.equals("")) pd = "0.0";
        if(ml.equals("")) ml = "0.0";

        if(boarderExist == 0){
            ArrayList<MealOrPaymentDetails> paymentD = new ArrayList<>(),
                    mealD = new ArrayList<>();
            double paid = Double.parseDouble(pd),
                    meal = Double.parseDouble(ml),
                    due = meal*mealRate - paid,
                    overHead = paid - meal*mealRate;
            if(due < 0) due = 0;
            if(overHead < 0) overHead = 0;
            paymentD.add(new MealOrPaymentDetails(getTodayDate(), paid + ""));
            mealD.add(new MealOrPaymentDetails(getTodayDate(), meal + ""));

            Boarder boarder = new Boarder(name, name, paid, meal, due, overHead,
                    paymentD, mealD, true);
            boarders.add(boarder);
            recalculate();

            final DatabaseReference r = rootRef.child("Meal Status").child(name),
                    r2 = rootRef.child("Meal Period"),
                    r3 = rootRef.child("cooksBill");

            cooksBills.add(new CooksBill(name, "0.0"));
            r3.setValue(cooksBills);

            double total = 0, b, l, d;
            if(isBreakfastOn){
                b = 1;
                total++;
            }
            else{
                b = 0;
            }
            if(isLunchOn){
                l = 1;
                total++;
            }
            else{
                l = 0;
            }
            if(isDinnerOn){
                d = 1;
                total++;
            }
            else{
                d = 0;
            }
            TodayMealStatus t = new TodayMealStatus(name, b, l, d, total);
            r.setValue(t);
            if(b == 0 && l == 0 && d == 0){
                TodayMealStatus tt =
                        new TodayMealStatus(name, 0, 1,
                                1, 2);
                r.setValue(tt);
                r2.child("Breakfast").setValue(false);
                r2.child("Lunch").setValue(true);
                r2.child("Dinner").setValue(true);
            }

            curProfileName = getProfileName(selectedBoarderIndex, selectedStoppedBoarderIndex,
                    selectedCookBillIndex);

            isAddingBoarder = true;
            readingRef.setValue("");
            readingPermissionAccepted = true;
            readingRef.setValue("read");
        }
        else if(boarderExist == -1){
            Toast.makeText(MainActivity.this, "Enter Name \uD83D\uDE44",
                    Toast.LENGTH_LONG).show();
        }
        else if(boarderExist == -2){
            Toast.makeText(MainActivity.this, "Invalid Name \uD83D\uDE20",
                    Toast.LENGTH_LONG).show();
        }
        else{
            Toast.makeText(MainActivity.this,
                    "Boarder with this name exist\n" +
                            "Change name \uD83E\uDD0F",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void setMemberApproval(){
        if(memSug.size() > 0){
            findViewById(R.id.layoutSuggestion).setVisibility(View.VISIBLE);
            findViewById(R.id.layoutSuggestion).setOnClickListener(this);
            String s1 = memSug.size() + "", s2 = " Member suggestions",
                    finalS = s1 + s2;

            TextView tv = findViewById(R.id.txtSuggestion);
            tv.setText(finalS);
            tv.setTextColor(Color.RED);
            /*
            Spannable spannable = new SpannableString(finalS);
            spannable.setSpan(new ForegroundColorSpan(Color.RED),
                    0, s1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            tv.setText(spannable, TextView.BufferType.SPANNABLE);

             */
        }
        else{
            findViewById(R.id.layoutSuggestion).setVisibility(View.GONE);
        }
    }

    private class ApproveMember extends Dialog{

        LinearLayout layout;

        public ApproveMember(@NonNull Context context) {
            super(context);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.select_name_page);

            initialize();
        }

        private void initialize(){
            findViewById(R.id.layoutBtnSelectNamePage).setVisibility(View.GONE);
            layout = findViewById(R.id.layout_selectName);

            TextView tv = findViewById(R.id.txtSelectNamePageInstr);
            String s = "Member Approval";
            tv.setText(s);

            setSugToFrame();
        }

        private void setSugToFrame(){

            layout.removeAllViews();

            Button btnCancelAll = new Button(MainActivity.this);
            layout.addView(btnCancelAll);
            btnCancelAll.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            String s = "Cancel";
            btnCancelAll.setText(s);
            btnCancelAll.setAllCaps(false);

            for(int i = 0; i < memSug.size(); i++){
                TextView txtName = new TextView(MainActivity.this),
                        txtGapH = new TextView(MainActivity.this);
                Button btnApprove = new Button(MainActivity.this),
                        btnDelete = new Button(MainActivity.this);
                LinearLayout ll = new LinearLayout(MainActivity.this);
                layout.addView(ll);
                ll.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
                ll.setGravity(Gravity.CENTER);ll.setOrientation(LinearLayout.HORIZONTAL);
                ll.addView(txtName); if(IS_MANAGER) {
                    ll.addView(btnApprove);
                    ll.addView(btnDelete);
                }

                txtName.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT,
                        50)); txtName.setGravity(Gravity.CENTER);
                String strNormal = memSug.get(i).getName(),
                        strSmall = "(suggested by " + memSug.get(i).getSuggestedBy() + ")",
                        strTotal = strNormal + strSmall;
                Spannable spannable = new SpannableString(strTotal);
                spannable.setSpan(new SubscriptSpan(), strNormal.length(),
                        strNormal.length() + strSmall.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                spannable.setSpan(new RelativeSizeSpan(.7f), strNormal.length(),
                        strNormal.length() + strSmall.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                txtName.setText(spannable, TextView.BufferType.SPANNABLE);
                txtName.setTextSize(17);

                btnApprove.setLayoutParams(new LinearLayout.LayoutParams(0,
                        ViewGroup.LayoutParams.WRAP_CONTENT, 25));
                s = "Approve";
                btnApprove.setText(s);
                btnApprove.setAllCaps(false);


                btnDelete.setLayoutParams(new LinearLayout.LayoutParams(0,
                        ViewGroup.LayoutParams.WRAP_CONTENT, 25));
                s = "Delete";
                btnDelete.setText(s);
                btnDelete.setAllCaps(false);

                layout.addView(txtGapH);
                txtGapH.setBackgroundColor(Color.BLACK);
                txtGapH.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        7));

                final int finalI = i;
                btnApprove.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MemberSuggestion member = memSug.get(finalI);
                        if(member != null){
                            DatabaseReference sugRef = rootRef.child("member-suggestions")
                                    .child(member.getPushId());
                            sugRef.removeValue();
                            memSug.remove(finalI);
                            saveMember(member.getName(), "", "", 0);
                            setSugToFrame();
                        }

                    }
                });

                btnDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MemberSuggestion member = memSug.get(finalI);
                        DatabaseReference sugRef = rootRef.child("member-suggestions")
                                .child(member.getPushId());
                        sugRef.removeValue();
                        memSug.remove(finalI);
                        setSugToFrame();
                    }
                });

            }

            btnCancelAll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });
        }

    }

    private int boarderExistsInArray(String name){

        if(name.equals("")) return -1;
        if(name.isEmpty()) return -1;

        for(int i = 0; i < boarders.size(); i++){
            if(boarders.get(i).getName().equals(name)) return (i + 1);
        }
        for(int i = 0; i < stoppedBoarders.size(); i++){
            if(stoppedBoarders.get(i).getName().equals(name)) return (i + 1);
        }


        return 0;
    }

    private int isNameExistInSuggestion(String name){
        for(int i = 0; i < memSug.size(); i++){
            if(name.equals(memSug.get(i).getName())) return i + 1;
        }

        return 0;
    }

    public static void readFromDatabase(final CallBack callBack){

        rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(boarders == null) boarders = new ArrayList<>();
                if(stoppedBoarders == null) stoppedBoarders = new ArrayList<>();
                if(marketerHistories == null) marketerHistories = new ArrayList<>();
                if(mealStatuses == null) mealStatuses = new ArrayList<>();
                if(marketerHistories == null) marketerHistories = new ArrayList<>();
                if(todayMealStatuses == null) todayMealStatuses = new ArrayList<>();
                if(paybacks == null) paybacks = new HashMap<>();
                if(cooksBills == null) cooksBills = new ArrayList<>();
                if(memSug == null) memSug = new ArrayList<>();
                boarders.clear();
                stoppedBoarders.clear();
                marketerHistories.clear();
                mealStatuses.clear();
                todayMealStatuses.clear();
                paybacks.clear();
                cooksBills.clear();
                memSug.clear();
                totalPaid = 0;
                totalMeal = 0;
                stoppedCost = 0.0;
                stoppedMeal = 0.0;
                extraMoney = 0.0;
                reminderMoney = 0.0;
                totalCost = 0.0;

                if(snapshot.child("members").exists()){
                    for(DataSnapshot items : snapshot.child("members").getChildren()){
                        Boarder boarder = items.getValue(Boarder.class);
                        totalMeal += boarder.getMeals();
                        totalPaid += boarder.getPaidMoney();
                        boarders.add(boarder);
                    }
                }

                if(snapshot.child("Extra Money").exists()){
                    extraMoney = snapshot.child("Extra Money").getValue(Double.class);
                    totalPaid += extraMoney;
                }

                if(snapshot.child("those who stopped meal").child("members").exists()){
                    for(DataSnapshot items:
                            snapshot.child("those who stopped meal").child("members").getChildren()){
                        Boarder boarder = items.getValue(Boarder.class);

                        totalMeal += boarder.getMeals();
                        totalPaid += boarder.getPaidMoney();
                        stoppedMeal += boarder.getMeals();
                        stoppedCost += (boarder.getPaidMoney() - boarder.getOverHead() + boarder.getDue());

                        stoppedBoarders.add(boarder);
                    }
                }

                if(snapshot.child("Meal Period").child("Breakfast").exists()){
                    isBreakfastOn = snapshot.child("Meal Period").child("Breakfast")
                            .getValue(Boolean.class);
                    isLunchOn = snapshot.child("Meal Period").child("Lunch")
                            .getValue(Boolean.class);
                    isDinnerOn = snapshot.child("Meal Period").child("Dinner")
                            .getValue(Boolean.class);
                }
                else{
                    isBreakfastOn = false;
                    isLunchOn = false;
                    isDinnerOn = false;
                }

                if(snapshot.child("Marketer History").exists()){
                    for(DataSnapshot items : snapshot.child("Marketer History").getChildren()){
                        MarketerHistory marketerHistory = items.getValue(MarketerHistory.class);
                        marketerHistories.add(marketerHistory);
                        totalCost += Double.parseDouble(marketerHistory.getTotalAmount());
                    }
                }

                if(snapshot.child("Meal Status").exists()){
                    if(snapshot.exists()){
                        for(DataSnapshot items : snapshot.child("Meal Status").getChildren()){
                            TodayMealStatus t = items.getValue(TodayMealStatus.class);
                            mealStatuses.add(t);
                        }
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

                if(snapshot.child("cooksBill").exists()){
                    for(DataSnapshot items : snapshot.child("cooksBill").getChildren()){

                        cooksBills.add(items.getValue(CooksBill.class));
                    }
                }

                if(snapshot.child("lastUpdate").exists()){
                    lastUpdate = "Last meal update: " + snapshot.child("lastUpdate").getValue(String.class);
                }
                else{
                    lastUpdate = "Last meal update: N/A";
                }
                if(snapshot.child("announcement").child(getTodayDate()).exists()){
                    announcement = snapshot.child("announcement").child(getTodayDate())
                            .getValue(String.class);

                }

                if(snapshot.child("Payback").exists()){
                    for(DataSnapshot name : snapshot.child("Payback").getChildren()){
                        paybacks.put(name.getKey(), name.getValue(Payback.class));
                    }
                }

                if(snapshot.child("member-suggestions").exists()){
                    for(DataSnapshot items : snapshot.child("member-suggestions").getChildren()){
                        MemberSuggestion mem = items.getValue(MemberSuggestion.class);
                        if(mem != null) memSug.add(mem);
                    }
                }


                sortDetailMealAndPayment(new Wait() {
                    @Override
                    public void onCallback() {

                        callBack.onCallback();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private static void sortDetailMealAndPayment(Wait wait){

        for(int i = 0; i < boarders.size(); i++){
            Collections.sort(boarders.get(i).getMealD(), new Comparator<MealOrPaymentDetails>() {
                @Override
                public int compare(MealOrPaymentDetails o1, MealOrPaymentDetails o2) {

                    return getIntegerOfDates(o1.getDate()) - getIntegerOfDates(o2.getDate());
                }
            });

            Collections.sort(boarders.get(i).getPaymentD(), new Comparator<MealOrPaymentDetails>() {
                @Override
                public int compare(MealOrPaymentDetails o1, MealOrPaymentDetails o2) {

                    return getIntegerOfDates(o1.getDate()) - getIntegerOfDates(o2.getDate());
                }
            });
        }

        for(int i = 0; i < stoppedBoarders.size(); i++){
            Collections.sort(stoppedBoarders.get(i).getMealD(), new Comparator<MealOrPaymentDetails>() {
                @Override
                public int compare(MealOrPaymentDetails o1, MealOrPaymentDetails o2) {

                    return getIntegerOfDates(o1.getDate()) - getIntegerOfDates(o2.getDate());
                }
            });
            Collections.sort(stoppedBoarders.get(i).getPaymentD(), new Comparator<MealOrPaymentDetails>() {
                @Override
                public int compare(MealOrPaymentDetails o1, MealOrPaymentDetails o2) {

                    return getIntegerOfDates(o1.getDate()) - getIntegerOfDates(o2.getDate());
                }
            });
        }
        wait.onCallback();
    }

    private static int getIntegerOfDates(String date){
        int day = Integer.parseInt(date.substring(0, 2)),
                month = Integer.parseInt(date.substring(3, 5)),
                year = Integer.parseInt(date.substring(6));
        return (day + (month*30) + (year*12*30));
    }

    private static String getMonth(String month){
        switch (month) {
            case "Jan":
                return "01";
            case "Feb":
                return "02";
            case "Mar":
                return "03";
            case "Apr":
                return "04";
            case "May":
                return "05";
            case "Jun":
                return "06";
            case "Jul":
                return "07";
            case "Aug":
                return "08";
            case "Sep":
                return "09";
            case "Oct":
                return "10";
            case "Nov":
                return "11";
            default:
                return "12";
        }

    }

    private void recalculate(){

        for(int i = 0; i < boarders.size(); i++){
            double paid = boarders.get(i).getPaidMoney(),
                    meal = boarders.get(i).getMeals(),
                    due = (meal*mealRate - paid),
                    overHead = (paid - meal*mealRate);
            if(due < 0) due = 0;
            if(overHead < 0) overHead = 0;
            boarders.get(i).setDue(due);
            boarders.get(i).setOverHead(overHead);
        }
        if(IS_MANAGER) saveBoarderToStorage();
    }

    private void saveBoarderToStorage(){
        for(int i = 0; i < boarders.size(); i++){
            DatabaseReference ref = membersRef.child(boarders.get(i).getName());
            ref.setValue(boarders.get(i));
        }
    }

    private int isCorrectInput(String payment){
        for(int i = 0; i < payment.length(); i++){
            if(!(payment.charAt(i) <= '9' && payment.charAt(i) >= '0')) return (i + 1);
        }
        return 0;
    }

    public static ArrayList<Boarder> getBoarders(){
        return boarders;
    }

    public static int getMaxBoarder(){
        return MAX_BOARDER;
    }

    public static String getManagerName(){
        return nameOfManager;
    }

    private WindowManager.LayoutParams getWindowParams(Dialog dialog, double width, double height){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int displayWidth = displayMetrics.widthPixels;
        int displayHeight = displayMetrics.heightPixels;
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(dialog.getWindow().getAttributes());
        int dialogWindowWidth = (int) (displayWidth * width);
        int dialogWindowHeight = (int) (displayHeight * height);
        layoutParams.width = dialogWindowWidth;
        layoutParams.height = dialogWindowHeight;
        return layoutParams;
    }

    public static DatabaseReference getLastUpdateRef() {
        return lastUpdateRef;
    }

    public static DatabaseReference getLastChangingTimeRef() {
        return lastChangingTimeRef;
    }

    public static DatabaseReference getMarketHistoryRef() {
        return marketHistoryRef;
    }

    public static DatabaseReference getMealPeriodRef() {
        return mealPeriodRef;
    }

    public static DatabaseReference getMealStatusRef() {
        return mealStatusRef;
    }

    public static DatabaseReference getPostRef() {
        return postRef;
    }

    public static DatabaseReference getTodayMealStatusRef() {
        return todayMealStatusRef;
    }

    public static DatabaseReference getCookBillRef() {
        return cookBillRef;
    }

    public static DatabaseReference getMembersRef() {
        return membersRef;
    }

}