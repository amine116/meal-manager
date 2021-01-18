package com.amine.mealmanager;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.text.method.KeyListener;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

import org.xmlpull.v1.XmlPullParserException;

public class DiscussionActivity extends AppCompatActivity implements View.OnClickListener, ValueEventListener {

    private ArrayList<DiscussionList> noteList, marketList;
    private TextView txtPublicMessage, txtNote, txtMarketList;
    private Button add, edit, publish, save;
    private EditText edtYourName, edtYourText;
    private LinearLayout lstListView;
    private String tempDate = "";
    private DatabaseReference refWithManagerName;
    private boolean isTxtNoteClck = false, isTxtMrktlstClck = false;
    private final boolean IS_MANAGER = MainActivity.IS_MANAGER;
    private ScrollView discussionTextScroll;
    private int selectedLayout = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discussion);
        initialize();
        readDiscussionList(new ReadDiscussionListCallback() {
            @Override
            public void onCallback() {
                isTxtMrktlstClck = true;
                txtMarketList.setPaintFlags(txtPublicMessage.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                txtMarketList.setTextColor(Color.CYAN);
                if(IS_MANAGER){
                    String s = "Create List of Market";
                    add.setText(s);
                    add.setVisibility(View.VISIBLE);
                }else{
                    add.setVisibility(View.GONE);
                }
                setDataToFrame();
            }
        });

    }

    private void initialize(){

        noteList = new ArrayList<>();
        marketList = new ArrayList<>();
        txtPublicMessage = findViewById(R.id.txtPublicMessage);
        txtNote = findViewById(R.id.txtNote);
        txtMarketList = findViewById(R.id.txtMarketList);
        add = findViewById(R.id.btnAddNew);
        edit = findViewById(R.id.btnEdit);
        publish = findViewById(R.id.publish);
        save = findViewById(R.id.btnSave);

        String nameOfManager = MainActivity.getManagerName();
        refWithManagerName = FirebaseDatabase.getInstance().getReference().child(nameOfManager);

        discussionTextScroll = findViewById(R.id.discussionTextScroll);

        refWithManagerName.addValueEventListener(DiscussionActivity.this);

        edit.setOnClickListener(DiscussionActivity.this);
        save.setOnClickListener(DiscussionActivity.this);
        publish.setOnClickListener(DiscussionActivity.this);
        add.setOnClickListener(DiscussionActivity.this);
        txtPublicMessage.setOnClickListener(DiscussionActivity.this);
        txtNote.setOnClickListener(DiscussionActivity.this);
        txtMarketList.setOnClickListener(DiscussionActivity.this);
        edtYourName = findViewById(R.id.discussionEnterYourName);
        edtYourText = findViewById(R.id.discussionEnterYourTextHere);
        lstListView = findViewById(R.id.discussionList);

        edtYourName.setTag(edtYourName.getKeyListener());
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.txtPublicMessage){

            Intent intent = new Intent(DiscussionActivity.this, ActivityMemberMarketNote.class);
            startActivity(intent);

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

            findViewById(R.id.writingLayout).setVisibility(View.GONE);
            discussionTextScroll.setVisibility(View.GONE);
            publish.setVisibility(View.GONE);
            edit.setVisibility(View.GONE);
            String s = "Create Note";
            add.setText(s);
            save.setVisibility(View.GONE);
            add.setVisibility(View.VISIBLE);

            selectedLayout = -1;

            setDataToFrame();

        }
        if(v.getId() == R.id.txtMarketList){
            isTxtNoteClck = false;
            isTxtMrktlstClck = true;


            txtPublicMessage.setTextColor(Color.BLACK);

            txtNote.setTextColor(Color.BLACK);
            txtPublicMessage.setPaintFlags(txtPublicMessage.getPaintFlags() & (~Paint.UNDERLINE_TEXT_FLAG));
            txtNote.setPaintFlags(txtPublicMessage.getPaintFlags() & (~Paint.UNDERLINE_TEXT_FLAG));
            txtMarketList.setPaintFlags(txtPublicMessage.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            txtMarketList.setTextColor(Color.CYAN);

            findViewById(R.id.writingLayout).setVisibility(View.GONE);
            discussionTextScroll.setVisibility(View.GONE);
            publish.setVisibility(View.GONE);
            edit.setVisibility(View.GONE);
            save.setVisibility(View.GONE);
            if(IS_MANAGER){
                String s = "Create List of Market";
                add.setText(s);
                add.setVisibility(View.VISIBLE);
            }
            else{
                edit.setVisibility(View.GONE);
                add.setVisibility(View.GONE);
            }
            selectedLayout = -1;

            setDataToFrame();

        }
        if(v.getId() == R.id.btnAddNew){
            discussionTextScroll.setVisibility(View.GONE);
            findViewById(R.id.writingLayout).setVisibility(View.VISIBLE);
            publish.setVisibility(View.VISIBLE);
            edtYourText.setText("");
            edtYourName.setText("");
            edit.setVisibility(View.GONE);
            save.setVisibility(View.GONE);
            selectedLayout = -1;

            if(IS_MANAGER && isTxtMrktlstClck){
                String s = "Manager";
                edtYourName.setText(s);
                edtYourName.setKeyListener(null);
            }
            else{
                KeyListener listener = (KeyListener) edtYourName.getTag();
                if(listener != null) edtYourName.setKeyListener(listener);
            }

        }
        if(v.getId() == R.id.publish){
            String name = edtYourName.getText().toString(),
                    text = edtYourText.getText().toString();

            DatabaseReference r = refWithManagerName.child("Discussion");

            if(!name.equals("")){
                if(!text.equals("")){
                    String date = getInstantTime();
                    DiscussionList discussionList = new DiscussionList(name, date, text);
                    if(isTxtNoteClck){
                        r.child("Notes").child(date).setValue(discussionList);
                        r.child("notifications").child("text").setValue(text);
                        r.child("notifications").child("title").setValue("Note from(" + name + ")");
                        r.child("notifications").child("id").setValue(3);
                    }
                    else{
                        r.child("Market List").child(date).setValue(discussionList);
                        r.child("notifications").child("text").setValue(text);
                        r.child("notifications").child("title").setValue("Market List from(" + name + ")");
                        r.child("notifications").child("id").setValue(4);
                    }

                    edit.setVisibility(View.GONE);
                    findViewById(R.id.writingLayout).setVisibility(View.GONE);
                    discussionTextScroll.setVisibility(View.GONE);
                    publish.setVisibility(View.GONE);
                    save.setVisibility(View.GONE);
                    add.setVisibility(View.VISIBLE);
                    selectedLayout = -1;

                }else{
                    Toast.makeText(DiscussionActivity.this, "Enter text", Toast.LENGTH_LONG).show();
                }
            }else{
                Toast.makeText(DiscussionActivity.this, "Enter Name", Toast.LENGTH_LONG).show();
            }
        }
        if(v.getId() == R.id.btnEdit){
            discussionTextScroll.setVisibility(View.GONE);
            edit.setVisibility(View.GONE);
            add.setVisibility(View.GONE);
            findViewById(R.id.writingLayout).setVisibility(View.VISIBLE);
            save.setVisibility(View.VISIBLE);
            if(isTxtNoteClck){
                edtYourName.setText(noteList.get(selectedLayout).getName());
                edtYourText.setText(noteList.get(selectedLayout).getText());
                tempDate = noteList.get(selectedLayout).getDate();
            }
            if(isTxtMrktlstClck){
                edtYourName.setText(marketList.get(selectedLayout).getName());
                edtYourText.setText(marketList.get(selectedLayout).getText());
                tempDate = marketList.get(selectedLayout).getDate();
            }

            if(IS_MANAGER && isTxtMrktlstClck){
                edtYourName.setKeyListener(null);
            }
            else{
                KeyListener listener = (KeyListener) edtYourName.getTag();
                if(listener != null) edtYourName.setKeyListener(listener);
            }
        }
        if(v.getId() == R.id.btnSave){
            String name = edtYourName.getText().toString(),
                    text = edtYourText.getText().toString();

            DatabaseReference r = refWithManagerName.child("Discussion");

            if(!name.equals("")){
                if(!text.equals("")){
                    String date = tempDate;
                    if(!date.equals("")){
                        DiscussionList discussionList = new DiscussionList(name, date, text);
                       if(isTxtNoteClck){
                            r.child("Notes").child(date).setValue(discussionList);
                            r.child("notifications").child("text").setValue(text);
                            r.child("notifications").child("title").setValue("Note edited(" + name + ")");
                            r.child("notifications").child("id").setValue(3);
                        }
                        else{
                            r.child("Market List").child(date).setValue(discussionList);
                            r.child("notifications").child("text").setValue(text);
                            r.child("notifications").child("title").setValue("Market List Edited(" + name + ")");
                            r.child("notifications").child("id").setValue(4);
                        }

                        readDiscussionList(new ReadDiscussionListCallback() {
                            @Override
                            public void onCallback() {
                                setDataToFrame();
                            }
                        });
                    }else{
                        Toast.makeText(DiscussionActivity.this, "Date not selected",
                                Toast.LENGTH_LONG).show();
                    }

                    edit.setVisibility(View.GONE);
                    findViewById(R.id.writingLayout).setVisibility(View.GONE);
                    discussionTextScroll.setVisibility(View.GONE);
                    publish.setVisibility(View.GONE);
                    save.setVisibility(View.GONE);
                    add.setVisibility(View.VISIBLE);
                    selectedLayout = -1;


                }else{
                    Toast.makeText(DiscussionActivity.this, "Enter text", Toast.LENGTH_LONG).show();
                }
            }else{
                Toast.makeText(DiscussionActivity.this, "Enter Name", Toast.LENGTH_LONG).show();
            }
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
        if(isTxtNoteClck){
            lstListView.removeAllViews();

            final LinearLayout[] linearLayouts = new LinearLayout[MainActivity.getMaxBoarder()];
            TextView[] names = new TextView[MainActivity.getMaxBoarder()],
                    dates = new TextView[MainActivity.getMaxBoarder()];

            for(int i = 0; i < noteList.size(); i++){
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
                            res.getXml(R.xml.rectangular_shape));
                    drawable1 = Drawable.createFromXml(res, res.getXml(R.xml.rectangular_shape2));
                } catch (IOException | XmlPullParserException e) {
                    e.printStackTrace();
                }
                linearLayouts[i].setGravity(Gravity.CENTER);
                if(drawable != null) linearLayouts[i].setBackground(drawable);

                fake.setLayoutParams(paramsF);
                names[i].setLayoutParams(paramsName);
                dates[i].setLayoutParams(paramsName);

                names[i].setText(noteList.get(i).getName());
                dates[i].setText(noteList.get(i).getDate());
                names[i].setTextSize(20);
                dates[i].setTextSize(20);

                final int finalI = i;
                final Drawable finalDrawable = drawable;
                final Drawable finalDrawable1 = drawable1;
                linearLayouts[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        linearLayouts[finalI].setBackground(finalDrawable1);
                        for(int j = 0; j < noteList.size(); j++){
                            if(finalI != j){
                                linearLayouts[j].setBackground(finalDrawable);
                            }
                        }
                        edit.setVisibility(View.VISIBLE);
                        selectedLayout = finalI;
                        publish.setVisibility(View.GONE);
                        findViewById(R.id.writingLayout).setVisibility(View.GONE);
                        TextView textView = findViewById(R.id.discussionYourTextHere);
                        discussionTextScroll.setVisibility(View.VISIBLE);
                        textView.setText(noteList.get(finalI).getText());
                    }
                });

            }

        }
        if(isTxtMrktlstClck){
            lstListView.removeAllViews();

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
                            res.getXml(R.xml.rectangular_shape));
                    drawable1 = Drawable.createFromXml(res,
                            res.getXml(R.xml.rectangular_shape2));
                } catch (IOException | XmlPullParserException e) {
                    e.printStackTrace();
                }
                linearLayouts[i].setGravity(Gravity.CENTER);
                if(drawable != null) linearLayouts[i].setBackground(drawable);

                fake.setLayoutParams(paramsF);
                names[i].setLayoutParams(paramsName);
                dates[i].setLayoutParams(paramsName);

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
                        for(int j = 0; j < marketList.size(); j++){
                            if(finalI != j){
                                linearLayouts[j].setBackground(finalDrawable);
                            }
                        }
                        if(IS_MANAGER) edit.setVisibility(View.VISIBLE);
                        else edit.setVisibility(View.GONE);
                        selectedLayout = finalI;
                        findViewById(R.id.writingLayout).setVisibility(View.GONE);
                        publish.setVisibility(View.GONE);
                        TextView textView = findViewById(R.id.discussionYourTextHere);
                        discussionTextScroll.setVisibility(View.VISIBLE);
                        textView.setText(marketList.get(finalI).getText());
                    }
                });

            }

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
                setDataToFrame();
            }
        });
    }

    @Override
    public void onCancelled(@NonNull DatabaseError error) {

    }

    private interface ReadDiscussionListCallback{
        void onCallback();
    }

}