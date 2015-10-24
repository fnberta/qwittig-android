package ch.giantific.qwittig.ui.fragments.dialogs;

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
public class GroupCreateDialogFragment extends DialogFragment {

    private static final String BUNDLE_MESSAGE = "BUNDLE_MESSAGE";
    private DialogInteractionListener mListener;
    private int mMessage;

    public static GroupCreateDialogFragment newInstance(int message) {
        GroupCreateDialogFragment fragment = new GroupCreateDialogFragment();
        Bundle args = new Bundle();
        args.putInt(BUNDLE_MESSAGE, message);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mListener = (DialogInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement FragmentInteractionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            mMessage = args.getInt(BUNDLE_MESSAGE);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder.setMessage(mMessage)
                .setPositiveButton(R.string.dialog_positive_create, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onCreateGroupSelected();
                        dismiss();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        return dialogBuilder.create();
    }

    public interface DialogInteractionListener {
        void onCreateGroupSelected();
    }
}
