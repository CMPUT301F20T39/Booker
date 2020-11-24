package com.example.booker;

import android.app.NotificationChannel;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
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
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Main hub for Owner's activity
 */
public class OwnerHomeActivity extends AppCompatActivity implements AddBookFragment.OnFragmentInteractionListener {

    private static final String TAG = "DEBUG";
    private FirebaseFirestore db;
    private FirebaseUser user;
    private String userEmail = user.getEmail();
    private final CollectionReference bookCollection = db.collection("Books");
    FirebaseStorage storage = FirebaseStorage.getInstance();
    public RecyclerView rvBookList;
    Uri image;
    private ImageView imageView;
    private Book book;
    private final static int REQUEST_CODE = 111;
    HashMap<String, Object> imgString = new HashMap<>();
    private ImageButton scanBtn;
    private BookListAdapter2 bookListAdapter2;
    private Chip availableButton;
    private Chip requestedButton;
    private Chip acceptedButton;
    private Chip borrowedButton;
    private androidx.appcompat.widget.Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_home);

        // firestore db and user set up
        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        // recyclerview set up
        rvBookList = findViewById(R.id.ownerBookListView);
        rvBookList.setHasFixedSize(true);
        rvBookList.setLayoutManager(new LinearLayoutManager(this));

        setUpAdapter();

        availableButton = findViewById(R.id.availableBttn);
        requestedButton = findViewById(R.id.requestedBttn);
        acceptedButton = findViewById(R.id.acceptedBttn);
        borrowedButton = findViewById(R.id.borrowedBttn);

        showMyAvailable();

        availableButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    showMyAvailable();
                }
            }
        });

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

        // set up toolbar
        toolbar = findViewById(R.id.toolbar4);

        // toolbar back button
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Check if there is any change in the status of the books
        bookCollection
                .whereEqualTo("ownerEmail", userEmail)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e);
                            return;
                        }

                        // Checking that query works as expected; Can be removed later
                        List<String> books = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : snapshots) {
                            if (doc.get("title") != null) {
                                books.add(doc.getString("title"));
                            }
                        }

                        Log.d(TAG, "Current books owned by " + user.getDisplayName() + " : " + books);

                        // See changes since the last snapshot
                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            if (dc.getType() == DocumentChange.Type.MODIFIED) {
                                Log.d(TAG, "Book data" + dc.getDocument().getData());
                                Log.d(TAG, "Modified book status:" + dc.getDocument().get("status"));

                            }
                        }
                    }
                });

        // When clicked, the floating action button shows the
        // AddBook dialog.
        final FloatingActionButton addBookBtn = findViewById(R.id.add_book_btn);
        addBookBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                availableButton.performClick();
                new AddBookFragment().show(getSupportFragmentManager(), "ADD_BOOK");
            }
        });

        // scanning stuff
        scanBtn = findViewById(R.id.scanButton);

        // Button takes user to user_profile.java
        scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goToScanner = new Intent(getApplicationContext(), barcodeScanner.class);
                startActivity(goToScanner);
            }
        });
    }

    /**
     * opens book requester intent
     *
     * @param book
     */
    public void createRequestList(Book book) {
        Intent goToRequests = new Intent(getApplicationContext(), OwnerRequestsActivity.class);
        goToRequests.putExtra("Book", book);
        startActivity(goToRequests);
    }

    /**
     * Get selected imageView from adapter and go to image gallery
     *
     * @param intent intent to open image gallery
     * @param view   the selected imageView
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
     *
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
                data.put("coordinates", Arrays.asList()); // no location set

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

    @Override
    protected void onStart() {
        super.onStart();
        bookListAdapter2.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        bookListAdapter2.stopListening();
    }

    private void setUpAdapter() {
        // used as a dummy query for initial set up
        Query query = db.collection("doesNotExist").limit(1);

        // build recyclerOptions object from query (used in place of a list of objects)
        FirestoreRecyclerOptions<Book> options = new FirestoreRecyclerOptions.Builder<Book>()
                .setQuery(query, Book.class)
                .build();

        // initialize adapter and connect to recyclerview
        bookListAdapter2 = new BookListAdapter2(options,
                R.layout.owner_list_content, this);
        rvBookList.setAdapter(bookListAdapter2);
    }

    private void showMyAvailable() {
        // query available books
        Query query = bookCollection
                .whereEqualTo("ownerUsername", user.getDisplayName())
                .whereEqualTo("status", "Available");

        // build recyclerOptions object from query (used in place of a list of objects)
        FirestoreRecyclerOptions<Book> options = new FirestoreRecyclerOptions.Builder<Book>()
                .setQuery(query, Book.class)
                .build();

        // update existing query
        bookListAdapter2.updateOptions(options);
    }

    private void showMyRequested() {
        // query available books
        Query query = bookCollection
                .whereEqualTo("ownerUsername", user.getDisplayName())
                .whereEqualTo("status", "Requested");

        // build recyclerOptions object from query (used in place of a list of objects)
        FirestoreRecyclerOptions<Book> options = new FirestoreRecyclerOptions.Builder<Book>()
                .setQuery(query, Book.class)
                .build();

        // update existing query
        bookListAdapter2.updateOptions(options);
    }

    private void showMyAccepted() {
        // query available books
        Query query = bookCollection
                .whereEqualTo("ownerUsername", user.getDisplayName())
                .whereEqualTo("status", "Accepted");

        // build recyclerOptions object from query (used in place of a list of objects)
        FirestoreRecyclerOptions<Book> options = new FirestoreRecyclerOptions.Builder<Book>()
                .setQuery(query, Book.class)
                .build();

        // update existing query
        bookListAdapter2.updateOptions(options);
    }

    private void showMyBorrowed() {
        // query available books
        Query query = bookCollection
                .whereEqualTo("ownerUsername", user.getDisplayName())
                .whereEqualTo("status", "Borrowed");

        // build recyclerOptions object from query (used in place of a list of objects)
        FirestoreRecyclerOptions<Book> options = new FirestoreRecyclerOptions.Builder<Book>()
                .setQuery(query, Book.class)
                .build();

        // update existing query
        bookListAdapter2.updateOptions(options);
    }

    // Create a Notification Channel for the notification to go through
//    private void createNotificationChannel() {
//        // Create the NotificationChannel, but only on API 26+ because
//        // the NotificationChannel class is new and not in the support library
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            CharSequence name = getString(R.string.channel_name);
//            String description = getString(R.string.channel_description);
//            int importance = NotificationManager.IMPORTANCE_DEFAULT;
//            NotificationChannel channel = new NotificationChannel(NotificationChannel.DEFAULT_CHANNEL_ID, name, importance);
//            channel.setDescription(description);
//            // Register the channel with the system; you can't change the importance
//            // or  other notification behaviours after this
//            NotificationManager notificationManager = getSystemService(NotificationManager.class);
//            notificationManager.createNotificationChannel(channel);
//        }
//    }

    public void createNotification(String requester, String book, String bookAuthor) {
        // Create the intent for the notification tap action
        Intent intent = new Intent(this, OwnerRequestsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        String textTitle = getString(R.string.app_name);
        String textContent = MessageFormat.format("{0} has requested the book {1} by {2}", requester, book, bookAuthor);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NotificationChannel.DEFAULT_CHANNEL_ID)
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle(textTitle)
                .setContentText(textContent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                // automatically remove the notification when a user tap on it
                .setAutoCancel(true);
    }
}