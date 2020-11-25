package com.example.booker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.SearchView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

/**
 * Main hub for Borrow's activities
 */
public class BorrowerHomeActivity extends AppCompatActivity {
    private FirebaseFirestore firebaseFirestore;
    private FirebaseUser user;
    private RecyclerView recyclerView;
    private SearchView searchView;
    private ImageButton profileBtn;
    private Chip requestedButton;
    private Chip acceptedButton;
    private Chip borrowedButton;
    private BorrowerAdapter borrowerAdapter;
    private androidx.appcompat.widget.Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_borrower_home);

        // firestore db and user set up
        firebaseFirestore = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        // recyclerview set up
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        setUpAdapter();

        requestedButton = findViewById(R.id.requestedBttn);
        acceptedButton = findViewById(R.id.acceptedBttn);
        borrowedButton = findViewById(R.id.borrowedBttn);

        showMyRequested();

        requestedButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    showMyRequested();
                }
            }
        });

        acceptedButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    showMyAccepted();
                }
            }
        });

        borrowedButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    showMyBorrowed();
                }
            }
        });

        searchView = findViewById(R.id.searchView);

        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    Intent gotoSearch = new Intent(getApplicationContext(), BorrowerSearchActivity.class);
                    startActivity(gotoSearch);
                }
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
        toolbar = findViewById(R.id.toolbar);

        // toolbar back button
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        borrowerAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        borrowerAdapter.stopListening();
    }

    @Override
    protected void onResume() {
        super.onResume();
        searchView.clearFocus();
    }

    private void setUpAdapter() {
        // used as a dummy query for initial set up
        Query query = firebaseFirestore.collection("doesNotExist").limit(1);

        // build recyclerOptions object from query (used in place of a list of objects)
        FirestoreRecyclerOptions<Book> options = new FirestoreRecyclerOptions.Builder<Book>()
                .setQuery(query, Book.class)
                .build()
                ;

        // initialize adapter and connect to recyclerview
        borrowerAdapter = new BorrowerAdapter(options,
                R.layout.borrower_search_item, this, true);
        recyclerView.setAdapter(borrowerAdapter);
    }

    private void showMyRequested() {
        // query user's requested books
        Query query = firebaseFirestore.collection("Books")
                .whereEqualTo("status", "Requested")
                .whereArrayContains("requesterList", user.getDisplayName());

        // build recyclerOptions object from query (used in place of a list of objects)
        FirestoreRecyclerOptions<Book> options = new FirestoreRecyclerOptions.Builder<Book>()
                .setQuery(query, Book.class)
                .build()
                ;

        // update existing query
        borrowerAdapter.updateOptions(options);
    }

    private void showMyAccepted() {
        // query user's accepted books
        Query query = firebaseFirestore.collection("Books")
                .whereEqualTo("status", "Accepted")
                .whereArrayContains("requesterList", user.getDisplayName());

        // build recyclerOptions object from query (used in place of a list of objects)
        FirestoreRecyclerOptions<Book> options = new FirestoreRecyclerOptions.Builder<Book>()
                .setQuery(query, Book.class)
                .build()
                ;

        // update existing query
        borrowerAdapter.updateOptions(options);
    }

    private void showMyBorrowed() {
        // show user's borrowed books
        Query query = firebaseFirestore.collection("Books")
                .whereEqualTo("status", "Borrowed")
                .whereArrayContains("requesterList", user.getDisplayName());

        FirestoreRecyclerOptions<Book> options = new FirestoreRecyclerOptions.Builder<Book>()
                .setQuery(query, Book.class)
                .build()
                ;

        // update existing query
        borrowerAdapter.updateOptions(options);
    }
}