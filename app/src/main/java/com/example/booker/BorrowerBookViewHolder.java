package com.example.booker;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

// recyclerview attributes and initialization here
public class BorrowerBookViewHolder extends RecyclerView.ViewHolder {
    protected TextView titleTextView;
    protected TextView authorTextView;
    protected TextView ISBNTextView;
    protected TextView ownerUsernameTextView;
    protected TextView statusTextView;
    protected Button requestButton;

    public BorrowerBookViewHolder(@NonNull View itemView) {
        super(itemView);

        titleTextView = itemView.findViewById(R.id.titleTextView);
        authorTextView = itemView.findViewById(R.id.authorTextView);
        ISBNTextView = itemView.findViewById(R.id.ISBNTextView);
        ownerUsernameTextView = itemView.findViewById(R.id.ownerUsernameTextView);
        statusTextView = itemView.findViewById(R.id.statusTextView);
        requestButton = itemView.findViewById(R.id.requestButton);
    }
}