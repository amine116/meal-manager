package com.amine.mealmanager;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
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

import java.util.concurrent.TimeUnit;

import io.michaelrocks.libphonenumber.android.PhoneNumberUtil;

public class RecoverPassword extends AppCompatActivity implements View.OnClickListener {

    private EditText edtUsername, edtOTP, edtResetPass;
    private FirebaseAuth fAuth;
    private DatabaseReference ref;
    private TextView textView;
    private String username = "", phoneNumber = "", verificationCode = "", verificationID = "", dummyPhone = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recover_password);

        initialize();
    }

    private void initialize(){
        edtUsername = findViewById(R.id.edtRecGiveUsername);
        edtOTP = findViewById(R.id.edtOTP);
        edtResetPass = findViewById(R.id.edtResetPass);
        textView = findViewById(R.id.txtWritePhoneNumb);
        fAuth = FirebaseAuth.getInstance();
        ref = FirebaseDatabase.getInstance().getReference();
        findViewById(R.id.edtOTP).setOnClickListener(RecoverPassword.this);
        findViewById(R.id.btnResetPass).setOnClickListener(RecoverPassword.this);
        findViewById(R.id.btnRecContinue).setOnClickListener(RecoverPassword.this);
        Button btnRecSubmitOTP = findViewById(R.id.btnRecSubmitOTP);
        btnRecSubmitOTP.setOnClickListener(RecoverPassword.this);
        findViewById(R.id.btnSubmitPhone).setOnClickListener(RecoverPassword.this);
    }

    @Override
    public void onClick(View v) {

        if(v.getId() == R.id.btnRecContinue){
            username = edtUsername.getText().toString().trim();
            if(username.isEmpty()){
                edtUsername.setError("Username required");
                edtUsername.requestFocus();
                return;
            }

            findViewById(R.id.recProgress).setVisibility(View.VISIBLE);
            findViewById(R.id.btnRecContinue).setEnabled(false);
            isUserNameExists();

        }
        if(v.getId() == R.id.btnSubmitPhone){
            EditText cntrCde = findViewById(R.id.edtCountryCode),
                    phn = findViewById(R.id.edtGivePhone),
                    cnfrmPhn = findViewById(R.id.edtConfirmPhone);

            String cntrcd = cntrCde.getText().toString().trim(),
                    pn = phn.getText().toString().trim(),
                    cnfrmphn = cnfrmPhn.getText().toString().trim();

            if(cntrcd.isEmpty()){
                cntrCde.setError("Enter country code");
                cntrCde.requestFocus();
                return;
            }
            if(pn.isEmpty()){
                phn.setError("Enter phone");
                phn.requestFocus();
                return;
            }
            if(cnfrmphn.isEmpty()){
                cnfrmPhn.setError("Confirm phone");
                cnfrmPhn.requestFocus();
                return;
            }
            if(!cnfrmphn.equals(pn)){
                cnfrmPhn.setError("Phone didn't match");
                cnfrmPhn.requestFocus();
                return;
            }

            phoneNumber = cntrcd + cnfrmphn;
            findViewById(R.id.phoneVerificationLayout).setVisibility(View.GONE);
            findViewById(R.id.recProgress).setVisibility(View.VISIBLE);
            verifyPhone();
        }

        if(v.getId() == R.id.btnRecSubmitOTP){
            verificationCode = edtOTP.getText().toString().trim();
            if(verificationCode.isEmpty()){
                edtOTP.setError("Enter Code");
                edtOTP.requestFocus();
                return;
            }
            verifyCode();

        }
        if(v.getId() == R.id.btnResetPass){
            resetPassword();
        }
    }

    private void isUserNameExists(){
        fAuth.createUserWithEmailAndPassword(getEmailFromUsername(username), "123456")
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            FirebaseUser user = task.getResult().getUser();
                            user.delete();
                            edtUsername.setError("Username doesn't exist");
                            edtUsername.requestFocus();
                            findViewById(R.id.recProgress).setVisibility(View.GONE);
                            findViewById(R.id.btnRecContinue).setEnabled(true);

                        }else if(task.getException() instanceof FirebaseAuthUserCollisionException){
                            findViewById(R.id.userNameVerificationLayout).setVisibility(View.GONE);

                            ref.child(username).child("phone")
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if(snapshot.exists()){
                                                dummyPhone = snapshot.getValue(String.class);

                                                String sss = "Enter the phone number matching with\n" +
                                                        snapshot.getValue(String.class);

                                                textView.setText(sss);
                                                findViewById(R.id.phoneVerificationLayout)
                                                        .setVisibility(View.VISIBLE);
                                                textView.setVisibility(View.VISIBLE);
                                                findViewById(R.id.recProgress).setVisibility(View.GONE);

                                                try {
                                                    TelephonyManager tm =
                                                            (TelephonyManager)getSystemService(
                                                                    Context.TELEPHONY_SERVICE);
                                                    int countryCode =
                                                            PhoneNumberUtil.createInstance(RecoverPassword.this)
                                                            .getCountryCodeForRegion(tm.getNetworkCountryIso()
                                                                    .toUpperCase());
                                                    String s = "+" + countryCode;
                                                    EditText edtCountryCode = findViewById(R.id.edtCountryCode);
                                                    edtCountryCode.setText(s);
                                                }catch (Exception e){
                                                    Toast.makeText(RecoverPassword.this,
                                                            "Write country code!",
                                                            Toast.LENGTH_LONG).show();
                                                }

                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                        }
                        else{
                            Toast.makeText(RecoverPassword.this, task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }


    private void verifyPhone(){
        if(phoneNumber != null){
            String s = "We have sent you a verification code to \n" + dummyPhone;
            textView.setText(s);

            PhoneAuthOptions options =
                    PhoneAuthOptions.newBuilder(fAuth)
                            .setPhoneNumber(phoneNumber)
                            .setTimeout(60L, TimeUnit.SECONDS)
                            .setActivity(this)
                            .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                                @Override
                                public void onVerificationCompleted(
                                        @NonNull PhoneAuthCredential phoneAuthCredential) {
                                    verificationCode = phoneAuthCredential.getSmsCode();
                                    if(verificationCode != null){
                                        Toast.makeText(RecoverPassword.this, "Verified",
                                                Toast.LENGTH_LONG).show();
                                        textView.setVisibility(View.GONE);
                                        findViewById(R.id.otpVerificationLayout).setVisibility(View.VISIBLE);
                                        verifyCode();
                                    }else{
                                        findViewById(R.id.otpVerificationLayout).setVisibility(View.VISIBLE);
                                        findViewById(R.id.recProgress).setVisibility(View.GONE);
                                    }

                                }

                                @Override
                                public void onVerificationFailed(@NonNull FirebaseException e) {
                                    Toast.makeText(RecoverPassword.this, e.getMessage(),
                                            Toast.LENGTH_LONG).show();
                                    //Log.i("failed", e.getMessage());
                                }

                                @Override
                                public void onCodeSent(@NonNull String s,
                                                       @NonNull PhoneAuthProvider.ForceResendingToken
                                                               forceResendingToken) {
                                    super.onCodeSent(s, forceResendingToken);
                                    verificationID = s;
                                }
                            })
                            .build();
            PhoneAuthProvider.verifyPhoneNumber(options);

        }
    }

    private void verifyCode(){
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationID, verificationCode);
        fAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            fAuth.signOut();
                            task.getResult().getUser().delete();
                            findViewById(R.id.recProgress).setVisibility(View.GONE);
                            findViewById(R.id.otpVerificationLayout).setVisibility(View.GONE);
                            findViewById(R.id.resetPassLayout).setVisibility(View.VISIBLE);
                        }else{
                            edtOTP.setError("Code didn't match");
                            edtOTP.requestFocus();
                            Toast.makeText(RecoverPassword.this, task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void resetPassword(){
        final String pas = edtResetPass.getText().toString().trim();
        if(pas.isEmpty()){
            edtResetPass.setError("Enter new password");
            edtResetPass.requestFocus();
            return;
        }
        if(pas.length() < 6) {
            edtResetPass.setError("Password should contain\nat least 6 characters");
            edtResetPass.requestFocus();
            return;
        }

        DatabaseReference r = FirebaseDatabase.getInstance().getReference().child("change request").child(username);

        r.child("tempPhone").setValue(phoneNumber);
        r.child("newPassword").setValue(pas);

        edtResetPass.setVisibility(View.GONE);
        findViewById(R.id.btnResetPass).setVisibility(View.GONE);
        String s = "Your password change request has been sent to Server.\n" +
                "If your phone number matches then\n" +
                "Your password will be changed in 24 hours.\n" +
                "If you can't login in 24 hours, please request change again!";

        textView.setText(s);
        textView.setVisibility(View.VISIBLE);

    }

    private String getEmailFromUsername(String s){
        return s + "@gmail.com";
    }
}