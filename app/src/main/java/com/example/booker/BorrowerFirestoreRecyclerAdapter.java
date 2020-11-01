package com.example.booker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

// general recyclerview behaviour here
public class BorrowerFirestoreRecyclerAdapter extends FirestoreRecyclerAdapter<Book, BorrowerBookViewHolder> {
    private boolean hideButton;

    public BorrowerFirestoreRecyclerAdapter(@NonNull FirestoreRecyclerOptions<Book> options) {
        super(options);
        this.hideButton = true;
    }

    @NonNull
    @Override
    public BorrowerBookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.borrower_search_item, parent, false);
        return new BorrowerBookViewHolder(view);
    }

    // individual recyclerview item behaviour here
    @Override
    protected void onBindViewHolder(@NonNull BorrowerBookViewHolder holder, final int position, @NonNull Book model) {
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
                Book targetBook = getItem(position);
                targetBook.setStatus("Requested");
                notifyDataSetChanged();
            }
        });
    }

    public void setHideButton(boolean hideButton) {
        this.hideButton = hideButton;
    }
}