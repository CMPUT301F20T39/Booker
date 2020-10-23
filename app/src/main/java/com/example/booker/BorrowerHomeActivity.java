package com.example.booker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.SearchView;

import com.google.firebase.firestore.FirebaseFirestore;

public class BorrowerHomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_borrower_home);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        final String TAG = "Sample";

        // goes to search activity on search bar press
        final SearchView searchView = findViewById(R.id.searchView);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Intent goToSearch = new Intent(getApplicationContext(), BorrowSearchActivity.class);
                String searchQuery = searchView.getQuery().toString();
                goToSearch.putExtra("searchQuery", searchQuery);
                startActivityForResult(goToSearch, 0);
                searchView.setQuery("", true);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }
}