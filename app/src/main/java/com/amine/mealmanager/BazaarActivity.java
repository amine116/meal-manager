package com.amine.mealmanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;

import static com.amine.mealmanager.MainActivity.getTodayDate;
import static com.amine.mealmanager.MainActivity.marketerHistories;
import static com.amine.mealmanager.MainActivity.rootRef;

public class BazaarActivity extends AppCompatActivity implements View.OnClickListener {
    private LinearLayout rootLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bazaar);
        initialize();
    }


    private void initialize(){
        rootLayout = findViewById(R.id.rootLayout);
        findViewById(R.id.btnAddBazaar).setOnClickListener(this);
        setMarketHistoryToFrame();
    }


    private void setMarketHistoryToFrame(){
        rootLayout.removeAllViews();
        Resources res = getResources();
        Drawable drawable = null;
        try {
            drawable = Drawable.createFromXml(res,
                    res.getXml(R.xml.rectangular_shape_market_history));
        }
        catch (IOException | XmlPullParserException e) {
            e.printStackTrace();
        }

        for(int i = 0; i < marketerHistories.size(); i++){
            LinearLayout ll = new LinearLayout(BazaarActivity.this),
                    fake = new LinearLayout(BazaarActivity.this);
            rootLayout.addView(ll);
            rootLayout.addView(fake);

            ll.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams paramsLl = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT),
                    paramsFake = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 10);

            ll.setLayoutParams(paramsLl);
            if(drawable != null) ll.setBackground(drawable);
            fake.setLayoutParams(paramsFake);
            fake.setBackgroundColor(Color.WHITE);


            TextView name = new TextView(this),
                    date = new TextView(this),
                    amount = new TextView(this),
                    fakeT = new TextView(this),
                    fakeH1 = new TextView(this),
                    fakeH2 = new TextView(this),
                    fakeH3 = new TextView(this);

            ll.addView(name);
            ll.addView(fakeH1);
            ll.addView(date);
            ll.addView(fakeH2);
            ll.addView(amount);
            ll.addView(fakeH3);
            ll.setGravity(Gravity.CENTER);

            name.setTypeface(Typeface.DEFAULT_BOLD);
            date.setTypeface(Typeface.DEFAULT_BOLD);
            amount.setTypeface(Typeface.DEFAULT_BOLD);

            date.setGravity(Gravity.CENTER);
            date.setTextColor(Color.WHITE);
            amount.setGravity(Gravity.CENTER);
            amount.setTextColor(Color.WHITE);

            name.setGravity(Gravity.CENTER);
            name.setTextColor(Color.WHITE);

            fakeH1.setLayoutParams(new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.MATCH_PARENT, 1));
            fakeH2.setLayoutParams(new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.MATCH_PARENT, 1));
            fakeH3.setLayoutParams(new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.MATCH_PARENT, 1));

            fakeH1.setBackgroundColor(Color.WHITE);
            fakeH2.setBackgroundColor(Color.WHITE);
            fakeH3.setBackgroundColor(Color.WHITE);

            LinearLayout.LayoutParams paramsNm = new LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    35
            );
            name.setLayoutParams(paramsNm);
            String nm = (i + 1) + ". " + marketerHistories.get(i).getName();
            name.setText(nm);

            LinearLayout.LayoutParams paramsDt = new LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    42
            );
            date.setText(marketerHistories.get(i).getExpenseHistory());
            date.setLayoutParams(paramsDt);

            LinearLayout.LayoutParams paramsAm = new LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    20
            );
            amount.setText(marketerHistories.get(i).getTotalAmount());
            amount.setLayoutParams(paramsAm);

            fakeT.setLayoutParams(paramsAm);
        }
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.btnAddBazaar){
            AddMarketHistoryDialog addMarketHistoryDialog = new
                    AddMarketHistoryDialog(BazaarActivity.this);
            addMarketHistoryDialog.show();

            WindowManager.LayoutParams layoutParams =
                    getWindowParams(addMarketHistoryDialog, 0.8f, 0.4f);
            addMarketHistoryDialog.getWindow().setAttributes(layoutParams);
        }
    }

    private class AddMarketHistoryDialog extends Dialog implements View.OnClickListener{

        EditText edtName, edtDate, edtAmount;

        public AddMarketHistoryDialog(@NonNull Context context) {
            super(context);
        }

        @Override
        public void onClick(View v) {

            if(v.getId() == R.id.saveTheMember){
                String name = edtName.getText().toString().toLowerCase().trim(),
                        amount = edtAmount.getText().toString().trim();
                if(!name.equals("")){
                    if(!amount.equals("")){
                        MainActivity.totalCost += Double.parseDouble(amount);
                        saveMarketHistoryToDatabase(name, edtDate.getText().toString(), amount);
                        dismiss();
                    }else{
                        Toast.makeText(BazaarActivity.this,
                                "Enter amount \uD83D\uDE44", Toast.LENGTH_LONG).show();
                    }

                }else{
                    Toast.makeText(BazaarActivity.this,
                            "Enter name \uD83D\uDE44", Toast.LENGTH_LONG).show();
                }
            }
            if(v.getId() == R.id.cancelSavingTheMember){
                dismiss();
            }
            if(v.getId() == R.id.btnLayoutCalc3){

                CalculatorInterface calculator = new CalculatorInterface(BazaarActivity.this,
                        v.getId(),
                        new UpdateEditText() {
                            @Override
                            public void onUpdate(String s, int ID) {
                                edtAmount.setText(s);
                            }
                        });

                calculator.show();

                WindowManager.LayoutParams layoutParams = getWindowParams(calculator, 0.9f, 0.8f);
                calculator.getWindow().setAttributes(layoutParams);
            }

        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.layout);
            initialize();
        }
        private void initialize(){
            TextView nm = findViewById(R.id.txtName);
            TextView am = findViewById(R.id.txtAddMeal);
            TextView dt = findViewById(R.id.txtAddPayment);

            String s = "Marketer Name: ";
            nm.setText(s);
            s = "Date: ";
            dt.setText(s);
            s = "Amount: ";
            am.setText(s);

            edtName = findViewById(R.id.edtName);
            edtDate = findViewById(R.id.edtAddPayment);
            edtAmount = findViewById(R.id.edtAddMeal);

            edtAmount.setHint("Amount");
            edtDate.setHint("Date");


            edtDate.setText(getTodayDate());

            findViewById(R.id.saveTheMember).setOnClickListener(this);
            findViewById(R.id.cancelSavingTheMember).setOnClickListener(this);
            findViewById(R.id.btnLayoutCalc3).setOnClickListener(this);
            findViewById(R.id.btnLayoutCalc1).setVisibility(View.INVISIBLE);
            findViewById(R.id.btnLayoutCalc2).setVisibility(View.INVISIBLE);

        }
    }

    private void saveMarketHistoryToDatabase(String name, String date, String amount){
        int exis = marketerExists(name);
        if(exis == 0){
            marketerHistories.add(new MarketerHistory(name, date + " (" + amount + ")", amount));
        }else{
            int index = exis - 1;
            String findExpense = marketerHistories.get(index).getExpenseHistory() + "\n" + date + " (" + amount + ")";
            String findAmount = (Double.parseDouble(marketerHistories.get(index).getTotalAmount()) +
                    Double.parseDouble(amount)) + "";
            marketerHistories.get(index).setExpenseHistory(findExpense);
            marketerHistories.get(index).setTotalAmount(findAmount);
        }
        for(int i = 0; i < marketerHistories.size(); i++){
            DatabaseReference ref = rootRef.child("Marketer History").child(marketerHistories.get(i).getName());
            ref.setValue(marketerHistories.get(i));
        }
    }

    private int marketerExists(String name){
        for(int i = 0; i < marketerHistories.size(); i++){
            if(marketerHistories.get(i).getName().equals(name)) return (i + 1);
        }

        return 0;
    }

    private WindowManager.LayoutParams getWindowParams(Dialog dialog, double width, double height){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int displayWidth = displayMetrics.widthPixels;
        int displayHeight = displayMetrics.heightPixels;
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(dialog.getWindow().getAttributes());
        int dialogWindowWidth = (int) (displayWidth * width);
        int dialogWindowHeight = (int) (displayHeight * height);
        layoutParams.width = dialogWindowWidth;
        layoutParams.height = dialogWindowHeight;
        return layoutParams;
    }

    private interface UpdateEditText{
        void onUpdate(String s, int ID);
    }

    private class CalculatorInterface extends Dialog implements View.OnClickListener{

        private TextView txtDisplayInputs, txtDisplayResult;
        private final int ID;
        private double result = 0;
        private String  prevOp = "", lastOp = "";
        private boolean equalClicked = false;
        private final UpdateEditText updateEditText;

        public CalculatorInterface(@NonNull Context context, int ID, UpdateEditText updateEditText) {
            super(context);
            this.updateEditText = updateEditText;
            this.ID = ID;
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.calculator_interface);
            initialize();
        }

        @Override
        public void onClick(View v) {

            if(v.getId() == R.id.btn0){
                if(equalClicked){
                    Toast.makeText(BazaarActivity.this, "Click 'AC' to clear", Toast.LENGTH_LONG).show();
                    return;
                }
                String s = txtDisplayResult.getText().toString();
                if(!s.equals("")) s = s + "0";
                txtDisplayResult.setText(s);
            }
            if(v.getId() == R.id.btn1){
                //addDigit("1");
                addDigit("1");
            }
            if(v.getId() == R.id.btn2){
                addDigit("2");
            }
            if(v.getId() == R.id.btn3){
                addDigit("3");
            }
            if(v.getId() == R.id.btn4){
                addDigit("4");
            }
            if(v.getId() == R.id.btn5){
                addDigit("5");
            }
            if(v.getId() == R.id.btn6){
                addDigit("6");
            }
            if(v.getId() == R.id.btn7){
                addDigit("7");
            }
            if(v.getId() == R.id.btn8){
                addDigit("8");
            }
            if(v.getId() == R.id.btn9){
                addDigit("9");
            }
            if(v.getId() == R.id.btnAc){

                result = 0;
                txtDisplayResult.setText("");
                txtDisplayInputs.setText("");
                equalClicked = false;
                lastOp = "";
                prevOp = "";
            }
            if(v.getId() == R.id.btnX){

                String s = txtDisplayResult.getText().toString();
                if(s.length() > 1) s = s.substring(0, s.length() - 1);
                else s = "";
                txtDisplayResult.setText(s);
            }
            if(v.getId() == R.id.btnDiv){

                operation("÷");
            }
            if(v.getId() == R.id.btnMul){

                operation("X");
            }
            if(v.getId() == R.id.btnMin){
                //addPlusMin("-");
                operation("-");
            }
            if(v.getId() == R.id.btnPlus){
                //addPlusMin("+");
                operation("+");
            }
            if(v.getId() == R.id.btnEqual){

                String prev = txtDisplayInputs.getText().toString(),
                        s = txtDisplayResult.getText().toString();
                if(s.equals("")) return;

                if(equalClicked) return;
                equalClicked = true;

                if(!prev.equals("")){
                    prev = "( " + prev + " " + s + " ) )";
                }
                else{
                    prev = "( " + s + " )";
                }
                txtDisplayInputs.setText(prev);


                switch (lastOp) {
                    case "÷":
                        result = result / Double.parseDouble(s);
                        break;
                    case "X":
                        result = result * Double.parseDouble(s);
                        break;
                    case "+":
                        result = result + Double.parseDouble(s);
                        break;
                    case "-":
                        result = result - Double.parseDouble(s);
                        break;
                    case "":
                        result = Double.parseDouble(s);
                        break;
                }

                String val = result + "";
                txtDisplayResult.setText(val);
                Log.i("test", result + "");
            }
            if(v.getId() == R.id.btnPoint){
                /*
                String numb = txtNumbers[index].getText().toString();
                if(!pointExistInNumber(numb)) numb = numb + ".";
                txtNumbers[index].setText(numb);
                displayInputs.removeView(txtNumbers[index]);
                displayInputs.addView(txtNumbers[index]);
                txtNumbers[index].setLayoutParams(params);

                 */
                String s = txtDisplayResult.getText().toString();
                if(!pointExistInNumber(s)) s = s + ".";
                txtDisplayResult.setText(s);
            }

            if(v.getId() == R.id.btnCalcInterfaceOk){

                String s = txtDisplayResult.getText().toString();
                if(s.equals("")) s = "0";
                int res = (int)Double.parseDouble(s);
                if(res < 0){
                    Toast.makeText(BazaarActivity.this, "Can't include negative numbers\n" +
                                    "Press cancel",
                            Toast.LENGTH_LONG).show();
                }else {
                    updateEditText.onUpdate(res + "", ID);
                    dismiss();
                }
            }
            if(v.getId() == R.id.btnCalcInterfaceCancel){
                dismiss();
            }

        }

        private void addDigit(String num){
            if(equalClicked){
                Toast.makeText(BazaarActivity.this, "Click 'AC' to clear", Toast.LENGTH_LONG).show();
                return;
            }
            String s = txtDisplayResult.getText().toString() + num;
            txtDisplayResult.setText(s);
        }
        private void operation(String op){

            if(equalClicked){
                Toast.makeText(BazaarActivity.this, "Click 'AC' to clear", Toast.LENGTH_LONG).show();
                return;
            }
            String curNum = txtDisplayResult.getText().toString();

            if(curNum.equals("") || curNum.equals("0")) return;
            lastOp = op;
            Log.i("test2", result + "");
            operate(prevOp, curNum);
            Log.i("test2", result + "");
        }
        private void operate(String op, String num){

            String prev = txtDisplayInputs.getText().toString();
            if(!prev.equals("")){
                prev = "( " + prev + " " + num + " ) " + lastOp;
            }
            else{
                prev = "( " + num + " " + lastOp + " ";
            }
            txtDisplayInputs.setText(prev);
            txtDisplayResult.setText("");

            switch (op) {
                case "÷":
                    result = result / Double.parseDouble(num);
                    break;
                case "X":
                    result = result * Double.parseDouble(num);
                    break;
                case "+":
                    result = result + Double.parseDouble(num);
                    break;
                case "-":
                    result = result - Double.parseDouble(num);
                    break;
                case "":
                    result = Double.parseDouble(num);
                    break;
            }
            prevOp = lastOp;

        }
        private boolean pointExistInNumber(String number){
            for(int i = 0; i < number.length(); i++){
                if(number.charAt(i) == '.'){
                    return true;
                }
            }
            return false;
        }
        private void initialize(){
            //divInd = new ArrayList<>();
            //mulInd  = new ArrayList<>();
            //tDiv = new ArrayList<>();

            //tMul = new ArrayList<>();

            //initializeViews();
            //displayInputs = findViewById(R.id.displayInputs);
            //displayResult = findViewById(R.id.displayResult);
            //params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            txtDisplayInputs = findViewById(R.id.txtDisplayInputs);
            txtDisplayResult = findViewById(R.id.txtDisplayResult);

            findViewById(R.id.btn0).setOnClickListener(this);
            findViewById(R.id.btn1).setOnClickListener(this);
            findViewById(R.id.btn2).setOnClickListener(this);
            findViewById(R.id.btn3).setOnClickListener(this);
            findViewById(R.id.btn4).setOnClickListener(this);
            findViewById(R.id.btn5).setOnClickListener(this);
            findViewById(R.id.btn6).setOnClickListener(this);
            findViewById(R.id.btn7).setOnClickListener(this);
            findViewById(R.id.btn8).setOnClickListener(this);
            findViewById(R.id.btn9).setOnClickListener(this);
            findViewById(R.id.btnAc).setOnClickListener(this);
            findViewById(R.id.btnPlus).setOnClickListener(this);
            findViewById(R.id.btnMin).setOnClickListener(this);
            findViewById(R.id.btnMul).setOnClickListener(this);
            findViewById(R.id.btnDiv).setOnClickListener(this);
            findViewById(R.id.btnEqual).setOnClickListener(this);
            findViewById(R.id.btnPoint).setOnClickListener(this);
            findViewById(R.id.btnX).setOnClickListener(this);
            findViewById(R.id.btnCalcInterfaceOk).setOnClickListener(this);
            findViewById(R.id.btnCalcInterfaceCancel).setOnClickListener(this);

        }
    }
}