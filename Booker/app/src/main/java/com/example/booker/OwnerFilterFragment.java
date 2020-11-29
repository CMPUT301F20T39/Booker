package com.example.booker;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class OwnerFilterFragment extends DialogFragment {
    private RadioGroup statusButtons;
    private RadioButton availableBttn;
    private RadioButton requestedBttn;
    private RadioButton acceptedBttn;
    private RadioButton borrowedBttn;

    private OnFragmentInteractionListener listener;

    public interface OnFragmentInteractionListener {
        void onOkPressed();
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
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.owner_filter_fragment, null);

        // Reference the EditTexts to retrieve their inputs
        availableBttn = view.findViewById(R.id.availableBttn);
        requestedBttn = view.findViewById(R.id.requestedBttn);
        acceptedBttn = view.findViewById(R.id.acceptedBttn);
        borrowedBttn = view.findViewById(R.id.borrowedBttn);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        return builder
                .setView(view)
                .setTitle("Filter By")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {


                        listener.onOkPressed();
                    }
                }).create();
    }



}
