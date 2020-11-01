package com.example.booker;

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

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BorrowerHomeActivity extends AppCompatActivity {
    private FirebaseFirestore firebaseFirestore;
    private RecyclerView recyclerView;
    private GeneralBorrowerAdapter generalAdapter;
    private PartialBorrowerAdapter partialAdapter;
    private SearchView searchView;
    private ImageButton profileBtn;
    private TextView listDisplayTextView;
    private List<Book> bookList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_borrower_home);

        bookList = new ArrayList<>();

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
        generalAdapter = new GeneralBorrowerAdapter(options);

        // connect adapter to recyclerview
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(generalAdapter);

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


                recyclerView.setAdapter(generalAdapter);

                // query root at Books
                Query searchQuery = showAllAvailableBooks();

                // recycler options
                FirestoreRecyclerOptions<Book> searchOptions = new FirestoreRecyclerOptions.Builder<Book>()
                        .setQuery(searchQuery, Book.class)
                        .build();


                generalAdapter.updateOptions(searchOptions);
                generalAdapter.setHideButton(false);
            }
        });

        // query on submit
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String listDisplay = "Displaying available \""
                        + searchView.getQuery().toString() + "\" books";
                listDisplayTextView.setText(listDisplay);

                // custom firestore recycler adapter
                partialAdapter = new PartialBorrowerAdapter(R.layout.borrower_search_item, bookList);

                // connect adapter to recyclerview
                recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                recyclerView.setAdapter(partialAdapter);

                // query root at Books
                showTitledAvailableBooks();

                partialAdapter.setHideButton(false);

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.length() == 0) {
                    String listDisplay = "Displaying all available books";
                    listDisplayTextView.setText(listDisplay);

                    recyclerView.setAdapter(generalAdapter);

                    // query root at Books
                    Query searchQuery = showAllAvailableBooks();

                    // recycler options
                    FirestoreRecyclerOptions<Book> searchOptions = new FirestoreRecyclerOptions.Builder<Book>()
                            .setQuery(searchQuery, Book.class)
                            .build();

                    generalAdapter.updateOptions(searchOptions);
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

                recyclerView.setAdapter(generalAdapter);

                generalAdapter.updateOptions(options);

                String myRequestsDisplay = "My Requests";
                listDisplayTextView.setTextSize(24);
                listDisplayTextView.setText(myRequestsDisplay);

                generalAdapter.setHideButton(true);
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

    public void showTitledAvailableBooks() {
        bookList.clear();

        List<String> whitelist = Arrays.asList("Available", "Requested");

        Query query = firebaseFirestore.collection("Books")
                .whereIn("status", whitelist);

        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                for (DocumentChange document: value.getDocumentChanges()) {
                    Book book = document.getDocument().toObject(Book.class);
                    if (document.getType() == DocumentChange.Type.ADDED &&
                            book.getTitle().toLowerCase().contains(searchView.getQuery().toString().toLowerCase())) {
                        bookList.add(book);
                    }
                    // when the status of a book goes from "Available" to "Requested"
                    if (document.getType() == DocumentChange.Type.MODIFIED &&
                            book.getTitle().toLowerCase().contains(searchView.getQuery().toString().toLowerCase())) {
                        // take user back to all available book search
                        searchView.setQuery("", false);
                    }
                }
                partialAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        generalAdapter.stopListening();
    }

    @Override
    protected void onStart() {
        super.onStart();
        generalAdapter.startListening();
    }
}