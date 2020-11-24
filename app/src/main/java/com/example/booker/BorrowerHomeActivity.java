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
import android.widget.CompoundButton;
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
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Main hub for Borrow's activities
 */
public class BorrowerHomeActivity extends AppCompatActivity {
    private FirebaseFirestore firebaseFirestore;
    private FirebaseUser user;
    private RecyclerView recyclerView;
    private BorrowerAdapter borrowerAdapter;
    private SearchView searchView;
    private ImageButton profileBtn;
    private List<Book> bookList;
    private Chip requestedButton;
    private Chip acceptedButton;
    private Chip borrowedButton;
    private EditText searchViewEditText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_borrower_home);

        // initialize chips
        requestedButton = findViewById(R.id.requestedBttn);
        acceptedButton = findViewById(R.id.acceptedBttn);
        borrowedButton = findViewById(R.id.borrowedBttn);

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

        // get internal edittext from search view (behaves strangely without)
        int id = searchView.getContext().getResources()
                .getIdentifier("android:id/search_src_text", null, null);
        searchViewEditText = (EditText) searchView.findViewById(id);

        // touching search edit text
        searchViewEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    Intent gotoSearch = new Intent(getApplicationContext(), BorrowerSearchActivity.class);
                    startActivity(gotoSearch);
                }

            }
        });

        // show requests on request chip toggle
        requestedButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    showMyRequests();
                }
                else {
                    requestedButton.setSelected(false);
                    requestedButton.setChecked(false);
                    bookList.clear();
                    borrowerAdapter.notifyDataSetChanged();
                }
            }
        });

        // show accepts on accept chip click
        acceptedButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    showMyAccepts();
                }
                else {
                    acceptedButton.setSelected(false);
                    acceptedButton.setChecked(false);
                    bookList.clear();
                    borrowerAdapter.notifyDataSetChanged();
                }
            }
        });

        // show borrows on borrow chip click
        borrowedButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    showMyBorrows();
                }
                else {
                    borrowedButton.setSelected(false);
                    borrowedButton.setChecked(false);
                    bookList.clear();
                    borrowerAdapter.notifyDataSetChanged();
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
     * show user's requested books
     */
    public void showMyRequests() {
        bookList.clear();
        borrowerAdapter.notifyDataSetChanged();

        // query user's requests
        Query query = firebaseFirestore.collection("Books")
                .whereEqualTo("status", "Requested")
                .whereArrayContains("requesterList", user.getDisplayName());

        // show user's requests
        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                for (DocumentChange documentChange: value.getDocumentChanges()) {
                    Book book = documentChange.getDocument().toObject(Book.class);
                    if (documentChange.getType().equals(DocumentChange.Type.ADDED)) {
                        bookList.add(book);
                    }
                }
                borrowerAdapter.notifyDataSetChanged();
            }
        });
    }

    /**
     * show user's accepted books
     */
    public void showMyAccepts() {
        bookList.clear();
        borrowerAdapter.notifyDataSetChanged();

        // query user's accepts
        Query query = firebaseFirestore.collection("Books")
                .whereEqualTo("status", "Accepted")
                .whereArrayContains("requesterList", user.getDisplayName());

        // show user's accepts
        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                for (DocumentChange documentChange: value.getDocumentChanges()) {
                    Book book = documentChange.getDocument().toObject(Book.class);
                    if (documentChange.getType().equals(DocumentChange.Type.ADDED)) {
                        bookList.add(book);
                    }
                }
                borrowerAdapter.notifyDataSetChanged();
            }
        });
    }

    /**
     * show user's borrowed books
     */
    public void showMyBorrows() {
        bookList.clear();
        borrowerAdapter.notifyDataSetChanged();

        // query user's borrows
        Query query = firebaseFirestore.collection("Books")
                .whereEqualTo("status", "Borrowed")
                .whereArrayContains("requesterList", user.getDisplayName());

        // show user's borrows
        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                for (DocumentChange documentChange: value.getDocumentChanges()) {
                    Book book = documentChange.getDocument().toObject(Book.class);
                    if (documentChange.getType().equals(DocumentChange.Type.ADDED)) {
                        bookList.add(book);
                    }
                }
                borrowerAdapter.notifyDataSetChanged();
            }
        });
    }

    public void refreshList() {
        if (requestedButton.isChecked()) {
            requestedButton.setChecked(false);
            requestedButton.setSelected(false);
            requestedButton.setChecked(true);
            requestedButton.setSelected(true);
        }
        else if (acceptedButton.isChecked()) {
            acceptedButton.setChecked(false);
            acceptedButton.setSelected(false);
            acceptedButton.setChecked(true);
            acceptedButton.setSelected(true);
        }
        else if (borrowedButton.isChecked()) {
            borrowedButton.setChecked(false);
            borrowedButton.setSelected(false);
            borrowedButton.setChecked(true);
            borrowedButton.setSelected(true);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // reset to initial home state
        searchViewEditText.clearFocus();
        refreshList();
    }
}