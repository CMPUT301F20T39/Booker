package com.example.booker;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controls behavior for each recyclerview item in BorrowerHomeActivity
 */
public class BorrowerAdapter extends RecyclerView.Adapter<BorrowerAdapter.BookViewHolder> {
    private int layoutResource;
    private List<Book> bookList;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseUser user;
    private boolean hideButton;
    private Context borrowerHomeContext;

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

            // initialize views
            titleTextView = itemView.findViewById(R.id.titleTextView);
            authorTextView = itemView.findViewById(R.id.authorTextView);
            ISBNTextView = itemView.findViewById(R.id.ISBNTextView);
            ownerUsernameTextView = itemView.findViewById(R.id.ownerUsernameTextView);
            statusTextView = itemView.findViewById(R.id.statusTextView);
            requestButton = itemView.findViewById(R.id.requestButton);
        }
    }

    // adapter constructor
    public BorrowerAdapter(int layoutResource, List<Book> bookList, Context borrowerHomeContext) {
        this.layoutResource = layoutResource;
        this.bookList = bookList;
        this.firebaseFirestore = FirebaseFirestore.getInstance();
        this.user = FirebaseAuth.getInstance().getCurrentUser();
        this.hideButton = true;
        this.borrowerHomeContext = borrowerHomeContext;
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

        // set texts to their values
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

        // greying out button if user in requester list
        if (book.containsRequester(user.getDisplayName())) {
            holder.requestButton.setAlpha(0.9f);
            holder.requestButton.setEnabled(false);
        }
        else {
            holder.requestButton.setAlpha(1.0f);
            holder.requestButton.setEnabled(true);
            holder.statusTextView.setText("Available"); // available to users not in requester list
        }

        // accessing book and changing its status to "Requested"
        holder.requestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickRequest(book);
            }
        });

        // click owner username to view profile
        holder.ownerUsernameTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goToProfile = new Intent(borrowerHomeContext, user_profile.class);
                goToProfile.putExtra("profileType", "READ_ONLY");
                goToProfile.putExtra("profileEmail", book.getOwnerEmail());
                borrowerHomeContext.startActivity(goToProfile);
            }
        });
    }

    @Override
    public int getItemCount() {
        return bookList.size();
    }

    /**
     * hides request button
     * @param hideButton
     */
    public void setHideButton(boolean hideButton) {
        this.hideButton = hideButton;
    }

    /**
     * request button click, set book to requested and add user as requester
     * @param book
     */
    private void clickRequest(Book book) {
        final String UID = book.getUID();

        // add user to requester list
        book.addRequester(user.getDisplayName());

        // set status to requested
        book.setStatus("Requested");

        // get books hash map
        HashMap<String, Object> data = book.getDataHashMap();

        // set book's status to requested
        firebaseFirestore.collection("Books").document(UID).set(data);
    }
}