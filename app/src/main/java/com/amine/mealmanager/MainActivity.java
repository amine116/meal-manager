package com.amine.mealmanager;

import android.app.Dialog;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import android.os.Handler;
import android.text.Html;
import android.text.InputType;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AlignmentSpan;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import java.util.Scanner;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, ValueEventListener{

    private LinearLayout rootLayout;
    public static ArrayList<Boarder> boarders, stoppedBoarders;
    public static ArrayList<MarketerHistory> marketerHistories;
    private ArrayList<CooksBill> cooksBills;
    private static ArrayList<TodayMealStatus> todayMealStatuses, mealStatuses;
    private File rootFolder, evidence;
    private TextView txtTotalPaid, txtTotalMeal, txtMealRate, txtTotalCost,
            txtOverView, txtDetails, txtMarketHistory, marketerNameLabel, marketDateLabel, marketAmountLabel,
            nameLabel, paidLabel, mealLabel, dueLabel, overHeadLabel, txtLastUpdate, txtCooksBill,
            txtReminderMoney;
    private static final int MAX_BOARDER = 100;
    private final EditText[] edtCooksBill = new EditText[MAX_BOARDER];

    private Button btnAddMarket, addBoarder;

    public static boolean IS_MANAGER = false, LOGGED_OUT = true, isBreakfastOn, isLunchOn, isDinnerOn,
            overViewClicked = true, detailsClicked = false, cooksBillClicked = false,
            marketHistoryClicked = false;
    public static double totalMeal = 0.0, totalPaid = 0.0, mealRate = 0.0, totalCost = 0.0,
            extraMoney = 0, stoppedMeal = 0.0, stoppedCost = 0.0;
    private final DecimalFormat df =  new DecimalFormat("0.#");
    public static DatabaseReference rootRef, lastUpdateRef, lastChangingTimeRef, marketHistoryRef,
            mealPeriodRef, mealStatusRef, postRef, todayMealStatusRef, cookBillRef, membersRef;
    private static String lastUpdate = "", nameOfManager = "";
    private static final String INFO_FILE = "Info.txt";
    private FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String s = getIntent().getStringExtra("ANIMATED");
        if(s == null){
           Intent intent = new Intent(MainActivity.this, Animate.class);
           startActivity(intent);
        }
        else{
            findViewById(R.id.main_progress).setVisibility(View.VISIBLE);
            initialize();
            setViewToFrame();
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
        tv.setTextColor(Color.WHITE);
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

    private void getLastUpdateTime(){

        rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child("lastUpdate").exists()){
                    lastUpdate = "Last meal update: " + snapshot.child("lastUpdate").getValue(String.class);
                }
                else{
                    lastUpdate = "Last meal update: N/A";
                }
                txtLastUpdate.setText(lastUpdate);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
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
                        rootRef.addValueEventListener(this);
                        setReferences();

                    }else{
                        setLogInPage();
                        IS_MANAGER = false;
                        LOGGED_OUT = true;
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }else{
                setLogInPage();
                IS_MANAGER = false;
                LOGGED_OUT = true;
            }
        }
        else{
            try {
                nameOfManager = getUsernameFromEmail(fAuth.getCurrentUser().getEmail());
                IS_MANAGER = true;
                LOGGED_OUT = false;
                rootRef = FirebaseDatabase.getInstance().getReference().child(nameOfManager);
                rootRef.addValueEventListener(this);
                setReferences();
                FirebaseDatabase.getInstance().getReference().child("change request").child(nameOfManager)
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
        findViewById(R.id.infoLayout).setVisibility(View.GONE);
        findViewById(R.id.main_progress).setVisibility(View.VISIBLE);
    }

    private void makeViewsVisible(){
        findViewById(R.id.infoLayout).setVisibility(View.VISIBLE);
        findViewById(R.id.main_progress).setVisibility(View.GONE);

    }

    private void setViewToFrame(){

        if(LOGGED_OUT){
            setLogInPage();
        }
        else{
            rootLayout.setVisibility(View.VISIBLE);
            findViewById(R.id.modeLayout).setVisibility(View.VISIBLE);
            findViewById(R.id.labelLayout).setVisibility(View.VISIBLE);
            findViewById(R.id.totalThingsLayout).setVisibility(View.VISIBLE);
            findViewById(R.id.announceLayout).setVisibility(View.VISIBLE);

            if(IS_MANAGER){
                findViewById(R.id.updateData).setVisibility(View.VISIBLE);
                findViewById(R.id.eraseAll).setVisibility(View.VISIBLE);
                findViewById(R.id.addBoarder).setVisibility(View.VISIBLE);
                findViewById(R.id.updatePayment).setVisibility(View.VISIBLE);

            }
            else{
                findViewById(R.id.updateData).setVisibility(View.GONE);
                findViewById(R.id.eraseAll).setVisibility(View.GONE);
                findViewById(R.id.addBoarder).setVisibility(View.GONE);
                findViewById(R.id.updatePayment).setVisibility(View.GONE);
            }
            txtOverView.setTextColor(Color.CYAN);
            txtOverView.setPaintFlags(txtOverView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

            getLastUpdateTime();

        }


    }

    private void setAnnouncementToFront(){
        findViewById(R.id.mealOverViewLayout).setVisibility(View.VISIBLE);
        TextView textView = findViewById(R.id.announcementInstruction);
        String instruction = "";
        if(IS_MANAGER) instruction = "Long click Here to make announcement";
        else instruction = "Click Here to see Details";
        textView.setText(instruction);

        DatabaseReference r = rootRef.child("Post").child(getTodayDate());
        r.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    findViewById(R.id.mealOverViewLayout).setVisibility(View.VISIBLE);
                    String ann = snapshot.getValue(String.class);
                    if(ann != null && !ann.equals("")){
                        TextView tv = findViewById(R.id.txtAnnouncement);
                        tv.setText(ann);
                        Thread thread = getAnnounceThread();
                        thread.start();
                    }
                }
                else{
                    findViewById(R.id.mealOverViewLayout).setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        if(IS_MANAGER){
            findViewById(R.id.mealOverViewLayout).setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    TextView textView = findViewById(R.id.txtAnnouncement);
                    String announcement = textView.getText().toString();

                    MakeAnnouncement makeAnnouncement = new MakeAnnouncement(MainActivity.this,
                            announcement);
                    makeAnnouncement.show();

                    WindowManager.LayoutParams params =
                            getWindowParams(makeAnnouncement, 0.9f, 0.3f);
                    makeAnnouncement.getWindow().setAttributes(params);

                    return false;
                }
            });
        }
        findViewById(R.id.mealOverViewLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView textView = findViewById(R.id.txtAnnouncement);
                String s = textView.getText().toString();
                Intent i = new Intent(MainActivity.this, ActivitySeeAnnouncement.class);
                i.putExtra("ANNOUNCEMENT_TEXT", s);
                startActivity(i);
            }
        });

    }

    private Thread getAnnounceThread(){
        final Handler handler = new Handler(getApplicationContext().getMainLooper());

        return new Thread(new Runnable() {
            int threadAlive = 0;
            @Override
            public void run() {
                final TextView textView = findViewById(R.id.baseAnnouncement);
                while (threadAlive <= 1000 && !LOGGED_OUT){

                    if(threadAlive < 2){
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            /*
                            getSupportActionBar().setTitle(Html.fromHtml("<font color='#ffffff'>" +
                                    announce + "</font>" + finalTx));

                             */
                            textView.setTextColor(Color.rgb(222, 31, 44));

                        }
                    });


                    try {
                        Thread.sleep(150);
                    } catch (InterruptedException e) {
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            /*
                            getSupportActionBar().setTitle(Html.fromHtml("<font color='#ffffff'>" +
                                    announce + "</font>" + finalTx));

                             */
                            textView.setTextColor(Color.rgb(31, 222, 181));
                        }
                    });


                    try {
                        Thread.sleep(150);
                    } catch (InterruptedException e) {
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }

                    threadAlive++;
                }
            }
        });
    }

    private void setStatistics(){
        double fakeTotalCost = totalCost - stoppedCost,
                fakeTotalMeal = totalMeal - stoppedMeal, reminderMoney;

        String s = "Total meal: " + df.format(totalMeal) + ",\nCurrent Meal: " + df.format(fakeTotalMeal);
        txtTotalMeal.setText(s);
        s = "Total Cost: " + df.format(totalCost) + ",\nCurrent cost: " + df.format(fakeTotalCost);
        txtTotalCost.setText(s);
        reminderMoney = totalPaid - totalCost;
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
                "(" + df.format(fakeTotalCost) + " ÷ " + df.format(fakeTotalMeal) + ")";
        txtMealRate.setText(s);
        s = "Total Paid: " + df.format(totalPaid);
        txtTotalPaid.setText(s);
        TextView editText = findViewById(R.id.txtExtraMoney);
        s = "Extra Money: " + df.format(extraMoney);
        editText.setText(s);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if(IS_MANAGER){
            menu.findItem(R.id.updateMPDPP).setVisible(true);
            menu.findItem(R.id.makeAnnouncement).setVisible(true);
            menu.findItem(R.id.manageTodayMeal).setVisible(true);
            menu.findItem(R.id.lastTimeOfChangingMeal).setVisible(true);
            menu.findItem(R.id.changePassword).setVisible(true);
            menu.findItem(R.id.btnLogOut).setVisible(true);
            menu.findItem(R.id.memberPasswords).setVisible(true);
            menu.findItem(R.id.menu_editInfo).setVisible(true);
        }else if(LOGGED_OUT){
            menu.findItem(R.id.updateMPDPP).setVisible(false);
            menu.findItem(R.id.makeAnnouncement).setVisible(false);
            menu.findItem(R.id.manageTodayMeal).setVisible(false);
            menu.findItem(R.id.changePassword).setVisible(false);
            menu.findItem(R.id.btnLogOut).setVisible(false);
            menu.findItem(R.id.memberPasswords).setVisible(false);
            menu.findItem(R.id.lastTimeOfChangingMeal).setVisible(false);
            menu.findItem(R.id.menu_editInfo).setVisible(false);
        }
        else{
            menu.findItem(R.id.updateMPDPP).setVisible(false);
            menu.findItem(R.id.makeAnnouncement).setVisible(false);
            menu.findItem(R.id.manageTodayMeal).setVisible(true);
            menu.findItem(R.id.lastTimeOfChangingMeal).setVisible(false);
            menu.findItem(R.id.memberPasswords).setVisible(false);
            menu.findItem(R.id.changePassword).setVisible(true);
            menu.findItem(R.id.btnLogOut).setVisible(true);
            menu.findItem(R.id.menu_editInfo).setVisible(false);
        }
        menu.findItem(R.id.itemAbout).setVisible(true);
        menu.findItem(R.id.btnInstructions).setVisible(true);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.lastTimeOfChangingMeal) {

            SetLastTimeOfChangingMeals setLastTimeOfChangingMeals = new SetLastTimeOfChangingMeals(
                    MainActivity.this);
            setLastTimeOfChangingMeals.show();
            return true;
        }
        if(id == R.id.changePassword){

            UpdatePassword updatePassword = new UpdatePassword(MainActivity.this);
            updatePassword.show();

            return true;
        }

        if(id == R.id.btnLogOut){
            PermissionToLogOut permissionToLogOut = new PermissionToLogOut(MainActivity.this);
            permissionToLogOut.show();
        }

        if(id == R.id.updateMPDPP){
            UpdateMPDPP updateMPDPP = new UpdateMPDPP(MainActivity.this);
            updateMPDPP.show();
        }

        if(id == R.id.makeAnnouncement){

            TextView textView = findViewById(R.id.txtAnnouncement);
            String announcement = textView.getText().toString();

            MakeAnnouncement makeAnnouncement = new MakeAnnouncement(this, announcement);
            makeAnnouncement.show();

            WindowManager.LayoutParams params = getWindowParams(makeAnnouncement, 0.9f, 0.3f);
            makeAnnouncement.getWindow().setAttributes(params);
        }

        if(id == R.id.manageTodayMeal){
            Intent intent = new Intent(MainActivity.this, MaintainTodaysMeal.class);
            startActivity(intent);
        }

        if(id == R.id.btnInstructions){
            Intent intent = new Intent(MainActivity.this, ActivityInstructions.class);
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
            Intent i = new Intent(this, EditInfo.class);
            startActivity(i);
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDataChange(@NonNull DataSnapshot snapshot) {
        if(snapshot.child("Discussion").child("notifications").child("text").exists()){
            String text =
                    snapshot.child("Discussion").child("notifications").child("text").getValue(String.class);

            if(snapshot.child("Discussion").child("notifications").child("title").exists()) {
                String title = snapshot.child("Discussion").child("notifications").
                        child("title").getValue(String.class);

                if(snapshot.child("Discussion").child("notifications").child("id").exists()){
                    int id = snapshot.child("Discussion").child("notifications").
                            child("id").getValue(Integer.class);

                    DatabaseReference r1 = rootRef.child("Discussion")
                            .child("notifications");

                    NotificationManagerCompat notificationManager =
                            NotificationManagerCompat.from(MainActivity.this);
                    Notification notification = new NotificationCompat.Builder(MainActivity.this,
                            MyNotificationChannels.ANNOUNCEMENT_ID)
                            .setSmallIcon(R.drawable.announcementicon)
                            .setContentTitle(title)
                            .setLargeIcon(BitmapFactory.decodeResource(getResources()
                                    ,R.drawable.announcementicon))
                            .setContentText(text)
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                            .build();
                    notificationManager.notify(id, notification);

                    r1.removeValue();
                }
            }

        }

        makeViewsInvisible();
        readFromDatabase(new CallBack() {
            @Override
            public void onCallback() {
                makeViewsVisible();
                setStatistics();
                recalculate();

                if(overViewClicked) setDataToAppFrame();
                else if(detailsClicked) setDetailsToFrame();
                else if(cooksBillClicked) setCooksBillsToFrame();
                else if(marketHistoryClicked) setMarketHistoryToFrame();
                countOfTodaysMeal();
                setAnnouncementToFront();
            }
        });

    }

    @Override
    public void onCancelled(@NonNull DatabaseError error) {

    }

    public class MakeAnnouncement extends Dialog implements View.OnClickListener{

        private EditText editText;
        private final String ann;

        public MakeAnnouncement(@NonNull Context context, String ann) {
            super(context);
            this.ann = ann;
        }

        @Override
        public void onClick(View v) {
            if(v.getId() == R.id.btnMakeAnnouncement){

                editText = findViewById(R.id.edtEnterAnnouncement);
                String announcement = editText.getText().toString();

                DatabaseReference r = rootRef.child("Post").child(getTodayDate()),
                        r1 = rootRef.child("Discussion");
                r.setValue(announcement);

                r1.child("notifications").child("text").setValue(announcement);
                r1.child("notifications").child("title").setValue("Announcement from Manager");
                r1.child("notifications").child("id").setValue(1);


                setAnnouncementToFront();

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
            if(!ann.isEmpty()) editText.setText(ann);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
    public void initialize(){
        rootLayout = findViewById(R.id.rootLayout);
        txtTotalPaid = findViewById(R.id.txtTotalPaid);
        txtTotalMeal = findViewById(R.id.txtTotalMeal);
        txtMealRate = findViewById(R.id.txtMealRate);
        txtReminderMoney = findViewById(R.id.txtReminderMoney);
        txtTotalCost = findViewById(R.id.txtTotalCost);
        txtOverView = findViewById(R.id.txtOverView);
        txtDetails = findViewById(R.id.txtDetails);
        txtLastUpdate = findViewById(R.id.lastUpdate);
        txtMarketHistory = findViewById(R.id.txtMarketHistory);
        marketerNameLabel = findViewById(R.id.marketerNameLabel);
        marketDateLabel = findViewById(R.id.marketDateLabel);
        marketAmountLabel = findViewById(R.id.marketAmountLabel);
        nameLabel = findViewById(R.id.nameLabel);
        paidLabel = findViewById(R.id.paidLabel);
        mealLabel = findViewById(R.id.mealLabel);
        txtCooksBill = findViewById(R.id.txtCookBills);
        dueLabel = findViewById(R.id.dueLabel);
        overHeadLabel = findViewById(R.id.overHeadLabel);
        btnAddMarket = findViewById(R.id.btnAddMarket);

        boarders = new ArrayList<>();
        marketerHistories = new ArrayList<>();
        cooksBills = new ArrayList<>();
        mealStatuses = new ArrayList<>();
        todayMealStatuses = new ArrayList<>();
        stoppedBoarders = new ArrayList<>();
        fAuth = FirebaseAuth.getInstance();

        rootFolder = getCacheDir();
        evidence = new File(rootFolder, INFO_FILE);

        addBoarder = findViewById(R.id.addBoarder);
        addBoarder.setOnClickListener(MainActivity.this);
        findViewById(R.id.updateData).setOnClickListener(MainActivity.this);
        txtMarketHistory.setOnClickListener(MainActivity.this);
        txtDetails.setOnClickListener(MainActivity.this);
        txtOverView.setOnClickListener(MainActivity.this);
        btnAddMarket.setOnClickListener(MainActivity.this);

        findViewById(R.id.txtCookBills).setOnClickListener(MainActivity.this);
        findViewById(R.id.btnDiscussion).setOnClickListener(MainActivity.this);
        findViewById(R.id.updatePayment).setOnClickListener(MainActivity.this);
        findViewById(R.id.eraseAll).setOnClickListener(MainActivity.this);

        createNeededFiles();
        checkAndSetSecurity();
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

    }

    @Override
    public void onClick(View v) {

        if(v.getId() == R.id.addBoarder){

            if(boarders.size() + 1 == MAX_BOARDER){

                Toast.makeText(MainActivity.this, "Member more than " + MAX_BOARDER + " isn't allowed!",
                        Toast.LENGTH_LONG).show();
                return;
            }
            AddMemberDialog addMemberDialog = new AddMemberDialog(MainActivity.this);
            addMemberDialog.show();
            WindowManager.LayoutParams layoutParams = getWindowParams(addMemberDialog,
                    0.8f, 0.4f);
            addMemberDialog.getWindow().setAttributes(layoutParams);
        }
        if(v.getId() == R.id.updateData){

            if(totalCost == 0.0){
                Toast.makeText(this, "You need to input market cost before updating meal",
                        Toast.LENGTH_LONG).show();
                return;
            }

            UpdateMealDialog updateMealDialog = new UpdateMealDialog(MainActivity.this);
            updateMealDialog.show();

        }
        if(v.getId() == R.id.txtOverView){
            overViewClicked = true;
            detailsClicked = false;
            cooksBillClicked = false;
            marketHistoryClicked = false;
            setDataToAppFrame();
        }
        if(v.getId() == R.id.txtDetails){
            overViewClicked = false;
            detailsClicked = true;
            cooksBillClicked = false;
            marketHistoryClicked = false;
            setDetailsToFrame();
        }
        if(v.getId() == R.id.txtMarketHistory){
            overViewClicked = false;
            detailsClicked = false;
            cooksBillClicked = false;
            marketHistoryClicked = true;
            setMarketHistoryToFrame();
        }
        if(v.getId() == R.id.btnAddMarket){
            AddMarketHistoryDialog addMarketHistoryDialog = new AddMarketHistoryDialog(MainActivity.this);
            addMarketHistoryDialog.show();

            WindowManager.LayoutParams layoutParams =
                    getWindowParams(addMarketHistoryDialog, 0.8f, 0.4f);
            addMarketHistoryDialog.getWindow().setAttributes(layoutParams);
        }
        if(v.getId() == R.id.txtCookBills){
            overViewClicked = false;
            detailsClicked = false;
            cooksBillClicked = true;
            marketHistoryClicked = false;
            setCooksBillsToFrame();
            btnAddMarket.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DatabaseReference r = rootRef.child("cooksBill");
                    for(int i = 0; i < cooksBills.size(); i++){
                        String cur = edtCooksBill[i].getText().toString();
                        if(cur.equals("")) cur = "0";
                        String fin = (Double.parseDouble(cooksBills.get(i).getPaid()) +
                                Double.parseDouble(cur)) + "";

                        cooksBills.set(i, new CooksBill(cooksBills.get(i).getName(), fin));
                    }
                    r.setValue(cooksBills);
                    setCooksBillsToFrame();
                }
            });

        }
        if(v.getId() == R.id.btnDiscussion){
            Intent intent = new Intent(MainActivity.this, DiscussionActivity.class);
            startActivity(intent);
        }
        if(v.getId() == R.id.updatePayment){
            UpdatePaymentDialog updatePaymentDialog = new UpdatePaymentDialog(MainActivity.this);
            updatePaymentDialog.show();

        }
        if(v.getId() == R.id.eraseAll){

            EraseAll eraseAll = new EraseAll(MainActivity.this);
            eraseAll.show();
        }
    }

    private void prepareMarketHistoryLayout(){
        txtOverView.setPaintFlags(txtOverView.getPaintFlags() & (~Paint.UNDERLINE_TEXT_FLAG));
        txtOverView.setTextColor(Color.BLACK);

        txtMarketHistory.setPaintFlags(txtMarketHistory.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        txtMarketHistory.setTextColor(getResources().getColor(R.color.actionbar_color));

        txtDetails.setPaintFlags(txtDetails.getPaintFlags() & (~Paint.UNDERLINE_TEXT_FLAG));
        txtCooksBill.setPaintFlags(txtDetails.getPaintFlags() & (~Paint.UNDERLINE_TEXT_FLAG));

        txtCooksBill.setTextColor(Color.BLACK);
        txtDetails.setTextColor(Color.BLACK);

        rootLayout.removeAllViews();
        marketerNameLabel.setVisibility(View.VISIBLE);
        marketDateLabel.setVisibility(View.VISIBLE);
        marketAmountLabel.setVisibility(View.VISIBLE);
        btnAddMarket.setVisibility(View.VISIBLE);
        btnAddMarket.setText("+");
        nameLabel.setVisibility(View.GONE);
        paidLabel.setVisibility(View.GONE);
        mealLabel.setVisibility(View.GONE);
        dueLabel.setVisibility(View.GONE);
        overHeadLabel.setVisibility(View.GONE);
        findViewById(R.id.labelLayout).setVisibility(View.VISIBLE);
        findViewById(R.id.infoScroll).setVisibility(View.VISIBLE);
        findViewById(R.id.detailsLayout).setVisibility(View.GONE);
        rootLayout.setVisibility(View.VISIBLE);
        findViewById(R.id.addPaymentLabel).setVisibility(View.GONE);
        btnAddMarket.setOnClickListener(MainActivity.this);

        if(IS_MANAGER) btnAddMarket.setVisibility(View.VISIBLE);
        else btnAddMarket.setVisibility(View.GONE);
    }
    private void prepareDetailsLayout(){
        txtOverView.setPaintFlags(txtOverView.getPaintFlags() & (~Paint.UNDERLINE_TEXT_FLAG));
        txtOverView.setTextColor(Color.BLACK);

        txtCooksBill.setPaintFlags(txtDetails.getPaintFlags() & (~Paint.UNDERLINE_TEXT_FLAG));
        txtCooksBill.setTextColor(Color.BLACK);

        txtMarketHistory.setPaintFlags(txtMarketHistory.getPaintFlags() & (~Paint.UNDERLINE_TEXT_FLAG));
        txtMarketHistory.setTextColor(Color.BLACK);

        txtDetails.setPaintFlags(txtDetails.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        txtDetails.setTextColor(getResources().getColor(R.color.actionbar_color));

        findViewById(R.id.labelLayout).setVisibility(View.GONE);
        findViewById(R.id.detailsLayout).setVisibility(View.VISIBLE);
        findViewById(R.id.rootLayout).setVisibility(View.GONE);
    }
    private void prepareOverViewLayout(){
        txtOverView.setPaintFlags(txtOverView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        txtOverView.setTextColor(getResources().getColor(R.color.actionbar_color));

        txtMarketHistory.setPaintFlags(txtMarketHistory.getPaintFlags() & (~Paint.UNDERLINE_TEXT_FLAG));
        txtMarketHistory.setTextColor(Color.BLACK);

        txtDetails.setPaintFlags(txtDetails.getPaintFlags() & (~Paint.UNDERLINE_TEXT_FLAG));
        txtDetails.setTextColor(Color.BLACK);

        findViewById(R.id.addPaymentLabel).setVisibility(View.GONE);
        txtCooksBill.setPaintFlags(txtDetails.getPaintFlags() & (~Paint.UNDERLINE_TEXT_FLAG));
        txtCooksBill.setTextColor(Color.BLACK);
        marketerNameLabel.setVisibility(View.GONE);
        btnAddMarket.setVisibility(View.GONE);
        marketDateLabel.setVisibility(View.GONE);
        marketAmountLabel.setVisibility(View.GONE);
        nameLabel.setVisibility(View.VISIBLE);
        paidLabel.setVisibility(View.VISIBLE);
        mealLabel.setVisibility(View.VISIBLE);
        dueLabel.setVisibility(View.VISIBLE);
        overHeadLabel.setVisibility(View.VISIBLE);
        findViewById(R.id.detailsLayout).setVisibility(View.GONE);
        findViewById(R.id.rootLayout).setVisibility(View.VISIBLE);
        findViewById(R.id.labelLayout).setVisibility(View.VISIBLE);
    }
    private void prepareCooksBillLayout(){
        txtCooksBill.setPaintFlags(txtDetails.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        txtCooksBill.setTextColor(getResources().getColor(R.color.actionbar_color));
        txtOverView.setPaintFlags(txtOverView.getPaintFlags() & (~Paint.UNDERLINE_TEXT_FLAG));
        txtOverView.setTextColor(Color.BLACK);
        txtMarketHistory.setPaintFlags(txtMarketHistory.getPaintFlags() & (~Paint.UNDERLINE_TEXT_FLAG));
        txtMarketHistory.setTextColor(Color.BLACK);
        txtDetails.setPaintFlags(txtDetails.getPaintFlags() & (~Paint.UNDERLINE_TEXT_FLAG));
        txtDetails.setTextColor(Color.BLACK);
        findViewById(R.id.labelLayout).setVisibility(View.VISIBLE);
        findViewById(R.id.infoScroll).setVisibility(View.VISIBLE);
        findViewById(R.id.detailsLayout).setVisibility(View.GONE);
        findViewById(R.id.addPaymentLabel).setVisibility(View.VISIBLE);
        mealLabel.setVisibility(View.GONE);
        dueLabel.setVisibility(View.GONE);
        overHeadLabel.setVisibility(View.GONE);
        findViewById(R.id.marketerNameLabel).setVisibility(View.GONE);
        findViewById(R.id.marketAmountLabel).setVisibility(View.GONE);
        findViewById(R.id.marketDateLabel).setVisibility(View.GONE);
        paidLabel.setVisibility(View.VISIBLE);
        nameLabel.setVisibility(View.VISIBLE);
        findViewById(R.id.detailsLayout).setVisibility(View.GONE);
        findViewById(R.id.rootLayout).setVisibility(View.VISIBLE);
        if(!IS_MANAGER){
            btnAddMarket.setVisibility(View.INVISIBLE);
            findViewById(R.id.addPaymentLabel).setVisibility(View.INVISIBLE);
        }

        if(IS_MANAGER){
            btnAddMarket.setVisibility(View.VISIBLE);
            String s = "Update Bills";
            btnAddMarket.setText(s);
        }
    }

    private void setCooksBillsToFrame(){
        prepareCooksBillLayout();
        final ImageView[] calc = new ImageView[MAX_BOARDER];
        rootLayout.removeAllViews();

        Resources res = getResources();
        Drawable drawable = null;
        try {
            drawable = Drawable.createFromXml(res,
                    res.getXml(R.xml.rectangular_shape_cook_bill));
        }
        catch (IOException | XmlPullParserException e) {
            e.printStackTrace();
        }

        for(int i = 0; i < cooksBills.size(); i++) {

            LinearLayout ll = new LinearLayout(MainActivity.this),
                    fake = new LinearLayout(MainActivity.this);
            rootLayout.addView(ll);
            rootLayout.addView(fake);

            ll.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams paramsLl = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, 150),
                    paramsFake =
                            new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 10);
            ll.setLayoutParams(paramsLl);
            fake.setLayoutParams(paramsFake);

            if(drawable != null) ll.setBackground(drawable);

            fake.setBackgroundColor(Color.WHITE);

            TextView name = new TextView(MainActivity.this),
                    paid = new TextView(MainActivity.this);
            if(IS_MANAGER) {
                edtCooksBill[i] = new EditText(MainActivity.this);
                edtCooksBill[i].setInputType(InputType.TYPE_CLASS_NUMBER);
                calc[i] = new ImageView(MainActivity.this);
                calc[i].setId(i);
            }
            TextView tv = new TextView(MainActivity.this);


            ll.addView(name);
            ll.addView(paid);
            if(IS_MANAGER)
                ll.addView(edtCooksBill[i]);
            if(IS_MANAGER) {
                ll.addView(calc[i]);
            }
            else
                ll.addView(tv);
            ll.setGravity(Gravity.CENTER);


            LinearLayout.LayoutParams paramsNm = new LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.MATCH_PARENT, 30);

            name.setLayoutParams(paramsNm);
            String s = (i + 1) + ". " + cooksBills.get(i).getName();
            name.setText(s);
            name.setGravity(Gravity.CENTER);
            name.setTypeface(Typeface.DEFAULT_BOLD);

            LinearLayout.LayoutParams paramsDt = new LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    20
            );
            paid.setText(df.format(Double.parseDouble(cooksBills.get(i).getPaid())));
            paid.setGravity(Gravity.CENTER);
            paid.setTypeface(Typeface.DEFAULT_BOLD);
            paid.setLayoutParams(paramsDt);

            LinearLayout.LayoutParams paramsAm = new LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    30
            );
            if(IS_MANAGER){
                edtCooksBill[i].setHint("Add Payment");
                edtCooksBill[i].setLayoutParams(paramsAm);

                calc[i].setImageResource(R.drawable.calcimagee);
                calc[i].setLayoutParams(new LinearLayout.LayoutParams(
                        100, 100));
            }

            if(!IS_MANAGER)
                tv.setLayoutParams(paramsAm);

            if(IS_MANAGER){
                final int finalI = i;
                calc[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        CalculatorInterface c = new CalculatorInterface(MainActivity.this,
                                calc[finalI].getId(),
                                new UpdateEditText() {
                                    @Override
                                    public void onUpdate(String s, int ID) {
                                        edtCooksBill[ID].setText(s);
                                    }
                                });
                        c.show();
                        DisplayMetrics displayMetrics = new DisplayMetrics();
                        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                        int displayWidth = displayMetrics.widthPixels;
                        int displayHeight = displayMetrics.heightPixels;
                        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
                        layoutParams.copyFrom(c.getWindow().getAttributes());
                        int dialogWindowWidth = (int) (displayWidth * 0.9f);
                        int dialogWindowHeight = (int) (displayHeight * 0.8f);
                        layoutParams.width = dialogWindowWidth;
                        layoutParams.height = dialogWindowHeight;
                        c.getWindow().setAttributes(layoutParams);
                    }
                });
            }

        }
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

                for(int i = 0; i < boarders.size(); i++){
                    DatabaseReference r = rootRef.child("members").child(boarders.get(i).getName());
                    r.removeValue();
                }

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

            }
            dismiss();
        }

        private void initialize(){
            TextView textView = findViewById(R.id.wantToLogOut);
            Button button = findViewById(R.id.yesToDelete);
            txtConsent = findViewById(R.id.permission_txtConsent);
            edtConsentText = findViewById(R.id.permission_edtWriteMove);

            txtConsent.setVisibility(View.VISIBLE);
            edtConsentText.setVisibility(View.VISIBLE);

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

    private void setDetailsToFrame(){

        prepareDetailsLayout();
        rootLayout.removeAllViews();
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
            LinearLayout ll = new LinearLayout(MainActivity.this),
                    fake = new LinearLayout(MainActivity.this);
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



            //if(drawable != null) ll.setBackground(drawable);
            ll.setBackgroundColor(getResources().getColor(R.color.meal_details));

            for(int j = 0; boarders.get(i).getMealD() != null && j < boarders.get(i).getMealD().size(); j++){
                TextView det = new TextView(MainActivity.this),
                        nameView = new TextView(MainActivity.this);
                LinearLayout h = new LinearLayout(MainActivity.this);

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

            final int finalI = i;
            ll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, EditInterfaceActivity.class);
                    intent.putExtra("TYPE", "MEAL");
                    intent.putExtra("INDEX", finalI);
                    startActivity(intent);
                }
            });
        }

        for(int i = 0; i < stoppedBoarders.size(); i++){
            LinearLayout ll = new LinearLayout(MainActivity.this),
                    fake = new LinearLayout(MainActivity.this);
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
                TextView det = new TextView(MainActivity.this),
                        nameView = new TextView(MainActivity.this);
                LinearLayout h = new LinearLayout(MainActivity.this);

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
            if(boarders.get(i).getMealD().size() > width)
                width = boarders.get(i).getPaymentD().size();
        }
        for(int i = 0; i < stoppedBoarders.size(); i++){
            if(stoppedBoarders.get(i).getMealD().size() > width)
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
            LinearLayout ll = new LinearLayout(MainActivity.this),
                    fake = new LinearLayout(MainActivity.this);
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
                TextView det = new TextView(MainActivity.this),
                        nameView = new TextView(MainActivity.this);
                LinearLayout h = new LinearLayout(MainActivity.this);

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

            final int finalI = i;
            ll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, EditInterfaceActivity.class);
                    intent.putExtra("TYPE", "PAYMENT");
                    intent.putExtra("INDEX", finalI);
                    startActivity(intent);
                }
            });
        }

        for(int i = 0; i < stoppedBoarders.size(); i++){
            LinearLayout ll = new LinearLayout(MainActivity.this),
                    fake = new LinearLayout(MainActivity.this);
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
                TextView det = new TextView(MainActivity.this),
                        nameView = new TextView(MainActivity.this);
                LinearLayout h = new LinearLayout(MainActivity.this);

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

    private interface CallBack{
        void onCallback();
    }

    private void setLogInPage(){

        findViewById(R.id.mealOverViewLayout).setVisibility(View.GONE);
        findViewById(R.id.announceLayout).setVisibility(View.GONE);
        findViewById(R.id.modeLayout).setVisibility(View.GONE);
        findViewById(R.id.labelLayout).setVisibility(View.GONE);
        findViewById(R.id.totalThingsLayout).setVisibility(View.GONE);
        findViewById(R.id.rootLayout).setVisibility(View.GONE);
        findViewById(R.id.detailsLayout).setVisibility(View.GONE);

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
                        mealStatusThreadAlive = false;
                        getLastUpdateTime();
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

            findViewById(R.id.updateLabelLayout).setVisibility(View.GONE);

            rbB = findViewById(R.id.rbBreakFirstDecide);
            rbL = findViewById(R.id.rbLunchDecide);
            rbD = findViewById(R.id.rbDinnerDecide);
            rbB.setChecked(isBreakfastOn);
            rbL.setChecked(isLunchOn);
            rbD.setChecked(isDinnerOn);

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
                        //if(payment.equals("")) payment = "0";
                        if(payment.equals("")) continue;
                        i++;
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
                        df.format(boarders.get(index).getPaidMoney()) + "): ";
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
                        }else{
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
                    }


                }
                r2.child("Breakfast").setValue(rbB.isChecked());
                r2.child("Lunch").setValue(rbL.isChecked());
                r2.child("Dinner").setValue(rbD.isChecked());
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
                                String s1 = "Do you want to change meal status of " +
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

                    txtOverView.setTextSize(15);
                    txtOverView.setPaintFlags(txtOverView.getPaintFlags() & (~Paint.UNDERLINE_TEXT_FLAG));
                    txtOverView.setTextColor(Color.BLACK);

                    txtCooksBill.setTextSize(15);
                    txtCooksBill.setPaintFlags(txtDetails.getPaintFlags() & (~Paint.UNDERLINE_TEXT_FLAG));
                    txtCooksBill.setTextColor(Color.BLACK);

                    txtMarketHistory.setTextSize(15);
                    txtMarketHistory.setPaintFlags(txtMarketHistory.getPaintFlags() & (~Paint.UNDERLINE_TEXT_FLAG));
                    txtMarketHistory.setTextColor(Color.BLACK);

                    txtDetails.setTextSize(15);
                    txtDetails.setPaintFlags(txtDetails.getPaintFlags() & (~Paint.UNDERLINE_TEXT_FLAG));
                    txtDetails.setTextColor(Color.CYAN);
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

    private void setMarketHistoryToFrame(){
        prepareMarketHistoryLayout();
        rootLayout.removeAllViews();

        Resources res = getResources();
        Drawable drawable = null;
        try {
            drawable = Drawable.createFromXml(res,
                    res.getXml(R.xml.rectangular_shape_market_history));
        }
        catch (IOException | XmlPullParserException e) {
            e.printStackTrace();
        }

        for(int i = 0; i < marketerHistories.size(); i++){
            LinearLayout ll = new LinearLayout(MainActivity.this),
                    fake = new LinearLayout(MainActivity.this);
            rootLayout.addView(ll);
            rootLayout.addView(fake);

            ll.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams paramsLl = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT),
                    paramsFake = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 10);

            ll.setLayoutParams(paramsLl);
            if(drawable != null) ll.setBackground(drawable);
            fake.setLayoutParams(paramsFake);
            fake.setBackgroundColor(Color.WHITE);


            TextView name = new TextView(this),
                    date = new TextView(this),
                    amount = new TextView(this),
                    fakeT = new TextView(this),
                    fakeH1 = new TextView(this),
                    fakeH2 = new TextView(this),
                    fakeH3 = new TextView(this);

            ll.addView(name);
            ll.addView(fakeH1);
            ll.addView(date);
            ll.addView(fakeH2);
            ll.addView(amount);
            ll.addView(fakeH3);
            ll.setGravity(Gravity.CENTER);

            //name.setTextSize(20);
            //date.setTextSize(20);
            //amount.setTextSize(20);
            name.setTypeface(Typeface.DEFAULT_BOLD);
            date.setTypeface(Typeface.DEFAULT_BOLD);
            amount.setTypeface(Typeface.DEFAULT_BOLD);

            date.setGravity(Gravity.CENTER);
            date.setTextColor(Color.WHITE);
            amount.setGravity(Gravity.CENTER);
            amount.setTextColor(Color.WHITE);

            name.setGravity(Gravity.CENTER);
            name.setTextColor(Color.WHITE);

            fakeH1.setLayoutParams(new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.MATCH_PARENT, 1));
            fakeH2.setLayoutParams(new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.MATCH_PARENT, 1));
            fakeH3.setLayoutParams(new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.MATCH_PARENT, 1));

            fakeH1.setBackgroundColor(Color.WHITE);
            fakeH2.setBackgroundColor(Color.WHITE);
            fakeH3.setBackgroundColor(Color.WHITE);

            LinearLayout.LayoutParams paramsNm = new LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    35
            );
            name.setLayoutParams(paramsNm);
            String nm = (i + 1) + ". " + marketerHistories.get(i).getName();
            name.setText(nm);

            LinearLayout.LayoutParams paramsDt = new LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    42
            );
            date.setText(marketerHistories.get(i).getExpenseHistory());
            date.setLayoutParams(paramsDt);

            LinearLayout.LayoutParams paramsAm = new LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    20
            );
            amount.setText(marketerHistories.get(i).getTotalAmount());
            amount.setLayoutParams(paramsAm);

            fakeT.setLayoutParams(paramsAm);
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
                    }else{
                        b = 0;
                    }
                    if(isLunchOn){
                        l = 1;
                        total++;
                    }else{
                        l = 0;
                    }
                    if(isDinnerOn){
                        d = 1;
                        total++;
                    }else{
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

                    dismiss();
                }
                else if(boarderExist == -1){
                    Toast.makeText(MainActivity.this, "Enter Name \uD83D\uDE44",
                            Toast.LENGTH_LONG).show();
                }
                else if(boarderExist == -2){
                    Toast.makeText(MainActivity.this, "Invalid Name \uD83D\uDE20",
                            Toast.LENGTH_LONG).show();
                }else{
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

    private void readFromDatabase(final CallBack callBack){
        rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boarders.clear();
                stoppedBoarders.clear();
                marketerHistories.clear();
                mealStatuses.clear();
                todayMealStatuses.clear();
                cooksBills.clear();
                totalPaid = 0;
                totalMeal = 0;
                stoppedCost = 0.0;
                stoppedMeal = 0.0;
                extraMoney = 0.0;
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
                        if(boarder.getDue() == 0)
                            stoppedCost += (boarder.getPaidMoney() - boarder.getOverHead());
                        else stoppedCost += (boarder.getPaidMoney() + boarder.getDue());

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
                }else{
                    isBreakfastOn = false;
                    isLunchOn = false;
                    isDinnerOn = false;
                }

                if(snapshot.child("Marketer History").exists()){
                    totalCost = 0;
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

                callBack.onCallback();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

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

    private void setDataToAppFrame(){

        prepareOverViewLayout();
        rootLayout.removeAllViews();
        setStatistics();

        if(boarders.size() == 0 && stoppedBoarders.size() == 0){
            rootLayout.setVisibility(View.VISIBLE);

            findViewById(R.id.announceLayout).setVisibility(View.GONE);
            findViewById(R.id.mealOverViewLayout).setVisibility(View.GONE);
            findViewById(R.id.modeLayout).setVisibility(View.GONE);
            findViewById(R.id.labelLayout).setVisibility(View.GONE);
            //findViewById(R.id.totalThingsScroll).setVisibility(View.GONE);

            TextView txtWelcomeText = new TextView(MainActivity.this),
                    txtInstructions = new TextView(this);
            rootLayout.addView(txtWelcomeText);
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


            txtWelcomeText.setText(spannable, TextView.BufferType.SPANNABLE);

            addBoarder.setTextColor(Color.RED);

            rootLayout.addView(txtInstructions);
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
        else{
            addBoarder.setTextColor(Color.BLACK);

            findViewById(R.id.announceLayout).setVisibility(View.VISIBLE);
            findViewById(R.id.mealOverViewLayout).setVisibility(View.VISIBLE);
            findViewById(R.id.modeLayout).setVisibility(View.VISIBLE);
            findViewById(R.id.labelLayout).setVisibility(View.VISIBLE);
            //findViewById(R.id.totalThingsScroll).setVisibility(View.VISIBLE);

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
                LinearLayout ll = new LinearLayout(MainActivity.this),
                        fake = new LinearLayout(MainActivity.this),
                        fakeH1 = new LinearLayout(MainActivity.this),
                        fakeH2 = new LinearLayout(MainActivity.this),
                        fakeH3 = new LinearLayout(MainActivity.this),
                        fakeH4 = new LinearLayout(MainActivity.this);
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


                int pos = -1;
                double d1, ov1, d2, ov2, d = 0, ov = 0;
                for(int j = 0; j < stoppedBoarders.size(); j++){
                    if(boarders.get(i).getName().equals(stoppedBoarders.get(j).getName())){
                        d1 = boarders.get(i).getDue();
                        d2 = stoppedBoarders.get(j).getDue();
                        ov1 = boarders.get(i).getOverHead();
                        ov2 = stoppedBoarders.get(j).getOverHead();
                        d = d1 + d2;
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
                name.setTextColor(Color.rgb(245, 247, 246));
                //name.setTextSize(17);

                TextView paid = new TextView(this);
                if(pos == -1) s = df.format(boarders.get(i).getPaidMoney()) + "";
                else s = df.format((boarders.get(i).getPaidMoney() + stoppedBoarders.get(pos).getPaidMoney())) + "";
                paid.setText(s);
                paid.setGravity(Gravity.CENTER);
                paid.setTypeface(Typeface.DEFAULT_BOLD);
                //paid.setTextSize(17);
                paid.setTextColor(Color.rgb(245, 247, 246));

                TextView meal = new TextView(this);
                if(pos == -1) s = df.format(boarders.get(i).getMeals()) + "";
                else s = df.format((boarders.get(i).getMeals() + stoppedBoarders.get(pos).getMeals())) + "";
                meal.setText(s);
                meal.setGravity(Gravity.CENTER);
                meal.setTypeface(Typeface.DEFAULT_BOLD);
                //meal.setTextSize(17);
                meal.setTextColor(Color.rgb(245, 247, 246));

                TextView due = new TextView(this);
                if(pos == -1) s = df.format(boarders.get(i).getDue());
                else s = df.format(d);
                due.setText(s);
                due.setGravity(Gravity.CENTER);
                due.setTypeface(Typeface.DEFAULT_BOLD);
                //due.setTextSize(17);
                due.setTextColor(Color.rgb(245, 247, 246));

                TextView overHead = new TextView(this);
                if(pos == -1) s = df.format(boarders.get(i).getOverHead());
                else s = df.format(ov);
                overHead.setText(s);
                overHead.setGravity(Gravity.CENTER);
                overHead.setTextColor(Color.rgb(245, 247, 246));
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
                    LinearLayout ll = new LinearLayout(MainActivity.this),
                            fake = new LinearLayout(MainActivity.this),
                            fakeH1 = new LinearLayout(MainActivity.this),
                            fakeH2 = new LinearLayout(MainActivity.this),
                            fakeH3 = new LinearLayout(MainActivity.this),
                            fakeH4 = new LinearLayout(MainActivity.this);
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
                    s = df.format(stoppedBoarders.get(i).getDue()) + "";
                    due.setText(s);
                    due.setGravity(Gravity.CENTER);
                    due.setTextColor(Color.rgb(250, 245, 150));
                    due.setTextSize(17);
                    due.setTextColor(Color.rgb(245, 247, 246));

                    TextView overHead = new TextView(this);
                    s = df.format(stoppedBoarders.get(i).getOverHead()) + "";
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
                            Toast.makeText(MainActivity.this, stoppedBoarders.get(finalI).getName() +
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
                                                Toast.makeText(MainActivity.this,
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
                                                Toast.makeText(MainActivity.this,
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
                                                Toast.makeText(MainActivity.this,
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
                                                Toast.makeText(MainActivity.this,
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
                                                Toast.makeText(MainActivity.this,
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
                                                Toast.makeText(MainActivity.this,
                                                        "Dinner meal off!", Toast.LENGTH_LONG).show();
                                            }
                                        }
                                        else if(id == R.id.reactivateMeal){
                                            final double b = Double.parseDouble(br.getText().toString()),
                                                    l = Double.parseDouble(ln.getText().toString()),
                                                    d = Double.parseDouble(dn.getText().toString()),
                                                    total = b + l + d;
                                            if(total == 0){
                                                Toast.makeText(MainActivity.this, "Can't Reactivate" +
                                                        " with 0 meal", Toast.LENGTH_LONG).show();
                                            }else{
                                                final String nm = stoppedBoarders.get(finalI).getName();
                                                final ArrayList<MealOrPaymentDetails> temp = new ArrayList<>();
                                                temp.add(new MealOrPaymentDetails(getTodayDate(), "0"));

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
                                                    MainActivity.this, stoppedBoarders.get(finalI),
                                                    df.format(b), df.format(l), df.format(d), df.format(total));
                                            p.show();
                                            dismiss();
                                        }
                                    }
                                }

                                ActivateHisMeal activateHisMeal = new ActivateHisMeal(MainActivity.this);
                                activateHisMeal.show();
                                return true;
                            }
                        });
                    }



                    index++;
                }
            }

            LinearLayout ll = new LinearLayout(MainActivity.this);
            LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 10);
            rootLayout.addView(ll);
            ll.setLayoutParams(params);
            ll.setBackgroundColor(Color.BLACK);

        }
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
            edtConsentText.setVisibility(View.VISIBLE);

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
                mealStatusRef.child(boarder.getName()).setValue(m);
                membersRef.child(boarder.getName()).setValue(boarder);
                rootRef.child("those who stopped meal").child("members").
                        child(boarder.getName()).removeValue();

                dismiss();
            }
            else if(id == R.id.noToDelete){
                dismiss();
            }
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