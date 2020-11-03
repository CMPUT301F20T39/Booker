package com.example.booker;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

/**
 * Converts book objects into elements of a recyclerview
 */
public class BookListAdapter extends RecyclerView.Adapter<BookListAdapter.MyViewHolder> {
	private List<Book> bookList;
	private FirebaseFirestore firebaseFirestore;
	private FirebaseUser firebaseUser;
	
	public static class MyViewHolder extends RecyclerView.ViewHolder {
		public TextView titleView, authorView, ISBNView, statusView;
		public Button deleteButton;
		
		public MyViewHolder(View v) {
			super(v);
			
			titleView = v.findViewById(R.id.OwnerBookTitle);
			authorView = v.findViewById(R.id.OwnerBookAuthor);
			ISBNView = v.findViewById(R.id.OwnerBookISBN);
			statusView = v.findViewById(R.id.OwnerBookStatus);
			deleteButton = v.findViewById(R.id.deleteBook);
		}
	}
	
	public BookListAdapter(List<Book> bookList) {
		this.bookList = bookList;
		this.firebaseFirestore = FirebaseFirestore.getInstance();
		this.firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
	}
	
	@NonNull
	@Override
	public BookListAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.owner_list_content, parent, false);
		
		return new MyViewHolder(view);
	}
	
	@Override
	public void onBindViewHolder(@NonNull BookListAdapter.MyViewHolder holder, final int position) {
		Book book = bookList.get(position);
		final String UID = book.getUID();
		
		holder.titleView.setText(book.getTitle());
		holder.authorView.setText(book.getAuthor());
		holder.ISBNView.setText(book.getISBN());
		holder.statusView.setText(book.getStatus());

		// delete a book on click
		holder.deleteButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				firebaseFirestore.collection("Books").document(UID).delete();
			}
		});
	}
	
	@Override
	public int getItemCount() {
		return bookList.size();
	}
}
