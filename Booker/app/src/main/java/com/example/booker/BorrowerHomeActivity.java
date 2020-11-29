package com.example.booker;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.TextView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
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

        SearchView searchView = findViewById(R.id.searchView);

        // get internal edittext from search view
        int id = searchView.getContext().getResources()
                .getIdentifier("android:id/search_src_text", null, null);
        final EditText searchViewEditText = (EditText) searchView.findViewById(id);

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
                for (QueryDocumentSnapshot document: value) {
                    Book book = document.toObject(Book.class);
                    bookList.add(book);
                }
                adapter.notifyDataSetChanged();
            }
        });

        // switch to search activity on click and clear focus
        searchViewEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Intent goToSearch = new Intent(getApplicationContext(), BorrowerSearchActivity.class);
                goToSearch.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // close all other activities on finish
                startActivity(goToSearch);
                searchViewEditText.clearFocus(); // close keyboard
                return false;
            }
        });

    }
}