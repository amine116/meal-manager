package com.amine.mealmanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import static com.amine.mealmanager.MainActivity.getTodayDate;

public class ManagerLoginActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText edtEnterUserName, edtEnterPassword;
    private FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager_login);
        ActionBar a = getSupportActionBar();
        if(a != null) a.setTitle("  Manager log in");
        initialize();
    }
    private void initialize(){
        edtEnterPassword = findViewById(R.id.edtEnterPassword);
        edtEnterUserName = findViewById(R.id.edtEnterUserName);
        Button btnLogIn = findViewById(R.id.btnLogIn);
        fAuth = FirebaseAuth.getInstance();
        btnLogIn.setOnClickListener(ManagerLoginActivity.this);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.btnLogIn){
            final String userName = edtEnterUserName.getText().toString().trim(),
                    pass = edtEnterPassword.getText().toString().trim();

            InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(edtEnterUserName.getWindowToken(), 0);
            inputMethodManager.hideSoftInputFromWindow(edtEnterPassword.getWindowToken(), 0);

            findViewById(R.id.btnLogIn).setEnabled(false);
            findViewById(R.id.mLogProgress).setVisibility(View.VISIBLE);

            if(userName.isEmpty()){
                edtEnterUserName.setError("Username required \uD83D\uDE15");
                edtEnterUserName.requestFocus();
                findViewById(R.id.btnLogIn).setEnabled(true);
                findViewById(R.id.mLogProgress).setVisibility(View.GONE);
                return;
            }
            if(pass.isEmpty()){
                edtEnterPassword.setError("Password required \uD83D\uDE15");
                edtEnterPassword.requestFocus();
                findViewById(R.id.btnLogIn).setEnabled(true);
                findViewById(R.id.mLogProgress).setVisibility(View.GONE);
                return;
            }
            if(pass.length() < 6){
                edtEnterPassword.setError("Wrong password. Password consist\n at least 6 characters ");
                edtEnterPassword.requestFocus();
                findViewById(R.id.btnLogIn).setEnabled(true);
                findViewById(R.id.mLogProgress).setVisibility(View.GONE);
                return;
            }

            if(fAuth.getCurrentUser() != null) fAuth.signOut();

            fAuth.signInWithEmailAndPassword(getEmailFromUsername(userName), pass)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                findViewById(R.id.btnLogIn).setEnabled(true);
                                findViewById(R.id.mLogProgress).setVisibility(View.GONE);
                                DatabaseReference r =
                                        FirebaseDatabase.getInstance().getReference()
                                                .child("change request").child("users").child(userName);

                                r.child("lastActivity").setValue(getTodayDate());

                                    /*
                                    r.child("username").setValue(userName);
                                    r.child("currentPassword").setValue(pass);
                                    r.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if(!snapshot.child("newPassword").exists()){
                                                r.child("newPassword").setValue("");
                                            }
                                            else if(snapshot.child("newPassword").getValue(String.class).equals("")) {
                                                r.child("newPassword").setValue("");
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });

                                     */
                                gotToMainActivity();
                                finish();
                            }
                            else{
                                findViewById(R.id.btnLogIn).setEnabled(true);
                                findViewById(R.id.mLogProgress).setVisibility(View.GONE);
                                Toast.makeText(ManagerLoginActivity.this,
                                        task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        }
    }


    private void gotToMainActivity(){
        Intent intent = new Intent(ManagerLoginActivity.this, MainActivity.class);
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