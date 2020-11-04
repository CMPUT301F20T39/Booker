package com.example.booker;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.ArrayList;
import java.util.List;

/*
Most of the code is derived from lab 3
 */
public class AddBookFragment extends DialogFragment {

    // EditTexts found in the fragment
    private EditText titleEdit;
    private EditText authorEdit;
    private EditText isbnEdit;
    private EditText descrpEdit;

    private OnFragmentInteractionListener listener;

    public interface OnFragmentInteractionListener {
        void onOkPressed(String dialogType, String bookUID, String title, String author, String isbn, String description);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            listener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + "must implement OnFragmentInteractionListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
//        LayoutInflater inflater = requireActivity().getLayoutInflater();
//        View view = inflater.inflate(R.layout.add_book_fragment_layout, null);

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.add_book_fragment_layout, null);

        // Reference the EditTexts to retrieve their inputs
        titleEdit = view.findViewById(R.id.editTextTitle);
        authorEdit = view.findViewById(R.id.editTextAuthor);
        isbnEdit = view.findViewById(R.id.editTextISBN);
        descrpEdit = view.findViewById(R.id.editTextDescr);

        Bundle bundle = getArguments();
        String dialogType = "Add Book";
        String bookUID = generateUID();
        if (bundle != null) {
            titleEdit.setText(getArguments().getString("bookTitle"));
            authorEdit.setText(getArguments().getString("bookAuthor"));
            isbnEdit.setText(getArguments().getString("bookISBN"));
            dialogType = "Edit Book";
            bookUID = getArguments().getString("bookUID");
        }


        // Build the alert dialog
        // set the View to the inflated layout
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        final String finalDialogType = dialogType;
        final String finalBookUID = bookUID;

        return builder
                .setView(view)
                .setTitle(dialogType)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String title = titleEdit.getText().toString();
                        String author = authorEdit.getText().toString();
                        String isbn = isbnEdit.getText().toString();
                        String description = descrpEdit.getText().toString();

                        listener.onOkPressed(finalDialogType, finalBookUID, title, author, isbn, description);
                    }
                }).create();
    }

    /**
     * Generates a random, unique* document ID
     *
     * https://github.com/firebase/firebase-android-sdk/issues/408
     * Firestore itself doesn't actually generate a unique UID. It generates a
     * statistically rare alphanumeric String sequence.
     *
     * @return UID
     *         A unique* String sequence of random alphanumeric characters.
     */
    public String generateUID() {
        int length = 20;
        List<String> potentialCharacters = new ArrayList<>();

        for (char chr = '0'; chr <= '9'; chr++) {
            potentialCharacters.add(String.valueOf(chr));
        }
        for (char chr = 'A'; chr <= 'Z'; chr++) {
            potentialCharacters.add(String.valueOf(chr));
        }
        for (char chr = 'a'; chr <= 'z'; chr++) {
            potentialCharacters.add(String.valueOf(chr));
        }

        int range = potentialCharacters.size();
        String UID = "";
        for (int chr = 0; chr < length; chr++) {
            int randomIndex = (int) (Math.random() * range);
            UID = UID.concat(potentialCharacters.get(randomIndex));
        }

        return UID;
    }
}

// TODO
// 1. EditTexts are not clickable
// 2. List shows Title as blank
