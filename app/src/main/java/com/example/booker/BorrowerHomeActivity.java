package com.example.booker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.Arrays;
import java.util.List;

public class BorrowerHomeActivity extends AppCompatActivity {
    private FirebaseFirestore firebaseFirestore;
    private RecyclerView recyclerView;
    private BorrowerFirestoreRecyclerAdapter adapter;
    private SearchView searchView;
    private ImageButton profileBtn;
    private TextView listDisplayTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_borrower_home);

        // initialize firestore stuff
        firebaseFirestore = FirebaseFirestore.getInstance();
        recyclerView = findViewById(R.id.recyclerView);

        // query root at Books
        Query query = firebaseFirestore.collection("Books");

        // recycler options
        final FirestoreRecyclerOptions<Book> options = new FirestoreRecyclerOptions.Builder<Book>()
                .setQuery(query, Book.class)
                .build();

        // custom firestore recycler adapter
        adapter = new BorrowerFirestoreRecyclerAdapter(options);

        // connect adapter to recyclerview
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // searchview stuff
        searchView = findViewById(R.id.searchView);
        listDisplayTextView = findViewById(R.id.listDisplayTextView);

        // on search bar click
        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                String listDisplay = "Displaying all available books";
                listDisplayTextView.setText(listDisplay);
                listDisplayTextView.setTextSize(18);

                // query root at Books
                Query searchQuery = showAllAvailableBooks();

                // recycler options
                FirestoreRecyclerOptions<Book> searchOptions = new FirestoreRecyclerOptions.Builder<Book>()
                        .setQuery(searchQuery, Book.class)
                        .build();


                adapter.updateOptions(searchOptions);
                adapter.setHideButton(false);
            }
        });

        // query on submit
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String listDisplay = "Displaying available \""
                        + searchView.getQuery().toString() + "\" books";
                listDisplayTextView.setText(listDisplay);

                // query root at Books
                Query searchQuery = showTitledAvailableBooks();

                // recycler options
                FirestoreRecyclerOptions<Book> searchOptions = new FirestoreRecyclerOptions.Builder<Book>()
                        .setQuery(searchQuery, Book.class)
                        .build();

                adapter.updateOptions(searchOptions);

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.length() == 0) {
                    String listDisplay = "Displaying all available books";
                    listDisplayTextView.setText(listDisplay);

                    // query root at Books
                    Query searchQuery = showAllAvailableBooks();

                    // recycler options
                    FirestoreRecyclerOptions<Book> searchOptions = new FirestoreRecyclerOptions.Builder<Book>()
                            .setQuery(searchQuery, Book.class)
                            .build();

                    adapter.updateOptions(searchOptions);
                }
                return false;
            }
        });

        // home button stuff
        ImageButton homeButton = findViewById(R.id.homeButton);
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchView.clearFocus();
                searchView.setQuery("", false);

                adapter.updateOptions(options);

                String myRequestsDisplay = "My Requests";
                listDisplayTextView.setTextSize(24);
                listDisplayTextView.setText(myRequestsDisplay);

                adapter.setHideButton(true);
            }
        });

        // profile button stuff
        profileBtn = findViewById(R.id.profileButton);

        // Button takes user to user_profile.java
        profileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goToProfile = new Intent(getApplicationContext(), user_profile.class);
                startActivity(goToProfile);
            }
        });

    }

    public Query showAllAvailableBooks() {
        List<String> whitelist = Arrays.asList("Available", "Requested");

        Query searchQuery = firebaseFirestore.collection("Books")
                .whereIn("status", whitelist);

        return searchQuery;
    }

    public Query showTitledAvailableBooks() {
        List<String> whitelist = Arrays.asList("Available", "Requested");

        Query searchQuery = firebaseFirestore.collection("Books")
                .whereIn("status", whitelist)
                .whereEqualTo("title", searchView.getQuery().toString());

        return searchQuery;
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }
}