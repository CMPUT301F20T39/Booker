package com.example.booker;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthMultiFactorException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.MultiFactorResolver;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.auth.User;

import java.util.HashMap;
import java.util.Map;


public class SignUpActivity extends AppCompatActivity {

    EditText name, email, phone, username, password;
    Button signUpBtn;
    private FirebaseAuth mAuth;
    FirebaseFirestore db;
    private boolean validInput;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        name = findViewById(R.id.editTextName);
        email = findViewById(R.id.editTextEmail);
        phone = findViewById(R.id.editTextPhone);
        username = findViewById(R.id.editTextUsername);
        password = findViewById(R.id.editTextPassword);
        signUpBtn = findViewById(R.id.signUpBtn);

        Toolbar toolbar = findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);
        ActionBar myToolbar = getSupportActionBar();
        myToolbar.setDisplayHomeAsUpEnabled(true);
        myToolbar.setTitle("Sign Up");

        // database
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();


        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String Email = email.getText().toString().trim();
                final String FullName = name.getText().toString().trim();
                final String Password = password.getText().toString().trim();
                final String Phone = phone.getText().toString().trim();
                final String Username = username.getText().toString();

                validInput = true;

                if (TextUtils.isEmpty(Username)) {
                    username.setError("Must choose a valid username");
                    if (validInput)
                        username.requestFocus();
                    validInput = false;
                }
                else {
                    DocumentReference doc = db.collection("Users").document(Username);
                    doc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot snapshot = task.getResult();
                                if (snapshot.exists()) {
                                    username.setError("Username already exists");
                                    if (validInput)
                                        username.requestFocus();
                                    validInput = false;
                                }
                            }
                        }
                    });
                }

                if (TextUtils.isEmpty(FullName)) {
                    name.setError("Name is Required");
                    if (validInput)
                        name.requestFocus();
                    validInput = false;
                }

                if (TextUtils.isEmpty(Email) || !(Patterns.EMAIL_ADDRESS.matcher(Email).matches())) {
                    if (TextUtils.isEmpty(Email))
                        email.setError("Please enter the Email address");
                    else
                        email.setError("Please enter the correct format");
                    if (validInput)
                        email.requestFocus();
                    validInput = false;
                }

                if (TextUtils.isEmpty(Phone) || Phone.length() != 10) {
                    phone.setError("A 10-digit phone number is required");
                    if (validInput)
                        phone.requestFocus();
                    validInput = false;
                }

                if (Password.length() < 5) {
                    password.setError("Password must at least have 5 characters");
                    if (validInput)
                        password.requestFocus();
                    validInput = false;
                }

                if (!validInput) {
                    return;
                }

                /* Create user account and add data to database */

                mAuth.createUserWithEmailAndPassword(Email, Password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            Toast.makeText(SignUpActivity.this, "It worked! :D", Toast.LENGTH_SHORT).show();
                            // Add data to database

                            FirebaseUser user = mAuth.getCurrentUser();

                            // Update the DisplayName
                            UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(FullName)
                                    .build();

                            user.updateProfile(profileChangeRequest);

                            // get user ID
                            String ID = user.getUid();

                            Map<String, Object> data = new HashMap<>();
                            data.put("Email", Email);
                            data.put("Name", FullName);
                            data.put("Phone", Phone);
                            data.put("Username", Username);

                            db.collection("Users").document(username.getText().toString())
                                    .set(data)
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.d("ERROR", e.getMessage());
                                        }
                                    });

                            Toast.makeText(SignUpActivity.this, "Successfully Registered", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(SignUpActivity.this, "Registration Failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }//OnClick
        });//signUpBtn OnClickListener
    }//OnCreate()
}//Class signUpActivity
