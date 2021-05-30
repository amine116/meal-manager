package com.amine.mealmanager;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class ActivityInstructions extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instructions);
        ActionBar a = getSupportActionBar();
        if(a != null) a.setTitle("    Instructions");
        initialize();
    }


    private void initialize(){
        TextView t2 = findViewById(R.id.txtSeeVideoTutorial);
        t2.setMovementMethod(LinkMovementMethod.getInstance());
        findViewById(R.id.txtCopyVideoTutorialLink).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if(id == R.id.txtCopyVideoTutorialLink){
            TextView t = new TextView(this);
            t.setText(R.string.video_tutorial_link_base);
            String s = t.getText().toString();
            /*
            Log.i("test", getResources().getString(R.string.video_tutorial_link_base));
            Log.i("test", s);

            Those s and getResources().getString(R.string.video_tutorial_link_base)
            are same.

             */
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("text", s);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "Link Copied", Toast.LENGTH_LONG).show();
        }
    }
}