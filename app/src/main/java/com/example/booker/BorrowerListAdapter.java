package com.example.booker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

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
    public void onBindViewHolder(@NonNull final BorrowerListAdapter.MyViewHolder holder, final int position) {
        final Book book = bookList.get(position);

        holder.titleTextView.setText(book.getTitle());
        holder.authorTextView.setText(book.getAuthor());
        holder.ISBNTextView.setText(book.getISBN());
        holder.ownerUsernameTextView.setText(book.getOwnerUsername());
        holder.statusTextView.setText(book.getStatus());

        // hide button on borrower homer screen
        if (hideButton) {
            holder.requestButton.setVisibility(View.GONE);
        }

        // grey out button on "Requested" books
        if (book.getStatus().equals("Requested")) {
            holder.requestButton.setAlpha(0.9f);
            holder.requestButton.setEnabled(false);
        }

        // change status to "Requested" on request button click
        holder.requestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                book.setStatus("Requested");

                holder.statusTextView.setText(book.getStatus());

                // grey out button on "Requested" books
                if (book.getStatus().equals("Requested")) {
                    holder.requestButton.setAlpha(0.8f);
                    holder.requestButton.setEnabled(false);
                }
            }
        });
    }



    @Override
    public int getItemCount() {
        return bookList.size();
    }
}
