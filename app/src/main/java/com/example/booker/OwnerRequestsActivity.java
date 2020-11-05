package com.example.booker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class OwnerRequestsActivity extends AppCompatActivity {

    private Book book;
    private RequestAdapter adapter;
    private RecyclerView rvNameList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_requests);

        book = (Book) getIntent().getSerializableExtra("Book");

        Toolbar toolbar = findViewById(R.id.toolbarRequests);
        setSupportActionBar(toolbar);
        ActionBar myToolbar = getSupportActionBar();
        myToolbar.setDisplayHomeAsUpEnabled(true);
        myToolbar.setTitle("Requests for " + book.getTitle());

        rvNameList = findViewById(R.id.requestsListView);
        adapter = new RequestAdapter(book, this);
        rvNameList.setAdapter(adapter);
        rvNameList.setLayoutManager(new LinearLayoutManager(this));

        // toolbar back button
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }
}
