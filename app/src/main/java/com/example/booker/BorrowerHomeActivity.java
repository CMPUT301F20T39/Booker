package com.example.booker;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.SearchView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class BorrowerHomeActivity extends AppCompatActivity {
    private List<Book> bookList = new ArrayList<>();
    private CollectionReference bookDB = FirebaseFirestore.getInstance().collection("Books");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_borrower_home);

        final SearchView searchView = findViewById(R.id.searchView);

        // goes to search activity on query submit
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Intent goToSearch = new Intent(getApplicationContext(), BorrowerSearchActivity.class);
                String searchQuery = searchView.getQuery().toString();
                goToSearch.putExtra("searchQuery", searchQuery);
                startActivityForResult(goToSearch, 0);
                searchView.setQuery("", false);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        // initialize recyclerview
        RecyclerView rvBookList = findViewById(R.id.recyclerView);

        // connect adapter and layout to recyclerview
        final BorrowerListAdapter adapter = new BorrowerListAdapter(bookList, true);
        rvBookList.setAdapter(adapter);
        rvBookList.setLayoutManager(new LinearLayoutManager(this));

        // debug: used for testing recyclerview layout and firestore
        // still need to work on accessing personal request list for each user
        bookDB.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                List<Book> books = value.toObjects(Book.class);
                for (int i = 0; i < books.size(); i++) {
                    bookList.add(books.get(i));
                }
                adapter.notifyDataSetChanged();
            }
        });



    }
}