package com.brightsky.medicabdriver;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class DriverLogin extends AppCompatActivity {

    private EditText emailEditText;
    private EditText passwordEditText;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_login);

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        TextView signUpPrompt = findViewById(R.id.signup_prompt);
        Button mLoginButton = findViewById(R.id.login_button);

        mAuth = FirebaseAuth.getInstance();

        firebaseAuthStateListener = firebaseAuth -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            if(user != null) {
                Intent homeIntent = new Intent(DriverLogin.this, DriverHomePage.class);
                startActivity(homeIntent);
                finish();
            }
        };

        mLoginButton.setOnClickListener(v -> {
            final String email = emailEditText.getText().toString();
            final String password = passwordEditText.getText().toString();

            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(DriverLogin.this, task -> {
                if(!task.isSuccessful()) {
                    Toast.makeText(DriverLogin.this, "Login Failed!", Toast.LENGTH_SHORT).show();
                }
            });
        });

        signUpPrompt.setOnClickListener(v -> {
            Intent signUpIntent = new Intent(DriverLogin.this, DriverSignup.class);
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