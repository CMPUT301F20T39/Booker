package com.example.booker;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
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

/**
 * Controls behavior of owner's book display on recyclerview
 */
public class BookListAdapter extends FirestoreRecyclerAdapter<Book, BookListAdapter.BookHolder> {
    private int layoutResource;
    private OwnerHomeActivity instance;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseUser user;
    private FirebaseStorage storage = FirebaseStorage.getInstance();

    class BookHolder extends RecyclerView.ViewHolder {
        public TextView titleView, authorView, ISBNView, statusView, ownerBookBorrowerName;
        public Button deleteButton, requestsButton, editButton;
        public ImageView imageView;
        public ImageButton imageButtonLocation;

        public BookHolder(@NonNull View itemView) {
            super(itemView);
            // initialize views
            titleView = itemView.findViewById(R.id.OwnerBookTitle);
            authorView = itemView.findViewById(R.id.OwnerBookAuthor);
            ISBNView = itemView.findViewById(R.id.OwnerBookISBN);
            statusView = itemView.findViewById(R.id.OwnerBookStatus);
            deleteButton = itemView.findViewById(R.id.deleteBook);
            requestsButton = itemView.findViewById(R.id.requestsBtn);
            editButton = itemView.findViewById(R.id.editBook);
            imageView = itemView.findViewById(R.id.imageView2);
            ownerBookBorrowerName = itemView.findViewById(R.id.ownerBookBorrowerName);
            imageButtonLocation = itemView.findViewById(R.id.imageButtonLocation3);
        }
    }

    /**
     * initialize firestore adapter for owner books
     * @param options
     * @param layoutResource
     * @param instance
     */
    public BookListAdapter(@NonNull FirestoreRecyclerOptions<Book> options,
                           int layoutResource, OwnerHomeActivity instance) {
        super(options);
        this.layoutResource = layoutResource;
        this.firebaseFirestore = FirebaseFirestore.getInstance();
        this.user = FirebaseAuth.getInstance().getCurrentUser();
        this.instance = instance;
    }

    @NonNull
    @Override
    public BookListAdapter.BookHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(layoutResource, parent, false);
        return new BookListAdapter.BookHolder(v);
    }

    @Override
    protected void onBindViewHolder(@NonNull final BookListAdapter.BookHolder holder, final int position, @NonNull final Book model) {
        final String UID = model.getUID();
        StorageReference storageRef = storage.getReference(user.getDisplayName() + "/" + model.getUID());

        // set texts to their values
        holder.titleView.setText(model.getTitle());
        holder.authorView.setText(model.getAuthor());
        holder.ISBNView.setText(model.getISBN());
        holder.statusView.setText(model.getStatus());

        // book has no URI, set it to empty string
        if (model.getImageURI() == null)
            model.setImageURI("");

        // book has a URI, try getting its picture
        if (!model.getImageURI().isEmpty())
            try {
                final File file = File.createTempFile(model.getUID(), "jpg");
                storageRef.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                        holder.imageView.setImageBitmap(bitmap);
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        else
            // set its picture to the default picture
            holder.imageView.setImageResource(R.drawable.defaultphoto);

        // delete a book on click
        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseFirestore.collection("Books").document(UID).delete();
            }
        });

        // open request screen on click
        holder.requestsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                instance.createRequestList(model);
            }
        });

        // open edit fragment on click
        holder.editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddBookFragment addBookFragment = new AddBookFragment();
                Bundle bundle = new Bundle();
                bundle.putString("bookUID", model.getUID());
                bundle.putString("bookTitle", model.getTitle());
                bundle.putString("bookAuthor", model.getAuthor());
                bundle.putString("bookISBN", model.getISBN());
                addBookFragment.setArguments(bundle);
                addBookFragment.show(instance.getSupportFragmentManager(), "EDIT_BOOK");
            }
        });

        // if no photo then select one, else view photo in new activity
        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent photoIntent = new Intent();
                if (model.getImageURI().isEmpty()) {
                    photoIntent.setType("image/*");
                    photoIntent.setAction(Intent.ACTION_GET_CONTENT);
                    instance.selectImage(photoIntent, holder.imageView, model);
                }
                else {
                    instance.showPhoto(model);

                }
            }
        });

        // set borrower name if borrowed
        if (model.getStatus().equals("Borrowed")) {
            String borrowedDisplay = "Borrowed: " + model.getRequesterList().get(0);
            holder.ownerBookBorrowerName.setText(borrowedDisplay);
        }
        else {
            holder.ownerBookBorrowerName.setText("Borrowed: None");
        }

        // location button is only visible on accepted or borrowed books
        if (model.getStatus().equals("Accepted") || model.getStatus().equals("Borrowed")) {
            holder.imageButtonLocation.setVisibility(View.VISIBLE);
        }
        else {
            holder.imageButtonLocation.setVisibility(View.GONE);
        }

        // clicking a book's picture to view it
        holder.imageButtonLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent goToMaps = new Intent(instance, MapsActivity.class);
                goToMaps.putExtra("accessType", "WRITE");
                goToMaps.putExtra("book", model);
                instance.startActivity(goToMaps);
            }
        });
    }
}
