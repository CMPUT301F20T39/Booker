package com.example.booker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.firestore.SnapshotParser;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Main hub for Borrow's activities
 */
public class BorrowerHomeActivity extends AppCompatActivity {
    private FirebaseFirestore firebaseFirestore;
    private FirebaseUser user;
    private RecyclerView recyclerView;
    private SearchView searchView;
    private ImageButton profileBtn;
    private ChipGroup chipGroup;
    private Chip requestedButton;
    private Chip acceptedButton;
    private Chip borrowedButton;
    private BorrowerAdapter borrowerAdapter;
    private androidx.appcompat.widget.Toolbar toolbar;
    private CollectionReference bookCollection;
    private final String TAG = "NOTIF DEBUG";
    private final String CHANNEL_ID = "Accepted Book Requests";
    private ArrayList<String> filters = new ArrayList<>();
    private TextView listDisplayTextView;
    private ImageButton scanBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_borrower_home);

        listDisplayTextView = findViewById(R.id.listDisplayTextView);

        // firestore db and user set up
        firebaseFirestore = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        bookCollection = firebaseFirestore.collection("Books");

        // recyclerview set up
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // create the notification channel
        createNotificationChannel();

        setUpAdapter();

        chipGroup = findViewById(R.id.chipGroup);
        requestedButton = findViewById(R.id.requestedBttn);
        acceptedButton = findViewById(R.id.acceptedBttn);
        borrowedButton = findViewById(R.id.borrowedBttn);

        requestedButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b && !filters.contains("Requested")) {
                    filters.add("Requested");
                }
                else if (!acceptedButton.isChecked() && !borrowedButton.isChecked()) {
                    // crashes without this case
                }
                else {
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
                }
                else if (!requestedButton.isChecked() && !borrowedButton.isChecked()) {
                    // crashes without this case
                }
                else {
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
                }
                else if (!requestedButton.isChecked() && !acceptedButton.isChecked()) {
                    // crashes without this case
                }
                else {
                    filters.remove("Borrowed");
                }
                updateBookFilters();
            }
        });

        checkAll();

        // set up toolbar
        toolbar = findViewById(R.id.toolbar);

        // toolbar back button
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!listDisplayTextView.getText().toString().equals("Borrower Home")) {
                    homeScreen();
                }
                else {
                    finish();
                }
            }
        });

        searchView = findViewById(R.id.searchView);

        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    searchScreen();
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

        // Checks which requests are not available, i.e requested or accepted
        bookCollection
                .whereNotEqualTo("status", "Available")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e);
                            return;
                        }

                        // See changes since the last snapshot
                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            // If a document has been modified
                            if (dc.getType() == DocumentChange.Type.MODIFIED) {
                                // For debugging purposes, show which book has been modified
                                Log.d(TAG, "Book data" + dc.getDocument().getData());

                                // Get the status, title, author of the book
                                final String status = dc.getDocument().getString("status");
                                final String bookTitle = dc.getDocument().getString("title");
                                final String bookAuthor = dc.getDocument().getString("author");

                                // Take the array in the firestore and convert it to a list of strings
                                List<String> requesterList = (List<String>) dc.getDocument().get("requesterList");
                                String recentRequester = "";

                                // if there is at least one requester
                                // Get the most recent one
                                assert requesterList != null;
                                if (!requesterList.isEmpty()) {
                                    recentRequester = requesterList.get(requesterList.size() - 1);
                                }

                                // if the user is the most recent requester of a book and the request has been accepted
                                // send a notification
                                assert status != null;
                                if (status.equals("Accepted") && recentRequester.equals(user.getDisplayName())) {
                                    Log.d(TAG, "the requester is " + recentRequester);
                                    Log.d(TAG, "Requested book title is " + bookTitle);
                                    createNotification(bookTitle, bookAuthor);
                                }
                            }
                        }
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

    public void viewPhoto(Book book) {
        Intent goToPhoto = new Intent(getApplicationContext(), ViewPhotoActivity.class);
        goToPhoto.putExtra("Book", book);
        goToPhoto.putExtra("Type", "borrower");
        startActivity(goToPhoto);
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
        borrowerAdapter = new BorrowerAdapter(options, R.layout.borrower_search_item, this);
        recyclerView.setAdapter(borrowerAdapter);
    }

    private void checkAll() {
        requestedButton.setChecked(true);
        acceptedButton.setChecked(true);
        borrowedButton.setChecked(true);
    }

    private void updateBookFilters() {
        // query available books
        Query query = bookCollection
                .whereArrayContains("requesterList", user.getDisplayName())
                .whereIn("status", filters);

        // build recyclerOptions object from query (used in place of a list of objects)
        FirestoreRecyclerOptions<Book> options = new FirestoreRecyclerOptions.Builder<Book>()
                .setQuery(query, Book.class)
                .build();

        // update existing query
        borrowerAdapter.updateOptions(options);
    }

    private void homeScreen() {
        searchView.clearFocus();
        searchView.setQuery("", false);
        chipGroup.setVisibility(View.VISIBLE);
        borrowerAdapter.setHideButton(true);
        listDisplayTextView.setTextSize(24.0f);
        listDisplayTextView.setText("Borrower Home");
        updateBookFilters();
    }

    private void searchScreen() {
        listDisplayTextView.setTextSize(18.0f);
        listDisplayTextView.setText("Displaying all available books");
        chipGroup.setVisibility(View.GONE);
        borrowerAdapter.setHideButton(false);
        showAllAvailable();
    }

    private void showAllAvailable() {
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
                        // check for user's books and return a dummy book object
                        if (book.getOwnerUsername().equals(user.getDisplayName())) {
                            return new Book("", "", "", "");
                        }
                        // else, return the book regularly
                        return book;
                    }
                })
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
                        // check for user's books and return a dummy book object
                        if (book.getOwnerUsername().equals(user.getDisplayName())) {
                            return new Book("", "", "", "");
                        } // parse book for partial title, description or ISBN match
                        else if (book.getTitle().toLowerCase().contains(searchView.getQuery().toString().toLowerCase()) ||
                                book.getAuthor().toLowerCase().contains(searchView.getQuery().toString().toLowerCase()) ||
                                book.getISBN().toLowerCase().contains(searchView.getQuery().toString())) {
                            // return book regularly
                            return book;
                        }
                        // no matches, return a dummy book object
                        return new Book("", "", "", "");
                    }
                })
                .build()
                ;

        // update existing query
        borrowerAdapter.updateOptions(options);
    }

    //     Create a Notification Channel for the notification to go through
    //     Important for proper notification display
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);                   // channel name
            String description = getString(R.string.channel_description); // channel description
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            // Create a new notification channel
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or  other notification behaviours after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
            // For debugging purposes
            Log.d(TAG, "Notification channel successfully created");
        }
    }

    /**
     * Function for creating a notification when a request has been accepted
     * @param bookTitle  Title of the book which was requested
     * @param bookAuthor Author of the book
     */
    public void createNotification(String bookTitle, String bookAuthor) {
        // Create the intent for the notification tap action
        Intent intent = new Intent(this, BorrowerHomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
        String textTitle = "Request accepted";
        String textContent = MessageFormat.format("Your request for the book {0} by {1} has been accepted.", bookTitle, bookAuthor);

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