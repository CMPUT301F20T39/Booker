package com.example.booker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class AccountTypeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_type);

        // go to borrower home page
        Button borrowerButton = findViewById(R.id.borrowerButton);
        borrowerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goToBorrowerHome = new Intent(getApplicationContext(), BorrowerHomeActivity.class);
                startActivity(goToBorrowerHome);
            }
        });
    }
}