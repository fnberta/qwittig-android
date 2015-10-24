package ch.giantific.qwittig.ui.fragments.dialogs;

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

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.utils.Utils;

/**
 * Created by fabio on 20.11.14.
 */
public class ResetPasswordDialogFragment extends DialogFragment {

    private static final String BUNDLE_EMAIL = "BUNDLE_EMAIL";
    private FragmentInteractionListener mListener;
    private String mEmail;
    private TextInputLayout mTextInputLayoutEmail;

    public static ResetPasswordDialogFragment newInstance(String email) {
        ResetPasswordDialogFragment fragment = new ResetPasswordDialogFragment();

        Bundle args = new Bundle();
        args.putString(BUNDLE_EMAIL, email);
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
            mEmail = getArguments().getString(BUNDLE_EMAIL);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_reset_password, null);
        mTextInputLayoutEmail = (TextInputLayout) view.findViewById(R.id.til_email);
        if (!TextUtils.isEmpty(mEmail)) {
            mTextInputLayoutEmail.getEditText().setText(mEmail);
        }

        dialogBuilder.setTitle(R.string.dialog_login_reset_password_title)
                .setMessage(R.string.dialog_login_reset_password_message)
                .setView(view)
                .setPositiveButton(R.string.dialog_positive_reset, null)
                .setNegativeButton(android.R.string.no, null);

        return dialogBuilder.create();
    }

    @Override
    public void onStart() {
        super.onStart();

        // Override View onClickListener because we only want the dialog to close when a valid
        // email address was entered. Default behavior is to always call dismiss().
        AlertDialog dialog = (AlertDialog) getDialog();
        if (dialog != null) {
            Button positiveButton = dialog.getButton(Dialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mEmail = mTextInputLayoutEmail.getEditText().getText().toString();
                    if (Utils.emailIsValid(mEmail)) {
                        mTextInputLayoutEmail.setErrorEnabled(false);
                        mListener.onResetPasswordSelected(mEmail);
                        dismiss();
                    } else {
                        mTextInputLayoutEmail.setError(getString(R.string.error_email));
                    }
                }
            });
        }
    }

    public interface FragmentInteractionListener {
        void onResetPasswordSelected(String email);
    }
}
