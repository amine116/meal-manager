package com.amine.mealmanager;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import android.os.Handler;
import android.text.InputType;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AlignmentSpan;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
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
import java.util.Scanner;
import static com.amine.mealmanager.DiscussionActivity.getProfileName;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, ValueEventListener{

    public static ArrayList<Boarder> boarders, stoppedBoarders;
    public static ArrayList<MarketerHistory> marketerHistories;
    public static ArrayList<CooksBill> cooksBills;
    public static Map<String, Payback> paybacks;
    public static ArrayList<UNotifications> unSeenNot, seenNot;
    private static ArrayList<TodayMealStatus> todayMealStatuses, mealStatuses;
    private File rootFolder, evidence, loggedInPersonFile;
    private TextView txtTotalPaid, txtTotalMeal, txtMealRate, txtTotalCost,
            txtLastUpdate, txtReminderMoney;
    public static final int MAX_BOARDER = 100;

    public static boolean IS_MANAGER = false, LOGGED_OUT = true,
            isBreakfastOn, isLunchOn, isDinnerOn, readingPermissionAccepted = false,
            isAnimationAlive = false;
    public static double totalMeal = 0.0, totalPaid = 0.0, mealRate = 0.0, totalCost = 0.0,
            extraMoney = 0, stoppedMeal = 0.0, stoppedCost = 0.0, fakeTotalCost, fakeTotalMeal,
            reminderMoney;
    public static final DecimalFormat df =  new DecimalFormat("0.#");
    public static DatabaseReference rootRef, lastUpdateRef, lastChangingTimeRef, marketHistoryRef,
            mealPeriodRef, mealStatusRef, postRef, todayMealStatusRef, cookBillRef, membersRef,
            readingRef;
    private static String lastUpdate = "", nameOfManager = "", announcement = "";
    private static final String INFO_FILE = "Info.txt", MEMBER_NAME_FILE = "Member Name.txt";
    private FirebaseAuth fAuth;
    private DrawerLayout drawerLayout;
    public static int selectedBoarderIndex = -1,
            selectedStoppedBoarderIndex = -1, selectedCookBillIndex = -1;

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
            findViewById(R.id.addBoarder).setVisibility(View.GONE);

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
                        rootRef = FirebaseDatabase.getInstance().getReference().child(nameOfManager);
                        readingRef = rootRef.child("readingNode");
                        readingPermissionAccepted = true;
                        readingRef.addValueEventListener(this);

                        if(loggedInPersonFile.exists()){
                            Scanner scanner1 = new Scanner(loggedInPersonFile);
                            if(scanner1.hasNextLine()){
                                selectedBoarderIndex = Integer.parseInt(scanner1.nextLine());
                                selectedStoppedBoarderIndex = Integer.parseInt(scanner1.nextLine());
                                selectedCookBillIndex = Integer.parseInt(scanner1.nextLine());
                            }
                            scanner1.close();
                        }

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

                if(loggedInPersonFile.exists()){
                    Scanner scanner1 = new Scanner(loggedInPersonFile);
                    if(scanner1.hasNextLine()){
                        selectedBoarderIndex = Integer.parseInt(scanner1.nextLine());
                        selectedStoppedBoarderIndex = Integer.parseInt(scanner1.nextLine());
                        selectedCookBillIndex = Integer.parseInt(scanner1.nextLine());
                    }
                    scanner1.close();
                }

                nameOfManager = getUsernameFromEmail(fAuth.getCurrentUser().getEmail());
                IS_MANAGER = true;
                LOGGED_OUT = false;
                rootRef = FirebaseDatabase.getInstance().getReference().child(nameOfManager);
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

    }

    private String getUsernameFromEmail(String email){
        int i = 0;
        while (i < email.length() && email.charAt(i) != '@') i++;

        return email.substring(0, i);
    }

    private void makeViewsInvisible(){
        findViewById(R.id.infoLayout).setVisibility(View.INVISIBLE);
        findViewById(R.id.imgAnimate).setVisibility(View.VISIBLE);
        isAnimationAlive = true;
        animate();
    }

    private void makeViewsVisible(){
        findViewById(R.id.infoLayout).setVisibility(View.VISIBLE);
        isAnimationAlive = false;
        findViewById(R.id.imgAnimate).setVisibility(View.GONE);

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

    @Override
    public void onDataChange(@NonNull DataSnapshot snapshot) {
        makeViewsInvisible();
        disableNavItems();
        if(readingPermissionAccepted){
            makeViewsInvisible();
            readFromDatabase(new CallBack() {
                @Override
                public void onCallback() {

                    readingPermissionAccepted = false;
                    makeViewsVisible();
                    enableNavItems();
                    showNotice();

                    if(selectedBoarderIndex == -1 && selectedStoppedBoarderIndex == -1 &&
                            selectedCookBillIndex == -1 && (boarders.size() != 0 ||
                            stoppedBoarders.size() != 0)){
                        SelectNamesForLogIn d = new SelectNamesForLogIn(MainActivity.this);
                        if(!MainActivity.this.isFinishing()) d.show();

                    }
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

                if(bI != -1){
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

                if(cbI != -1){
                    cBill = Double.parseDouble(cooksBills.get(cbI).getPaid());
                }
                if(sbI != -1){

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
                }

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

    private class SelectNamesForLogIn extends Dialog implements View.OnClickListener {

        private final CheckBox[] names = new CheckBox[boarders.size() + stoppedBoarders.size() + 1];
        private int totalIndex = -1;
        private String selectedName = "";

        public SelectNamesForLogIn(@NonNull Context context) {
            super(context);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.select_name_page);
            initialize();

        }

        @Override
        public void onClick(View v) {
            int id = v.getId();
            if(id == R.id.btnSaveSelection){

                selectedBoarderIndex = -1;
                selectedStoppedBoarderIndex = -1;
                selectedCookBillIndex = -1;

                if(selectedName.equals("")){
                    Toast.makeText(MainActivity.this, "Select a name",
                            Toast.LENGTH_LONG).show();
                }
                else{
                    for(int i = 0; i < boarders.size(); i++){
                        if(boarders.get(i).getName().equals(selectedName)){
                            selectedBoarderIndex = i;
                            break;
                        }
                    }

                    for(int i = 0; i < stoppedBoarders.size(); i++){
                        if(stoppedBoarders.get(i).getName().equals(selectedName)){
                            selectedStoppedBoarderIndex = i;
                            break;
                        }
                    }

                    for(int i = 0; i < cooksBills.size(); i++){
                        if(cooksBills.get(i).getName().equals(selectedName)){
                            selectedCookBillIndex = i;
                            break;
                        }
                    }

                    saveSelectedIndexes();

                    dismiss();
                }
            }
            else if(id == R.id.btnCancelSelection){
                dismiss();
            }
        }

        private void initialize(){
            findViewById(R.id.btnSaveSelection).setOnClickListener(this);
            findViewById(R.id.btnCancelSelection).setOnClickListener(this);
            LinearLayout selectName = findViewById(R.id.layout_selectName);

            totalIndex = -1;

            for(int i = 0; i < boarders.size(); i++){

                totalIndex++;

                LinearLayout ll = new LinearLayout(MainActivity.this);
                selectName.addView(ll);
                ll.setLayoutParams(new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                ll.setPadding(3, 3, 3,3);
                ll.setBackgroundColor(getResources().getColor(R.color.light_green));

                names[i] = new CheckBox(MainActivity.this);
                View view = new View(MainActivity.this);

                ll.addView(names[i]);
                selectName.addView(view);

                names[i].setLayoutParams(new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                names[i].setBackgroundColor(getResources().getColor(R.color.white));

                view.setLayoutParams(new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, 10));

                String s = boarders.get(i).getName();
                names[i].setGravity(Gravity.CENTER);
                names[i].setText(s);

                final int finalI = i;
                names[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        names[finalI].setChecked(names[finalI].isChecked());
                        selectedName = names[finalI].getText().toString();

                        for(int k = 0; k <= totalIndex; k++){
                            if(finalI != k){

                                names[k].setChecked(false);
                            }
                        }
                    }
                });
            }

            int j = 0, newInd = totalIndex + 1;
            for( ; j < stoppedBoarders.size(); j++){

                String s = stoppedBoarders.get(j).getName();
                if(isBoarderOn(s) == 0) {
                    totalIndex++;

                    LinearLayout ll = new LinearLayout(MainActivity.this);
                    selectName.addView(ll);
                    ll.setLayoutParams(new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    ll.setPadding(3, 3, 3,3);
                    ll.setBackgroundColor(getResources().getColor(R.color.light_green));

                    names[newInd] = new CheckBox(MainActivity.this);
                    View view = new View(MainActivity.this);

                    ll.addView(names[newInd]);
                    names[newInd].setBackgroundColor(getResources().getColor(R.color.white));

                    selectName.addView(view);

                    names[newInd].setLayoutParams(new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                    view.setLayoutParams(new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, 10));

                    names[newInd].setGravity(Gravity.CENTER);
                    names[newInd].setText(s);

                    final int finalJ = newInd;
                    names[newInd].setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            names[finalJ].setChecked(names[finalJ].isChecked());
                            selectedName = names[finalJ].getText().toString();

                            for(int i = 0; i < totalIndex; i++){
                                if(finalJ != i){

                                    names[i].setChecked(false);
                                }
                            }
                        }
                    });

                    newInd++;
                }
            }
        }


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
        paybacks = new HashMap<>();
        unSeenNot = new ArrayList<>();
        seenNot = new ArrayList<>();
        fAuth = FirebaseAuth.getInstance();

        rootFolder = getCacheDir();
        evidence = new File(rootFolder, INFO_FILE);
        loggedInPersonFile = new File(rootFolder, MEMBER_NAME_FILE);

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

        createNeededFiles();

        checkAndSetSecurity();
        setDrawerHome();
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
                    Toast.makeText(MainActivity.this, "", Toast.LENGTH_LONG).show();
                }
            } catch (IOException e) {
                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
        if(!loggedInPersonFile.exists()){
            try {
                if(!loggedInPersonFile.createNewFile()){
                    Toast.makeText(MainActivity.this, "", Toast.LENGTH_LONG).show();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void onClick(View v) {

        if(v.getId() == R.id.addBoarder ||
            v.getId() == R.id.addBoarder2){

            if(boarders.size() + 1 == MAX_BOARDER){

                Toast.makeText(MainActivity.this, "Member more than " + MAX_BOARDER + " isn't allowed!",
                        Toast.LENGTH_LONG).show();
                return;
            }
            AddMemberDialog addMemberDialog = new AddMemberDialog(MainActivity.this);
            addMemberDialog.show();
        }
        if(v.getId() == R.id.updateData){

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
            SelectNamesForLogIn d = new SelectNamesForLogIn(this);
            d.show();
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

    }

    private void showNotice() {

        boolean isBazaarNoticeShown = false;
        DatabaseReference r = FirebaseDatabase.getInstance().getReference().child("change request")
                .child("versionName");
        if((boarders.size() > 0 || stoppedBoarders.size() > 0) && totalCost == 0 && IS_MANAGER){

            isBazaarNoticeShown = true;
            BazaarNoticeForUsers nfu = new BazaarNoticeForUsers(this);
            if(!MainActivity.this.isFinishing()) nfu.show();
        }
        final boolean finalIsBazaarNoticeShown = isBazaarNoticeShown;
        r.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String versionName = snapshot.getValue(String.class);
                    if (!finalIsBazaarNoticeShown && versionName != null &&
                            !versionName.equals(BuildConfig.VERSION_NAME)){
                        AppUpdateNoticeForUsers nfu =
                                new AppUpdateNoticeForUsers(MainActivity.this,
                                        versionName);

                        if(!MainActivity.this.isFinishing()) nfu.show();

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

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
            String bazaarCostNotice = "You didn't add bazaar cost yet." +
                    " Click navigation bar on top-left and " +
                    "go to 'Bazaar history' to add bazaar cost. Otherwise calculation can" +
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
            String updateNotice = info + " update available. Please visit play store to update " +
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

                if(loggedInPersonFile.exists()){
                    if(!loggedInPersonFile.delete()){
                        Toast.makeText(MainActivity.this,
                                "Logged In File couldn't be found", Toast.LENGTH_SHORT).show();
                    }
                    selectedBoarderIndex = -1;
                    selectedCookBillIndex = -1;
                    selectedStoppedBoarderIndex = -1;
                }

                readingRef.setValue("");
                readingPermissionAccepted = true;
                readingRef.setValue("read");


            }
            dismiss();
        }

        private void initialize(){
            TextView textView = findViewById(R.id.wantToLogOut);
            Button button = findViewById(R.id.yesToDelete);
            txtConsent = findViewById(R.id.permission_txtConsent);
            edtConsentText = findViewById(R.id.permission_edtWriteMove);

            txtConsent.setVisibility(View.VISIBLE);
            findViewById(R.id.permission_writeMoveLayout).setVisibility(View.VISIBLE);

            String s = "Do you want to erase all data?\n" +
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

        final TextView[] textViewsB = new TextView[MAX_BOARDER];
        final TextView[] textViewsL = new TextView[MAX_BOARDER];
        final TextView[] textViewsD = new TextView[MAX_BOARDER];
        final Button[] minusB = new Button[MAX_BOARDER];
        final Button[] minusL = new Button[MAX_BOARDER];
        final Button[] minusD = new Button[MAX_BOARDER];
        final Button[] plusB = new Button[MAX_BOARDER];
        final Button[] plusL = new Button[MAX_BOARDER];
        final Button[] plusD = new Button[MAX_BOARDER];

        CheckBox rbB, rbL, rbD;

        LinearLayout updateDataLayout;
        CheckBox extraMoney;
        EditText edtExtraMoney, edtCustomDate;
        boolean mealStatusThreadAlive = true;

        public UpdateMealDialog(@NonNull Context context) {
            super(context);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.update_layout);
            initialize();
        }

        @Override
        public void onClick(View v) {
            if(v.getId() == R.id.btnSaveUpdate){
                String date = edtCustomDate.getText().toString();
                if(!isDateFormatCorrect(date)){
                    TextView tv = findViewById(R.id.checkCustomDate);
                    String s = "Date format must be DD-MM-YYYY";
                    tv.setText(s);
                    tv.setTextColor(Color.RED);
                    return;
                }
                for(int i = 0; i < boarders.size(); i++){

                    double meal = Double.parseDouble(textViewsB[i].getText().toString()) +
                            Double.parseDouble(textViewsL[i].getText().toString()) +
                            Double.parseDouble(textViewsD[i].getText().toString());

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
                DatabaseReference r1 = FirebaseDatabase.getInstance().getReference().child("change request")
                        .child(nameOfManager);
                r1.child("lastActivity").setValue(date);
                rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(extraMoney.isChecked()){
                            String money = edtExtraMoney.getText().toString();
                            if(isCorrectInput(money) == 0) {
                                if (money.equals("")) money = "0";
                                if(snapshot.child("Extra Money").exists()){

                                    double prev = snapshot.child("Extra Money").getValue(Double.class);
                                    double newM = prev + Double.parseDouble(money);
                                    rootRef.child("Extra Money").setValue(newM);
                                }else{
                                    rootRef.child("Extra Money").setValue(Double.parseDouble(money));
                                }
                            }

                        }
                        readingRef.setValue("");
                        readingPermissionAccepted = true;
                        readingRef.setValue("read");
                        mealStatusThreadAlive = false;
                        dismiss();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


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
                        getWindowParams(calculator, 0.9f, 0.8f);
                calculator.getWindow().setAttributes(layoutParams);
            }
        }

        private void initialize(){
            edtCustomDate = findViewById(R.id.edtCustomDate);
            edtCustomDate.setText(getTodayDate());
            updateDataLayout = findViewById(R.id.updateDataLayout);
            updateDataLayout.setPadding(10, 10, 10, 0);
            extraMoney = findViewById(R.id.checkUpdateExtraMoney);
            edtExtraMoney = findViewById(R.id.edtUpdateExtraMoney);
            findViewById(R.id.btnSaveUpdate).setOnClickListener(this);
            findViewById(R.id.btnCancelUpdate).setOnClickListener(this);
            findViewById(R.id.btnUpdateMealCalc).setOnClickListener(this);
            findViewById(R.id.rbBreakFirstDecide).setVisibility(View.GONE);
            findViewById(R.id.rbLunchDecide).setVisibility(View.GONE);
            findViewById(R.id.rbDinnerDecide).setVisibility(View.GONE);
            findViewById(R.id.checkboxLayout).setVisibility(View.GONE);

            rbB = findViewById(R.id.rbBreakFirstDecide);
            rbL = findViewById(R.id.rbLunchDecide);
            rbD = findViewById(R.id.rbDinnerDecide);
            rbB.setChecked(isBreakfastOn);
            rbL.setChecked(isLunchOn);
            rbD.setChecked(isDinnerOn);

            if(todayMealStatuses.size() == 0){
                findViewById(R.id.updateLabelLayout).setVisibility(View.GONE);
            }
            else{
                findViewById(R.id.updateLabelLayout).setVisibility(View.VISIBLE);
                TextView tv = findViewById(R.id.updateNameLabel);
                tv.setText(closedMemberString());
            }

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
                final LinearLayout ll = new LinearLayout(MainActivity.this),
                        ll2 = new LinearLayout(MainActivity.this),
                        fake = new LinearLayout(MainActivity.this),
                        baseLL = new LinearLayout(MainActivity.this);
                updateDataLayout.addView(baseLL);
                updateDataLayout.addView(fake);

                baseLL.setOrientation(LinearLayout.VERTICAL);
                ll.setOrientation(LinearLayout.HORIZONTAL);
                ll2.setOrientation(LinearLayout.HORIZONTAL);

                baseLL.addView(ll);
                baseLL.addView(ll2);

                if(drawable != null){
                    baseLL.setBackground(drawable);
                }

                baseLL.setLayoutParams(new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

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
                fake.setLayoutParams(paramsFake);
                fake.setBackgroundColor(Color.WHITE);

                final TextView nameView = new TextView(MainActivity.this),
                        typeLabelB = new TextView(MainActivity.this),
                        typeLabelL = new TextView(MainActivity.this),
                        typeLabelD = new TextView(MainActivity.this);
                minusB[i] = new Button(MainActivity.this);
                minusL[i] = new Button(MainActivity.this);
                minusD[i] = new Button(MainActivity.this);

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


                nameView.setLayoutParams(paramsName);
                nameView.setTextColor(Color.WHITE);
                typeLabelB.setLayoutParams(paramsLabel);
                typeLabelL.setLayoutParams(paramsLabel);
                typeLabelD.setLayoutParams(paramsLabel);

                minusB[i].setLayoutParams(params3);
                textViewsB[i].setLayoutParams(params3);
                textViewsB[i].setTextColor(Color.WHITE);
                plusB[i].setLayoutParams(params3);

                minusL[i].setLayoutParams(params3);
                textViewsL[i].setLayoutParams(params3);
                textViewsL[i].setTextColor(Color.WHITE);
                plusL[i].setLayoutParams(params3);

                minusD[i].setLayoutParams(params3);
                textViewsD[i].setLayoutParams(params3);
                textViewsD[i].setTextColor(Color.WHITE);
                plusD[i].setLayoutParams(params3);

                nameView.setGravity(Gravity.CENTER);
                typeLabelB.setGravity(Gravity.END);
                typeLabelL.setGravity(Gravity.END);
                typeLabelD.setGravity(Gravity.END);
                textViewsB[i].setGravity(Gravity.CENTER);
                textViewsL[i].setGravity(Gravity.CENTER);
                textViewsD[i].setGravity(Gravity.CENTER);


                String s = (i + 1) + ". " + boarders.get(i).getName() + ": ";
                nameView.setText(s);
                nameView.setTextSize(20);
                s = "                Breakfast:";
                typeLabelB.setText(s);
                s = "                Lunch:";
                typeLabelL.setText(s);
                s = "                Dinner:";
                typeLabelD.setText(s);

                typeLabelB.setTextColor(Color.WHITE);
                typeLabelL.setTextColor(Color.WHITE);
                typeLabelD.setTextColor(Color.WHITE);

                minusB[i].setText("-");
                minusL[i].setText("-");
                minusD[i].setText("-");

                plusB[i].setText("+");
                plusL[i].setText("+");
                plusD[i].setText("+");

                if(isBreakfastOn) textViewsB[i].setText(df.format(mealStatuses.get(i).getBreakFirst()));
                else textViewsB[i].setText(df.format(0.0));
                if(isLunchOn) textViewsL[i].setText(df.format(mealStatuses.get(i).getLunch()));
                else textViewsL[i].setText(df.format(0.0));
                if(isDinnerOn) textViewsD[i].setText(df.format(mealStatuses.get(i).getDinner()));
                else textViewsD[i].setText(df.format(0.0));


                final int finalI = i;
                minusB[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(rbB.isChecked()){
                            double prev = Double.parseDouble(textViewsB[finalI].getText().toString());
                            if(prev > 0) {
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
                            if(prev > 0){
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


                for(int j = 0; j < todayMealStatuses.size(); j++){
                    if(mealStatuses.get(i).getName().equals(todayMealStatuses.get(j).getName())){
                        if(mealStatuses.get(i).getBreakFirst() != todayMealStatuses.get(j).getBreakFirst() ||
                                mealStatuses.get(i).getLunch() != todayMealStatuses.get(j).getLunch() ||
                                mealStatuses.get(i).getDinner() != todayMealStatuses.get(j).getDinner()){

                            final int finalJ = j;
                            if(mealStatuses.get(i).getBreakFirst() != todayMealStatuses.get(j).getBreakFirst()){
                                final Handler handler = new Handler(getApplicationContext().getMainLooper());
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        while (mealStatusThreadAlive){
                                            handler.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    textViewsB[finalI].setBackgroundColor(Color.rgb(168, 109, 162));
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
                                                    textViewsB[finalI].setBackgroundColor(Color.rgb(179, 162, 177));
                                                }
                                            });
                                            try {
                                                Thread.sleep(100);
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                }).start();

                                textViewsB[finalI].setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        String s = df.format(todayMealStatuses.get(finalJ).getBreakFirst());
                                        Toast.makeText(MainActivity.this, s, Toast.LENGTH_LONG).show();
                                    }
                                });
                            }

                            if(mealStatuses.get(i).getDinner() != todayMealStatuses.get(j).getDinner()){
                                final Handler handler = new Handler(getApplicationContext().getMainLooper());
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        while (mealStatusThreadAlive){
                                            handler.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    textViewsD[finalI].setBackgroundColor(Color.rgb(168, 109, 162));
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
                                                    textViewsD[finalI].setBackgroundColor(Color.rgb(179, 162, 177));
                                                }
                                            });
                                            try {
                                                Thread.sleep(100);
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                }).start();
                                textViewsD[finalI].setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        String s = df.format(todayMealStatuses.get(finalJ).getDinner());
                                        Toast.makeText(MainActivity.this, s, Toast.LENGTH_LONG).show();
                                    }
                                });
                            }


                            if(mealStatuses.get(i).getLunch() != todayMealStatuses.get(j).getLunch()){
                                final Handler handler = new Handler(getApplicationContext().getMainLooper());
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        while (mealStatusThreadAlive){
                                            handler.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    textViewsL[finalI].setBackgroundColor(Color.rgb(168, 109, 162));
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
                                                    textViewsL[finalI].setBackgroundColor(Color.rgb(179, 162, 177));
                                                }
                                            });
                                            try {
                                                Thread.sleep(100);
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                }).start();

                                textViewsL[finalI].setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        String s = df.format(todayMealStatuses.get(finalJ).getLunch());
                                        Toast.makeText(MainActivity.this, s, Toast.LENGTH_LONG).show();
                                    }
                                });
                            }

                            nameView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    String s = df.format(todayMealStatuses.get(finalJ).getBreakFirst()) + " + " +
                                            df.format(todayMealStatuses.get(finalJ).getLunch()) + " + " +
                                            df.format(todayMealStatuses.get(finalJ).getDinner()) + " = " +
                                            df.format(todayMealStatuses.get(finalJ).getTotal());
                                    Toast.makeText(MainActivity.this, s, Toast.LENGTH_LONG).show();
                                }
                            });
                            break;
                        }
                    }
                }

            }

        }

        private String closedMemberString() {
            if (todayMealStatuses.size() > 0) {
                StringBuilder s = new StringBuilder();
                if(todayMealStatuses.size() == 1)
                    s = new StringBuilder(todayMealStatuses.get(0).getName()
                            + " has changed his meal.\n");
                else {
                    s = new StringBuilder(todayMealStatuses.size() +
                            " Members have changed their meal.\n");
                    for (int i = 0; i < todayMealStatuses.size(); i++) {

                        if(i == todayMealStatuses.size() - 1){
                            s.append(todayMealStatuses.get(i).getName());
                        }else{
                            s.append(todayMealStatuses.get(i).getName()).append(", ");
                        }
                    }
                }

                return s.toString();
            }
            else{
                return "";
            }
        }
    }

    private class UpdatePaymentDialog extends Dialog implements View.OnClickListener{
        private final EditText[] editText = new EditText[MAX_BOARDER];
        private final TextView[] name = new TextView[MAX_BOARDER];
        private final ImageView[] calc = new ImageView[MAX_BOARDER];
        private int index = 0;

        public UpdatePaymentDialog(@NonNull Context context) {
            super(context);
        }

        @Override
        public void onClick(View v) {
            if(v.getId() == R.id.btnSavePayment){

                String date = getTodayDate();

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
        CheckBox rbB, rbL, rbD;
        public UpdateMPDPP(@NonNull Context context) {
            super(context);
        }

        @Override
        public void onClick(View v) {
            if(v.getId() == R.id.btnSaveUpdate){
                DatabaseReference r2 = rootRef.child("Meal Period");
                for(int i = 0; i < boarders.size(); i++){
                    final DatabaseReference r = rootRef.child("Meal Status").child(boarders.get(i).getName()),
                            r3 = rootRef.child("those who stopped meal")
                                    .child("members").child(boarders.get(i).getName()),
                            r4 = rootRef.child("members").child(boarders.get(i).getName());

                    double b = 0, l = 0, d = 0, total = 0;
                    if(rbB.isChecked()){
                        b = Double.parseDouble(textViewsB[i].getText().toString());
                        total += b;
                    }
                    if(rbL.isChecked()){
                        l = Double.parseDouble(textViewsL[i].getText().toString());
                        total += l;
                    }
                    if(rbD.isChecked()){
                        d = Double.parseDouble(textViewsD[i].getText().toString());
                        total += d;
                    }

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


            findViewById(R.id.checkUpdateExtraMoney).setVisibility(View.GONE);
            findViewById(R.id.edtUpdateExtraMoney).setVisibility(View.GONE);
            findViewById(R.id.customDateLayout).setVisibility(View.GONE);

            rbB.setChecked(isBreakfastOn);
            rbL.setChecked(isLunchOn);
            rbD.setChecked(isDinnerOn);

            findViewById(R.id.btnExtraMoneyLayout).setVisibility(View.GONE);
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
                                        " from Update MPDPH.\n\n" + "After reading that,\n" +
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

    private class AddMarketHistoryDialog extends Dialog implements View.OnClickListener{

        EditText edtName, edtDate, edtAmount;

        public AddMarketHistoryDialog(@NonNull Context context) {
            super(context);
        }

        @Override
        public void onClick(View v) {

            if(v.getId() == R.id.saveTheMember){
                String name = edtName.getText().toString().toLowerCase().trim(),
                        amount = edtAmount.getText().toString().trim();
                if(!name.equals("")){
                    if(!amount.equals("")){
                        totalCost += Double.parseDouble(amount);
                        saveMarketHistoryToDatabase(name, edtDate.getText().toString(), amount);
                        dismiss();
                    }else{
                        Toast.makeText(MainActivity.this,
                                "Enter amount \uD83D\uDE44", Toast.LENGTH_LONG).show();
                    }

                }else{
                    Toast.makeText(MainActivity.this,
                            "Enter name \uD83D\uDE44", Toast.LENGTH_LONG).show();
                }
            }
            if(v.getId() == R.id.cancelSavingTheMember){
                dismiss();
            }
            if(v.getId() == R.id.btnLayoutCalc3){

                CalculatorInterface calculator = new CalculatorInterface(MainActivity.this,
                        v.getId(),
                        new UpdateEditText() {
                            @Override
                            public void onUpdate(String s, int ID) {
                                edtAmount.setText(s);
                            }
                        });

                calculator.show();

                WindowManager.LayoutParams layoutParams = getWindowParams(calculator, 0.9f, 0.8f);
                calculator.getWindow().setAttributes(layoutParams);
            }

        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.layout);
            initialize();
        }
        private void initialize(){
            TextView nm = findViewById(R.id.txtName);
            TextView am = findViewById(R.id.txtAddMeal);
            TextView dt = findViewById(R.id.txtAddPayment);

            String s = "Marketer Name: ";
            nm.setText(s);
            s = "Date: ";
            dt.setText(s);
            s = "Amount: ";
            am.setText(s);

            edtName = findViewById(R.id.edtName);
            edtDate = findViewById(R.id.edtAddPayment);
            edtAmount = findViewById(R.id.edtAddMeal);

            edtAmount.setHint("Amount");
            edtDate.setHint("Date");


            edtDate.setText(getTodayDate());

            findViewById(R.id.saveTheMember).setOnClickListener(this);
            findViewById(R.id.cancelSavingTheMember).setOnClickListener(this);
            findViewById(R.id.btnLayoutCalc3).setOnClickListener(this);
            findViewById(R.id.btnLayoutCalc1).setVisibility(View.INVISIBLE);
            findViewById(R.id.btnLayoutCalc2).setVisibility(View.INVISIBLE);

        }
    }

    public static String getTodayDate(){
        String date = Calendar.getInstance().getTime().toString(),
                day = date.substring(8, 10),
                month = getMonth(date.substring(4, 7)),
                year = date.substring(date.length() - 4);

        return (day + "-" + month + "-" + year);
    }

    private void saveMarketHistoryToDatabase(String name, String date, String amount){
        int exis = marketerExists(name);
        if(exis == 0){
            marketerHistories.add(new MarketerHistory(name, date + " (" + amount + ")", amount));
        }else{
            int index = exis - 1;
            String findExpense = marketerHistories.get(index).getExpenseHistory() + "\n" + date + " (" + amount + ")";
            String findAmount = (Double.parseDouble(marketerHistories.get(index).getTotalAmount()) +
                    Double.parseDouble(amount)) + "";
            marketerHistories.get(index).setExpenseHistory(findExpense);
            marketerHistories.get(index).setTotalAmount(findAmount);
        }
        for(int i = 0; i < marketerHistories.size(); i++){
            DatabaseReference ref = rootRef.child("Marketer History").child(marketerHistories.get(i).getName());
            ref.setValue(marketerHistories.get(i));
        }
    }

    private int marketerExists(String name){
        for(int i = 0; i < marketerHistories.size(); i++){
            if(marketerHistories.get(i).getName().equals(name)) return (i + 1);
        }

        return 0;
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

                if(pd.equals("")) pd = "0.0";
                if(ml.equals("")) ml = "0.0";
                int boarderExist = boarderExistsInArray(name);
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

                    readingRef.setValue("");
                    readingPermissionAccepted = true;
                    readingRef.setValue("read");

                    dismiss();
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

        private void initialize(){
            Button save, cancel;
            save = findViewById(R.id.saveTheMember);
            cancel = findViewById(R.id.cancelSavingTheMember);
            edtName = findViewById(R.id.edtName);
            edtPayment = findViewById(R.id.edtAddPayment);
            edtMeal = findViewById(R.id.edtAddMeal);

            save.setOnClickListener(this);
            cancel.setOnClickListener(this);
            findViewById(R.id.btnLayoutCalc1).setVisibility(View.INVISIBLE);
            findViewById(R.id.btnLayoutCalc2).setOnClickListener(this);
            findViewById(R.id.btnLayoutCalc3).setOnClickListener(this);
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

    public static void readFromDatabase(final CallBack callBack){

        rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boarders.clear();
                stoppedBoarders.clear();
                marketerHistories.clear();
                mealStatuses.clear();
                todayMealStatuses.clear();
                paybacks.clear();
                cooksBills.clear();
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
                    if(snapshot.child("Extra Money").exists()){
                        extraMoney = snapshot.child("Extra Money").getValue(Double.class);
                        totalPaid += extraMoney;
                    }
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
        saveBoarderToStorage();
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