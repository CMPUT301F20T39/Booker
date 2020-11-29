package com.example.booker;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
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
import java.util.Map;
import java.util.Random;

/**
 * Main hub for Owner's activity
 */
public class OwnerHomeActivity extends AppCompatActivity implements AddBookFragment.OnFragmentInteractionListener {
    private static final String TAG = "NOTIF DEBUG"; // tag used for debugging purposes
    private final static int REQUEST_CODE = 111;
    public RecyclerView rvBookList;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    Uri image;
    HashMap<String, Object> imgString = new HashMap<>();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference bookCollection = db.collection("Books");
    private final CollectionReference requestsCollection = db.collection("Requests");
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private String userEmail = user.getEmail();
    private ImageView imageView;
    private ImageButton profileBtn;
    private Book book;
    private ImageButton scanBtn;
    private BookListAdapter bookListAdapter;
    private Chip availableButton;
    private Chip requestedButton;
    private Chip acceptedButton;
    private Chip borrowedButton;
    private androidx.appcompat.widget.Toolbar toolbar;
    private String CHANNEL_ID = "Borrower Requests";
    private ArrayList<String> filters = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_home);

        // firestore db and user set up
        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        // create the notification channel
        createNotificationChannel();

        // recyclerview set up
        rvBookList = findViewById(R.id.ownerBookListView);
        rvBookList.setHasFixedSize(true);
        rvBookList.setLayoutManager(new LinearLayoutManager(this));

        setUpAdapter();

        availableButton = findViewById(R.id.availableBttn);
        requestedButton = findViewById(R.id.requestedBttn);
        acceptedButton = findViewById(R.id.acceptedBttn);
        borrowedButton = findViewById(R.id.borrowedBttn);

        availableButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b && !filters.contains("Available")) {
                    filters.add("Available");
                } else if (!requestedButton.isChecked() && !acceptedButton.isChecked() && !borrowedButton.isChecked()) {
                    // crashes without this case
                } else {
                    filters.remove("Available");
                }
                updateBookFilters();
            }
        });

        requestedButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b && !filters.contains("Requested")) {
                    filters.add("Requested");
                } else if (!availableButton.isChecked() && !acceptedButton.isChecked() && !borrowedButton.isChecked()) {
                    // crashes without this case
                } else {
                    filters.remove("Requested");
                }
                updateBookFilters();
            }
        });

        acceptedButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b && !filters.contains("Accepted")) {
                    filters.add("Accepted");
                } else if (!availableButton.isChecked() && !requestedButton.isChecked() && !borrowedButton.isChecked()) {
                    // crashes without this case
                } else {
                    filters.remove("Accepted");
                }
                updateBookFilters();
            }
        });

        borrowedButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (borrowedButton.isChecked() && !filters.contains("Borrowed")) {
                    filters.add("Borrowed");
                } else if (!requestedButton.isChecked() && !acceptedButton.isChecked() && !availableButton.isChecked()) {
                    // crashes without this case
                } else {
                    filters.remove("Borrowed");
                }
                updateBookFilters();
            }
        });

        checkAll();

        // set up toolbar
        toolbar = findViewById(R.id.toolbar4);

        // toolbar back button
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // profile button takes user to view/edit their profile
        profileBtn = findViewById(R.id.ownerProfile);
        profileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goToProfile = new Intent(getApplicationContext(), user_profile.class);
                goToProfile.putExtra("profileType", "EDIT");
                goToProfile.putExtra("profileEmail", user.getEmail());
                startActivity(goToProfile);
            }
        });
        
        // Check books owned by the user

        requestsCollection
                .whereEqualTo("ownerUsername", user.getDisplayName())
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e);
                            return;
                        }

                        // for every request in the collection of requests
                        for (QueryDocumentSnapshot doc : snapshots) {
                            final String status = doc.getString("status");
                            final String bookTitle = doc.getString("title");
                            final String bookAuthor = doc.getString("author");
                            final boolean notified = doc.getBoolean("notified");

                            Log.d(TAG, doc.getData().toString());

                            // Take the array in the firestore and convert it to a list of strings
                            List<String> requesterList = (List<String>) doc.get("requesterList");
                            String recentRequester = "";

                            // if there is at least one requester
                            // Get the most recent one
                            assert requesterList != null;
                            if (!requesterList.isEmpty()) {
                                recentRequester = requesterList.get(requesterList.size() - 1);
                            }

                            // If one of the books of the user has been requested
                            // and the owner of the book hasn't been notified yet
                            // send a notification
                            assert status != null;
                            if (status.equals("Requested") && !notified) {
                                Log.d(TAG, "the requester is " + recentRequester);
                                Log.d(TAG, "Requested book title is " + bookTitle);
                                createNotification(recentRequester, bookTitle, bookAuthor);

                                requestsCollection.document(bookTitle).update("notified",true);
                            }
                        }

                        // See changes since the last snapshot
