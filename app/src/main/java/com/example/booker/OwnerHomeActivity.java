package com.example.booker;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.SearchView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.api.Distribution;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class OwnerHomeActivity extends AppCompatActivity {
	private CollectionReference db = FirebaseFirestore.getInstance().collection("Users/test-user/Books"); // TODO: Make this access differently depending on the user (by username)
	
	private List<Book> bookList = new ArrayList<Book>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_owner_home);
		
		RecyclerView rvBookList = findViewById(R.id.ownerBookListView);
		
		final BookListAdapter adapter = new BookListAdapter(bookList);
		rvBookList.setAdapter(adapter);
		rvBookList.setLayoutManager(new LinearLayoutManager(this));
		
		db.addSnapshotListener(new EventListener<QuerySnapshot>() {
			@Override
			public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException error) {
				bookList.clear();
				for(QueryDocumentSnapshot doc: queryDocumentSnapshots) {
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
		
	
		
	}
}