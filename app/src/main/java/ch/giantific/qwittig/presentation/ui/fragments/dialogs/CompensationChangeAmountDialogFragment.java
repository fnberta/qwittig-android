/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.presentation.ui.fragments.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import org.apache.commons.math3.fraction.BigFraction;

import java.math.BigDecimal;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.domain.models.parse.Compensation;
import ch.giantific.qwittig.utils.MoneyUtils;

/**
 * Provides a dialog that allows the user to change the amount of a {@link Compensation}.
 * <p/>
 * The dialog will only accept a proper new amount value, therefore overrides the standard
 * onClickListener which calls dismiss() in any case.
 * <p/>
 * Subclass of {@link DialogFragment}.
 */
public class CompensationChangeAmountDialogFragment extends DialogFragment {

    private static final String BUNDLE_AMOUNT_OLD = "BUNDLE_AMOUNT_OLD";
    private static final String BUNDLE_CURRENCY = "BUNDLE_CURRENCY";
    private DialogInteractionListener mListener;
    private double mAmountOld;
    @Nullable
    private String mCurrentGroupCurrency;
    private TextInputLayout mTextInputLayoutAmount;

    /**
     * Returns a new instance of {@link CompensationChangeAmountDialogFragment}.
     *
     * @param amountOld the amount currently set for the compensation
     * @param currency  the currency code to use to format the amount
     * @return a new instance of {@link CompensationChangeAmountDialogFragment}
     */
    @NonNull
    public static CompensationChangeAmountDialogFragment newInstance(@NonNull BigFraction amountOld,
                                                                     String currency) {
        CompensationChangeAmountDialogFragment fragment = new CompensationChangeAmountDialogFragment();

        Bundle args = new Bundle();
        args.putDouble(BUNDLE_AMOUNT_OLD, amountOld.doubleValue());
        args.putString(BUNDLE_CURRENCY, currency);
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
                    + " must implement SeparateBillFragmentCallback");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            mAmountOld = args.getDouble(BUNDLE_AMOUNT_OLD);
            mCurrentGroupCurrency = args.getString(BUNDLE_CURRENCY);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_change_amount, null);
        mTextInputLayoutAmount = (TextInputLayout) view.findViewById(R.id.til_amount);
        if (mAmountOld != 0) {
            String amountOld = MoneyUtils.formatMoneyNoSymbol(mAmountOld, mCurrentGroupCurrency);
            mTextInputLayoutAmount.getEditText().setText(amountOld);
        }

        dialogBuilder.setMessage(R.string.dialog_change_amount_message)
                .setView(view)
                .setPositiveButton(R.string.dialog_positive_change, null)
                .setNegativeButton(android.R.string.no, null);

        return dialogBuilder.create();
    }

    @Override
    public void onStart() {
        super.onStart();

        // Override View onClickListener because we only want the dialog to close when a valid
        // amount address was entered. Default behavior is to always call dismiss().
        AlertDialog dialog = (AlertDialog) getDialog();
        if (dialog != null) {
            Button positiveButton = dialog.getButton(Dialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String amountNewString = mTextInputLayoutAmount.getEditText().getText().toString();
                    int maxFractionDigits = MoneyUtils.getMaximumFractionDigits(mCurrentGroupCurrency);
                    BigDecimal amount = MoneyUtils.parsePrice(amountNewString)
                            .setScale(maxFractionDigits, BigDecimal.ROUND_HALF_UP);
                    BigFraction amountNew = new BigFraction(amount.doubleValue());
                    if (amountNew.compareTo(BigFraction.ZERO) > 0) {
                        mTextInputLayoutAmount.setErrorEnabled(false);
                        mListener.onChangedAmountSet(amountNew);
                        dismiss();
                    } else {
                        mTextInputLayoutAmount.setError(getString(R.string.error_valid_amount));
                    }
                }
            });
        }
    }

    /**
     * Defines the actions to take when user clicks on one of the dialog's buttons.
     */
    public interface DialogInteractionListener {
        /**
         * Handles the click on the save new amount button.
         *
         * @param amount the new amount
         */
        void onChangedAmountSet(@NonNull BigFraction amount);
    }
}
