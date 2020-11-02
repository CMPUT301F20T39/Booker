package com.example.booker;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class OwnerHomeActivity extends AppCompatActivity implements AddBookFragment.OnFragmentInteractionListener {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private String userEmail = user.getEmail();
    private final CollectionReference bookCollection = db.collection("Users").document(userEmail).collection("Books");


    private List<Book> bookList = new ArrayList<Book>();
    private ArrayList<Book> filteredBooks = new ArrayList<>();
    private BookListAdapter adapter;
    private ImageButton profileBtn;
    private RecyclerView rvBookList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_home);


        rvBookList = findViewById(R.id.ownerBookListView);

        final BookListAdapter adapter = new BookListAdapter(bookList);
        rvBookList.setAdapter(adapter);
        rvBookList.setLayoutManager(new LinearLayoutManager(this));

        profileBtn = findViewById(R.id.profileButton);


        bookCollection.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException error) {
                bookList.clear();
                assert queryDocumentSnapshots != null;
                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    Book aBook = doc.toObject(Book.class);
                    bookList.add(aBook);
                }
                adapter.notifyDataSetChanged();
            }
        });



//
//		db.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//			@Override
//			public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
//				if (queryDocumentSnapshots.isEmpty()) {
//					Log.d("FIRESTORE","no documents found");
//				} else {
//					for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
//						Book aBook = doc.toObject(Book.class);
//						bookList.add(aBook);
//					}
//
//				}
//			}
//		});

        // When clicked, the floating action button shows the
        // AddBook dialog.
        final FloatingActionButton addBookBtn = findViewById(R.id.add_book_btn);
        addBookBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AddBookFragment().show(getSupportFragmentManager(), "ADD_BOOK");
            }
        });


        final Chip availableButton = findViewById(R.id.availableBttn);
        availableButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            /**
             * shows available books
             */
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                showAvailableBooks();
            }
        });


        final Chip requestedButton = findViewById(R.id.requestedBttn);
        requestedButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            /**
             * shows requested books
             *
             */
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                showRequestedBooks();
            }
        });

        final Chip acceptedButton = findViewById(R.id.acceptedBttn);
        acceptedButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            /**
             * shows accepted books
             */
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                showAcceptedBooks();
            }
        });

        final Chip borrowedButton = findViewById(R.id.borrowedBttn);
        borrowedButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                showBorrowedBooks();
            }
        });


        // Button takes user to com.example.booker.user_profile.java
        /** profileBtn.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
        Intent goToProfile = new Intent(getApplicationContext(), user_profile.class);
        startActivity(goToProfile);
        }
        }); */

    }

    /**
     * Method associated with the OK button in the Dialog button
     * Add the book to Firestore
     * Used in AddBookFragment.java
     *
     * @param title       title of the book to be added
     * @param author      author of the book
     * @param isbn        isbn of the book
     * @param description description of the book
     */
    @Override
    public void onOkPressed(String title, String author, String isbn, String description) {
        final String TAG = "Add Book method";   // just a tag for debugging purposes

        HashMap<String, String> data = new HashMap<>(); // a data structure for adding info to the db

        // generate a UID; see method at the bottom for details
        String UID = generateUID();

        // check that those three fields are not empty
        // TODO Needs to have error checking that inputs are proper (shows red text and enforce input
        //  where mandatory)
        if (title.length() > 0 && author.length() > 0 && isbn.length() > 0) {
            data.put("ISBN", isbn);
            data.put("title", title);
            data.put("author", author);
            data.put("status", "Available");
            data.put("UID", UID);
            data.put("ownerUsername", user.getDisplayName());
        }

        // UID is randomly generated for the document/collection
        // then all the book info is put within it
        bookCollection
                .document(UID)
                .set(data)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Data has been added successfully");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Data could not be added!" + e.toString());
                    }
                });
    }

    public void showAvailableBooks() {

        adapter = (BookListAdapter) rvBookList.getAdapter();
        List<String> filter = Collections.singletonList("Available"); // whitelist

        Query titleQuery = bookCollection
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

    public void showRequestedBooks() {
        adapter = (BookListAdapter) rvBookList.getAdapter();
        List<String> filter = Collections.singletonList("Requested"); // whitelist

        Query titleQuery = bookCollection
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

    public void showAcceptedBooks() {
        adapter = (BookListAdapter) rvBookList.getAdapter();
        List<String> filter = Collections.singletonList("Accepted"); // whitelist

        Query titleQuery = bookCollection
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

    public void showBorrowedBooks() {
        adapter = (BookListAdapter) rvBookList.getAdapter();
        List<String> filter = Collections.singletonList("Borrowed"); // whitelist

        Query titleQuery = bookCollection
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

    /**
     * Generates a random, unique* document ID
     *
     * https://github.com/firebase/firebase-android-sdk/issues/408
     * Firestore itself doesn't actually generate a unique UID. It generates a
     * statistically rare alphanumeric String sequence.
     *
     * @return UID
     *         A unique* String sequence of random alphanumeric characters.
     */
    public String generateUID() {
        int length = 20;
        List<String> potentialCharacters = new ArrayList<>();

        for (char chr = '0'; chr <= '9'; chr++) {
            potentialCharacters.add(String.valueOf(chr));
        }
        for (char chr = 'A'; chr <= 'Z'; chr++) {
            potentialCharacters.add(String.valueOf(chr));
        }
        for (char chr = 'a'; chr <= 'z'; chr++) {
            potentialCharacters.add(String.valueOf(chr));
        }

        int range = 1000000; // just to be safe
        String UID = "";
        for (int chr = 0; chr < length; chr++) {
            int randomIndex = (int) ((Math.random() * range) % potentialCharacters.size());
            UID = UID.concat(potentialCharacters.get(randomIndex));
        }

        return UID;
    }
}