package com.example.plonka.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

import com.example.plonka.R;

/**
 * Custom DialogFragment asking the user whether they want to abandon an ongoing work shift
 */
public class AbandonShiftDialogFragment extends DialogFragment {

    /**
     * Constructor that sets up the dialog with abandon and cancel buttons
     * @param savedInstanceState unused, used when overriding
     * @return dialog that was set up
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.end_shift_dialog_message)
                .setTitle(R.string.end_shift_dialog_title)
                .setPositiveButton(R.string.end_shift_dialog_pos, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Send the pos button event back to the host activity
                        listener.onDialogPositiveClick(AbandonShiftDialogFragment.this);
                    }
                })

                .setNegativeButton(R.string.end_shift_dialog_neg, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Send the neg button event back to the host activity
                        listener.onDialogNegativeClick(AbandonShiftDialogFragment.this);
                    }
                });

        // Create the AlertDialog object and return it
        return builder.create();
    }



    /** The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it.
     */
    public interface AbandonShiftDialogListener {
        public void onDialogPositiveClick(DialogFragment dialog);
        public void onDialogNegativeClick(DialogFragment dialog);
    }

    // Use this instance of the interface to deliver action events
    AbandonShiftDialogListener listener;

    /** Override the Fragment.onAttach() method to instantiate the AbandonShiftDialogListener
     * @param context to attach dialog to
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the AbandonShiftDialogListener so we can send events to the host
            listener = (AbandonShiftDialogListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException("Parent activity must implement AbandonShiftDialogListener");
        }
    }
}


