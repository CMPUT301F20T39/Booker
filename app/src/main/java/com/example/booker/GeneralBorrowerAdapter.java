package com.example.booker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

// general recyclerview behaviour here
public class GeneralBorrowerAdapter extends FirestoreRecyclerAdapter<Book, GeneralBorrowerAdapter.BookViewHolder> {
    private boolean hideButton;

    // initialize and store view objects
    public class BookViewHolder extends RecyclerView.ViewHolder {
        protected TextView titleTextView;
        protected TextView authorTextView;
        protected TextView ISBNTextView;
        protected TextView ownerUsernameTextView;
        protected TextView statusTextView;
        protected Button requestButton;

        // constructor initializes view objects
        public BookViewHolder(@NonNull View itemView) {
            super(itemView);

            titleTextView = itemView.findViewById(R.id.titleTextView);
            authorTextView = itemView.findViewById(R.id.authorTextView);
            ISBNTextView = itemView.findViewById(R.id.ISBNTextView);
            ownerUsernameTextView = itemView.findViewById(R.id.ownerUsernameTextView);
            statusTextView = itemView.findViewById(R.id.statusTextView);
            requestButton = itemView.findViewById(R.id.requestButton);
        }
    }

    public GeneralBorrowerAdapter(@NonNull FirestoreRecyclerOptions<Book> options) {
        super(options);
        this.hideButton = true;
    }

    @Override
    protected void onBindViewHolder(@NonNull BookViewHolder holder, final int position, @NonNull Book model) {
        holder.titleTextView.setText(model.getTitle());
        holder.authorTextView.setText(model.getAuthor());
        holder.ISBNTextView.setText(model.getISBN());
        holder.ownerUsernameTextView.setText(model.getOwnerUsername());
        holder.statusTextView.setText(model.getStatus());

        if (hideButton) {
            holder.requestButton.setVisibility(View.GONE);
        }
        else {
            holder.requestButton.setVisibility(View.VISIBLE);
        }

        if (model.getStatus().equals("Requested")) {
            holder.requestButton.setAlpha(0.9f);
            holder.requestButton.setEnabled(false);
        }
        else {
            holder.requestButton.setAlpha(1.0f);
            holder.requestButton.setEnabled(true);
        }

        holder.requestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSnapshots().getSnapshot(position).getReference().update("status", "Requested");
            }
        });
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.borrower_search_item, parent, false);
        return new BookViewHolder(view);
    }

    public void setHideButton(boolean hideButton) {
        this.hideButton = hideButton;
    }
}