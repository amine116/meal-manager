package com.amine.mealmanager;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class ActivitySeeAnnouncement extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_see_announcement);
        ActionBar a = getSupportActionBar();
        if(a != null) a.setTitle("  Announcement");
        initialize();

    }
    private void initialize(){
        TextView textView = findViewById(R.id.seeAnnouncement_txtAnnouncement);
        String text = getIntent().getStringExtra("ANNOUNCEMENT_TEXT");
        if(text == null || text.isEmpty()) text = "Nothing has been announced today";
        textView.setText(text);

        findViewById(R.id.seeAnnounce_btnSee).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if(id == R.id.seeAnnounce_btnSee){
            Intent intent = new Intent(ActivitySeeAnnouncement.this,
                    ActivityAnnouncementHistory.class);

            startActivity(intent);
        }
    }
}