//                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
//                            // when document is modified
//                            if (dc.getType() == DocumentChange.Type.MODIFIED) {
//                                Log.d(TAG, "Book data" + dc.getDocument().getData());
//
//                                final String status = dc.getDocument().getString("status");
//                                final String bookTitle = dc.getDocument().getString("title");
//                                final String bookAuthor = dc.getDocument().getString("author");
//                                final boolean notified = dc.getDocument().getBoolean("notified");
//
//                                // Take the array in the firestore and convert it to a list of strings
//                                List<String> requesterList = (List<String>) dc.getDocument().get("requesterList");
//                                String recentRequester = "";
//
//                                // if there is at least one requester
//                                // Get the most recent one
//                                assert requesterList != null;
//                                if (!requesterList.isEmpty()) {
//                                    recentRequester = requesterList.get(requesterList.size() - 1);
//                                }
//
//                                // If one of the books of the user has been requested,
//                                // send a notification
//                                assert status != null;
//                                if (status.equals("Accepted") && notified) {
//                                    // For debugging purposes, make sure that we are getting the right data
//                                    Log.d(TAG, "the requester is " + recentRequester);
//                                    Log.d(TAG, "Requested book title is " + bookTitle);
//                                    requestsCollection.document(bookTitle).update("notified",true);
//
//                                    createNotification(recentRequester, bookTitle, bookAuthor);
//                                }
//                            }
//                        }
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

        // scanning stuff
        scanBtn = findViewById(R.id.scanButton);

        // Button takes user to OwnerScanSelect.java
        scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goToScanner = new Intent(getApplicationContext(), OwnerScanSelect.class);
                startActivity(goToScanner);
            }
        });

    }

    /**
     * opens book requester activity
     *
     * @param book
     */
    public void createRequestList(Book book) {
        Intent goToRequests = new Intent(getApplicationContext(), OwnerRequestsActivity.class);
        goToRequests.putExtra("Book", book);
        startActivity(goToRequests);
    }

    /**
     * Shows full book image in new activity
     *
     * @param book
     */
    public void showPhoto(Book book) {
        Intent goToPhoto = new Intent(getApplicationContext(), ViewPhotoActivity.class);
        goToPhoto.putExtra("Book", book);
        goToPhoto.putExtra("Type", "owner");
        startActivity(goToPhoto);
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
     * @param dialogType determines behavior of dialog
     * @param bookUID    for editing a specific book
     * @param title      title of the book to be added
     * @param author     author of the book
     * @param isbn       isbn of the book
     */
    @Override
    public void onOkPressed(String dialogType, final String bookUID, final String title, final String author, final String isbn) {
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
        bookListAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        bookListAdapter.stopListening();
    }

    private void setUpAdapter() {
        // used as a dummy query for initial set up
        Query query = db.collection("doesNotExist").limit(1);

        // build recyclerOptions object from query (used in place of a list of objects)
        FirestoreRecyclerOptions<Book> options = new FirestoreRecyclerOptions.Builder<Book>()
                .setQuery(query, Book.class)
                .build();

        // initialize adapter and connect to recyclerview
        bookListAdapter = new BookListAdapter(options,
                R.layout.owner_list_content, this);
        rvBookList.setAdapter(bookListAdapter);
    }

    private void checkAll() {
        availableButton.setChecked(true);
        requestedButton.setChecked(true);
        acceptedButton.setChecked(true);
        borrowedButton.setChecked(true);
    }

    private void updateBookFilters() {
        // query available books
        Query query = bookCollection
                .whereEqualTo("ownerUsername", user.getDisplayName())
                .whereIn("status", filters);

        // build recyclerOptions object from query (used in place of a list of objects)
        FirestoreRecyclerOptions<Book> options = new FirestoreRecyclerOptions.Builder<Book>()
                .setQuery(query, Book.class)
                .build();

        // update existing query
        bookListAdapter.updateOptions(options);
    }

    //     Create a Notification Channel for the notification to go through
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or  other notification behaviours after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
            Log.d(TAG, "Notification channel successfully created");
        }
    }

    public void createNotification(String requester, String book, String bookAuthor) {
        // Create the intent for the notification tap action
        Intent intent = new Intent(this, OwnerHomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
        String textTitle = "Book request";
        String textContent = MessageFormat.format("{0} has requested the book {1} by {2}", requester, book, bookAuthor);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle(textTitle)
                .setContentText(textContent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                // automatically remove the notification when a user tap on it
                .setAutoCancel(true);

        // Show the notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());

        Random notification_id = new Random();
        notificationManager.notify(notification_id.nextInt(100), builder.build());
        Log.d(TAG, "Notification created successfully");
    }

}