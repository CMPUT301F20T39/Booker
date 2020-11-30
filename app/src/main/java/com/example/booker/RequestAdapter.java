package com.example.booker;

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

import java.util.HashMap;
import java.util.List;

/**
 * Converts book objects into elements of a recyclerview
 */
public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.MyViewHolder> {
    private Book book;
    private List<String> nameList;
    private FirebaseFirestore db;
    private FirebaseUser firebaseUser;
    private OwnerRequestsActivity instance;

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView nameView;
        public Button rejectButton;
        public Button acceptButton;
        private TextView textViewAccepted;

        public MyViewHolder(View v) {
            super(v);

            nameView = v.findViewById(R.id.requestsName);
            rejectButton = v.findViewById(R.id.rejectBtn);
            acceptButton = v.findViewById(R.id.acceptBtn);
            textViewAccepted = v.findViewById(R.id.textViewAccepted);
        }
    }

    /**
     * adapter for book's requester list
     *
     * @param book
     * @param instance
     */
    public RequestAdapter(Book book, OwnerRequestsActivity instance) {
        this.book = book;
        this.instance = instance;
        nameList = book.getRequesterList();
        this.db = FirebaseFirestore.getInstance();
        this.firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @NonNull
    @Override
    public RequestAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.owner_requests_content, parent, false);

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {
        final String username = nameList.get(position);

        // set username text for user
        holder.nameView.setText(username);

        // go to user's profile on username click
        holder.nameView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                instance.getProfile(holder.nameView.getText().toString());

            }
        });

        // reject user's request
        holder.rejectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // removes requester from requester list
                book.removeRequester(username);

                // set status back to available if nobody is requesting it anymore
                if (book.numRequesters() == 0) {
                    book.setStatus("Available");
                }

                // remove user from local name list and firestore Books collection
                HashMap<String, Object> data = book.getDataHashMap();
                db.collection("Books").document(book.getUID()).set(data);
                nameList.remove(username);

                // remove the book from the requests collection
                db.collection("Requests").document(book.getTitle()).delete();
                notifyDataSetChanged();
            }
        });

        // accept user's request
        holder.acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // leaves only that requester in the requester list
                book.leaveOneRequester(username);

                book.setStatus("Accepted");

                HashMap<String, Object> data = book.getDataHashMap();

                db.collection("Books").document(book.getUID()).set(data);

                // Why is notified false?
                // Book request has just been accepted and notification has not yet been sent
                // notified is true only when a notification has been sent.
                // In this case, the notification is pending
                db.collection("Requests").document(book.getTitle()).update("status", "Accepted");
                db.collection("Requests").document(book.getTitle()).update("notified",false);
                notifyDataSetChanged();
            }
        });

        // hide buttons on requested or borrowed
        if (book.getStatus().equals("Accepted") || book.getStatus().equals("Borrowed")) {
            holder.rejectButton.setVisibility(View.GONE);
            holder.acceptButton.setVisibility(View.GONE);
            holder.textViewAccepted.setText(book.getStatus());
        } else {
            holder.rejectButton.setVisibility(View.VISIBLE);
            holder.acceptButton.setVisibility(View.VISIBLE);
            holder.textViewAccepted.setText("");
        }

    }

    @Override
    public int getItemCount() {
        return nameList.size();
    }
}
