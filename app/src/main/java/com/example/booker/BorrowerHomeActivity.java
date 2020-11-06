package com.example.booker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BorrowerHomeActivity extends AppCompatActivity {
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
        setContentView(R.layout.activity_borrower_home);

        // initialize chips
        final ChipGroup chipGroup = findViewById(R.id.chipGroup);
        final Chip requestedButton = findViewById(R.id.requestedBttn);
        final Chip acceptedButton = findViewById(R.id.acceptedBttn);
        final Chip borrowedButton = findViewById(R.id.borrowedBttn);

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

        // touching search edit text
        searchViewEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    // change text display to all available
                    String listDisplay = "Displaying all available books";
                    listDisplayTextView.setText(listDisplay);
                    listDisplayTextView.setTextSize(18);

                    // hide chips
                    chipGroup.setVisibility(View.GONE);

                    // show all available books and show request buttons
                    showAllAvailableBooks();
                    borrowerAdapter.setHideButton(false);
                }

            }
        });

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
                if (newText.length() == 0 &&
                !listDisplayTextView.getText().toString().equals("Borrower Home")) {
                    // change text display to titled available
                    String listDisplay = "Displaying all available books";
                    listDisplayTextView.setText(listDisplay);

                    // show all available books
                    showAllAvailableBooks();
                }
                return false;
            }
        });

        // show requests on request chip click
        requestedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                acceptedButton.setChecked(false);
                borrowedButton.setChecked(false);
                showMyRequests();
            }
        });

        // show accepts on accept chip click
        acceptedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestedButton.setChecked(false);
                borrowedButton.setChecked(false);
                showMyAccepts();
            }
        });

        // show borrows on borrow chip click
        borrowedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                acceptedButton.setChecked(false);
                requestedButton.setChecked(false);
                showMyBorrows();
            }
        });

        // home button stuff
        final ImageButton homeButton = findViewById(R.id.homeButton);
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // change text display to my requests
                String myRequestsDisplay = "Borrower Home";
                listDisplayTextView.setTextSize(24);
                listDisplayTextView.setText(myRequestsDisplay);

                // show chips
                chipGroup.setVisibility(View.VISIBLE);

                // close keyboard and reset search text
                searchView.clearFocus();
                searchView.setQuery("", false);

                // user's personal requests list
                requestedButton.performClick();
                requestedButton.setChecked(true);

                // hide buttons
                borrowerAdapter.setHideButton(true);
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
                if (listDisplayTextView.getText().toString().startsWith("Displaying")) {
                    homeButton.performClick();
                }
                else {
                    finish();
                }
            }
        });

    }

    public void showMyRequests() {
        bookList.clear();
        borrowerAdapter.notifyDataSetChanged();

        Query query = firebaseFirestore.collection("Books")
                .whereEqualTo("status", "Requested")
                .whereArrayContains("requesterList", user.getDisplayName());

        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                for (DocumentSnapshot document: value.getDocuments()) {
                    Book book = document.toObject(Book.class);
                    bookList.add(book);

                }
                borrowerAdapter.notifyDataSetChanged();
            }
        });
    }

    public void showMyAccepts() {
        bookList.clear();
        borrowerAdapter.notifyDataSetChanged();

        Query query = firebaseFirestore.collection("Books")
                .whereEqualTo("status", "Accepted")
                .whereArrayContains("requesterList", user.getDisplayName());

        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                for (DocumentSnapshot document: value.getDocuments()) {
                    Book book = document.toObject(Book.class);
                    bookList.add(book);

                }
                borrowerAdapter.notifyDataSetChanged();
            }
        });
    }

    public void showMyBorrows() {
        bookList.clear();
        borrowerAdapter.notifyDataSetChanged();

        Query query = firebaseFirestore.collection("Books")
                .whereEqualTo("status", "Borrowed")
                .whereArrayContains("requesterList", user.getDisplayName());

        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                for (DocumentSnapshot document: value.getDocuments()) {
                    Book book = document.toObject(Book.class);
                    bookList.add(book);

                }
                borrowerAdapter.notifyDataSetChanged();
            }
        });
    }

    public void showAllAvailableBooks() {
        bookList.clear();
        borrowerAdapter.notifyDataSetChanged();

        // filter for only available and requested
        List<String> whitelist = Arrays.asList("Available", "Requested");

        Query query = firebaseFirestore.collection("Books")
                .whereIn("status", whitelist);

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

    public void showSearchedAvailableBooks() {
        bookList.clear();
        borrowerAdapter.notifyDataSetChanged();

        // filter for only available and requested
        List<String> whitelist = Arrays.asList("Available", "Requested");

        Query query = firebaseFirestore.collection("Books")
                .whereIn("status", whitelist);

        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                for (DocumentChange documentChange: value.getDocumentChanges()) {
                    Book book = documentChange.getDocument().toObject(Book.class);

                    // add new books to results
                    if (documentChange.getType() == DocumentChange.Type.ADDED &&
                            book.getTitle().toLowerCase().contains(searchView.getQuery().toString().toLowerCase())) {
                        bookList.add(book);
                    }
                    else if (documentChange.getType() == DocumentChange.Type.ADDED &&
                            book.getAuthor().toLowerCase().contains(searchView.getQuery().toString().toLowerCase())) {
                        bookList.add(book);
                    }
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