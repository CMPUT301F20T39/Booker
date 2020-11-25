package com.example.booker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

/**
 * Screen for checking a book's requests
 */
public class OwnerRequestsActivity extends AppCompatActivity {

    private Book book;
    private RequestAdapter adapter;
    private RecyclerView rvNameList;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_requests);

        book = (Book) getIntent().getSerializableExtra("Book");

        // set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbarRequests);
        setSupportActionBar(toolbar);
        ActionBar myToolbar = getSupportActionBar();
        myToolbar.setDisplayHomeAsUpEnabled(true);
        myToolbar.setTitle("Requests for " + book.getTitle());

        // connect adapter
        rvNameList = findViewById(R.id.requestsListView);
        adapter = new RequestAdapter(book, this);
        rvNameList.setAdapter(adapter);
        rvNameList.setLayoutManager(new LinearLayoutManager(this));

        // toolbar back button
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    /**
     * goes to a user's profile in read only mode
     * @param username
     */
    public void getProfile(String username) {
        // query user
        Query query = db.collection("Users")
                .whereEqualTo("username", username);

        // go to user's profile
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                for (QueryDocumentSnapshot doc: task.getResult()) {
                    userEmail = doc.getString("email");
                    Intent goToProfile = new Intent(getApplicationContext(), user_profile.class);
                    goToProfile.putExtra("profileType", "READ_ONLY");
                    goToProfile.putExtra("profileEmail", userEmail);
                    startActivity(goToProfile);
                }
            }
        });


    }
}
