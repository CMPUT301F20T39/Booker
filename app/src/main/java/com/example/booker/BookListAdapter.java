package com.example.booker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * Converts book objects into elements of a recyclerview
 */
public class BookListAdapter extends RecyclerView.Adapter<BookListAdapter.MyViewHolder> {
	private List<Book> bookList;
	
	public static class MyViewHolder extends RecyclerView.ViewHolder {
		public TextView descView, authorView, ISBNView, statusView;
		
		public MyViewHolder(View v) {
			super(v);
			
			descView = v.findViewById(R.id.OwnerBookDesc);
			authorView = v.findViewById(R.id.OwnerBookAuthor);
			ISBNView = v.findViewById(R.id.OwnerBookISBN);
			statusView = v.findViewById(R.id.OwnerBookStatus);
		}
	}
	
	public BookListAdapter(List<Book> bookList) {
		this.bookList = bookList;
	}
	
	@NonNull
	@Override
	public BookListAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.owner_list_content, parent, false);
		
		return new MyViewHolder(view);
	}
	
	@Override
	public void onBindViewHolder(@NonNull BookListAdapter.MyViewHolder holder, int position) {
		Book book = bookList.get(position);
		
		holder.descView.setText(book.getDescription());
		holder.authorView.setText(book.getAuthor());
		holder.ISBNView.setText(book.getISBN());
		holder.statusView.setText(book.getStatus());
	}
	
	@Override
	public int getItemCount() {
		return bookList.size();
	}
}
