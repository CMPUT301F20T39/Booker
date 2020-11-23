package com.example.booker;

import android.content.Intent;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Arrays;
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
        public ImageButton imageButtonLocation;

        public MyViewHolder(View v) {
            super(v);

            nameView = v.findViewById(R.id.requestsName);
            rejectButton = v.findViewById(R.id.rejectBtn);
            acceptButton = v.findViewById(R.id.acceptBtn);
            imageButtonLocation = v.findViewById(R.id.imageButtonLocation2);
        }
    }

    /**
     * adapter for book's requester list
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
            holder.imageButtonLocation.setVisibility(View.VISIBLE);
        }
        else {
            holder.rejectButton.setVisibility(View.VISIBLE);
            holder.acceptButton.setVisibility(View.VISIBLE);
            holder.imageButtonLocation.setVisibility(View.GONE);
        }

        holder.imageButtonLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent goToMaps = new Intent(instance, MapsActivity.class);
                goToMaps.putExtra("accessType", "WRITE");
                goToMaps.putExtra("book", book);
                instance.startActivity(goToMaps);
            }
        });

    }

    @Override
    public int getItemCount() {
        return nameList.size();
    }
}
