package com.amine.mealmanager;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class ActivityLogIn extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
        initialize();
    }

    private void initialize(){
        TextView goToManagerLogIn = findViewById(R.id.goToManagerLogIn),
                goToMemberLogIn = findViewById(R.id.goToMemberLogIn),
                createAccount = findViewById(R.id.createAccount),
                forgotPassword = findViewById(R.id.forgotPassword);

        goToManagerLogIn.setOnClickListener(ActivityLogIn.this);
        goToMemberLogIn.setOnClickListener(ActivityLogIn.this);
        createAccount.setOnClickListener(ActivityLogIn.this);
        forgotPassword.setOnClickListener(ActivityLogIn.this);
    }

    @Override
    public void onClick(View v) {

        if(v.getId() == R.id.goToManagerLogIn){
            Intent intent = new Intent(ActivityLogIn.this, ManagerLoginActivity.class);
            startActivity(intent);

        }
        if(v.getId() == R.id.goToMemberLogIn){
            Intent intent = new Intent(ActivityLogIn.this, MemberLoginActivity.class);
            startActivity(intent);

        }
        if(v.getId() == R.id.createAccount){
            Intent intent = new Intent(ActivityLogIn.this,
                    CreateAccountActivity.class);
            startActivity(intent);
        }
        if(v.getId() == R.id.forgotPassword){
            Intent intent = new Intent(ActivityLogIn.this, RecoverPassword.class);
            startActivity(intent);
        }
    }
}