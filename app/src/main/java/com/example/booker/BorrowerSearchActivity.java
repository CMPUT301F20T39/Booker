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
import android.widget.TextView;

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
    private BorrowerListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_borrower_search);

        searchView = findViewById(R.id.searchView);
        searchView.requestFocus(); // get keyboard

        final TextView displayingTextView = findViewById(R.id.displayingTextView);

        RecyclerView rvBookList = findViewById(R.id.recyclerView);

        // connect adapter and layout to recyclerview
        adapter = new BorrowerListAdapter(bookList, false);
        rvBookList.setAdapter(adapter);
        rvBookList.setLayoutManager(new LinearLayoutManager(this));

        // initially display all available books
        booksCollection.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                showAllAvailableBooks();
            }
        });

        // return to borrower home on home button click
        ImageButton homeButton = findViewById(R.id.homeButton);
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // query on submit
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                String displayText = "Displaying available \""
                        + searchView.getQuery().toString() + "\" books";
                displayingTextView.setText(displayText);

                showSearchedBooks();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.length() == 0) {
                    String displayText = "Displaying all available books";
                    displayingTextView.setText(displayText);
                    showAllAvailableBooks();
                }
                return false;
            }
        });
    }

    // displays searched books
    public void showSearchedBooks() {
        List<String> filter = Arrays.asList("Available", "Requested"); // whitelist

        Query titleQuery = booksCollection
                .whereIn("status", filter)
                .whereEqualTo("title", searchView.getQuery().toString());

        titleQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
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
    }

    // displays all available books
    public void showAllAvailableBooks() {
        List<String> filter = Arrays.asList("Available", "Requested"); // whitelist

        Query titleQuery = booksCollection
                .whereIn("status", filter);

        titleQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
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
    }
}