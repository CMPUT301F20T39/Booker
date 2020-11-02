package com.example.booker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.List;

public class BorrowerAdapter extends RecyclerView.Adapter<BorrowerAdapter.BookViewHolder> {
    private int layoutResource;
    private List<Book> bookList;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseUser user;
    private CollectionReference myRequests;
    private boolean hideButton;

    // initialize and store view objects
    public class BookViewHolder extends RecyclerView.ViewHolder {
        private TextView titleTextView;
        private TextView authorTextView;
        private TextView ISBNTextView;
        private TextView ownerUsernameTextView;
        private TextView statusTextView;
        private Button requestButton;

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
    public BorrowerAdapter(int layoutResource, List<Book> bookList) {
        this.layoutResource = layoutResource;
        this.bookList = bookList;
        this.firebaseFirestore = FirebaseFirestore.getInstance();
        this.user = FirebaseAuth.getInstance().getCurrentUser();
        this.myRequests = firebaseFirestore
                .collection("Users").document(user.getEmail())
                .collection("myRequests");
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
    public void onBindViewHolder(@NonNull final BookViewHolder holder, final int position) {
        final Book book = bookList.get(position);

        holder.titleTextView.setText(book.getTitle());
        holder.authorTextView.setText(book.getAuthor());
        holder.ISBNTextView.setText(book.getISBN());
        holder.ownerUsernameTextView.setText(book.getOwnerUsername());
        holder.statusTextView.setText(book.getStatus());

        // hiding button
        if (hideButton) {
            holder.requestButton.setVisibility(View.GONE);
        }
        else {
            holder.requestButton.setVisibility(View.VISIBLE);
        }

        // greying out button
        if (bookList.get(position).getStatus().equals("Requested")) {
            holder.requestButton.setAlpha(0.9f);
            holder.requestButton.setEnabled(false);
        }
        else {
            holder.requestButton.setAlpha(1.0f);
            holder.requestButton.setEnabled(true);
        }

        // accessing book and changing its status to "Requested"
        holder.requestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickRequest(book);
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

    private void clickRequest(Book book) {
        String UID = book.getUID();

        book.setStatus("Requested");

        // get books hash map
        HashMap<String, String> data = book.getDataHashMap();

        // set book's status to requested
        //firebaseFirestore.collection("Books").document(UID).set(data);
        firebaseFirestore.collectionGroup("Books")
                .whereEqualTo("UID", UID)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        value.getDocuments().get(0).getReference().update("status", "Requested");
                    }
                });

        // add book to user's requests
        myRequests.document(UID).set(data);
    }
}
