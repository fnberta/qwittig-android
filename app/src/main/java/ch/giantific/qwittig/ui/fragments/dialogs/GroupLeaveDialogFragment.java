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
public class GroupLeaveDialogFragment extends DialogFragment {

    private static final String BUNDLE_MESSAGE = "message";
    private FragmentInteractionListener mListener;
    private String mMessage;

    public static GroupLeaveDialogFragment newInstance(String message) {
        GroupLeaveDialogFragment fragment = new GroupLeaveDialogFragment();

        Bundle args = new Bundle();
        args.putString(BUNDLE_MESSAGE, message);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mListener = (FragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement SeparateBillFragmentCallback");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mMessage = getArguments().getString(BUNDLE_MESSAGE);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder.setMessage(mMessage)
                .setPositiveButton(R.string.dialog_positive_leave, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onLeaveGroupSelected();
                        dismiss();
                    }
                })
                .setNegativeButton(android.R.string.no, null);
        return dialogBuilder.create();
    }

    public interface FragmentInteractionListener {
        void onLeaveGroupSelected();
    }
}
