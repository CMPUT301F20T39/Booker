package com.example.booker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SearchView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class BorrowerSearchActivity extends AppCompatActivity {
    private List<Book> bookList = new ArrayList<>();
    private CollectionReference booksCollection = FirebaseFirestore.getInstance().collection("Books");
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_borrower_search);

        // takes query from borrower home and sets to current query
        searchView = findViewById(R.id.searchView);
        searchView.setQuery(getIntent().getStringExtra("searchQuery"), true);

        RecyclerView rvBookList = findViewById(R.id.recyclerView);

        // connect adapter and layout to recyclerview
        final BorrowerListAdapter adapter = new BorrowerListAdapter(bookList, false);
        rvBookList.setAdapter(adapter);
        rvBookList.setLayoutManager(new LinearLayoutManager(this));

        // return to borrower home on home button click
        ImageButton homeButton = findViewById(R.id.homeButton);
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // query on initial submit
        // TODO query only works on exact match; improve query to partial match
        List<String> filter = Arrays.asList("Available", "Requested"); // whitelist

        Query descriptionQuery = booksCollection
                .whereIn("status", filter)
                .whereEqualTo("description", searchView.getQuery().toString());

        descriptionQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document: task.getResult()) {
                        Book book = document.toObject(Book.class);
                        bookList.add(book);
                    }
                    adapter.notifyDataSetChanged();
                }
            }
        });

        // query on sequential submits
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                List<String> filter = Arrays.asList("Available", "Requested"); // whitelist

                Query descriptionQuery = booksCollection
                        .whereIn("status", filter)
                        .whereEqualTo("description", searchView.getQuery().toString());

                descriptionQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            bookList.clear();
                            for (QueryDocumentSnapshot document: task.getResult()) {
                                Book book = document.toObject(Book.class);
                                bookList.add(book);
                            }
                            adapter.notifyDataSetChanged();
                        }
                    }
                });
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }
}