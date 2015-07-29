package ch.giantific.qwittig.ui.dialogs;

import android.app.Activity;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.models.ItemUserPicker;
import ch.giantific.qwittig.ui.adapter.RecipientsArrayAdapter;

/**
 * Created by fabio on 20.11.14.
 */
public class CompensationAddManualDialogFragment extends DialogFragment {

    private static final String BUNDLE_USERS = "bundle_users";
    private List<ItemUserPicker> mUsers;
    private DialogInteractionListener mListener;
    private Spinner mSpinnerUsers;
    private TextInputLayout mTextInputLayoutAmount;

    public static CompensationAddManualDialogFragment newInstance(ArrayList<ItemUserPicker> users) {
        CompensationAddManualDialogFragment fragment = new CompensationAddManualDialogFragment();

        Bundle args = new Bundle();
        args.putParcelableArrayList(BUNDLE_USERS, users);
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

        dialogBuilder.setTitle(R.string.dialog_compensation_manual_title)
                .setView(view)
                .setPositiveButton(R.string.dialog_positive_pay, null)
                .setNegativeButton(android.R.string.no, null);

        return dialogBuilder.create();
    }

    private void setupSpinner() {
        RecipientsArrayAdapter spinnerAdapter = new RecipientsArrayAdapter(getActivity(),
                R.layout.spinner_item_with_image, R.layout.row_spinner_recipients, mUsers);
        mSpinnerUsers.setAdapter(spinnerAdapter);
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
            ItemUserPicker recipientSelected = (ItemUserPicker) mSpinnerUsers.getSelectedItem();
            mListener.onManualPaymentValuesSet(recipientSelected, amountString);
            dismiss();
        } else {
            mTextInputLayoutAmount.setError(getActivity().getString(R.string.error_valid_amount));
        }
    }

    public interface DialogInteractionListener {
        public void onManualPaymentValuesSet(ItemUserPicker recipient, String amount);
    }
}
