package com.amine.mealmanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import org.xmlpull.v1.XmlPullParserException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.amine.mealmanager.MainActivity.getManagerName;
import static com.amine.mealmanager.MainActivity.getTodayDate;
import static com.amine.mealmanager.MainActivity.rootRef;

public class ActivityMemberMarketNote extends AppCompatActivity implements View.OnClickListener,
        AdapterView.OnItemSelectedListener, ValueEventListener {

    private int numberOfItems = 0;
    private final int MAX_ITEM = 100;
    private LinearLayout itemListLayout, sLayout, itemLayout;
    private ArrayList<MarketItem> marketItems;
    private final EditText[] txtItemName = new EditText[MAX_ITEM], txtAmount = new EditText[MAX_ITEM],
            txtPrice = new EditText[MAX_ITEM];
    private LinearLayout.LayoutParams params;
    private String date = "", selectedName = "";
    private ArrayList<String> namesForSpinner, datesForSpinner;
    private Spinner spinnerName, spinnerDate;
    private Map<String, Map<String, ArrayList<MarketItem>>> allItems;
    private TextView txtTotal;
    private double total = 0;
    private final DecimalFormat df =  new DecimalFormat("0.#");
    private DatabaseReference ref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_market_note);
        ActionBar a = getSupportActionBar();
        if(a != null) a.setTitle("    Bazaar Note");
        initialize();
    }

    private void setItemsToFrame(String name, String time){

        itemLayout.removeAllViews();
        if(name.equals("Select Name")) return;
        if(time.equals("Select Date")) return;

        findViewById(R.id.marketNote_itemsScroll).setVisibility(View.GONE);
        findViewById(R.id.marketNote_scrollItems).setVisibility(View.VISIBLE);

        Map<String, ArrayList<MarketItem>> personalItem = allItems.get(name);
        ArrayList<MarketItem> item = personalItem.get(time);
        double total = 0;
        for(int i = 0; i < item.size(); i++){
            LinearLayout ll = new LinearLayout(ActivityMemberMarketNote.this),
                    fake = new LinearLayout(ActivityMemberMarketNote.this);

            itemLayout.addView(ll);
            itemLayout.addView(fake);

            ll.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 150));
            fake.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 10));
            ll.setOrientation(LinearLayout.HORIZONTAL);
            ll.setGravity(Gravity.CENTER);

            Resources res = getResources();
            Drawable drawable = null;
            try {
                drawable = Drawable.createFromXml(res,
                        res.getXml(R.xml.rectangular_shape));
            }
            catch (IOException | XmlPullParserException e) {
                e.printStackTrace();
            }
            if(drawable != null){
                ll.setBackground(drawable);
            }

            String itemName = item.get(i).getItemName(),
                    amount = item.get(i).getAmount(),
                    price = item.get(i).getPrice();
            total += Double.parseDouble(price);

            TextView txtName = new TextView(ActivityMemberMarketNote.this),
                    txtAmount = new TextView(ActivityMemberMarketNote.this),
                    txtPrice = new TextView(ActivityMemberMarketNote.this),
                    txtInterMid1 = new TextView(ActivityMemberMarketNote.this),
                    txtInterMid2 = new TextView(ActivityMemberMarketNote.this);
            txtName.setGravity(Gravity.CENTER);
            txtAmount.setGravity(Gravity.CENTER);
            txtPrice.setGravity(Gravity.CENTER);

            txtName.setBackground(drawable);
            txtAmount.setBackground(drawable);
            txtPrice.setBackground(drawable);

            txtName.setLayoutParams(new LinearLayout.LayoutParams(0,
                    ViewGroup.LayoutParams.MATCH_PARENT, 40));
            txtAmount.setLayoutParams(new LinearLayout.LayoutParams(0,
                    ViewGroup.LayoutParams.MATCH_PARENT, 20));
            txtPrice.setLayoutParams(new LinearLayout.LayoutParams(0,
                    ViewGroup.LayoutParams.MATCH_PARENT, 20));
            txtInterMid1.setLayoutParams(new LinearLayout.LayoutParams(0,
                    ViewGroup.LayoutParams.MATCH_PARENT, 10));
            txtInterMid2.setLayoutParams(new LinearLayout.LayoutParams(0,
                    ViewGroup.LayoutParams.MATCH_PARENT, 10));

            ll.addView(txtName);
            ll.addView(txtInterMid1);
            ll.addView(txtAmount);
            ll.addView(txtInterMid2);
            ll.addView(txtPrice);

            String s = (i + 1) + ". " + itemName;
            txtName.setText(s);
            s = "Amount: " + amount + " unit";
            txtAmount.setText(s);
            s = "Price: " + price + " BDT";
            txtPrice.setText(s);

        }
        String strTotal = "Total: " + df.format(total);
        txtTotal.setText(strTotal);
    }

    private void setNameSpinner(){
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, namesForSpinner);

        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerName.setAdapter(dataAdapter);
    }

    private void setDateSpinner(String name){

        if(!name.equals("Select Name")){
            Map<String, ArrayList<MarketItem>> personalItem = allItems.get(name);
            datesForSpinner.clear();
            datesForSpinner.add("Select Date");

            for (Map.Entry<String, ArrayList<MarketItem>> entry : personalItem.entrySet()) {
                datesForSpinner.add(entry.getKey());
            }

            ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, datesForSpinner);

            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            spinnerDate.setAdapter(dataAdapter);
        }
        else{
            datesForSpinner.clear();
            datesForSpinner.add("Select Date");
            ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, datesForSpinner);

            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            spinnerDate.setAdapter(dataAdapter);
        }
    }

    private void readAllItems(final ReadCurrent read){
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allItems.clear();
                namesForSpinner.clear();
                namesForSpinner.add("Select Name");
                if(snapshot.exists()){
                    for(DataSnapshot names : snapshot.getChildren()){
                        String name = names.getKey();
                        assert name != null;
                        Map<String, ArrayList<MarketItem>> tempMap = new HashMap<>();
                        for (DataSnapshot dates : names.getChildren()){
                            String date = dates.getKey();
                            assert date != null;
                            ArrayList<MarketItem> tempItems = new ArrayList<>();
                            for(DataSnapshot items : dates.getChildren()){
                                MarketItem item = items.getValue(MarketItem.class);
                                if(item != null) tempItems.add(item);
                            }
                            tempMap.put(date, tempItems);
                        }
                        allItems.put(name, tempMap);
                        namesForSpinner.add(name);
                    }
                }
                read.onCallback();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void initialize(){
        itemListLayout = findViewById(R.id.marketNote_itemListLayout);
        sLayout = findViewById(R.id.marketNote_sLayout);
        itemLayout = findViewById(R.id.marketNote_itemListLayout1);
        spinnerName = findViewById(R.id.marketNote_nameSpin);
        txtTotal = findViewById(R.id.marketNote_txtTotal);
        spinnerDate = findViewById(R.id.marketNote_dateSpin);
        spinnerDate.setOnItemSelectedListener(this);
        spinnerName.setOnItemSelectedListener(this);

        findViewById(R.id.marketNote_btnAddItem).setOnClickListener(this);
        findViewById(R.id.marketNote_btnShowItems).setOnClickListener(this);
        findViewById(R.id.marketNote_imgCalc).setOnClickListener(this);
        findViewById(R.id.marketNote_btnSave).setOnClickListener(this);
        marketItems = new ArrayList<>();
        namesForSpinner = new ArrayList<>();
        datesForSpinner = new ArrayList<>();
        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 150);
        allItems = new HashMap<>();

        ref = FirebaseDatabase.getInstance().getReference().child(MainActivity.getManagerName())
                .child("Discussion").child("Public Message");
        ref.addValueEventListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if(id == R.id.marketNote_btnAddItem){
            findViewById(R.id.marketNote_itemsScroll).setVisibility(View.VISIBLE);
            findViewById(R.id.marketNote_scrollItems).setVisibility(View.GONE);
            findViewById(R.id.marketNote_btnShowItems).setVisibility(View.VISIBLE);

            findViewById(R.id.marketNote_edtNameLayout).setVisibility(View.VISIBLE);
            findViewById(R.id.marketNote_btnSave).setVisibility(View.VISIBLE);
            txtTotal.setVisibility(View.GONE);
            txtTotal.setText("");

            spinnerDate.setVisibility(View.GONE);
            spinnerName.setVisibility(View.GONE);

            setNameSpinner();
            setDateSpinner("Select Name");

            LinearLayout layout = new LinearLayout(ActivityMemberMarketNote.this),
                    fakeLayout = new LinearLayout(ActivityMemberMarketNote.this);
            itemListLayout.addView(layout);
            itemListLayout.addView(fakeLayout);
            layout.setLayoutParams(params);
            fakeLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    10));

            Resources res = getResources();
            Drawable drawable = null;
            try {
                drawable = Drawable.createFromXml(res,
                        res.getXml(R.xml.rectangular_shape));
            } catch (IOException | XmlPullParserException e) {
                e.printStackTrace();
            }
            layout.setGravity(Gravity.CENTER);
            if(drawable != null) layout.setBackground(drawable);
            layout.setOrientation(LinearLayout.HORIZONTAL);
            layout.setGravity(Gravity.CENTER);

            txtItemName[numberOfItems] = new EditText(ActivityMemberMarketNote.this);
            txtAmount[numberOfItems] = new EditText(ActivityMemberMarketNote.this);
            txtPrice[numberOfItems] = new EditText(ActivityMemberMarketNote.this);
            TextView textView = new TextView(ActivityMemberMarketNote.this),
                    textView1 = new TextView(ActivityMemberMarketNote.this);

            layout.addView(txtItemName[numberOfItems]);
            layout.addView(txtAmount[numberOfItems]);
            layout.addView(txtPrice[numberOfItems]);

            sLayout.addView(textView);
            sLayout.addView(textView1);
            textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    150));
            textView.setGravity(Gravity.CENTER);
            textView.setBackground(drawable);
            textView1.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 10));

            String s = "Item Name";
            txtItemName[numberOfItems].setHint(s);
            s = "Amount\n(unit)";
            txtAmount[numberOfItems].setHint(s);
            s = "Total Price";
            txtPrice[numberOfItems].setHint(s);

            s = (numberOfItems + 1) + ". ";
            textView.setText(s);

            txtItemName[numberOfItems].setLayoutParams(new LinearLayout.LayoutParams(0,
                    ViewGroup.LayoutParams.MATCH_PARENT, 40));
            txtAmount[numberOfItems].setLayoutParams(new LinearLayout.LayoutParams(0,
                    ViewGroup.LayoutParams.MATCH_PARENT, 30));
            txtPrice[numberOfItems].setLayoutParams(new LinearLayout.LayoutParams(0,
                    ViewGroup.LayoutParams.MATCH_PARENT, 30));

            txtAmount[numberOfItems].setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            txtPrice[numberOfItems].setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);


            numberOfItems++;

        }
        if(id == R.id.marketNote_imgCalc){
            CalculatorInterface calc = new CalculatorInterface(this);
            calc.show();

            WindowManager.LayoutParams param = getWindowParams(calc);
            calc.getWindow().setAttributes(param);

        }
        if(id == R.id.marketNote_btnSave){
            EditText edtName = findViewById(R.id.marketNote_edtName);
            final String name = edtName.getText().toString().toLowerCase().trim();
            if(name.isEmpty()){
                edtName.setError("Name of Marketer is Required");
                edtName.requestFocus();
                return;
            }
            for(int i = 0; i < numberOfItems; i++){
                final String itemName = txtItemName[i].getText().toString().toLowerCase().trim(),
                        amount = txtAmount[i].getText().toString(),
                        price = txtPrice[i].getText().toString();
                if(itemName.isEmpty() && amount.isEmpty() && price.isEmpty()) continue;

                if(itemName.isEmpty()){
                    txtItemName[i].setError("Item Name Required");
                    txtItemName[i].requestFocus();
                    return;
                }

                if(amount.isEmpty()){
                    txtAmount[i].setError("Amount of Item is Required");
                    txtAmount[i].requestFocus();
                    return;
                }

                if(price.isEmpty()){
                    txtPrice[i].setError("Price of Item is Required");
                    txtPrice[i].requestFocus();
                    return;
                }
            }
            date = getTodayDate();
            findViewById(R.id.marketNote_progress).setVisibility(View.VISIBLE);
            findViewById(R.id.marketNote_itemsScroll).setVisibility(View.GONE);

            fillMarketItems(name, new ReadCurrent() {
                @Override
                public void onCallback() {
                    total = 0;
                    for(int i = 0; i < numberOfItems; i++){

                        final String itemName = txtItemName[i].getText().toString().toLowerCase().trim(),
                                amount = txtAmount[i].getText().toString(),
                                price = txtPrice[i].getText().toString();

                        marketItems.add(new MarketItem(itemName, amount, price));

                        total += Double.parseDouble(price);
                    }

                    saveAllItems(name);

                    itemListLayout.removeAllViews();
                    sLayout.removeAllViews();
                    numberOfItems = 0;

                }
            });

            InputMethodManager inputMethodManager =
                    (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(edtName.getWindowToken(), 0);


        }
        if(id == R.id.marketNote_btnShowItems){
            findViewById(R.id.marketNote_itemsScroll).setVisibility(View.GONE);
            findViewById(R.id.marketNote_scrollItems).setVisibility(View.VISIBLE);
            findViewById(R.id.marketNote_btnAddItem).setVisibility(View.VISIBLE);
            findViewById(R.id.marketNote_btnShowItems).setVisibility(View.GONE);
            findViewById(R.id.marketNote_edtNameLayout).setVisibility(View.GONE);
            findViewById(R.id.marketNote_btnSave).setVisibility(View.GONE);
            findViewById(R.id.marketNote_txtTotal).setVisibility(View.VISIBLE);
            txtTotal.setVisibility(View.VISIBLE);

            spinnerDate.setVisibility(View.VISIBLE);
            spinnerName.setVisibility(View.VISIBLE);

            itemListLayout.removeAllViews();
            sLayout.removeAllViews();
            numberOfItems = 0;
        }

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if(parent.getId() == spinnerName.getId()){
            selectedName = namesForSpinner.get(position);
            setDateSpinner(selectedName);
        }
        else{
            setItemsToFrame(selectedName, datesForSpinner.get(position));
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onDataChange(@NonNull DataSnapshot snapshot) {


        findViewById(R.id.marketNote_itemsScroll).setVisibility(View.GONE);
        findViewById(R.id.marketNote_progress).setVisibility(View.VISIBLE);
        findViewById(R.id.marketNote_edtNameLayout).setVisibility(View.GONE);
        findViewById(R.id.marketNote_btnSave).setVisibility(View.GONE);
        readAllItems(new ReadCurrent() {
            @Override
            public void onCallback() {
                itemLayout.removeAllViews();
                itemListLayout.removeAllViews();
                sLayout.removeAllViews();
                numberOfItems = 0;
                findViewById(R.id.marketNote_progress).setVisibility(View.GONE);
                findViewById(R.id.marketNote_scrollItems).setVisibility(View.VISIBLE);
                setNameSpinner();
            }
        });
    }

    @Override
    public void onCancelled(@NonNull DatabaseError error) {

    }

    private interface ReadCurrent{
        void onCallback();
    }

    private void saveAllItems(String name){
        DatabaseReference r1 = rootRef.child("Discussion").child("Public Message").child(name).child(date);

        if(!namesForSpinner.contains(name)) namesForSpinner.add(name);
        r1.setValue(marketItems);

        DatabaseReference r = rootRef.child("Discussion"),
                nR = rootRef.child("notifications").child(getManagerName() + "-Manager-").child("unseen")
                        .push();
        String notId = nR.getKey();

        String s = "Total market cost: " + df.format(total);
        r.child("notifications").child("text").setValue(s);
        r.child("notifications").child("title").setValue("Market Note from: (" + name + ")");
        r.child("notifications").child("id").setValue(2);

        UNotifications not = new UNotifications(notId, "Market Note from: (" + name + ")",
                s);
        nR.setValue(not);

        findViewById(R.id.marketNote_progress).setVisibility(View.GONE);
        findViewById(R.id.marketNote_itemsScroll).setVisibility(View.VISIBLE);
    }

    private void fillMarketItems(String name, final ReadCurrent read){
        DatabaseReference r = FirebaseDatabase.getInstance().getReference().child(MainActivity.getManagerName())
                .child("Discussion").child("Public Message").child(name).child(date);
        r.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                marketItems.clear();
                if(snapshot.exists()){
                    for(DataSnapshot items : snapshot.getChildren()){
                        MarketItem item = items.getValue(MarketItem.class);
                        if(item != null){
                            marketItems.add(item);
                        }
                    }
                }
                read.onCallback();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private class CalculatorInterface extends Dialog implements View.OnClickListener{

        private TextView txtDisplayInputs, txtDisplayResult;
        private double result = 0;
        private String  prevOp = "", lastOp = "";
        private boolean equalClicked = false;

        public CalculatorInterface(@NonNull Context context) {
            super(context);
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
                    Toast.makeText(ActivityMemberMarketNote.this, "Click 'AC' to clear", Toast.LENGTH_LONG).show();
                    return;
                }
                String s = txtDisplayResult.getText().toString();
                if(!s.equals("")) s = s + "0";
                txtDisplayResult.setText(s);
            }
            if(v.getId() == R.id.btn1){
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
                operation("-");
            }
            if(v.getId() == R.id.btnPlus){
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
            }
            if(v.getId() == R.id.btnPoint){

                String s = txtDisplayResult.getText().toString();
                if(!pointExistInNumber(s)) s = s + ".";
                txtDisplayResult.setText(s);
            }

            if(v.getId() == R.id.btnCalcInterfaceOk){

                String s = txtDisplayResult.getText().toString();
                if(s.equals("") || s.isEmpty()) s = "0.0";

                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("text", (int)Double.parseDouble(s) + "");
                clipboard.setPrimaryClip(clip);

                dismiss();
            }
            if(v.getId() == R.id.btnCalcInterfaceCancel){
                dismiss();
            }

        }

        private void addDigit(String num){
            if(equalClicked){
                Toast.makeText(ActivityMemberMarketNote.this, "Click 'AC' to clear", Toast.LENGTH_LONG).show();
                return;
            }
            String s = txtDisplayResult.getText().toString() + num;
            txtDisplayResult.setText(s);
        }
        private void operation(String op){

            if(equalClicked){
                Toast.makeText(ActivityMemberMarketNote.this, "Click 'AC' to clear", Toast.LENGTH_LONG).show();
                return;
            }
            String curNum = txtDisplayResult.getText().toString();

            if(curNum.equals("") || curNum.equals("0")) return;
            lastOp = op;
            operate(prevOp, curNum);
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


            txtDisplayInputs = findViewById(R.id.txtDisplayInputs);
            txtDisplayResult = findViewById(R.id.txtDisplayResult);

            findViewById(R.id.btn0).setOnClickListener(this);
            findViewById(R.id.btn1).setOnClickListener(this);
            findViewById(R.id.btn2).setOnClickListener(this);
            findViewById(R.id.btn3).setOnClickListener(this);
            findViewById(R.id.btn3).setOnClickListener(this);
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
            findViewById(R.id.btnCalcInterfaceCancel).setOnClickListener(this);

            TextView b = findViewById(R.id.btnCalcInterfaceOk);
            String s = "Copy result";
            b.setText(s);
            b.setOnClickListener(this);

        }
    }

    private WindowManager.LayoutParams getWindowParams(Dialog dialog){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int displayWidth = displayMetrics.widthPixels;
        int displayHeight = displayMetrics.heightPixels;
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(dialog.getWindow().getAttributes());
        int dialogWindowWidth = (int) (displayWidth * 0.9f);
        int dialogWindowHeight = (int) (displayHeight * 0.8f);
        layoutParams.width = dialogWindowWidth;
        layoutParams.height = dialogWindowHeight;
        return layoutParams;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent i = new Intent(ActivityMemberMarketNote.this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
    }
}