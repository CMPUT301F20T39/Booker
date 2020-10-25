package com.example.booker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SearchView;

import java.util.ArrayList;
import java.util.List;

public class BorrowerSearchActivity extends AppCompatActivity {
    private List<Book> bookList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_borrower_search);

        SearchView searchView = findViewById(R.id.searchView);
        searchView.setQuery(getIntent().getStringExtra("searchQuery"), true);

        ImageButton homeButton = findViewById(R.id.homeButton);
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        RecyclerView rvBookList = findViewById(R.id.recyclerView);

        BorrowerListAdapter adapter = new BorrowerListAdapter(bookList, false);
        rvBookList.setAdapter(adapter);
        rvBookList.setLayoutManager(new LinearLayoutManager(this));

        bookList.add(new Book("test", "test", "test", "test", "test"));
        bookList.add(new Book("test2", "test2", "test2", "test2", "test2"));
        bookList.add(new Book("test3", "test3", "test3", "test3", "test3"));
        adapter.notifyDataSetChanged();
    }
}