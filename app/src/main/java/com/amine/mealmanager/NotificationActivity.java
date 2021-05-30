package com.amine.mealmanager;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.google.firebase.database.DatabaseReference;
import java.util.ArrayList;
import static com.amine.mealmanager.MainActivity.rootRef;
import static com.amine.mealmanager.MainActivity.seenNot;
import static com.amine.mealmanager.MainActivity.unSeenNot;

public class NotificationActivity extends AppCompatActivity {

    private ArrayList<UNotifications> unseen, seen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        initialize();
    }

    private void initialize(){
        unseen = new ArrayList<>();
        seen = new ArrayList<>();
        unseen.addAll(unSeenNot);
        seen.addAll(seenNot);
        toSeen();

    }

    private void setNotToFrame(){

        ListView lstNotUnseen = findViewById(R.id.lstNotifications),
                lstNotMore = findViewById(R.id.lstNotifications_more);
        BaseAdapter unseenNotAdapter = new BaseAdapter() {
            @Override
            public int getCount() {
                return unseen.size();
            }

            @Override
            public Object getItem(int position) {
                return unseen.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View view, ViewGroup parent) {

                if(view == null){
                    LayoutInflater inflater = (LayoutInflater)
                            getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    view = inflater.inflate(R.layout.notification_format, null);

                    TextView txtNotTitle = view.findViewById(R.id.txtNot_title),
                            txtNotText = view.findViewById(R.id.txtNot_text);
                    String s = unseen.get(position).getTitle();
                    txtNotTitle.setText(s);
                    s = unseen.get(position).getText();
                    txtNotText.setText(s);

                    txtNotTitle.setTextColor(getResources().getColor(R.color.pure_green));
                    txtNotText.setTextColor(getResources().getColor(R.color.pure_green));
                }

                return view;
            }
        },
                seenNotAdapter = new BaseAdapter() {
                    @Override
                    public int getCount() {
                        return seen.size();
                    }

                    @Override
                    public Object getItem(int position) {
                        return seen.get(position);
                    }

                    @Override
                    public long getItemId(int position) {
                        return position;
                    }

                    @Override
                    public View getView(int position, View view, ViewGroup parent) {

                        if(view == null){
                            LayoutInflater inflater = (LayoutInflater)
                                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                            view = inflater.inflate(R.layout.notification_format, null);

                            TextView txtNotTitle = view.findViewById(R.id.txtNot_title),
                                    txtNotText = view.findViewById(R.id.txtNot_text);
                            String s = seen.get(position).getTitle();
                            txtNotTitle.setText(s);
                            s = seen.get(position).getText();
                            txtNotText.setText(s);
                        }

                        return view;
                    }
                };

        if(unseen.size() == 0){
            findViewById(R.id.unseenNotLayout).setVisibility(View.GONE);
        }
        else{
            findViewById(R.id.unseenNotLayout).setVisibility(View.VISIBLE);
            lstNotUnseen.setAdapter(unseenNotAdapter);
        }

        if(seen.size() == 0){
            findViewById(R.id.seenNotLayout).setVisibility(View.GONE);
        }
        else{
            findViewById(R.id.seenNotLayout).setVisibility(View.VISIBLE);
            lstNotMore.setAdapter(seenNotAdapter);
        }

    }

    private void toSeen(){
        String name = getIntent().getStringExtra("NAME");
        assert name != null;
        if(!name.equals("")){
            for(int i = 0; i < unseen.size(); i++){
                DatabaseReference nRU = rootRef.child("notifications")
                        .child(name).child("unseen"),
                        nRS = rootRef.child("notifications")
                                .child(name).child("seen").push();
                String notId = nRS.getKey();
                UNotifications not = unseen.get(i);
                not.setId(notId);

                nRS.setValue(not);
                nRU.removeValue();
            }
        }
        setNotToFrame();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent i = new Intent(NotificationActivity.this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
    }
}