package com.example.booker;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;

public class RequestAdapter2 extends FirestoreRecyclerAdapter<Book, RequestAdapter2.BookHolder>  {
    private Book book;
    private List<String> nameList;
    private FirebaseFirestore db;
    private FirebaseUser firebaseUser;
    private OwnerRequestsActivity instance;
    private int layoutResource;

    class BookHolder extends RecyclerView.ViewHolder {
        public TextView nameView;
        public Button rejectButton;
        public Button acceptButton;

        public BookHolder(@NonNull View itemView) {
            super(itemView);
            nameView = itemView.findViewById(R.id.requestsName);
            rejectButton = itemView.findViewById(R.id.rejectBtn);
            acceptButton = itemView.findViewById(R.id.acceptBtn);
        }
    }

    public RequestAdapter2(@NonNull FirestoreRecyclerOptions<Book> options,
                           int layoutResource, OwnerRequestsActivity instance) {
        super(options);
        this.book = book;
        this.instance = instance;
        nameList = book.getRequesterList();
        this.db = FirebaseFirestore.getInstance();
        this.firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        this.layoutResource = layoutResource;
    }

    @NonNull
    @Override
    public RequestAdapter2.BookHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(layoutResource, parent, false);
        return new RequestAdapter2.BookHolder(v);
    }

    @Override
    protected void onBindViewHolder(@NonNull final RequestAdapter2.BookHolder holder, int position, @NonNull Book model) {
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
                book.removeRequester(username);

                if (book.numRequesters() == 0) {
                    book.setStatus("Available");
                }

                HashMap<String, Object> data = book.getDataHashMap();
                db.collection("Books").document(book.getUID()).set(data);
                nameList.remove(username);
                notifyDataSetChanged();
            }
        });

        // accept user's request
        holder.acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                book.leaveOneRequester(username);

                book.setStatus("Accepted");

                HashMap<String, Object> data = book.getDataHashMap();

                db.collection("Books").document(book.getUID()).set(data);

                notifyDataSetChanged();
            }
        });

        // hide buttons on requested
        if (book.getStatus().equals("Accepted") || book.getStatus().equals("Borrowed")) {
            holder.rejectButton.setVisibility(View.GONE);
            holder.acceptButton.setVisibility(View.GONE);
        }
        else {
            holder.rejectButton.setVisibility(View.VISIBLE);
            holder.acceptButton.setVisibility(View.VISIBLE);
        }
    }
}
