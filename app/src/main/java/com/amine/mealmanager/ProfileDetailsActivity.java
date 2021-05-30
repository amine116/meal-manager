package com.amine.mealmanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.util.ArrayList;

import static com.amine.mealmanager.MainActivity.boarders;
import static com.amine.mealmanager.MainActivity.cooksBills;
import static com.amine.mealmanager.MainActivity.isAnimationAlive;
import static com.amine.mealmanager.MainActivity.rootRef;
import static com.amine.mealmanager.MainActivity.stoppedBoarders;

public class ProfileDetailsActivity extends AppCompatActivity {

    private ArrayList<StoppedHistory> stoppedHistories;
    private String name = "";
    private ListView lstStoppedHistories;
    private BaseAdapter adapter;
    private static final DecimalFormat df =  new DecimalFormat("0.#");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_details);
        ActionBar a = getSupportActionBar();
        if(a != null) a.setTitle("  Profile");
        initialize();
    }

    private void initialize(){
        stoppedHistories = new ArrayList<>();
        lstStoppedHistories = findViewById(R.id.profile_lstStoppedHistory);
        getName();
        initializeAdapter();
        makeViewInvisible();
        readStoppedHistory(new Wait() {
            @Override
            public void onCallback() {
                makeViewVisible();
                TextView tv = findViewById(R.id.txtProfileName),
                        tv2 = findViewById(R.id.txtProfile_title);
                tv.setText(name);
                String s;
                if(stoppedHistories.size() == 0) s = "Your meal was never closed";
                else s = "Your meal was closed " + stoppedHistories.size() + " time(s)";
                tv2.setText(s);
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void makeViewInvisible() {
        findViewById(R.id.imgAnimate).setVisibility(View.VISIBLE);
        findViewById(R.id.closedHistory_info).setVisibility(View.GONE);
        isAnimationAlive = true;
        animate();

    }

    private void makeViewVisible(){
        isAnimationAlive = false;
        findViewById(R.id.imgAnimate).setVisibility(View.GONE);
        findViewById(R.id.closedHistory_info).setVisibility(View.VISIBLE);
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

                            imgAnimate.setImageResource(R.drawable.i1);
                        }
                    });

                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        Toast.makeText(ProfileDetailsActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }

                    handler.post(new Runnable() {
                        @Override
                        public void run() {

                            imgAnimate.setImageResource(R.drawable.i2);
                        }
                    });

                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        Toast.makeText(ProfileDetailsActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }


                    handler.post(new Runnable() {
                        @Override
                        public void run() {

                            imgAnimate.setImageResource(R.drawable.i3);
                        }
                    });

                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        Toast.makeText(ProfileDetailsActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }



                    handler.post(new Runnable() {
                        @Override
                        public void run() {

                            imgAnimate.setImageResource(R.drawable.i4);
                        }
                    });

                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        Toast.makeText(ProfileDetailsActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }


                    handler.post(new Runnable() {
                        @Override
                        public void run() {

                            imgAnimate.setImageResource(R.drawable.i5);
                        }
                    });

                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        Toast.makeText(ProfileDetailsActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }


                    handler.post(new Runnable() {
                        @Override
                        public void run() {

                            imgAnimate.setImageResource(R.drawable.i6);
                        }
                    });

                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        Toast.makeText(ProfileDetailsActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }


                    handler.post(new Runnable() {
                        @Override
                        public void run() {

                            imgAnimate.setImageResource(R.drawable.i7);
                        }
                    });

                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        Toast.makeText(ProfileDetailsActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }


                    handler.post(new Runnable() {
                        @Override
                        public void run() {

                            imgAnimate.setImageResource(R.drawable.i8);
                        }
                    });

                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        Toast.makeText(ProfileDetailsActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
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
                        Toast.makeText(ProfileDetailsActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }


                    handler.post(new Runnable() {
                        @Override
                        public void run() {

                            imgAnimate.setImageResource(R.drawable.i10);
                        }
                    });

                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        Toast.makeText(ProfileDetailsActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }


                    handler.post(new Runnable() {
                        @Override
                        public void run() {

                            imgAnimate.setImageResource(R.drawable.i11);
                        }
                    });

                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        Toast.makeText(ProfileDetailsActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }


                    handler.post(new Runnable() {
                        @Override
                        public void run() {

                            imgAnimate.setImageResource(R.drawable.i12);
                        }
                    });

                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        Toast.makeText(ProfileDetailsActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }



                    handler.post(new Runnable() {
                        @Override
                        public void run() {

                            imgAnimate.setImageResource(R.drawable.i13);
                        }
                    });

                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        Toast.makeText(ProfileDetailsActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }


                    handler.post(new Runnable() {
                        @Override
                        public void run() {

                            imgAnimate.setImageResource(R.drawable.i14);
                        }
                    });

                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        Toast.makeText(ProfileDetailsActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            }
        }).start();
    }

    private void initializeAdapter() {
        adapter = new BaseAdapter() {
            @Override
            public int getCount() {
                return stoppedHistories.size();
            }

            @Override
            public Object getItem(int position) {
                return stoppedHistories.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View view, ViewGroup parent) {

                if(view == null){
                    LayoutInflater inflater =
                            (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                    view = inflater.inflate(R.layout.stopped_history_format, null);

                    TextView title = view.findViewById(R.id.format_title),
                            paid = view.findViewById(R.id.format_paid),
                            meal = view.findViewById(R.id.format_meal),
                            mealRate = view.findViewById(R.id.format_mealRate),
                            cost = view.findViewById(R.id.format_cost),
                            dueOrOver = view.findViewById(R.id.format_dueOrOverhead);
                    String s = getTitle(position);
                    title.setText(s);
                    double pd = stoppedHistories.get(position).getBoarder().getPaidMoney();
                    s = "Paid: " + df.format(pd);
                    paid.setText(s);
                    s = "Consumed Meal: " +
                            df.format(stoppedHistories.get(position).getBoarder().getMeals());
                    meal.setText(s);
                    s = "Meal rate: " +
                            df.format(stoppedHistories.get(position).getMealRate());
                    mealRate.setText(s);
                    double cst = stoppedHistories.get(position).getBoarder().getMeals() *
                            stoppedHistories.get(position).getMealRate();
                    s = "Cost: " + df.format(cst);
                    cost.setText(s);
                    if(cst > pd) s = "Due: " + df.format((cst - pd));
                    else s = "Overhead: " + df.format((pd - cst));
                    dueOrOver.setText(s);

                }


                return view;
            }

            String getTitle(int position){
                if(position == 0) return "For the first time";
                else if(position == 1) return "Second Time";
                else if(position == 2) return "Third time";
                else if(position == 3) return "Fourth time";
                else if(position == 4) return "Fifth time";
                else if(position == 5) return "Sixth time";
                else if(position == 6) return "Seventh time";
                else if(position == 7) return "Eighth time";
                else return position + "th time";
            }
        };
        lstStoppedHistories.setAdapter(adapter);
    }

    private void getName() {
        int bi = getIntent().getIntExtra("BI", -1),
                sbi = getIntent().getIntExtra("SBI", -1),
                cbi = getIntent().getIntExtra("CBI", -1);
        if(bi != -1) name = boarders.get(bi).getName();
        if(sbi != -1) name = stoppedBoarders.get(sbi).getName();
        if(cbi != -1) name = cooksBills.get(cbi).getName();
    }

    private interface Wait{
        void onCallback();
    }

    private void readStoppedHistory(final Wait wait){
        if(!name.equals("")){
            DatabaseReference r = rootRef.child("those who stopped meal").child("stoppedHistory")
                    .child(name);
            r.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    stoppedHistories.clear();
                    if(!snapshot.exists()) {
                        wait.onCallback();
                        return;
                    }
                    for(DataSnapshot push : snapshot.getChildren()){
                        StoppedHistory item = push.getValue(StoppedHistory.class);
                        stoppedHistories.add(item);
                    }
                    wait.onCallback();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }
}