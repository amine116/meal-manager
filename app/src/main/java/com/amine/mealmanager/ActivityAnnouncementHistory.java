package com.amine.mealmanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;

public class ActivityAnnouncementHistory extends AppCompatActivity implements ValueEventListener{

    private LinearLayout layout;
    private TextView[] txtCopy, txtText;
    private ArrayList<Announcement> announcements;
    private DatabaseReference ref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_announecement_history);
        ActionBar a = getSupportActionBar();
        if(a != null) a.setTitle("    Announcement History");
        initialize();
    }

    private void initialize(){
        announcements = new ArrayList<>();
        layout = findViewById(R.id.ann_history_Ann_layout);
        ref = FirebaseDatabase.getInstance().getReference()
                .child(MainActivity.getManagerName()).child("announcement");
        ref.addValueEventListener(this);
    }

    private void setAnnouncementsToFrame(){
        layout.removeAllViews();
        Resources res = getResources();
        Drawable drawable = null, drawable1 = null;
        try {
            drawable = Drawable.createFromXml(res,
                    res.getXml(R.xml.rectangular_shape_details_meal));
            drawable1 = Drawable.createFromXml(res,
                    res.getXml(R.xml.rectangular_shape_white));
        }
        catch (IOException | XmlPullParserException e) {
            e.printStackTrace();
        }

        for(int i = 0; i < announcements.size(); i++){
            txtCopy[i] = new TextView(this);
            txtText[i] = new TextView(this);
            TextView date = new TextView(this),
                    hFake = new TextView(this),
                    vFake = new TextView(this);

            LinearLayout ll = new LinearLayout(this);
            layout.addView(ll);

            ll.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 150));
            if(drawable != null) ll.setBackground(drawable);
            ll.setOrientation(LinearLayout.HORIZONTAL);
            ll.setGravity(Gravity.CENTER);

            ll.addView(date);
            ll.addView(hFake);
            ll.addView(txtCopy[i]);

            txtText[i].setTextColor(Color.DKGRAY);
            txtCopy[i].setTextColor(Color.DKGRAY);
            date.setTextColor(Color.DKGRAY);

            date.setPadding(5, 5, 5, 5);
            txtCopy[i].setPadding(5, 5, 5, 5);
            txtText[i].setPadding(5, 5, 5, 5);

            date.setLayoutParams(new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.MATCH_PARENT, 65));
            date.setGravity(Gravity.CENTER);
            date.setText(announcements.get(i).getDate());

            hFake.setLayoutParams(new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.MATCH_PARENT, 5));
            if(drawable1 != null) hFake.setBackground(drawable1);

            txtCopy[i].setLayoutParams(new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.MATCH_PARENT, 30));
            txtCopy[i].setGravity(Gravity.CENTER);
            String s = "Copy";
            txtCopy[i].setText(s);

            final int finalI = i;
            txtCopy[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("text", txtText[finalI].getText().toString());
                    clipboard.setPrimaryClip(clip);

                    Toast.makeText(ActivityAnnouncementHistory.this, "Copied to click board",
                            Toast.LENGTH_LONG).show();
                }
            });

            layout.addView(txtText[i]);
            txtText[i].setText(announcements.get(i).getAnnouncement());
            txtText[i].setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            if(drawable != null) txtText[i].setBackground(drawable);
            txtText[i].setGravity(Gravity.CENTER);

            layout.addView(vFake);

            vFake.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, 10));
            vFake.setBackground(drawable1);


        }
    }

    @Override
    public void onDataChange(@NonNull DataSnapshot snapshot) {

        findViewById(R.id.ann_history_Ann_scroll).setVisibility(View.GONE);
        findViewById(R.id.ann_history_progress).setVisibility(View.VISIBLE);

        announcements.clear();
        if(snapshot.exists()){
            for(DataSnapshot ann : snapshot.getChildren()){
                String date = ann.getKey(),
                        text = ann.getValue(String.class);
                announcements.add(new Announcement(text, date));
            }
        }

        findViewById(R.id.ann_history_Ann_scroll).setVisibility(View.VISIBLE);
        findViewById(R.id.ann_history_progress).setVisibility(View.GONE);

        txtCopy = new TextView[announcements.size() + 1];
        txtText = new TextView[announcements.size() + 1];
        setAnnouncementsToFrame();
    }

    @Override
    public void onCancelled(@NonNull DatabaseError error) {

    }

    private static class Announcement{
        private final String announcement, date;

        public Announcement(String announcement, String date) {
            this.announcement = announcement;
            this.date = date;
        }

        public String getAnnouncement() {
            return announcement;
        }

        public String getDate() {
            return date;
        }
    }
}