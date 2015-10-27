/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.ui.fragments.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.models.ItemUserPicker;
import ch.giantific.qwittig.ui.adapters.RecipientsArrayAdapter;

/**
 * Provides a dialog that allows the user to choose a recipient and set an amount for a single
 * compensation payment.
 * <p/>
 * The dialog will only accept a proper amount value, therefore overrides the standard
 * onClickListener which calls dismiss() in any case.
 * <p/>
 * Subclass of {@link DialogFragment}.
 */
public class CompensationSingleDialogFragment extends DialogFragment {

    private static final String BUNDLE_USERS = "BUNDLE_USERS";
    @Nullable
    private List<ItemUserPicker> mUsers;
    private DialogInteractionListener mListener;
    private Spinner mSpinnerUsers;
    private TextInputLayout mTextInputLayoutAmount;

    /**
     * Returns a new instance of {@link CompensationSingleDialogFragment}.
     *
     * @param users the users available as recipients
     * @return a new instance of {@link CompensationSingleDialogFragment}
     */
    @NonNull
    public static CompensationSingleDialogFragment newInstance(ArrayList<ItemUserPicker> users) {
        CompensationSingleDialogFragment fragment = new CompensationSingleDialogFragment();

        Bundle args = new Bundle();
        args.putParcelableArrayList(BUNDLE_USERS, users);
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
            mUsers = getArguments().getParcelableArrayList(BUNDLE_USERS);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_recipient, null);
        mSpinnerUsers = (Spinner) view.findViewById(R.id.sp_users);
        mTextInputLayoutAmount = (TextInputLayout) view.findViewById(R.id.til_amount);
        setupSpinner();

        dialogBuilder.setTitle(R.string.dialog_compensation_single_title)
                .setView(view)
                .setPositiveButton(R.string.dialog_positive_pay, null)
                .setNegativeButton(android.R.string.no, null);

        return dialogBuilder.create();
    }

    private void setupSpinner() {
        if (mUsers != null) {
            RecipientsArrayAdapter spinnerAdapter = new RecipientsArrayAdapter(getActivity(), mUsers);
            mSpinnerUsers.setAdapter(spinnerAdapter);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        // Override View onClickListener because we only want the dialog to close when a valid
        // amount was entered. Default behavior is to always call dismiss().
        AlertDialog dialog = (AlertDialog) getDialog();
        if (dialog != null) {
            Button positiveButton = dialog.getButton(Dialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    validateAmount();
                }
            });
        }
    }

    private void validateAmount() {
        String amountString = mTextInputLayoutAmount.getEditText().getText().toString();
        if (!TextUtils.isEmpty(amountString)) {
            mTextInputLayoutAmount.setErrorEnabled(false);
            ItemUserPicker recipientSelected = (ItemUserPicker) mSpinnerUsers.getSelectedItem();
            mListener.onSinglePaymentValuesSet(recipientSelected, amountString);
            dismiss();
        } else {
            mTextInputLayoutAmount.setError(getActivity().getString(R.string.error_valid_amount));
        }
    }

    /**
     * Defines the actions to take when user clicks on one of the dialog's buttons.
     */
    public interface DialogInteractionListener {
        /**
         * Handles the click on the save single payment button
         *
         * @param recipient the recipient chosen for the payment
         * @param amount    the amount set for the payment
         */
        void onSinglePaymentValuesSet(@NonNull ItemUserPicker recipient, @NonNull String amount);
    }
}
