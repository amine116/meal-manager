package com.amine.mealmanager;

import androidx.annotation.NonNull;
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
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

public class MemberLoginActivity extends AppCompatActivity implements View.OnClickListener {
    private File evidence;
    private EditText edtEnterManagerUsername;
    DatabaseReference r;
    FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_login);

        initialize();
    }

    private void initialize(){
        File root;
        String INFO_FILE = "Info.txt";
        root = getCacheDir();
        evidence = new File(root, INFO_FILE);
        r = FirebaseDatabase.getInstance().getReference();
        fAuth = FirebaseAuth.getInstance();
        if(!root.exists()) root.mkdir();

        try {
            evidence.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        edtEnterManagerUsername = findViewById(R.id.edtEnterManagerUsername);
        Button btnLogIn = findViewById(R.id.btnUseAsMember);
        btnLogIn.setOnClickListener(MemberLoginActivity.this);


    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.btnUseAsMember) {
            findViewById(R.id.btnUseAsMember).setEnabled(false);
            findViewById(R.id.mLogProgress).setVisibility(View.VISIBLE);
            final String managerUsername = edtEnterManagerUsername.getText().toString().trim();

            InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(edtEnterManagerUsername.getWindowToken(), 0);

            if (managerUsername.isEmpty()) {
                edtEnterManagerUsername.setError("Manager's Username is required \uD83D\uDE15");
                edtEnterManagerUsername.requestFocus();
                findViewById(R.id.btnUseAsMember).setEnabled(true);
                findViewById(R.id.mLogProgress).setVisibility(View.GONE);
                return;
            }
            fAuth.createUserWithEmailAndPassword(getEmailFromUsername(managerUsername), "123456")
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if(task.isSuccessful()){
                                edtEnterManagerUsername.setError("Username doesn't exist \uD83E\uDD14");
                                edtEnterManagerUsername.requestFocus();
                                FirebaseUser user = task.getResult().getUser();
                                user.delete();
                                findViewById(R.id.btnUseAsMember).setEnabled(true);
                                findViewById(R.id.mLogProgress).setVisibility(View.GONE);

                            }else if(task.getException() instanceof FirebaseAuthUserCollisionException){
                                PrintWriter pr;
                                try {
                                    pr = new PrintWriter(evidence);
                                    pr.println(managerUsername);
                                    pr.close();
                                    findViewById(R.id.btnUseAsMember).setEnabled(true);
                                    findViewById(R.id.mLogProgress).setVisibility(View.GONE);
                                    gotToMainActivity();
                                    finish();

                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                }
                            }else{
                                findViewById(R.id.btnUseAsMember).setEnabled(true);
                                findViewById(R.id.mLogProgress).setVisibility(View.GONE);
                                Toast.makeText(MemberLoginActivity.this,
                                        task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        }
    }
    private void gotToMainActivity(){
        Intent intent = new Intent(MemberLoginActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }


    private String getEmailFromUsername(String s){
        return s + "@gmail.com";
    }


}