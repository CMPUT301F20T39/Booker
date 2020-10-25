package com.example.booker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/** separate view adapter for borrower's requested books and searches
 *
 */
public class BorrowerListAdapter extends RecyclerView.Adapter<BorrowerListAdapter.MyViewHolder> {
    private List<Book> bookList;
    private boolean hideButton;

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView titleTextView, authorTextView, ISBNTextView, ownerUsernameTextView, statusTextView;
        public Button requestButton;

        public MyViewHolder(View v) {
            super(v);

            titleTextView = v.findViewById(R.id.titleTextView);
            authorTextView = v.findViewById(R.id.authorTextView);
            ISBNTextView = v.findViewById(R.id.ISBNTextView);
            ownerUsernameTextView = v.findViewById(R.id.ownerUsernameTextView);
            statusTextView = v.findViewById(R.id.statusTextView);

            requestButton = v.findViewById(R.id.requestButton);
        }
    }

    public BorrowerListAdapter(List<Book> bookList, boolean hideButton) {
        this.bookList = bookList;
        this.hideButton = hideButton;
    }

    @NonNull
    @Override
    public BorrowerListAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.borrower_search_item, parent, false);

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BorrowerListAdapter.MyViewHolder holder, int position) {
        Book book = bookList.get(position);

        holder.titleTextView.setText(book.getTitle());
        holder.authorTextView.setText(book.getAuthor());
        holder.ISBNTextView.setText(book.getISBN());
        holder.ownerUsernameTextView.setText(book.getOwnerUsername());
        holder.statusTextView.setText(book.getStatus());

        if (hideButton) {
            holder.requestButton.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return bookList.size();
    }
}
