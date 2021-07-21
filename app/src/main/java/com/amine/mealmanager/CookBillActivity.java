package com.amine.mealmanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;

import static com.amine.mealmanager.MainActivity.IS_MANAGER;
import static com.amine.mealmanager.MainActivity.MAX_BOARDER;
import static com.amine.mealmanager.MainActivity.getTodayDate;
import static com.amine.mealmanager.MainActivity.readFromDatabase;
import static com.amine.mealmanager.MainActivity.rootRef;

public class CookBillActivity extends AppCompatActivity implements View.OnClickListener {

    private ArrayList<CooksBill> cooksBills;
    private LinearLayout rootLayout;
    private static final DecimalFormat df =  new DecimalFormat("0.#");
    private double totalCookBill = 0, paidCookBill = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cook_bill);
        ActionBar a = getSupportActionBar();
        if(a != null) a.setTitle("  Cook Bills");
        initialize();
        makeInvisible();
        setPaid(new Wait() {
            @Override
            public void onCallback() {
                makeVisible();
                setCooksBillsToFrame();
            }
        });
    }

    private void setPaid(final Wait wait) {
        rootRef.child("cookBillPaid").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String bill = snapshot.getValue(String.class);
                    paidCookBill = Double.parseDouble(bill);
                    TextView tv = findViewById(R.id.txtCookBillPaid);
                    String s = "Paid: " + df.format(paidCookBill);
                    tv.setText(s);
                }
                wait.onCallback();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void makeInvisible(){

        findViewById(R.id.progress_cookBill).setVisibility(View.VISIBLE);
        findViewById(R.id.billInfoLayout).setVisibility(View.GONE);

    }
    private void makeVisible(){

        findViewById(R.id.progress_cookBill).setVisibility(View.GONE);
        findViewById(R.id.billInfoLayout).setVisibility(View.VISIBLE);
    }

    private void initialize(){
        cooksBills = MainActivity.cooksBills;
        rootLayout = findViewById(R.id.rootLayout);

        if(!IS_MANAGER){
            findViewById(R.id.btnUpdateCookBill).setVisibility(View.GONE);
            findViewById(R.id.btnPayCook).setVisibility(View.GONE);
        }

        else{
            findViewById(R.id.btnUpdateCookBill).setVisibility(View.VISIBLE);
            findViewById(R.id.btnPayCook).setVisibility(View.VISIBLE);
            findViewById(R.id.btnUpdateCookBill).setOnClickListener(this);
            findViewById(R.id.btnPayCook).setOnClickListener(this);
        }

    }

    private void setCooksBillsToFrame(){
        rootLayout.removeAllViews();
        totalCookBill = 0;
        Resources res = getResources();
        Drawable drawable = null;
        try {
            drawable = Drawable.createFromXml(res,
                    res.getXml(R.xml.rectangular_shape_cook_bill));
        }
        catch (IOException | XmlPullParserException e) {
            e.printStackTrace();
        }

        for(int i = 0; i < cooksBills.size(); i++) {

            totalCookBill += Double.parseDouble(cooksBills.get(i).getPaid());

            LinearLayout ll = new LinearLayout(CookBillActivity.this),
                    fake = new LinearLayout(CookBillActivity.this);
            rootLayout.addView(ll);
            rootLayout.addView(fake);

            ll.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams paramsLl = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, 150),
                    paramsFake =
                            new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 10);
            ll.setLayoutParams(paramsLl);
            fake.setLayoutParams(paramsFake);

            if(drawable != null) ll.setBackground(drawable);

            fake.setBackgroundColor(Color.WHITE);

            TextView name = new TextView(CookBillActivity.this),
                    paid = new TextView(CookBillActivity.this);

            ll.addView(name);
            ll.addView(paid);

            ll.setGravity(Gravity.CENTER);


            LinearLayout.LayoutParams paramsNm = new LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.MATCH_PARENT, 30);

            name.setLayoutParams(paramsNm);
            String s = (i + 1) + ". " + cooksBills.get(i).getName();
            name.setText(s);
            name.setGravity(Gravity.CENTER);
            name.setTypeface(Typeface.DEFAULT_BOLD);

            LinearLayout.LayoutParams paramsDt = new LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    20
            );
            paid.setText(df.format(Double.parseDouble(cooksBills.get(i).getPaid())));
            paid.setGravity(Gravity.CENTER);
            paid.setTypeface(Typeface.DEFAULT_BOLD);
            paid.setLayoutParams(paramsDt);

        }

        TextView tv = findViewById(R.id.txtCookBillTotalPaid);
        String s = "Cash: " + df.format(totalCookBill);
        tv.setText(s);


    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if(id == R.id.btnUpdateCookBill){
            UpdateDialog d = new UpdateDialog(this);
            d.show();
        }
        else if(id == R.id.btnPayCook){
            RefundDialog r = new RefundDialog(this);
            r.show();
        }
    }

    private class RefundDialog extends Dialog implements View.OnClickListener{

        public RefundDialog(@NonNull Context context) {
            super(context);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.payback_dialog);
            initialize();
        }

        @Override
        public void onClick(View v) {
            int id = v.getId();
            if(id == R.id.paybackDialog_btnPayback){
                EditText ed = findViewById(R.id.paybackDialog_edtAmount);
                String s = ed.getText().toString();
                if(s.isEmpty()){
                    ed.setError("Enter amount");
                    ed.requestFocus();
                    return;
                }
                double amount = Double.parseDouble(s) + paidCookBill;
                final DatabaseReference r = rootRef.child("cookBillPaid");
                r.setValue(amount + "");
                makeInvisible();
                setPaid(new Wait() {
                    @Override
                    public void onCallback() {
                        makeVisible();
                        setCooksBillsToFrame();
                    }
                });

            }
            dismiss();

        }

        private void initialize(){
            findViewById(R.id.paybackDialog_btnCancel).setOnClickListener(this);
            Button b = findViewById(R.id.paybackDialog_btnPayback);
            String s = "Pay";
            b.setText(s);
            b.setOnClickListener(this);
        }
    }


    private interface UpdateEditText{
        void onUpdate(String s, int ID);
    }
    private interface Wait{
        void onCallback();
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
                    Toast.makeText(CookBillActivity.this, "Click 'AC' to clear", Toast.LENGTH_LONG).show();
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
                    Toast.makeText(CookBillActivity.this, "Can't include negative numbers\n" +
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
                Toast.makeText(CookBillActivity.this, "Click 'AC' to clear", Toast.LENGTH_LONG).show();
                return;
            }
            String s = txtDisplayResult.getText().toString() + num;
            txtDisplayResult.setText(s);
        }
        private void operation(String op){

            if(equalClicked){
                Toast.makeText(CookBillActivity.this, "Click 'AC' to clear", Toast.LENGTH_LONG).show();
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

    private class UpdateDialog extends Dialog implements View.OnClickListener{

        LinearLayout rootLayout;
        private final EditText[] edtCooksBill = new EditText[MAX_BOARDER];

        public UpdateDialog(@NonNull Context context) {
            super(context);
        }

        @Override
        public void onClick(View v) {
            int id = v.getId();
            if(id == R.id.btnUpdateBill){
                totalCookBill = 0;
                DatabaseReference r = rootRef.child("cooksBill");
                for(int i = 0; i < cooksBills.size(); i++){
                    String cur = edtCooksBill[i].getText().toString();
                    if(cur.equals("")) cur = "0";
                    String fin = (Double.parseDouble(cooksBills.get(i).getPaid()) +
                            Double.parseDouble(cur)) + "";

                    cooksBills.set(i, new CooksBill(cooksBills.get(i).getName(), fin));
                }
                r.setValue(cooksBills);
                setCooksBillsToFrame();
                dismiss();
            }
            else if(id == R.id.btnCancelUpdate){
                dismiss();
            }
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.cook_bill_update);
            initialize();
        }

        private void initialize(){
            rootLayout = findViewById(R.id.rootLayout);
            findViewById(R.id.btnUpdateBill).setOnClickListener(this);
            findViewById(R.id.btnCancelUpdate).setOnClickListener(this);
            setInfoToDialog();
        }

        private void setInfoToDialog(){
            final ImageView[] calc = new ImageView[MAX_BOARDER];
            rootLayout.removeAllViews();

            Resources res = getResources();
            Drawable drawable = null;
            try {
                drawable = Drawable.createFromXml(res,
                        res.getXml(R.xml.rectangular_shape_cook_bill));
            }
            catch (IOException | XmlPullParserException e) {
                e.printStackTrace();
            }

            for(int i = 0; i < cooksBills.size(); i++) {

                LinearLayout ll = new LinearLayout(CookBillActivity.this),
                        fake = new LinearLayout(CookBillActivity.this);
                rootLayout.addView(ll);
                rootLayout.addView(fake);

                ll.setOrientation(LinearLayout.HORIZONTAL);
                LinearLayout.LayoutParams paramsLl = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, 150),
                        paramsFake =
                                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 10);
                ll.setLayoutParams(paramsLl);
                fake.setLayoutParams(paramsFake);

                if(drawable != null) ll.setBackground(drawable);

                fake.setBackgroundColor(Color.WHITE);

                TextView name = new TextView(CookBillActivity.this);
                if(IS_MANAGER) {
                    edtCooksBill[i] = new EditText(CookBillActivity.this);
                    edtCooksBill[i].setInputType(InputType.TYPE_CLASS_NUMBER);
                    calc[i] = new ImageView(CookBillActivity.this);
                    calc[i].setId(i);
                }
                TextView tv = new TextView(CookBillActivity.this),
                        gap = new TextView(CookBillActivity.this);


                ll.addView(name);
                ll.addView(gap);

                gap.setLayoutParams(new LinearLayout.LayoutParams(
                        30, ViewGroup.LayoutParams.MATCH_PARENT));

                if(IS_MANAGER)
                    ll.addView(edtCooksBill[i]);
                if(IS_MANAGER) {
                    ll.addView(calc[i]);
                }
                else
                    ll.addView(tv);
                ll.setGravity(Gravity.CENTER);


                LinearLayout.LayoutParams paramsNm = new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.MATCH_PARENT, 30);

                name.setLayoutParams(paramsNm);
                String s = (i + 1) + ". " + cooksBills.get(i).getName() +
                        "(" + df.format(Double.parseDouble(cooksBills.get(i).getPaid())) + ")";
                name.setText(s);
                name.setGravity(Gravity.CENTER);
                name.setTypeface(Typeface.DEFAULT_BOLD);

                LinearLayout.LayoutParams paramsAm = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
                if(IS_MANAGER){
                    edtCooksBill[i].setHint("Enter payment");
                    edtCooksBill[i].setLayoutParams(paramsAm);

                    calc[i].setImageResource(R.drawable.calcimagee);
                    calc[i].setLayoutParams(new LinearLayout.LayoutParams(
                            100, 100));
                }

                if(!IS_MANAGER) {
                    tv.setLayoutParams(paramsAm);
                }

                if(IS_MANAGER){
                    final int finalI = i;
                    calc[i].setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            CalculatorInterface c = new CalculatorInterface(CookBillActivity.this,
                                    calc[finalI].getId(),
                                    new UpdateEditText() {
                                        @Override
                                        public void onUpdate(String s, int ID) {
                                            edtCooksBill[ID].setText(s);
                                        }
                                    });
                            c.show();
                            DisplayMetrics displayMetrics = new DisplayMetrics();
                            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                            int displayWidth = displayMetrics.widthPixels;
                            int displayHeight = displayMetrics.heightPixels;
                            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
                            layoutParams.copyFrom(c.getWindow().getAttributes());
                            int dialogWindowWidth = (int) (displayWidth * 0.9f);
                            int dialogWindowHeight = (int) (displayHeight * 0.8f);
                            layoutParams.width = dialogWindowWidth;
                            layoutParams.height = dialogWindowHeight;
                            c.getWindow().setAttributes(layoutParams);
                        }
                    });
                }

            }

        }
    }
}