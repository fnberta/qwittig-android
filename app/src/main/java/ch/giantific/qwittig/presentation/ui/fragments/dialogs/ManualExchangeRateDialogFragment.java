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
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.utils.MoneyUtils;

/**
 * Provides a dialog that allows the user to manually edit the currency exchange rate of a purchase.
 * <p/>
 * Subclass of {@link DialogFragment}.
 */
public class ManualExchangeRateDialogFragment extends DialogFragment {

    private static final String BUNDLE_EXCHANGE_RATE = "BUNDLE_EXCHANGE_RATE";
    private DialogInteractionListener mListener;
    private TextInputLayout mTextInputLayoutExchangeRate;
    @Nullable
    private String mAutoExchangeRate;

    /**
     * Returns a new instance of {@link ManualExchangeRateDialogFragment}.
     *
     * @param exchangeRate the currently set currency exchange rate
     * @return a new instance of {@link ManualExchangeRateDialogFragment}
     */
    @NonNull
    public static ManualExchangeRateDialogFragment newInstance(@NonNull String exchangeRate) {
        ManualExchangeRateDialogFragment fragment = new ManualExchangeRateDialogFragment();

        Bundle args = new Bundle();
        args.putString(BUNDLE_EXCHANGE_RATE, exchangeRate);
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

        Bundle args = getArguments();
        if (args != null) {
            mAutoExchangeRate = args.getString(BUNDLE_EXCHANGE_RATE);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_exchange_rate_manual, null);
        mTextInputLayoutExchangeRate = (TextInputLayout) view.findViewById(R.id.til_exchange_rate);
        if (!TextUtils.isEmpty(mAutoExchangeRate)) {
            mTextInputLayoutExchangeRate.getEditText().setText(mAutoExchangeRate);
        }

        dialogBuilder.setTitle(R.string.hint_exchange_rate)
                .setView(view)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String exchangeRate = mTextInputLayoutExchangeRate.getEditText().getText().toString().trim();
                        if (!TextUtils.isEmpty(exchangeRate)) {
                            mListener.onExchangeRateManuallySet(MoneyUtils.parsePrice(exchangeRate).floatValue());
                        }
                    }
                })
                .setNegativeButton(android.R.string.no, null);

        return dialogBuilder.create();
    }

    /**
     * Defines the actions to take when user clicks on one of the dialog's buttons.
     */
    public interface DialogInteractionListener {
        /**
         * Handles to click on the change exchange rate button.
         *
         * @param exchangeRate the newly set currency exchange rate
         */
        void onExchangeRateManuallySet(float exchangeRate);
    }
}
