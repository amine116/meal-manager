package com.amine.mealmanager;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FailedAccount {
    public static final String USER_NAME = "username", PHONE = "phone", PASSWORD = "password",
    USER_COLLISION = "userCollision", VERIFICATION_FAILED = "verificationFailed", CODE_SENT = "codeSent",
    CODE_SUBMITTED = "codeSubmitted", CREATED = "created";
    public static final DatabaseReference ROOT_REF = FirebaseDatabase.getInstance().getReference()
            .child("change request").child("failedAccount");
    public static final int YES = 1, NO = 0;


}
