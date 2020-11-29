package com.example.booker;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class BorrowerAdapter extends FirestoreRecyclerAdapter<Book, BorrowerAdapter.BookHolder> {
    private int layoutResource;
    private BorrowerHomeActivity instance;
    private boolean hideButton = true;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseUser user;
    private FirebaseStorage storage = FirebaseStorage.getInstance();

    class BookHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView authorTextView;
        TextView ISBNTextView;
        TextView ownerUsernameTextView;
        TextView statusTextView;
        Button requestButton;
        ImageButton imageButtonLocation;
        View bookView;
        ImageView bookImage;

        public BookHolder(@NonNull View itemView) {
            super(itemView);
            bookView = itemView;
            titleTextView = itemView.findViewById(R.id.titleTextView);
            authorTextView = itemView.findViewById(R.id.authorTextView);
            ISBNTextView = itemView.findViewById(R.id.ISBNTextView);
            ownerUsernameTextView = itemView.findViewById(R.id.ownerUsernameTextView);
            statusTextView = itemView.findViewById(R.id.statusTextView);
            requestButton = itemView.findViewById(R.id.requestButton);
            imageButtonLocation = itemView.findViewById(R.id.imageButtonLocation);
            bookImage = itemView.findViewById(R.id.imageView2);

            // treat every item as a dummy book object and hide it
            itemView.setLayoutParams(new AbsListView.LayoutParams(-1,1));
            itemView.setVisibility(View.GONE);
        }
    }

    /**
     * Create a new RecyclerView adapter that listens to a Firestore Query.  See {@link
     * FirestoreRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public BorrowerAdapter(@NonNull FirestoreRecyclerOptions<Book> options,
                           int layoutResource, BorrowerHomeActivity instance) {
        super(options);
        this.layoutResource = layoutResource;
        this.instance = instance;
        this.firebaseFirestore = FirebaseFirestore.getInstance();
        this.user = FirebaseAuth.getInstance().getCurrentUser();
    }

    public void setHideButton(boolean hideButton) {
        this.hideButton = hideButton;
    }

    @NonNull
    @Override
    public BookHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(layoutResource, parent, false);
        return new BookHolder(v);
    }

    @Override
    protected void onBindViewHolder(@NonNull final BookHolder holder, int position, @NonNull final Book model) {
        holder.titleTextView.setText(model.getTitle());
        holder.authorTextView.setText(model.getAuthor());
        holder.ISBNTextView.setText(model.getISBN());
        holder.ownerUsernameTextView.setText(model.getOwnerUsername());
        holder.statusTextView.setText(model.getStatus());
        StorageReference storageRef = storage.getReference(model.getOwnerUsername() + "/" + model.getTitle());

        // unhide non-dummy book objects
        if (!model.getStatus().equals("")) {
            holder.bookView.setLayoutParams(new AbsListView.LayoutParams(-1,-2));
            holder.bookView.setVisibility(View.VISIBLE);
        }
        else {
            holder.bookView.setLayoutParams(new AbsListView.LayoutParams(-1,1));
            holder.bookView.setVisibility(View.GONE);
        }

        // hiding button
        if (hideButton || model.getStatus().equals("")) {
            holder.requestButton.setVisibility(View.GONE);
        }
        else {
            holder.requestButton.setVisibility(View.VISIBLE);
        }

        // greying out button if user in requester list
        if (model.containsRequester(user.getDisplayName())) {
            holder.requestButton.setAlpha(0.9f);
            holder.requestButton.setEnabled(false);
        }
        else {
            holder.requestButton.setAlpha(1.0f);
            holder.requestButton.setEnabled(true);
            holder.statusTextView.setText("Available"); // available to users not in requester list
        }

        // Getting images (if they exist)
        if (model.getImageURI() == null)
            model.setImageURI("");
        if (!model.getImageURI().isEmpty())
            try {
                final File file = File.createTempFile(model.getTitle(), "jpg");
                storageRef.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                        holder.bookImage.setImageBitmap(bitmap);
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        else
            holder.bookImage.setImageDrawable(null);

        holder.bookImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!model.getImageURI().isEmpty())
                    instance.viewPhoto(model);
            }
        });

        // accessing book and changing its status to "Requested"
        holder.requestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String UID = model.getUID();

                // add user to requester list
                model.addRequester(user.getDisplayName());

                // set status to requested
                model.setStatus("Requested");

                // get books hash map
                HashMap<String, Object> data = model.getDataHashMap();

                // set book's status to requested
                firebaseFirestore.collection("Books").document(UID).set(data);

                // For notifications purposes
                data.put("notified", false);
                firebaseFirestore.collection("Requests").document(model.getTitle()).set(data);
            }
        });

        // click owner username to view profile
        holder.ownerUsernameTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goToProfile = new Intent(instance, user_profile.class);
                goToProfile.putExtra("profileType", "READ_ONLY");
                goToProfile.putExtra("profileEmail", model.getOwnerEmail());
                instance.startActivity(goToProfile);
            }
        });

        // hide location button if not set
        if (!model.hasCoordinates()) {
            holder.imageButtonLocation.setVisibility(View.GONE);
        }
        else {
            holder.imageButtonLocation.setVisibility(View.VISIBLE);
        }

        // location button click listener
        holder.imageButtonLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goToMaps = new Intent(instance, MapsActivity.class);
                goToMaps.putExtra("accessType", "READ_ONLY");
                goToMaps.putExtra("book", model);
                instance.startActivity(goToMaps);
            }
        });
    }
}
