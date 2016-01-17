/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.presentation.ui.fragments.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ch.giantific.qwittig.R;

/**
 * Provides a dialog that allows the user to select the users involved for an item of a purchase.
 * <p/>
 * At least one user needs to be selected, otherwise the dialog will only be dismissed by the
 * cancel action. Therefore overrides the default positive button onClickListener because the
 * default behaviour is to always call dismiss().
 * <p/>
 * Subclass of {@link DialogFragment}.
 */
public class PurchaseUserSelectionDialogFragment extends DialogFragment {

    private static final String BUNDLE_USERS_AVAILABLE = "BUNDLE_USERS_AVAILABLE";
    private static final String BUNDLE_USERS_CHECKED = "BUNDLE_USERS_CHECKED";
    private DialogInteractionListener mListener;
    @NonNull
    private List<Integer> mUsersSelected = new ArrayList<>();
    @NonNull
    private List<Integer> mUsersSelectedStatusQuo = new ArrayList<>();
    @Nullable
    private CharSequence[] mUsersAvailable;
    @Nullable
    private boolean[] mUsersChecked;
    private TextView mTextViewError;

    /**
     * Returns a new instance of {@link PurchaseUserSelectionDialogFragment}.
     *
     * @param usersAvailable the users available for selection
     * @param usersChecked   the currently selected users for the item
     * @return a new instance of {@link PurchaseUserSelectionDialogFragment}
     */
    @NonNull
    public static PurchaseUserSelectionDialogFragment newInstance(
            @NonNull CharSequence[] usersAvailable, @NonNull boolean[] usersChecked) {
        PurchaseUserSelectionDialogFragment fragment = new PurchaseUserSelectionDialogFragment();

        Bundle args = new Bundle();
        args.putCharSequenceArray(BUNDLE_USERS_AVAILABLE, usersAvailable);
        args.putBooleanArray(BUNDLE_USERS_CHECKED, usersChecked);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (DialogInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement DialogInteractionListener");
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
        if (mUsersChecked != null) {
            for (int i = 0; i < mUsersChecked.length; i++) {
                boolean isChecked = mUsersChecked[i];
                if (isChecked) {
                    mUsersSelected.add(i);
                    mUsersSelectedStatusQuo.add(i);
                }
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
                    public void onClick(@NonNull DialogInterface dialog, int id) {
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

        // For no sensible reason, we need to call onItemUsersInvolvedSet also onCancel
        // with the original usersSelected. Otherwise, cancel would behave the same way as pressing ok
        // (although no method in the calling activity or fragment gets called that sets the users...)
        mListener.onItemUsersInvolvedSet(mUsersSelectedStatusQuo);
    }

    /**
     * Defines the actions to take when user clicks on one of the dialog's buttons.
     */
    public interface DialogInteractionListener {
        /**
         * Handles the click on the set new users involved button.
         *
         * @param userInvolved the selected users involved
         */
        void onItemUsersInvolvedSet(@NonNull List<Integer> userInvolved);
    }
}
