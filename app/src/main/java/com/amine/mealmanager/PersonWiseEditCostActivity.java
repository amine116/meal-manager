package com.amine.mealmanager;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import java.util.ArrayList;

public class PersonWiseEditCostActivity extends AppCompatActivity {

    private ArrayList<MarketerHistory> marketerHistories;
    private int index = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_wise_edit_cost);
        initialize();
        setData();
    }

    private void initialize(){
        marketerHistories = MainActivity.marketerHistories;
        index = getIntent().getIntExtra("INDEX", -1);

    }

    private void setData(){
        if(index != -1){
            EditText edtDetails = findViewById(R.id.personWise_edtDetails),
                    edtTotal = findViewById(R.id.personWise_edtTotal);
            TextView txtPersonName = findViewById(R.id.personWiseEdit_label);
            String s = marketerHistories.get(index).getName();
            txtPersonName.setText(s);
            edtDetails.setText(marketerHistories.get(index).getExpenseHistory());
            edtTotal.setText(marketerHistories.get(index).getTotalAmount());
        }
    }
}