package com.amine.mealmanager;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;

import static com.amine.mealmanager.MainActivity.isDateFormatCorrect;

public class EditInterfaceActivity extends AppCompatActivity implements View.OnClickListener {

    private LinearLayout editableInfo;
    private ArrayList<Boarder> boarders;

    private Resources res;
    private Drawable drawable, drawable1;
    private EditText[] editableDate, editableMOrP;
    private int index = -1, mxSize = 0;
    private String type = "";
    private double mealRate = 1;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_interface);

        type = getIntent().getStringExtra("TYPE");
        index = getIntent().getIntExtra("INDEX", 0);

        initialize();
        if (type != null && type.equals("MEAL")) {
            setEditableMeal();
        } else if (type != null && type.equals("PAYMENT")) {
            setEditablePayment();
        }
    }

    private void initialize() {
        editableInfo = findViewById(R.id.editInterface_editableInfoLayout);
        boarders = MainActivity.getBoarders();
        res = getResources();
        int maxSize = Math.max(boarders.get(index).getMealD().size(),
                boarders.get(index).getPaymentD().size());
        mealRate = MainActivity.mealRate;

        editableDate = new EditText[maxSize];
        editableMOrP = new EditText[maxSize];
        findViewById(R.id.editInterface_btnSave).setOnClickListener(this);
        findViewById(R.id.editInterface_btnCancel).setOnClickListener(this);
    }

    private void setEditableMeal() {

        setPrimaryLabels("Meal");

        mxSize = boarders.get(index).getMealD().size();
        for (int i = 0; i < mxSize; i++) {
            LinearLayout ll = new LinearLayout(this),
                    fake = new LinearLayout(this),
                    llForEdtD = new LinearLayout(this),
                    llForEdtM = new LinearLayout(this);

            editableInfo.addView(ll);
            editableInfo.addView(fake);

            ll.setOrientation(LinearLayout.HORIZONTAL);
            ll.setLayoutParams(
                    new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 200));
            fake.setLayoutParams(
                    new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 10));

            try {
                drawable = Drawable.createFromXml(res,
                        res.getXml(R.xml.rectangular_shape_details_meal));
                drawable1 = Drawable.createFromXml(res,
                        res.getXml(R.xml.custom_ractangle_shape_red));
            } catch (IOException | XmlPullParserException e) {
                e.printStackTrace();
            }

            if (drawable != null) ll.setBackground(drawable);

            editableDate[i] = new EditText(this);
            editableMOrP[i] = new EditText(this);

            ll.setPadding(4, 4, 4,4);
            ll.addView(llForEdtD);
            ll.addView(llForEdtM);

            llForEdtD.setPadding(2, 2, 2, 2);
            llForEdtM.setPadding(2, 2, 2, 2);
            llForEdtD.setLayoutParams(new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.MATCH_PARENT, 50));
            if(drawable1 != null) llForEdtD.setBackground(drawable1);
            llForEdtM.setLayoutParams(new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.MATCH_PARENT, 50));
            if(drawable1 != null) llForEdtM.setBackground(drawable1);
            editableDate[i].setBackgroundColor(getResources().getColor(R.color.meal_details));
            editableMOrP[i].setBackgroundColor(getResources().getColor(R.color.meal_details));
            editableDate[i].setTextColor(Color.WHITE);
            editableMOrP[i].setTextColor(Color.WHITE);

            llForEdtD.addView(editableDate[i]);
            llForEdtM.addView(editableMOrP[i]);

            editableMOrP[i].setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            editableMOrP[i].setInputType(InputType.TYPE_CLASS_NUMBER);
            editableDate[i].setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            editableDate[i].setGravity(Gravity.CENTER);
            editableMOrP[i].setGravity(Gravity.CENTER);
            editableDate[i].setText(boarders.get(index).getMealD().get(i).getDate());
            editableMOrP[i].setText(boarders.get(index).getMealD().get(i).getMeal());
        }

    }
    private void setEditablePayment() {

        setPrimaryLabels("Payment");
        mxSize = boarders.get(index).getPaymentD().size();
        for (int i = 0; i < mxSize; i++) {
            LinearLayout ll = new LinearLayout(this),
                    fake = new LinearLayout(this),
                    llForEdtD = new LinearLayout(this),
                    llForEdtM = new LinearLayout(this);

            editableInfo.addView(ll);
            editableInfo.addView(fake);

            ll.setOrientation(LinearLayout.HORIZONTAL);
            ll.setLayoutParams(
                    new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 200));
            fake.setLayoutParams(
                    new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 10));

            try {
                drawable = Drawable.createFromXml(res,
                        res.getXml(R.xml.rectangular_shape_details_meal));
                drawable1 = Drawable.createFromXml(res,
                        res.getXml(R.xml.custom_ractangle_shape_red));
            } catch (IOException | XmlPullParserException e) {
                e.printStackTrace();
            }

            if (drawable != null) ll.setBackground(drawable);

            editableDate[i] = new EditText(this);
            editableMOrP[i] = new EditText(this);

            ll.setPadding(4, 4, 4,4);
            ll.addView(llForEdtD);
            ll.addView(llForEdtM);

            llForEdtD.setPadding(2, 2, 2, 2);
            llForEdtM.setPadding(2, 2, 2, 2);
            llForEdtD.setLayoutParams(new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.MATCH_PARENT, 50));
            if(drawable1 != null) llForEdtD.setBackground(drawable1);
            llForEdtM.setLayoutParams(new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.MATCH_PARENT, 50));
            if(drawable1 != null) llForEdtM.setBackground(drawable1);
            editableDate[i].setBackgroundColor(getResources().getColor(R.color.meal_details));
            editableMOrP[i].setBackgroundColor(getResources().getColor(R.color.meal_details));

            llForEdtD.addView(editableDate[i]);
            llForEdtM.addView(editableMOrP[i]);

            editableMOrP[i].setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            editableMOrP[i].setInputType(InputType.TYPE_CLASS_NUMBER);
            editableDate[i].setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            editableDate[i].setGravity(Gravity.CENTER);
            editableMOrP[i].setGravity(Gravity.CENTER);

            editableDate[i].setTextColor(Color.WHITE);
            editableMOrP[i].setTextColor(Color.WHITE);

            editableDate[i].setText(boarders.get(index).getPaymentD().get(i).getDate());
            editableMOrP[i].setText(boarders.get(index).getPaymentD().get(i).getMeal());
        }

    }

    private void setPrimaryLabels(String mealL){

        editableInfo.removeAllViews();

        TextView label = new TextView(this), fakeLabel = new TextView(this);
        editableInfo.addView(label);
        String s = mealL + " Edit info for " + boarders.get(index).getName();
        label.setText(s);
        label.setGravity(Gravity.CENTER);
        label.setBackgroundColor(Color.rgb(186, 222, 219));
        label.setTextSize(20);
        label.setTextColor(Color.RED);
        label.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 100));
        fakeLabel.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 10));

        LinearLayout labelLayout = new LinearLayout(this);
        TextView dateLabel = new TextView(this), mealLabel = new TextView(this);

        editableInfo.addView(labelLayout);
        editableInfo.addView(fakeLabel);
        labelLayout.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 50));
        labelLayout.setBackgroundColor(Color.rgb(186, 222, 219));
        labelLayout.addView(dateLabel);
        labelLayout.addView(mealLabel);
        labelLayout.setOrientation(LinearLayout.HORIZONTAL);

        dateLabel.setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.MATCH_PARENT, 50));
        mealLabel.setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.MATCH_PARENT, 50));
        dateLabel.setGravity(Gravity.CENTER);
        mealLabel.setGravity(Gravity.CENTER);
        dateLabel.setTextColor(Color.RED);
        mealLabel.setTextColor(Color.RED);

        s = "Date";
        dateLabel.setText(s);
        mealLabel.setText(mealL);
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        if(id == R.id.editInterface_btnSave){

            int ind = -1;
            for(int i = 0; i < mxSize; i++){
                if(!isDateFormatCorrect(editableDate[i].getText().toString())){
                    ind = i;
                    break;
                }
            }
            if(ind == -1){
                ArrayList<MealOrPaymentDetails> mop = new ArrayList<>();
                double total = 0;
                for(int i = 0; i < mxSize; i++){
                    mop.add(new MealOrPaymentDetails(
                            editableDate[i].getText().toString(), editableMOrP[i].getText().toString()));
                    total += Double.parseDouble(editableMOrP[i].getText().toString());
                }
                if(type != null && type.equals("MEAL")){
                    boarders.get(index).setMealD(mop);
                    boarders.get(index).setMeals(total);
                }
                else if(type != null && type.equals("PAYMENT")){
                    boarders.get(index).setPaymentD(mop);
                    boarders.get(index).setPaidMoney(total);
                }

                recalculate();

                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
            else{
                if(ind == 0) {
                    Toast.makeText(this, "Change " + (ind + 1) + "st date format to" +
                                    "\n15-05-2020(DD-MM-YYYY)",
                            Toast.LENGTH_LONG).show();
                }
                else if(ind == 1) {
                    Toast.makeText(this, "Change " + (ind + 1) + "nd date format to" +
                                    "\n15-05-2020(DD-MM-YYYY)",
                            Toast.LENGTH_LONG).show();
                }
                else if(ind == 2) {
                    Toast.makeText(this, "Change " + (ind + 1) + "rd date format to" +
                                    "\n15-05-2020(DD-MM-YYYY)",
                            Toast.LENGTH_LONG).show();
                }
                else{
                    Toast.makeText(this, "Change " + (ind + 1) + "th date format to" +
                                    "\n15-05-2020(DD-MM-YYYY)",
                            Toast.LENGTH_LONG).show();
                }
            }
        }
        else if(id == R.id.editInterface_btnCancel){
            Intent intent = new Intent(this, EditInfo.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }

    private void recalculate(){

        Log.i("test", mealRate + "");

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
            DatabaseReference ref = MainActivity.getMembersRef().child(boarders.get(i).getName());
            ref.setValue(boarders.get(i));
        }
    }
}