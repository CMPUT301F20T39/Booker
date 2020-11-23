package com.example.booker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Main hub for Borrow's activities
 */
public class BorrowerSearchActivity extends AppCompatActivity {
    private FirebaseFirestore firebaseFirestore;
    private FirebaseUser user;
    private RecyclerView recyclerView;
    private BorrowerAdapter borrowerAdapter;
    private SearchView searchView;
    private ImageButton profileBtn;
    private TextView listDisplayTextView;
    private List<Book> bookList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_borrower_search);

        user = FirebaseAuth.getInstance().getCurrentUser();

        // initialize firestore, recyclerview, and adapter stuff
        firebaseFirestore = FirebaseFirestore.getInstance();
        recyclerView = findViewById(R.id.recyclerView);
        bookList = new ArrayList<>();
        borrowerAdapter = new BorrowerAdapter(R.layout.borrower_search_item, bookList, this);

        // connect adapter to recyclerview
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(borrowerAdapter);

        // searchview stuff
        searchView = findViewById(R.id.searchView);
        listDisplayTextView = findViewById(R.id.listDisplayTextView);

        // get internal edittext from search view (behaves strangely without)
        int id = searchView.getContext().getResources()
                .getIdentifier("android:id/search_src_text", null, null);
        final EditText searchViewEditText = (EditText) searchView.findViewById(id);

        searchViewEditText.requestFocus();
        showAllAvailableBooks();
        borrowerAdapter.setHideButton(false);

        // query on submit
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // change text display to titled available
                String listDisplay = "Displaying available \""
                        + searchView.getQuery().toString() + "\" books";
                listDisplayTextView.setText(listDisplay);

                // show titled available books
                showSearchedAvailableBooks();

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // when search is blank and not on home screen
                if (newText.length() == 0) {
                    // change text display to titled available
                    String listDisplay = "Displaying all available books";
                    listDisplayTextView.setText(listDisplay);

                    // show all available books
                    showAllAvailableBooks();
                }
                return false;
            }
        });

        // home button stuff
        final ImageButton homeButton = findViewById(R.id.homeButton);
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // profile button stuff
        profileBtn = findViewById(R.id.profileButton);

        // Button takes user to user_profile.java
        profileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goToProfile = new Intent(getApplicationContext(), user_profile.class);
                goToProfile.putExtra("profileType", "EDIT");
                goToProfile.putExtra("profileEmail", user.getEmail());
                startActivity(goToProfile);
            }
        });

        // set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
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

    /**
     * show user's available books
     */
    public void showAllAvailableBooks() {
        bookList.clear();
        borrowerAdapter.notifyDataSetChanged();

        // filter for only available and requested
        List<String> whitelist = Arrays.asList("Available", "Requested");

        // query all available (available + requested) books
        Query query = firebaseFirestore.collection("Books")
                .whereIn("status", whitelist);

        // show all available (available + requested) books
        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                for (DocumentChange documentChange: value.getDocumentChanges()) {
                    Book book = documentChange.getDocument().toObject(Book.class);

                    // add new books to results
                    if (documentChange.getType() == DocumentChange.Type.ADDED) {
                        bookList.add(book);
                    }
                }
                borrowerAdapter.notifyDataSetChanged();
            }
        });
    }

    /**
     * show user's requested books using a keyword
     * Bugs: duplicate search results after requesting a book
     */
    public void showSearchedAvailableBooks() {
        bookList.clear();
        borrowerAdapter.notifyDataSetChanged();

        // filter for only available and requested
        List<String> whitelist = Arrays.asList("Available", "Requested");

        // query partial matched titles/authors/ISBNs
        Query query = firebaseFirestore.collection("Books")
                .whereIn("status", whitelist);

        // show partial matched titles/authors/ISBNs
        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                for (DocumentChange documentChange: value.getDocumentChanges()) {
                    Book book = documentChange.getDocument().toObject(Book.class);

                    // partial titles
                    if (documentChange.getType() == DocumentChange.Type.ADDED &&
                            book.getTitle().toLowerCase().contains(searchView.getQuery().toString().toLowerCase())) {
                        bookList.add(book);
                    }
                    // partial authors
                    else if (documentChange.getType() == DocumentChange.Type.ADDED &&
                            book.getAuthor().toLowerCase().contains(searchView.getQuery().toString().toLowerCase())) {
                        bookList.add(book);
                    }
                    // partial ISBNs
                    else if (documentChange.getType() == DocumentChange.Type.ADDED &&
                            book.getISBN().toLowerCase().contains(searchView.getQuery().toString())) {
                        bookList.add(book);
                    }
                }
                borrowerAdapter.notifyDataSetChanged();
            }
        });
    }
}