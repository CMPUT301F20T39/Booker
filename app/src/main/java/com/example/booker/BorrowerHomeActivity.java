package com.example.booker;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
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

        user = FirebaseAuth.getInstance().getCurrentUser();

        // initialize firestore, recyclerview, and adapter stuff
        firebaseFirestore = FirebaseFirestore.getInstance();
        recyclerView = findViewById(R.id.recyclerView);
        bookList = new ArrayList<>();
        borrowerAdapter = new BorrowerAdapter(R.layout.borrower_search_item, bookList);

        // connect adapter to recyclerview
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(borrowerAdapter);

        // user's personal requests list
        showMyRequests();

        // searchview stuff
        searchView = findViewById(R.id.searchView);
        listDisplayTextView = findViewById(R.id.listDisplayTextView);

        // get internal edittext from search view (behaves strangely without)
        int id = searchView.getContext().getResources()
                .getIdentifier("android:id/search_src_text", null, null);
        final EditText searchViewEditText = (EditText) searchView.findViewById(id);

        // touching search edit text
        searchViewEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // change text display to all available
                String listDisplay = "Displaying all available books";
                listDisplayTextView.setText(listDisplay);
                listDisplayTextView.setTextSize(18);

                // show all available books and show request buttons
                showAllAvailableBooks();
                borrowerAdapter.setHideButton(false);

                return false;
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
                showTitledAvailableBooks();

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // when search is blank and not on home screen
                if (newText.length() == 0 &&
                !listDisplayTextView.getText().toString().equals("My Requests")) {
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
        ImageButton homeButton = findViewById(R.id.homeButton);
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // change text display to my requests
                String myRequestsDisplay = "My Requests";
                listDisplayTextView.setTextSize(24);
                listDisplayTextView.setText(myRequestsDisplay);

                // close keyboard and reset search text
                searchView.clearFocus();
                searchView.setQuery("", false);

                // user's personal requests list
                showMyRequests();

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
                startActivity(goToProfile);
            }
        });



    }


    public void showMyRequests() {
        bookList.clear();

        Query query = firebaseFirestore.collection("Books")
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
                    // don't add modified books back to results, instead update their old position
                    if (documentChange.getType() == DocumentChange.Type.MODIFIED) {
                        for (int i = 0; i < bookList.size(); i ++) {
                            if (bookList.get(i).getUID().equals(book.getUID())) {
                                bookList.set(i, book);
                            }
                        }
                    }
                }
                borrowerAdapter.notifyDataSetChanged();
            }
        });
    }

    public void showTitledAvailableBooks() {
        bookList.clear();

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
                    // don't add modified books back to results, instead update their old position
                    if (documentChange.getType() == DocumentChange.Type.MODIFIED) {
                        for (int i = 0; i < bookList.size(); i ++) {
                            if (bookList.get(i).getUID().equals(book.getUID())) {
                                bookList.set(i, book);
                            }
                        }
                    }
                }
                borrowerAdapter.notifyDataSetChanged();
            }
        });
    }
}