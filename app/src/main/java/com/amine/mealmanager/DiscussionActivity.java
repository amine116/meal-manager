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
import android.text.method.KeyListener;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

import org.xmlpull.v1.XmlPullParserException;
import static com.amine.mealmanager.MainActivity.boarders;
import static com.amine.mealmanager.MainActivity.cooksBills;
import static com.amine.mealmanager.MainActivity.getManagerName;
import static com.amine.mealmanager.MainActivity.getTodayDate;
import static com.amine.mealmanager.MainActivity.rootRef;
import static com.amine.mealmanager.MainActivity.selectedBoarderIndex;
import static com.amine.mealmanager.MainActivity.selectedCookBillIndex;
import static com.amine.mealmanager.MainActivity.selectedStoppedBoarderIndex;
import static com.amine.mealmanager.MainActivity.stoppedBoarders;

public class DiscussionActivity extends AppCompatActivity implements View.OnClickListener, ValueEventListener {

    private ArrayList<DiscussionList> noteList, marketList;
    private TextView txtPublicMessage, txtNote, txtMarketList;
    private Button btnEdit, btnAdd;
    private LinearLayout lstListView;
    private String tempDate = "";
    private DatabaseReference refWithManagerName;
    private boolean isTxtNoteClck = false, isTxtMrktlstClck = false;
    private final boolean IS_MANAGER = MainActivity.IS_MANAGER;
    private LinearLayout discussionTextScroll;
    private int selectedLayout = -1;
    private static String profileName = "N/A";
    private final int EDIT = 101, PUBLISH = 102;
    private Drawable myMessDraw = null, otherMessDraw = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discussion);
        ActionBar a = getSupportActionBar();
        if(a != null) a.setTitle("  Discussions");
        initialize();

        profileName = getProfileName(selectedBoarderIndex, selectedStoppedBoarderIndex,
                selectedCookBillIndex);

        isTxtMrktlstClck = true;
        txtMarketList.setPaintFlags(txtPublicMessage.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        txtMarketList.setTextColor(Color.CYAN);

    }

    public static String getProfileName(int bI, int sbI, int cbI) {
        String profileName = "";
        if(bI != -1){
            profileName = boarders.get(bI).getName();
        }

        if(cbI != -1){
            profileName = cooksBills.get(cbI).getName();
        }
        if(sbI != -1){

            Boarder boarder = stoppedBoarders.get(sbI);

            profileName = boarder.getName();
        }

        return profileName;
    }

    private void initialize(){

        noteList = new ArrayList<>();
        marketList = new ArrayList<>();
        txtPublicMessage = findViewById(R.id.txtPublicMessage);
        txtNote = findViewById(R.id.txtNote);
        txtMarketList = findViewById(R.id.txtMarketList);
        btnAdd = findViewById(R.id.btnAddNew);
        btnEdit = findViewById(R.id.btnEdit);

        if(IS_MANAGER) btnAdd.setVisibility(View.VISIBLE);
        else btnAdd.setVisibility(View.GONE);
        findViewById(R.id.layout_messaging).setVisibility(View.GONE);

        refWithManagerName = rootRef;

        discussionTextScroll = findViewById(R.id.discussionTextScroll);

        btnEdit.setOnClickListener(DiscussionActivity.this);
        btnAdd.setOnClickListener(DiscussionActivity.this);
        txtPublicMessage.setOnClickListener(DiscussionActivity.this);
        txtNote.setOnClickListener(DiscussionActivity.this);
        txtMarketList.setOnClickListener(DiscussionActivity.this);
        lstListView = findViewById(R.id.discussionList);

        Resources res = getResources();
        try {
            refWithManagerName.addValueEventListener(DiscussionActivity.this);
            myMessDraw = Drawable.createFromXml(res, res.getXml(R.xml.my_message_shape));
            otherMessDraw = Drawable.createFromXml(res, res.getXml(R.xml.others_message_shape));
        }catch (Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }

    }

    private void populateMessages(){
        LinearLayout layout = findViewById(R.id.layoutListOfMessaging);
        layout.removeAllViews();
        String messengerName = getMessengerName();
        int textSize = 20;

        for(int i = 0; i < noteList.size(); i++){
            final TextView txtName = new TextView(this),
                    txtDate = new TextView(this),
                    txtMessage = new TextView(this),
                    txtGap = new TextView(this);

            if(i > 0 && !noteList.get(i).getName().equals(noteList.get(i - 1).getName()))
                layout.addView(txtName);
            else if(i == 0) layout.addView(txtName);
            layout.addView(txtDate);layout.addView(txtMessage);
            layout.addView(txtGap);
            txtGap.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    7));

            txtName.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_person_small, 0, 0, 0);


            txtDate.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            if(messengerName.equals(noteList.get(i).getName())){
                LinearLayout.LayoutParams params =
                        new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT);
                params.weight = 1f;
                params.gravity = Gravity.END;
                txtName.setLayoutParams(params);
                txtMessage.setLayoutParams(params);
                txtMessage.setPadding(10, 10, 10,10);

                if(myMessDraw != null) txtMessage.setBackground(myMessDraw);
            }
            else{
                LinearLayout.LayoutParams params =
                        new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT);
                params.weight = 1f;
                params.gravity = Gravity.START;
                txtName.setLayoutParams(params);
                txtMessage.setLayoutParams(params);

                if(otherMessDraw != null) txtMessage.setBackground(otherMessDraw);
            }
            txtDate.setGravity(Gravity.CENTER);
            txtName.setGravity(Gravity.CENTER);
            txtMessage.setGravity(Gravity.CENTER);
            txtName.setText(noteList.get(i).getName());
            txtDate.setText(noteList.get(i).getDate());
            txtMessage.setText(noteList.get(i).getText());

            txtName.setTypeface(Typeface.DEFAULT_BOLD);
            txtName.setTextSize(textSize);
            txtMessage.setTextSize(textSize);

            txtName.setTextColor(Color.WHITE);
            txtDate.setTextColor(Color.WHITE);
            txtMessage.setTextColor(Color.WHITE);

            txtDate.setVisibility(View.GONE);

            txtMessage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int visibility = txtDate.getVisibility();
                    if(visibility == View.GONE) txtDate.setVisibility(View.VISIBLE);
                    else if(visibility == View.VISIBLE) txtDate.setVisibility(View.GONE);
                }
            });

        }

        final ScrollView sc = findViewById(R.id.scrollMessage);
        sc.post(new Runnable() {
            @Override
            public void run() {
                sc.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    private void implementMessagingInterface(){
        findViewById(R.id.discussionCreateLayout).setVisibility(View.GONE);
        findViewById(R.id.txtSubjectInstr).setVisibility(View.GONE);
        findViewById(R.id.scrollMarketList).setVisibility(View.GONE);
        findViewById(R.id.discussionTextScroll).setVisibility(View.GONE);

        findViewById(R.id.layout_messaging).setVisibility(View.VISIBLE);
        findViewById(R.id.imgSendMessage).setOnClickListener(this);
    }

    private void removeMessagingInterface(){
        findViewById(R.id.discussionCreateLayout).setVisibility(View.VISIBLE);
        findViewById(R.id.txtSubjectInstr).setVisibility(View.VISIBLE);
        findViewById(R.id.scrollMarketList).setVisibility(View.VISIBLE);
        findViewById(R.id.discussionTextScroll).setVisibility(View.VISIBLE);
        findViewById(R.id.layout_messaging).setVisibility(View.GONE);
    }

    private String getMessengerName(){
        String name;
        if(IS_MANAGER) name = getManagerName();
        else{
            name = profileName.equals("")? "Anonymous":profileName;
            name = name.equals("N/A")?  "Anonymous":name;
        }

        return name;
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.txtPublicMessage){

            /*
            selectedLayout = -1;

            Intent intent = new Intent(DiscussionActivity.this,
                    ActivityMemberMarketNote.class);
            startActivity(intent);
             */
            Toast.makeText(this, "Temporarily unavailable", Toast.LENGTH_LONG).show();

        }
        if(v.getId() == R.id.txtNote){

            isTxtNoteClck = true;
            isTxtMrktlstClck = false;

            txtPublicMessage.setTextColor(Color.BLACK);
            txtPublicMessage.setPaintFlags(txtPublicMessage.getPaintFlags() & (~Paint.UNDERLINE_TEXT_FLAG));
            txtNote.setPaintFlags(txtPublicMessage.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            txtMarketList.setPaintFlags(txtPublicMessage.getPaintFlags() & (~Paint.UNDERLINE_TEXT_FLAG));
            txtNote.setTextColor(Color.CYAN);
            txtMarketList.setTextColor(Color.BLACK);
            btnEdit.setVisibility(View.GONE);

            implementMessagingInterface();
            populateMessages();

            prepareMessageBox();
        }
        if(v.getId() == R.id.txtMarketList){

            isTxtNoteClck = false;
            isTxtMrktlstClck = true;

            if(IS_MANAGER) btnAdd.setVisibility(View.VISIBLE);
            else btnAdd.setVisibility(View.GONE);

            txtPublicMessage.setTextColor(Color.BLACK);
            txtNote.setTextColor(Color.BLACK);
            txtPublicMessage.setPaintFlags(txtPublicMessage.getPaintFlags() & (~Paint.UNDERLINE_TEXT_FLAG));
            txtNote.setPaintFlags(txtPublicMessage.getPaintFlags() & (~Paint.UNDERLINE_TEXT_FLAG));
            txtMarketList.setPaintFlags(txtPublicMessage.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            txtMarketList.setTextColor(Color.CYAN);

            discussionTextScroll.setVisibility(View.GONE);
            btnEdit.setVisibility(View.GONE);

            removeMessagingInterface();

            selectedLayout = -1;
            initializeAddButton();
            setDataToFrame();

        }
        if(v.getId() == R.id.btnAddNew){

            selectedLayout = -1;
            btnEdit.setVisibility(View.GONE);
            MakeMessage d = new MakeMessage(this, PUBLISH);
            d.show();
        }
        if(v.getId() == R.id.imgSendMessage){
            EditText edtText = findViewById(R.id.edtMessage);
            String name = getMessengerName(),
                    message = edtText.getText().toString();
            if(!message.isEmpty()){
                sendMessage(name, getInstantTime(), message);
            }
            prepareMessageBox();
        }
        if(v.getId() == R.id.btnEdit){
            MakeMessage d = new MakeMessage(this, EDIT);
            d.show();
        }
    }

    private void prepareMessageBox(){
        EditText edtMessage = findViewById(R.id.edtMessage);
        edtMessage.setText("");
        edtMessage.requestFocus();
        edtMessage.setSelection(0);

    }

    private void sendMessage(String name, String date, String text){

        DatabaseReference r = refWithManagerName.child("Discussion");
        DiscussionList discussionList = new DiscussionList(name, date, text);

        r.child("Notes").child(date).setValue(discussionList);

        for(int i = 0; i < boarders.size(); i++){
            DatabaseReference nR = rootRef.child("notifications")
                    .child(boarders.get(i).getName()).child("unseen").push();
            String key = nR.getKey();
            UNotifications not = new UNotifications(key,
                    "Message(" + name + ")", date + "\n" + text);

            if(!profileName.equals(boarders.get(i).getName())) nR.setValue(not);
        }

        if(!IS_MANAGER){
            DatabaseReference nR = rootRef.child("notifications")
                    .child(getManagerName() + "-Manager-").child("unseen").push();
            String notId = nR.getKey();
            UNotifications not = new UNotifications(notId,
                    "Message from(" + name + ")", date + "\n" + text);

            nR.setValue(not);
        }
    }

    private void initializeAddButton(){
        TextView tv = findViewById(R.id.txtSubjectInstr);
        String s = "Create routine";
        btnAdd.setText(s);

        s = "Select any routine to view details";
        tv.setText(s);
    }

    private class MakeMessage extends Dialog implements View.OnClickListener{
        int type;
        EditText edtName, edtText;
        Button btnPub, btnCancel;
        public MakeMessage(@NonNull Context context, int type) {
            super(context);
            this.type = type;
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.disc_make_message);
            setCancelable(false);
            initialize();
        }

        @Override
        public void onClick(View v) {
            int id = v.getId();
            if(id == R.id.btnPublish){

                String name = edtName.getText().toString(),
                        text = edtText.getText().toString();

                if(name.isEmpty()){
                    edtName.setError("Name required");
                    edtName.requestFocus();
                    return;
                }
                if(text.isEmpty()){
                    String s = "";
                    if(isTxtMrktlstClck) s = "Market List/routine required";
                    else if(isTxtNoteClck) s = "Message required";
                    edtText.setError(s);
                    edtText.requestFocus();
                    return;
                }

                if(type == PUBLISH){
                    publish(name, text);
                }
                else if(type == EDIT){
                    save(name, text);
                }

                dismiss();

            }
            if(id == R.id.btnCancel){
                dismiss();
            }
        }

        private void initialize(){
            edtName = findViewById(R.id.edtName);
            edtText = findViewById(R.id.edtText);

            btnPub = findViewById(R.id.btnPublish);
            btnPub.setOnClickListener(this);
            btnCancel = findViewById(R.id.btnCancel);
            btnCancel.setOnClickListener(this);

            btnEdit.setVisibility(View.GONE);
            edtName.setTag(edtName.getKeyListener());

            setTextAndListener();

        }

        private void publish(String name, String text){
            DatabaseReference r = refWithManagerName.child("Discussion");

            String date = getInstantTime();
            DiscussionList discussionList = new DiscussionList(name, date, text);

            r.child("Market List").child(date).setValue(discussionList);

            for(int i = 0; i < boarders.size(); i++){
                DatabaseReference nR = rootRef.child("notifications")
                        .child(boarders.get(i).getName()).child("unseen").push();
                String key = nR.getKey();
                UNotifications not = new UNotifications(key,
                        "Bazaar Routine", date + "\n" + text);

                nR.setValue(not);
            }

            discussionTextScroll.setVisibility(View.GONE);
            selectedLayout = -1;
        }

        private void setTextAndListener(){
            discussionTextScroll.setVisibility(View.GONE);

            if(IS_MANAGER){
                String s = "Manager";
                edtName.setText(s);
                edtName.setKeyListener(null);

                if(type == EDIT){
                    edtName.setText(marketList.get(selectedLayout).getName());
                    edtText.setText(marketList.get(selectedLayout).getText());
                    tempDate = marketList.get(selectedLayout).getDate();
                }

            }
            else{
                KeyListener listener = (KeyListener) edtName.getTag();
                if(listener != null) edtName.setKeyListener(listener);
                edtName.setText(getProfileName(selectedBoarderIndex, selectedStoppedBoarderIndex,
                        selectedCookBillIndex));
            }
        }

        private void save(String name, String text){

            DatabaseReference r = refWithManagerName.child("Discussion");
            String date = tempDate;
            if(!date.equals("")){
                DiscussionList discussionList = new DiscussionList(name, date, text);
                r.child("Market List").child(date).setValue(discussionList);
                for(int i = 0; i < boarders.size(); i++){
                    DatabaseReference nR = rootRef.child("notifications")
                            .child(boarders.get(i).getName()).child("unseen").push();
                    String key = nR.getKey();
                    UNotifications not = new UNotifications(key,
                            "Bazaar Routine Changed", date + "\n" + text);

                    nR.setValue(not);
                }
                readDiscussionList(new ReadDiscussionListCallback() {
                    @Override
                    public void onCallback() {
                        setDataToFrame();
                    }
                });
            }
            else{
                Toast.makeText(DiscussionActivity.this, "Date not selected",
                        Toast.LENGTH_LONG).show();
            }

            discussionTextScroll.setVisibility(View.GONE);
            selectedLayout = -1;
        }
    }

    private String getInstantTime(){
        String calender = Calendar.getInstance().getTime().toString();

        String day = calender.substring(8, 10),
                month = getMonth(calender.substring(4, 7)),
                year = calender.substring(calender.length() - 4),
                h = calender.substring(11, 13),
                m = calender.substring(14, 16),
                s = calender.substring(17, 19);


        return day + "-" + month + "-" + year + "(" + h + ":" + m + ":" + s + ")";

    }

    private String getMonth(String month){
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

    private void setDataToFrame(){

        lstListView.removeAllViews();
        if(marketList.size() > 0)
            findViewById(R.id.txtSubjectInstr).setVisibility(View.VISIBLE);
        else findViewById(R.id.txtSubjectInstr).setVisibility(View.GONE);

        final LinearLayout[] linearLayouts = new LinearLayout[MainActivity.getMaxBoarder()];
        TextView[] names = new TextView[MainActivity.getMaxBoarder()],
                dates = new TextView[MainActivity.getMaxBoarder()];

        for(int i = 0; i < marketList.size(); i++){
            linearLayouts[i] = new LinearLayout(DiscussionActivity.this);
            LinearLayout fake = new LinearLayout(DiscussionActivity.this);
            names[i] = new TextView(DiscussionActivity.this);
            dates[i] = new TextView(DiscussionActivity.this);

            linearLayouts[i].setOrientation(LinearLayout.HORIZONTAL);
            linearLayouts[i].setBackgroundColor(Color.rgb(196, 227, 111));
            fake.setBackgroundColor(lstListView.getSolidColor());

            LinearLayout.LayoutParams paramsLay = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, 150),
                    paramsName = new LinearLayout.LayoutParams(0,
                            ViewGroup.LayoutParams.WRAP_CONTENT, 50),
                    paramsF = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 10);

            lstListView.addView(linearLayouts[i]);
            lstListView.addView(fake);
            linearLayouts[i].addView(names[i]);
            linearLayouts[i].addView(dates[i]);

            linearLayouts[i].setLayoutParams(paramsLay);
            Resources res = getResources();
            Drawable drawable = null, drawable1 = null;
            try {
                drawable = Drawable.createFromXml(res,
                        res.getXml(R.xml.rectangular_shape_update_payment));
                drawable1 = Drawable.createFromXml(res,
                        res.getXml(R.xml.rectangular_shape2));
            } catch (IOException | XmlPullParserException e) {
                e.printStackTrace();
            }
            linearLayouts[i].setGravity(Gravity.CENTER);
            if(drawable != null) linearLayouts[i].setBackground(drawable);

            fake.setLayoutParams(paramsF);
            names[i].setLayoutParams(paramsName);
            names[i].setTextColor(Color.WHITE);
            dates[i].setLayoutParams(paramsName);
            dates[i].setTextColor(Color.WHITE);

            names[i].setText(marketList.get(i).getName());
            dates[i].setText(marketList.get(i).getDate());
            names[i].setTextSize(20);
            dates[i].setTextSize(20);

            final int finalI = i;
            final Drawable finalDrawable = drawable;
            final Drawable finalDrawable1 = drawable1;
            linearLayouts[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    linearLayouts[finalI].setBackground(finalDrawable1);
                    if(IS_MANAGER) btnEdit.setVisibility(View.VISIBLE);
                    for(int j = 0; j < marketList.size(); j++){
                        if(finalI != j){
                            linearLayouts[j].setBackground(finalDrawable);
                        }
                    }
                    selectedLayout = finalI;

                    TextView textView = findViewById(R.id.discussionYourTextHere);
                    discussionTextScroll.setVisibility(View.VISIBLE);
                    textView.setText(marketList.get(finalI).getText());
                }
            });

        }

    }

    private void readDiscussionList(final ReadDiscussionListCallback readDiscussionListCallback){
        refWithManagerName.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                noteList.clear();
                marketList.clear();
                if(snapshot.child("Discussion").child("Notes").exists()){

                    for(DataSnapshot items:snapshot.child("Discussion")
                            .child("Notes").getChildren()){
                        DiscussionList discussionList = items.getValue(DiscussionList.class);
                        noteList.add(discussionList);
                    }
                }
                if(snapshot.child("Discussion").child("Market List").exists()){

                    for(DataSnapshot items:snapshot.child("Discussion")
                            .child("Market List").getChildren()){
                        DiscussionList discussionList = items.getValue(DiscussionList.class);
                        marketList.add(discussionList);
                    }
                }

                readDiscussionListCallback.onCallback();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onDataChange(@NonNull DataSnapshot snapshot) {
        readDiscussionList(new ReadDiscussionListCallback() {
            @Override
            public void onCallback() {
                initializeAddButton();
                if(isTxtNoteClck) {
                    populateMessages();
                }
                else setDataToFrame();
            }
        });
    }

    @Override
    public void onCancelled(@NonNull DatabaseError error) {

    }

    private interface ReadDiscussionListCallback{
        void onCallback();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent i = new Intent(DiscussionActivity.this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
    }
}