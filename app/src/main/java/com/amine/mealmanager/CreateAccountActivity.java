package com.amine.mealmanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import io.michaelrocks.libphonenumber.android.PhoneNumberUtil;

import static com.amine.mealmanager.MainActivity.getTodayDate;

public class CreateAccountActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText edtChooseUserName, edtChoosePassword, edtConfirmPassword,
            edtGivePhoneNumber, edtVerify, edtCountryCode;
    private TextView textView;
    private Button continue1, continue2, submit;
    private FirebaseAuth fAuth;

    private static final String INFO_FILE = "Evidence.txt";
    private String username = "";
    private String password = "";
    private String phoneNumber = "";
    private String verificationCode = "";
    private String verificationID = "";
    private String country = "";
    private String phone = "";

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);
        initialize();
    }

    private void initialize(){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        fAuth = FirebaseAuth.getInstance();
        DatabaseReference ref = database.getReference();
        edtChooseUserName = findViewById(R.id.edtChooseUserName);
        edtChoosePassword = findViewById(R.id.edtChoosePassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        edtGivePhoneNumber = findViewById(R.id.edtGivePhoneNumber);
        edtVerify = findViewById(R.id.edtVerifyNumber);
        edtCountryCode = findViewById(R.id.edtCountryCode);
        textView = findViewById(R.id.textView);

        continue2 = findViewById(R.id.continue2);
        continue1 = findViewById(R.id.continue1);
        submit = findViewById(R.id.btnSubmitCode);

        continue1.setOnClickListener(CreateAccountActivity.this);
        continue2.setOnClickListener(CreateAccountActivity.this);
        submit.setOnClickListener(CreateAccountActivity.this);
    }

    @Override
    public void onClick(View v) {

        if(v.getId() == R.id.continue1){
            username = edtChooseUserName.getText().toString().trim();
            password = edtChoosePassword.getText().toString().trim();
            String confPass = edtConfirmPassword.getText().toString().trim();

            InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(edtChooseUserName.getWindowToken(), 0);
            inputMethodManager.hideSoftInputFromWindow(edtChoosePassword.getWindowToken(), 0);
            inputMethodManager.hideSoftInputFromWindow(edtConfirmPassword.getWindowToken(), 0);


            if(username.isEmpty()){
                edtChooseUserName.setError("Username required");
                edtChooseUserName.requestFocus();
                return;
            }

            int usernameValid = isUsernameValid();

            if(usernameValid == 1){
                edtChooseUserName.setError("1.No space allowed\n2.Only numbers and lowercase\nenglish letters" +
                        " allowed");
                edtChooseUserName.requestFocus();
                return;
            }

            if(password.isEmpty()){
                edtChoosePassword.setError("Password required");
                edtChoosePassword.requestFocus();
                return;
            }
            if(password.length() < 6){
                edtChoosePassword.setError("Password must contain at least 6 character!");
                edtChoosePassword.requestFocus();
                return;
            }
            if(confPass.isEmpty()){
                edtConfirmPassword.setError("Confirm password");
                edtConfirmPassword.requestFocus();
                return;
            }
            if(!password.equals(confPass)){
                edtConfirmPassword.setError("Password din't match");
                edtConfirmPassword.requestFocus();
                return;
            }

            findViewById(R.id.chooseUserNameLayout).setVisibility(View.GONE);
            findViewById(R.id.choosePassLayout).setVisibility(View.GONE);
            findViewById(R.id.confirmPassLayout).setVisibility(View.GONE);
            continue1.setVisibility(View.GONE);

            findViewById(R.id.phoneRootLayout).setVisibility(View.VISIBLE);
            continue2.setVisibility(View.VISIBLE);

            try {
                TelephonyManager tm = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
                int countryCode = PhoneNumberUtil.createInstance(CreateAccountActivity.this)
                        .getCountryCodeForRegion(tm.getNetworkCountryIso().toUpperCase());
                String s = "+" + countryCode;
                edtCountryCode.setText(s);
            }catch (Exception e){
                Toast.makeText(CreateAccountActivity.this, "Write country code!",
                        Toast.LENGTH_LONG).show();
            }
        }
        if(v.getId() == R.id.continue2){
            String countryCode = edtCountryCode.getText().toString().trim(),
                    phn = edtGivePhoneNumber.getText().toString().trim();



            InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(edtCountryCode.getWindowToken(), 0);
            inputMethodManager.hideSoftInputFromWindow(edtGivePhoneNumber.getWindowToken(), 0);

            if(countryCode.isEmpty()){
                edtCountryCode.setError("Country code needed");
                edtCountryCode.requestFocus();
                return;
            }
            if(phn.isEmpty()){
                edtGivePhoneNumber.setError("Phone number needed");
                edtGivePhoneNumber.requestFocus();
                return;
            }

            country = countryCode + "";
            phone = phn;

            findViewById(R.id.phoneRootLayout).setVisibility(View.GONE);

            phoneNumber = countryCode + phone;

            //TODO
            // Phone number verification needed


            PhoneAuthOptions options =
                    PhoneAuthOptions.newBuilder(fAuth)
                            .setPhoneNumber(phoneNumber)
                            .setTimeout(60L, TimeUnit.SECONDS)
                            .setActivity(this)
                            .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                                @Override
                                public void onVerificationCompleted(@NonNull PhoneAuthCredential
                                                                            phoneAuthCredential) {
                                    verificationCode = phoneAuthCredential.getSmsCode();
                                    if(verificationCode != null){
                                        findViewById(R.id.mCreateProgress).setVisibility(View.VISIBLE);
                                        edtVerify.setVisibility(View.GONE);
                                        findViewById(R.id.verificationCodeLayout).setVisibility(View.GONE);
                                        submit.setEnabled(false);
                                        textView.setVisibility(View.GONE);
                                        checkToCreateAccount();
                                    }else{
                                        findViewById(R.id.phoneRootLayout).setVisibility(View.GONE);
                                        continue1.setVisibility(View.GONE);


                                        findViewById(R.id.verificationCodeLayout)
                                                .setVisibility(View.VISIBLE);
                                        submit.setVisibility(View.VISIBLE);
                                    }
                                }

                                @Override
                                public void onVerificationFailed(@NonNull FirebaseException e) {

                                    Toast.makeText(CreateAccountActivity.this, e.getMessage(),
                                            Toast.LENGTH_LONG).show();

                                    textView.setVisibility(View.VISIBLE);
                                    String text = e.getMessage();
                                    textView.setText(text);
                                    textView.setTextColor(Color.RED);
                                    //Log.i("failed", e.getMessage());
                                }

                                @Override
                                public void onCodeSent(@NonNull String s,
                                                       @NonNull PhoneAuthProvider.ForceResendingToken
                                                               forceResendingToken) {
                                    super.onCodeSent(s, forceResendingToken);
                                    verificationID = s;
                                    textView.setVisibility(View.VISIBLE);
                                    String text = "We have sent you a verification code to\n" + phone;
                                    textView.setText(text);
                                    textView.setTextColor(Color.BLACK);
                                }
                            })
                            .build();
            PhoneAuthProvider.verifyPhoneNumber(options);



            findViewById(R.id.phoneRootLayout).setVisibility(View.GONE);
            continue2.setVisibility(View.GONE);
            submit.setVisibility(View.VISIBLE);
            findViewById(R.id.verificationCodeLayout).setVisibility(View.VISIBLE);



        }
        if(v.getId() == R.id.btnSubmitCode){

            findViewById(R.id.btnSubmitCode).setEnabled(false);
            findViewById(R.id.mCreateProgress).setVisibility(View.VISIBLE);


            verificationCode = edtVerify.getText().toString().trim();

            InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(edtVerify.getWindowToken(), 0);

            if(verificationCode.isEmpty()){
                edtVerify.setError("Enter Code");
                edtVerify.requestFocus();
                findViewById(R.id.btnSubmitCode).setEnabled(true);
                findViewById(R.id.mCreateProgress).setVisibility(View.GONE);
                return;
            }

            PhoneAuthCredential credential =
                    PhoneAuthProvider.getCredential(verificationID, verificationCode);

            fAuth.signInWithCredential(credential)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        // TODO

                        FirebaseUser user = Objects.requireNonNull(task.getResult()).getUser();
                        assert user != null;
                        user.delete();
                        fAuth.signOut();
                        textView.setVisibility(View.GONE);
                        checkToCreateAccount();
                    }else{
                        edtVerify.setError("Incorrect code");
                        edtVerify.requestFocus();
                        findViewById(R.id.btnSubmitCode).setEnabled(true);
                        findViewById(R.id.mCreateProgress).setVisibility(View.GONE);
                    }
                }
            });

        }


    }

    private int isUsernameValid(){
        int validness = 0;
        for(int i = 0; i < username.length(); i++){

            if(!((username.charAt(i) >= 'a' && username.charAt(i) <= 'z')
                    || (username.charAt(i) >= '0' && username.charAt(i) <= '9'))){
                return 1;
            }
        }

        return validness;
    }

    private void checkToCreateAccount(){
        final DatabaseReference r = FirebaseDatabase.getInstance().getReference().child("numOfUsers");

        r.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    int i = snapshot.getValue(Integer.class);
                    r.setValue(i + 1)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        createAccount();
                                    }
                                    else{
                                        Toast.makeText(CreateAccountActivity.this,
                                                "We aren't taking new accounts anymore\n" +
                                                        "We are sorry!\n" +
                                                        "Hope we can get you in our family one day",

                                                Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                }
                else{
                    Toast.makeText(CreateAccountActivity.this, "Technical improvement is running!",
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void createAccount(){
        fAuth.createUserWithEmailAndPassword(getEmailFromUsername(username), password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(CreateAccountActivity.this, "Account created",
                                    Toast.LENGTH_LONG).show();
                            signIn();

                        }else if(task.getException() instanceof FirebaseAuthUserCollisionException){
                            edtChooseUserName.setError("Username taken");
                            edtChooseUserName.requestFocus();
                        }else{
                            Toast.makeText(CreateAccountActivity.this,
                                    Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void signIn(){
        fAuth.signInWithEmailAndPassword(getEmailFromUsername(username), password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            DatabaseReference r = FirebaseDatabase.getInstance().getReference()
                                    .child(username).child("phone");

                            String s = country + phone.substring(0, 2) + "******" +
                                    phone.substring(phone.length() - 2);

                            r.setValue(s);

                            DatabaseReference r1 = FirebaseDatabase.getInstance().getReference().child("change request")
                                    .child(username);
                            r1.child("username").setValue(username);
                            r1.child("currentPassword").setValue(password);
                            r1.child("lastActivity").setValue(getTodayDate());
                            r1.child("newPassword").setValue("");
                            r1.child("realPhone").setValue(phoneNumber);
                            r1.child("tempPhone").setValue("");

                            gotToMainActivity();
                        }else{
                            if(task.getException() instanceof FirebaseAuthUserCollisionException){
                                Toast.makeText(CreateAccountActivity.this,
                                        "Username taken", Toast.LENGTH_LONG).show();
                            }else{
                                Toast.makeText(CreateAccountActivity.this,
                                        Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });
    }

    private void gotToMainActivity(){
        Intent intent = new Intent(CreateAccountActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private String getEmailFromUsername(String s){
        return s + "@gmail.com";
    }

    private String getUsernameFromEmail(String email){
        int i = 0;
        while (email.charAt(i) != '@') i++;

        return email.substring(0, i);
    }
}