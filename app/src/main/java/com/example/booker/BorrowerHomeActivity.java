package com.example.booker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.SearchView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class BorrowerHomeActivity extends AppCompatActivity {
    private List<Book> bookList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_borrower_home);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        final String TAG = "Sample";

        // goes to search activity on search bar press
        final SearchView searchView = findViewById(R.id.searchView);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Intent goToSearch = new Intent(getApplicationContext(), BorrowerSearchActivity.class);
                String searchQuery = searchView.getQuery().toString();
                goToSearch.putExtra("searchQuery", searchQuery);
                startActivityForResult(goToSearch, 0);
                searchView.setQuery("", false);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        RecyclerView rvBookList = findViewById(R.id.recyclerView);

        BorrowerListAdapter adapter = new BorrowerListAdapter(bookList, true);
        rvBookList.setAdapter(adapter);
        rvBookList.setLayoutManager(new LinearLayoutManager(this));

        bookList.add(new Book("test", "test", "test", "test", "test"));
        bookList.add(new Book("test2", "test2", "test2", "test2", "test2"));
        adapter.notifyDataSetChanged();



    }
}