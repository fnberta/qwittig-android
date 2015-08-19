package ch.giantific.qwittig.ui.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ch.giantific.qwittig.R;

/**
 * Created by fabio on 27.10.14.
 */
public class PurchaseUserSelectionDialogFragment extends DialogFragment {

    private static final String BUNDLE_USERS_AVAILABLE = "users_available";
    private static final String BUNDLE_USERS_CHECKED = "users_checked";
    private FragmentInteractionListener mListener;
    private List<Integer> mUsersSelected = new ArrayList<>();
    private List<Integer> mUsersSelectedStatusQuo = new ArrayList<>();
    private CharSequence[] mUsersAvailable;
    private boolean[] mUsersChecked;
    private TextView mTextViewError;

    public static PurchaseUserSelectionDialogFragment newInstance(
            CharSequence[] usersAvailable, boolean[] usersChecked) {
        PurchaseUserSelectionDialogFragment fragment = new PurchaseUserSelectionDialogFragment();

        Bundle args = new Bundle();
        args.putCharSequenceArray(BUNDLE_USERS_AVAILABLE, usersAvailable);
        args.putBooleanArray(BUNDLE_USERS_CHECKED, usersChecked);
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
                    + " must implement FragmentInteractionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mUsersAvailable = getArguments().getCharSequenceArray(BUNDLE_USERS_AVAILABLE);
            mUsersChecked = getArguments().getBooleanArray(BUNDLE_USERS_CHECKED);
        }

        setupDefaultUsersSelected();
    }

    private void setupDefaultUsersSelected() {
        for (int i = 0; i < mUsersChecked.length; i++) {
            boolean isChecked = mUsersChecked[i];
            if (isChecked) {
                mUsersSelected.add(i);
                mUsersSelectedStatusQuo.add(i);
            }
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_user_selection, null);
        mTextViewError = (TextView) view.findViewById(R.id.tv_error);
        builder.setTitle(R.string.dialog_users_involved_title)
                .setView(view)
                .setMultiChoiceItems(mUsersAvailable, mUsersChecked,
                        new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which,
                                                boolean isChecked) {
                                if (isChecked) {
                                    mUsersSelected.add(which);
                                    if (mTextViewError.getCurrentTextColor() == ContextCompat.getColor(getActivity(), R.color.red_error)) {
                                        mTextViewError.setTextAppearance(getActivity(), R.style.TextAppearance_AppCompat_Caption);
                                    }
                                } else if (mUsersSelected.contains(which)) {
                                    mUsersSelected.remove(Integer.valueOf(which));
                                    if (mUsersSelected.isEmpty()) {
                                        mTextViewError.setTextColor(ContextCompat.getColor(getActivity(), R.color.red_error));
                                    }
                                }
                            }
                        })
                .setPositiveButton(android.R.string.ok, null)
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();

        // Override View onClickListener because we only want the dialog to close when a at least
        // one user was selected. Default behavior is to always call dismiss().
        AlertDialog dialog = (AlertDialog) getDialog();
        if (dialog != null) {
            Button positiveButton = dialog.getButton(Dialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!mUsersSelected.isEmpty()) {
                        mListener.onItemUsersInvolvedSet(mUsersSelected);
                        dismiss();
                    }
                }
            });
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);

        //For "no fucking idea why" reasons, we need to call onItemUsersInvolvedSet also onCancel
        // with the original usersSelected. Otherwise, cancel would behave the same way as pressing ok
        // (although no method in the calling activity or fragment gets called that sets the users...)
        mListener.onItemUsersInvolvedSet(mUsersSelectedStatusQuo);
    }

    public interface FragmentInteractionListener {
        void onItemUsersInvolvedSet(List<Integer> userInvolved);
    }
}
