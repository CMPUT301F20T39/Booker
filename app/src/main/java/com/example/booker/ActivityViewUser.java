package com.example.booker;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class ActivityViewUser extends AppCompatActivity {

    String fullName, email, phone, username;
    TextView nameText, emailText, phoneText, usernameText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_user);

        fullName = (String) getIntent().getSerializableExtra("name");
        email = (String) getIntent().getSerializableExtra("email");
        phone = (String) getIntent().getSerializableExtra("phone");
        username = (String) getIntent().getSerializableExtra("username");

        nameText = findViewById(R.id.nameText);
        emailText = findViewById(R.id.emailText);
        phoneText = findViewById(R.id.phoneText);
        usernameText = findViewById(R.id.usernameText);

        nameText.setText(fullName);
        emailText.setText(email);
        phoneText.setText(phone);
        usernameText.setText(username);

        Toolbar toolbar = findViewById(R.id.user_toolbar);
        setSupportActionBar(toolbar);
        ActionBar myToolbar = getSupportActionBar();
        myToolbar.setDisplayHomeAsUpEnabled(true);
        myToolbar.setTitle("Profile: " + username);

        // toolbar back button
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}