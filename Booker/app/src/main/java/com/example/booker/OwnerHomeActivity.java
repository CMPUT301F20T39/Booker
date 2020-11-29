package com.example.booker;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class OwnerHomeActivity extends AppCompatActivity implements AddBookFragment.OnFragmentInteractionListener {
	private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference bookCollection = db.collection("Users/test-user/Books"); // TODO: Make this access differently depending on the user (by username)
    private BookListAdapter adapter;
    private ArrayList<BookStatus> filters = new ArrayList<>();
    private List<Book> bookList = new ArrayList<Book>();
    private ArrayList<Book> filteredBooks = new ArrayList<>();


    private Chip requestedButton;
    private Chip acceptedButton;
    private Chip borrowedButton;
    private Chip availableButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_home);

        RecyclerView rvBookList = findViewById(R.id.ownerBookListView);

        BookListAdapter adapter = new BookListAdapter(bookList);
        rvBookList.setAdapter(adapter);
        rvBookList.setLayoutManager(new LinearLayoutManager(this));
        requestedButton = findViewById(R.id.requestedBttn);
        acceptedButton = findViewById(R.id.acceptedBttn);
        borrowedButton = findViewById(R.id.borrowedBttn);
        availableButton = findViewById(R.id.availableBttn);  //these are all chips in a chipgroup


        requestedButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            /**
             * Update the filter when the chip is checked or unchecked
             *
             * @param compoundButton: the compound button
             * @param b:              which state the button was clicked in
             */
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                updateFilter();
            }
        });
        acceptedButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            /**
             * Update the filter when the chip is checked or unchecked
             *
             * @param compoundButton: the compound button
             * @param b:              which state the button was clicked in
             */
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                updateFilter();
            }
        });
        borrowedButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            /**
             * Update the filter when the chip is checked or unchecked
             *
             * @param compoundButton: the compound button
             * @param b:              which state the button was clicked in
             */
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                updateFilter();
            }
        });
        availableButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            /**
             * Update the filter when the chip is checked or unchecked
             *
             * @param compoundButton: the compound button
             * @param b:              which state the button was clicked in
             */
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                updateFilter();
            }
        });
    }

    /**
    * changes filter based on which chip(s) are checked
     *
    */
    private void updateFilter() {
        ArrayList<BookStatus> newFilters = new ArrayList<>();
        if (requestedButton.isChecked()){
            newFilters.add(BookStatus.REQUESTED);
        }
        if (acceptedButton.isChecked()){
            newFilters.add(BookStatus.ACCEPTED);
        }
        if (borrowedButton.isChecked()){
            newFilters.add(BookStatus.BORROWED);
        }
        if (availableButton.isChecked()){
            newFilters.add(BookStatus.AVAILABLE);
        }
        filters = newFilters;
        updateBooksFiltered();
    }


    /**
     * updates books to display based on filters
     */
    private void updateBooksFiltered() {
        ArrayList<Book> booksFiltered = new ArrayList<>();

        for (Book book: bookList){
            if (filters.contains(book.getStatus())){
                booksFiltered.add(book);
            }
        }

        filteredBooks.clear();
        filteredBooks.addAll(booksFiltered);
        adapter.notifyDataSetChanged();
    }


    private void getBooks() {
        bookCollection


    }









        bookCollection.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException error) {
                bookList.clear();
                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    if(Book.)
                    Book aBook = doc.toObject(Book.class);
                    bookList.add(aBook);
                }
                adapter.notifyDataSetChanged();
            }
        });


//
//		db.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
////			@Override
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

        final FloatingActionButton addBookBtn = findViewById(R.id.add_book_btn);
        addBookBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AddBookFragment().show(getSupportFragmentManager(), "ADD_BOOK");
            }
        });

    }

    @Override
    public void onOkPressed(String title, String author, String isbn, String description) {
        final String TAG = "Add Book method";
        HashMap<String, String> data = new HashMap<>();

        if (title.length() > 0 && author.length() > 0 && isbn.length() > 0)
        {
            data.put("ISBN", isbn);
            data.put("title", title);
            data.put("author", author);
            data.put("status", "Available");
        }
        bookCollection
                .document(title)
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





}