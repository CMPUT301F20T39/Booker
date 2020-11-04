package com.example.booker;

import android.os.Bundle;
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
        adapter = new RequestAdapter(book);
        rvNameList.setAdapter(adapter);
        rvNameList.setLayoutManager(new LinearLayoutManager(this));

    }
}
