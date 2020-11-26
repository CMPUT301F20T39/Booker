package com.example.booker;

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
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.SearchView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.chip.Chip;
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

import java.text.MessageFormat;
import java.util.ArrayList;
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
    private Chip requestedButton;
    private Chip acceptedButton;
    private Chip borrowedButton;
    private BorrowerAdapter borrowerAdapter;
    private androidx.appcompat.widget.Toolbar toolbar;
    private CollectionReference bookCollection;
    private final String TAG = "NOTIF DEBUG";
    private final String CHANNEL_ID = "Accepted Book Requests";

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

        // create the notification channel
        createNotificationChannel();

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

        bookCollection = firebaseFirestore.collection("Books");
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

                        // Checking that query works as expected; Can be removed later
//                        List<String> books = new ArrayList<>();
//                        for (QueryDocumentSnapshot doc : snapshots) {
//                            if (doc.get("title") != null) {
//                                books.add(doc.getString("title"));
//                            }
//                        }

                        // See changes since the last snapshot
                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            if (dc.getType() == DocumentChange.Type.MODIFIED) {
                                Log.d(TAG, "Book data" + dc.getDocument().getData());
                                Log.d(TAG, "Modified book status:" + dc.getDocument().get("status"));

                                final String status = dc.getDocument().getString("status");
                                final String bookTitle = dc.getDocument().getString("title");
                                final String bookAuthor = dc.getDocument().getString("author");

                                List<String> requesterList = (List<String>) dc.getDocument().get("requesterList");
                                String recentRequester = requesterList.get(requesterList.size() - 1);

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