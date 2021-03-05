package com.brightsky.medicabdriver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;

public class DriverSignup extends AppCompatActivity {

    private EditText emailEditText;
    private EditText passwordEditText;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthStateListener;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_signup);

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        TextView loginPrompt = findViewById(R.id.login_prompt);
        Button mSignUpButton = findViewById(R.id.signup_button);

        mAuth = FirebaseAuth.getInstance();

        firebaseAuthStateListener = firebaseAuth -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            if(user != null) {
                Intent homeIntent = new Intent(DriverSignup.this, DriverHomePage.class);
                startActivity(homeIntent);
                finish();
            }
        };

        mSignUpButton.setOnClickListener(v -> {
            final String email = emailEditText.getText().toString();
            final String password = passwordEditText.getText().toString();

            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(DriverSignup.this, task -> {
                if(!task.isSuccessful()) {
                    Toast.makeText(DriverSignup.this, "SignUp Failed!", Toast.LENGTH_SHORT).show();
                }
                else {
                    if(mAuth.getCurrentUser() != null) {
                        userId = mAuth.getCurrentUser().getUid();
                        DatabaseReference currentUserDBReference = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(userId);
                        currentUserDBReference.setValue(true);
                    }
                }
            });
        });

        loginPrompt.setOnClickListener(v -> {
            Intent signUpIntent = new Intent(DriverSignup.this, DriverLogin.class);
            startActivity(signUpIntent);
            finish();
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(firebaseAuthStateListener);
    }
}