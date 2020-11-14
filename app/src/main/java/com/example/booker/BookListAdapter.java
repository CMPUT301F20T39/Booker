package com.example.booker;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
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
	private OwnerHomeActivity instance;
	
	public static class MyViewHolder extends RecyclerView.ViewHolder {
		public TextView titleView, authorView, ISBNView, statusView;
		public Button deleteButton, requestsButton, editButton;
		public ImageView imageView;
		
		public MyViewHolder(View v) {
			super(v);

			// initialize views
			titleView = v.findViewById(R.id.OwnerBookTitle);
			authorView = v.findViewById(R.id.OwnerBookAuthor);
			ISBNView = v.findViewById(R.id.OwnerBookISBN);
			statusView = v.findViewById(R.id.OwnerBookStatus);
			deleteButton = v.findViewById(R.id.deleteBook);
			requestsButton = v.findViewById(R.id.requestsBtn);
			editButton = v.findViewById(R.id.editBook);
			imageView = v.findViewById(R.id.bookImage);
		}
	}
	
	public BookListAdapter(List<Book> bookList, OwnerHomeActivity instance) {
		this.bookList = bookList;
		this.firebaseFirestore = FirebaseFirestore.getInstance();
		this.firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
		this.instance = instance;
	}
	
	@NonNull
	@Override
	public BookListAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.owner_list_content, parent, false);
		
		return new MyViewHolder(view);
	}
	
	@Override
	public void onBindViewHolder(@NonNull final BookListAdapter.MyViewHolder holder, final int position) {
		final Book book = bookList.get(position);
		final String UID = book.getUID();

		// set texts to their values
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

		// open request screen on click
		holder.requestsButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Book book = bookList.get(position);
				instance.createRequestList(book);
			}
		});

		// open edit fragment on click
		holder.editButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				AddBookFragment addBookFragment = new AddBookFragment();
				Bundle bundle = new Bundle();
				bundle.putString("bookUID", book.getUID());
				bundle.putString("bookTitle", book.getTitle());
				bundle.putString("bookAuthor", book.getAuthor());
				bundle.putString("bookISBN", book.getISBN());
				addBookFragment.setArguments(bundle);
				addBookFragment.show(instance.getSupportFragmentManager(), "EDIT_BOOK");
			}
		});

		holder.imageView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent photoIntent = new Intent();
				photoIntent.setType("image/*");
				photoIntent.setAction(Intent.ACTION_GET_CONTENT);
				instance.selectImage(photoIntent, holder.imageView, book);
			}
		});

	}

	@Override
	public int getItemCount() {
		return bookList.size();
	}
	
	// TODO: implement a method to get a book from a position in the list
}
