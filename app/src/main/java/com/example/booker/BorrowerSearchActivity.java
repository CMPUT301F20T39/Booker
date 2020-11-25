package com.example.booker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.firestore.SnapshotParser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.Arrays;

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
    private ImageButton homeButton;
    private TextView listDisplayTextView;
    private androidx.appcompat.widget.Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_borrower_search);

        // firestore db and user set up
        firebaseFirestore = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        // recyclerview set up
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        setUpAdapter();

        showAllAvailable();

        listDisplayTextView = findViewById(R.id.listDisplayTextView);
        searchView = findViewById(R.id.searchView);

        // query on submit
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // change text display to titled available
                String listDisplay = "Displaying available \""
                        + searchView.getQuery().toString() + "\" books";
                listDisplayTextView.setText(listDisplay);

                // show searched available books
                showSearchedAvailable();

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
                    showAllAvailable();
                }
                return false;
            }
        });

        // home button stuff
        homeButton = findViewById(R.id.homeButton);

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
        searchView.requestFocus();
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
                R.layout.borrower_search_item, this, false);
        recyclerView.setAdapter(borrowerAdapter);
    }

    private void showAllAvailable() {
        // query user's requested books
        Query query = firebaseFirestore.collection("Books")
                .whereIn("status", Arrays.asList("Available", "Requested"));

        // build recyclerOptions object from query (used in place of a list of objects)
        FirestoreRecyclerOptions<Book> options = new FirestoreRecyclerOptions.Builder<Book>()
                .setQuery(query, Book.class)
                .build()
                ;

        // update existing query
        borrowerAdapter.updateOptions(options);
    }

    private void showSearchedAvailable() {
        // query user's requested books
        Query query = firebaseFirestore.collection("Books")
                .whereIn("status", Arrays.asList("Available", "Requested"));

        // build recyclerOptions object from query (used in place of a list of objects)
        FirestoreRecyclerOptions<Book> options = new FirestoreRecyclerOptions.Builder<Book>()
                .setQuery(query, new SnapshotParser<Book>() {
                    @NonNull
                    @Override
                    public Book parseSnapshot(@NonNull DocumentSnapshot snapshot) {
                        Book book = snapshot.toObject(Book.class);
                        // parse book for partial title match
                        if (book.getTitle().toLowerCase().contains(searchView.getQuery().toString().toLowerCase())) {
                            return book;
                        }
                        // parse book for partial author match
                        else if (book.getAuthor().toLowerCase().contains(searchView.getQuery().toString().toLowerCase())) {
                            return book;
                        }
                        // parse book for partial ISBN match
                        else if (book.getISBN().toLowerCase().contains(searchView.getQuery().toString())) {
                            return book;
                        }
                        // no matches, return a dummy book object
                        return new Book("", "", "", "", "");
                    }
                })
                .build()
                ;

        // update existing query
        borrowerAdapter.updateOptions(options);
    }
}