package com.example.booker;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Main hub for Owner's activity
 */
public class OwnerHomeActivity extends AppCompatActivity implements AddBookFragment.OnFragmentInteractionListener {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private String userEmail = user.getEmail();
    private final CollectionReference bookCollection = db.collection("Books");
    FirebaseStorage storage = FirebaseStorage.getInstance();
    public List<Book> bookList = new ArrayList<Book>();
    private BookListAdapter adapter;
    private ImageButton profileBtn;
    public RecyclerView rvBookList;
    Uri image;
    private ImageView imageView;
    private Book book;
    private final static int REQUEST_CODE = 111;
    HashMap<String, Object> imgString = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_home);

        rvBookList = findViewById(R.id.ownerBookListView);

        // connect adapter
        final BookListAdapter adapter = new BookListAdapter(bookList, this);
        rvBookList.setAdapter(adapter);
        rvBookList.setLayoutManager(new LinearLayoutManager(this));

        profileBtn = findViewById(R.id.profileButton);

        // get owner's books
        bookCollection.whereEqualTo("ownerUsername", user.getDisplayName())
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
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

        // set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar4);
        setSupportActionBar(toolbar);
        ActionBar myToolbar = getSupportActionBar();
        myToolbar.setTitle("Owner Home");
        myToolbar.setDisplayHomeAsUpEnabled(true);
        // toolbar back button
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // When clicked, the floating action button shows the
        // AddBook dialog.
        final FloatingActionButton addBookBtn = findViewById(R.id.add_book_btn);
        addBookBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AddBookFragment().show(getSupportFragmentManager(), "ADD_BOOK");
            }
        });

        // initialize chips
        final Chip availableButton = findViewById(R.id.availableBttn);
        final Chip requestedButton = findViewById(R.id.requestedBttn);
        final Chip acceptedButton = findViewById(R.id.acceptedBttn);
        final Chip borrowedButton = findViewById(R.id.borrowedBttn);

        // toggled available chip
        availableButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            /**
             * shows available books
             */
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                bookList.clear();
                if (availableButton.isChecked())
                    showAvailableBooks();
                if (requestedButton.isChecked())
                    showRequestedBooks();
                if (acceptedButton.isChecked())
                    showAcceptedBooks();
                if (borrowedButton.isChecked())
                    showBorrowedBooks();
            }
        });

        // toggled requested chip
        requestedButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            /**
             * shows requested books
             *
             */
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                bookList.clear();
                if (availableButton.isChecked())
                    showAvailableBooks();
                if (requestedButton.isChecked())
                    showRequestedBooks();
                if (acceptedButton.isChecked())
                    showAcceptedBooks();
                if (borrowedButton.isChecked())
                    showBorrowedBooks();

            }
        });

        // toggled accepted chip
        acceptedButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            /**
             * shows accepted books
             */
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                bookList.clear();
                if (availableButton.isChecked())
                    showAvailableBooks();
                if (requestedButton.isChecked())
                    showRequestedBooks();
                if (acceptedButton.isChecked())
                    showAcceptedBooks();
                if (borrowedButton.isChecked())
                    showBorrowedBooks();
            }
        });

        // toggled borrowed chip
        borrowedButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                bookList.clear();
                if (availableButton.isChecked())
                    showAvailableBooks();
                if (requestedButton.isChecked())
                    showRequestedBooks();
                if (acceptedButton.isChecked())
                    showAcceptedBooks();
                if (borrowedButton.isChecked())
                    showBorrowedBooks();
            }
        });

    }

    /**
     * opens book requester intent
     * @param book
     */
    public void createRequestList(Book book) {
        Intent goToRequests = new Intent(getApplicationContext(), OwnerRequestsActivity.class);
        goToRequests.putExtra("Book", book);
        startActivity(goToRequests);
    }

    /**
     * Get selected imageView from adapter and go to image gallery
     * @param intent intent to open image gallery
     * @param view the selected imageView
     */
    public void selectImage(Intent intent, ImageView view, Book book) {
        imageView = view;
        this.book = book;
        startActivityForResult(Intent.createChooser(intent, "Choose a photo"), REQUEST_CODE);
    }

    /**
     * Retrieves chosen photo from image gallery and sets as book photo in Owner menu.
     * Adds image details to book document in firestore and adds image to firebase storage
     * in a folder titled <username> of the owner.
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        StorageReference storageRef = storage.getReference(user.getDisplayName() + "/" + book.getTitle());
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK && data != null)
            image = data.getData();
            storageRef.putFile(image);
            imgString.put("imageURI", image.toString());
            bookCollection.document(book.getUID()).update(imgString);
            imageView.setImageURI(image);
    }

    /**
     * Method associated with the OK button in the Dialog fragment
     * Add the book to Firestore
     * Used in AddBookFragment.java
     *
     * @param dialogType  determines behavior of dialog
     * @param bookUID     for editing a specific book
     * @param title       title of the book to be added
     * @param author      author of the book
     * @param isbn        isbn of the book
     * @param description description of the book
     */
    @Override
    public void onOkPressed(String dialogType, final String bookUID, final String title, final String author, final String isbn, String description) {
        final String TAG = "Add Book method";   // just a tag for debugging purposes

        HashMap<String, Object> data = new HashMap<>(); // a data structure for adding info to the db

        // check that those three fields are not empty
        // TODO Needs to have error checking that inputs are proper (shows red text and enforce input
        //  where mandatory)

        if ((title.length() > 0 && author.length() > 0 && isbn.length() > 0)) {
            // adding book
            if (dialogType.equals("Add Book")) {
                data.put("ISBN", isbn);
                data.put("title", title);
                data.put("author", author);
                data.put("status", "Available");
                data.put("UID", bookUID);
                data.put("ownerUsername", user.getDisplayName()); // TODO: (from Matthew) There seems to be a problem with assigning an owner username to a book
                data.put("ownerEmail", user.getEmail());
                data.put("requesterList", Arrays.asList()); // allows a user to be the 0th index instead of an empty string
                data.put("imageURI", "");

                // UID is randomly generated for the document/collection
                // then all the book info is put within it
                bookCollection
                        .document(bookUID)
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
            // editing book
            else {
                Query query = bookCollection.whereEqualTo("UID", bookUID);
                query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        Book book = task.getResult().getDocuments().get(0).toObject(Book.class);
                        book.setISBN(isbn);
                        book.setTitle(title);
                        book.setAuthor(author);
                        bookCollection.document(bookUID)
                                .set(book.getDataHashMap());
                    }
                });
            }

        }


    }

    /**
     * shows all owner's available books
     */
    public void showAvailableBooks() {
        adapter = (BookListAdapter) rvBookList.getAdapter();
        List<String> filter = Collections.singletonList("Available"); // whitelist

        // query available books
        Query titleQuery = bookCollection
                .whereEqualTo("ownerUsername", user.getDisplayName())
                .whereIn("status", filter);

        // show available books
        titleQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
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
     * shows all owner's requested books
     */
    public void showRequestedBooks() {
        adapter = (BookListAdapter) rvBookList.getAdapter();
        List<String> filter = Collections.singletonList("Requested"); // whitelist

        // query requested books
        Query titleQuery = bookCollection
                .whereEqualTo("ownerUsername", user.getDisplayName())
                .whereIn("status", filter);

        // show requested books
        titleQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
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
     * shows all owner's accepted books
     */
    public void showAcceptedBooks() {
        adapter = (BookListAdapter) rvBookList.getAdapter();
        List<String> filter = Collections.singletonList("Accepted"); // whitelist

        // query available books
        Query titleQuery = bookCollection
                .whereEqualTo("ownerUsername", user.getDisplayName())
                .whereIn("status", filter);

        // show available books
        titleQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
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
     * shows all owner's borrowed books
     */
    public void showBorrowedBooks() {
        adapter = (BookListAdapter) rvBookList.getAdapter();
        List<String> filter = Collections.singletonList("Borrowed"); // whitelist

        // query borrowed books
        Query titleQuery = bookCollection
                .whereEqualTo("ownerUsername", user.getDisplayName())
                .whereIn("status", filter);

        // show borrowed books
        titleQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
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