package com.example.booker;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class AccountTypeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_type);
    
        // go to owner home page
        Button ownerButton = findViewById(R.id.ownerButton);
        ownerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goToOwnerHome = new Intent(getApplicationContext(), OwnerHomeActivity.class);
                startActivity(goToOwnerHome);
            }
        });
        
        // go to borrower home page
        Button borrowerButton = findViewById(R.id.borrowerButton);
        borrowerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goToBorrowerHome = new Intent(getApplicationContext(), BorrowerHomeActivity.class);
                startActivity(goToBorrowerHome);
            }
        });

        // set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar3);
        setSupportActionBar(toolbar);
        ActionBar myToolbar = getSupportActionBar();
        myToolbar.setTitle("");
        myToolbar.setDisplayHomeAsUpEnabled(true);

        // toolbar back button
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}