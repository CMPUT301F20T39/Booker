package com.example.booker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {


    /** TODO: CHANGE USERNAME LABELS TO EMAIL LABELS?
     *  I don't know if Firebase's sign in function will work with username. If it does then no need to change.
     */
    // Button signUpButton, signInButton;
    EditText email, password;
    private FirebaseAuth mAuth;
    FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        email = findViewById(R.id.usernameEditText);
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

                /** Straight to Account Type Activity */
                Intent goToAccountType = new Intent(getApplicationContext(), AccountTypeActivity.class);
                startActivity(goToAccountType);



                /** THIS IS A WORKING LOGIN FUNCTION! Implement whenever ready.
                    login requires email, not username.

                 final String Password = password.getText().toString().trim();
                final String Email = email.getText().toString();


                if (TextUtils.isEmpty(Email) || !(Patterns.EMAIL_ADDRESS.matcher(Email).matches())) {
                    email.setError("Please enter the correct email format");
                    return;
                }

                if (TextUtils.isEmpty(Password)) {
                    password.setError("Password is Required");
                    return;
                }

                if (Password.length() < 5) {
                    password.setError("Password Must Be >= 5 Characters");
                    return;
                }

                if (TextUtils.isEmpty(Email)) {
                    Toast.makeText(getApplicationContext(),
                            "Please enter email!!",
                            Toast.LENGTH_LONG)
                            .show();
                    return;
                }

                if (TextUtils.isEmpty(Password)) {
                    Toast.makeText(getApplicationContext(),
                            "Please enter password!!",
                            Toast.LENGTH_LONG)
                            .show();
                    return;
                }

                 // sign in function from
                 // geeks for geeks
                 // https://www.geeksforgeeks.org/user-authentication-using-firebase-in-android/

                // sign-in existing user
                mAuth.signInWithEmailAndPassword(Email, Password)
                        .addOnCompleteListener(
                                new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(
                                            @NonNull Task<AuthResult> task)
                                    {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(getApplicationContext(),
                                                    "Login successful!!",
                                                    Toast.LENGTH_LONG)
                                                    .show();

                                            // if sign-in is successful
                                            // intent to home activity
                                            Intent intent = new Intent(MainActivity.this, AccountTypeActivity.class);
                                            startActivity(intent);
                                        }

                                        else {

                                            // sign-in failed
                                            Toast.makeText(getApplicationContext(),
                                                    "Login failed!!",
                                                    Toast.LENGTH_LONG)
                                                    .show();

                                        }
                                    }
                                });




                 */


            }
        });


    }

}