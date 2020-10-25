package com.example.booker;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class OwnerHomeActivity extends AppCompatActivity implements AddBookFragment.OnFragmentInteractionListener {
	private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference bookCollection = db.collection("Users/test-user/Books"); // TODO: Make this access differently depending on the user (by username)

    private List<Book> bookList = new ArrayList<Book>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_home);

        RecyclerView rvBookList = findViewById(R.id.ownerBookListView);

        final BookListAdapter adapter = new BookListAdapter(bookList);
        rvBookList.setAdapter(adapter);
        rvBookList.setLayoutManager(new LinearLayoutManager(this));

        bookCollection.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException error) {
                bookList.clear();
                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    Book aBook = doc.toObject(Book.class);
                    bookList.add(aBook);
                }
                adapter.notifyDataSetChanged();
            }
        });


//
//		db.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//			@Override
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
                new AddBookFragment().show(getSupportFragmentManager(), "ADD BOOK");
            }
        });
    }

    @Override
    public void onOkPressed(String title, String author, String isbn) {
        final String TAG = "Add Book method";
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