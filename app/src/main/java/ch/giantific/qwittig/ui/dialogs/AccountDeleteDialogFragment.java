package ch.giantific.qwittig.ui.dialogs;

import android.app.Activity;
import android.support.v7.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import ch.giantific.qwittig.R;

/**
 * Created by fabio on 20.11.14.
 */
public class AccountDeleteDialogFragment extends DialogFragment {

    private DialogInteractionListener mListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mListener = (DialogInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement DialogInteractionListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder.setMessage(R.string.dialog_account_delete_message)
                .setPositiveButton(R.string.dialog_positive_delete, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.startAccountDeletion();
                        dismiss();
                    }
                })
                .setNegativeButton(android.R.string.no, null);
        return dialogBuilder.create();
    }

    public interface DialogInteractionListener {
        public void startAccountDeletion();
    }
}
