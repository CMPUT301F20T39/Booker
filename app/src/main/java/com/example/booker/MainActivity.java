package com.example.booker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    // Button signUpButton, signInButton;
    EditText username, password;
    FirebaseAuth mAuth;
    FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        username = findViewById(R.id.usernameEditText);
        password = findViewById(R.id.passwordEditText);

        // database
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        final String TAG = "Sample";

        // Sign Up Button OnClickListener
        Button signUpButton = findViewById(R.id.signUpButton);
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goToSignUp = new Intent(getApplicationContext(), SignUpActivity.class);
                startActivity(goToSignUp);
            }
        });

        // goes directly to home page for now
        Button signInButton = findViewById(R.id.signInButton);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goToAccountType = new Intent(getApplicationContext(), AccountTypeActivity.class);
                startActivity(goToAccountType);
            }
        });


    }

}