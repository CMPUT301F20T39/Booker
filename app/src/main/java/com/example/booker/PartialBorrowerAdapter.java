package com.example.booker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class PartialBorrowerAdapter extends RecyclerView.Adapter<PartialBorrowerAdapter.BookViewHolder> {
    private int layoutResource;
    private List<Book> bookList;
    private FirebaseFirestore firebaseFirestore;
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

    // adapter constructor
    public PartialBorrowerAdapter(int layoutResource, List<Book> bookList) {
        this.layoutResource = layoutResource;
        this.bookList = bookList;
        this.firebaseFirestore = FirebaseFirestore.getInstance();
        this.hideButton = true;
    }

    // behaviour on creation
    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutResource, parent, false);
        return new BookViewHolder(view);
    }

    // behaviour for list items
    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, final int position) {
        holder.titleTextView.setText(bookList.get(position).getTitle());
        holder.authorTextView.setText(bookList.get(position).getAuthor());
        holder.ISBNTextView.setText(bookList.get(position).getISBN());
        holder.ownerUsernameTextView.setText(bookList.get(position).getOwnerUsername());
        holder.statusTextView.setText(bookList.get(position).getStatus());

        if (hideButton) {
            holder.requestButton.setVisibility(View.GONE);
        }
        else {
            holder.requestButton.setVisibility(View.VISIBLE);
        }

        if (bookList.get(position).getStatus().equals("Requested")) {
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
                Query query = firebaseFirestore.collection("Books").whereEqualTo("UID", bookList.get(position).getUID());

                query.addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        value.getDocuments().get(0).getReference().update("status", "Requested");
                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return bookList.size();
    }

    public void setHideButton(boolean hideButton) {
        this.hideButton = hideButton;
    }
}
