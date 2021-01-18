package com.amine.mealmanager;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class MemberPasswordsActivity extends AppCompatActivity {

    private ArrayList<Boarder> boarders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_passwords);
        initialize();
    }

    private void initialize(){
        ListView lstPasswordList = findViewById(R.id.lstPasswordList);
        boarders = MainActivity.getBoarders();
        BaseAdapter adapter = initAdapter();
        lstPasswordList.setAdapter(adapter);
    }

    private BaseAdapter initAdapter(){

        return new BaseAdapter() {
            @Override
            public int getCount() {
                return boarders.size();
            }

            @Override
            public Object getItem(int position) {
                return boarders.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View view, ViewGroup parent) {
                if(view == null){
                    LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    view = inflater.inflate(R.layout.password_item, null);
                }

                TextView textView = view.findViewById(R.id.passMemberName),
                        txtSerialNumber = view.findViewById(R.id.txtSerialNumber);
                String s = "Name: " + boarders.get(position).getName() + "\n\nPassword: " +
                        boarders.get(position).getMemberPassword();
                textView.setText(s);

                s = (position + 1) + ". ";
                txtSerialNumber.setText(s);
                return view;
            }
        };
    }

}