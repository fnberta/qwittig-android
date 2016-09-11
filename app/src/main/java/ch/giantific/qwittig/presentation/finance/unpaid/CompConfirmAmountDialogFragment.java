/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.finance.unpaid;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.widget.Button;

import org.apache.commons.math3.fraction.BigFraction;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.databinding.DialogCompensationConfirmAmountBinding;
import ch.giantific.qwittig.presentation.common.fragments.dialogs.BaseDialogFragment;
import ch.giantific.qwittig.utils.MoneyUtils;

/**
 * Provides a dialog that allows the user to confirm a compensation and if needed change the amount.
 * <p/>
 * The dialog will only accept a proper amount value, therefore overrides the default
 * onClickListener which calls dismiss() in any case.
 * <p/>
 * Subclass of {@link DialogFragment}.
 */
public class CompConfirmAmountDialogFragment extends BaseDialogFragment<CompConfirmAmountDialogFragment.DialogInteractionListener> {

    private static final String DIALOG_TAG = CompConfirmAmountDialogFragment.class.getCanonicalName();
    private static final String KEY_AMOUNT = "AMOUNT";
    private static final String KEY_DEBTOR_NICKNAME = "DEBTOR_NICKNAME";
    private static final String KEY_CURRENCY = "CURRENCY";

    private DialogCompensationConfirmAmountBinding binding;
    private double amount;
    private String debtorNickname;
    private String currency;

    /**
     * Shows a new instance of {@link CompConfirmAmountDialogFragment}.
     *
     * @param fragmentManager the fragment manager to use for the transaction
     * @param amount          the amount currently set for the compensation
     * @param debtorNickname  the debtorNickname of the debtor
     * @param currency        the currency code to use to format the amount
     */
    public static void display(@NonNull FragmentManager fragmentManager,
                               @NonNull BigFraction amount,
                               @NonNull String debtorNickname,
                               @NonNull String currency) {
        final CompConfirmAmountDialogFragment dialog =
                CompConfirmAmountDialogFragment.newInstance(amount, debtorNickname, currency);
        dialog.show(fragmentManager, DIALOG_TAG);
    }

    @NonNull
    private static CompConfirmAmountDialogFragment newInstance(@NonNull BigFraction amount,
                                                               @NonNull String nickname,
                                                               @NonNull String currency) {
        CompConfirmAmountDialogFragment fragment = new CompConfirmAmountDialogFragment();

        Bundle args = new Bundle();
        args.putDouble(KEY_AMOUNT, amount.doubleValue());
        args.putString(KEY_DEBTOR_NICKNAME, nickname);
        args.putString(KEY_CURRENCY, currency);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            amount = args.getDouble(KEY_AMOUNT);
            debtorNickname = args.getString(KEY_DEBTOR_NICKNAME);
            currency = args.getString(KEY_CURRENCY);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final FragmentActivity activity = getActivity();
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
        binding = DialogCompensationConfirmAmountBinding.inflate(activity.getLayoutInflater());
        final String amountNoSymbol = MoneyUtils.getMoneyFormatter(currency, false, true).format(amount);
        binding.etDialogCompConfirmAmount.setText(amountNoSymbol);

        final String amountSymbol = MoneyUtils.getMoneyFormatter(currency, true, true).format(amount);
        dialogBuilder
                .setTitle(R.string.dialog_compensation_confirm_amount_title)
                .setMessage(getString(R.string.dialog_compensation_confirm_amount_message, debtorNickname, amountSymbol))
                .setView(binding.getRoot())
                .setPositiveButton(R.string.dialog_positive_confirm, null)
                .setNegativeButton(android.R.string.no, null);

        return dialogBuilder.create();
    }

    @Override
    public void onStart() {
        super.onStart();

        final AlertDialog dialog = (AlertDialog) getDialog();
        if (dialog == null) {
            return;
        }

        // Override View onClickListener because we only want the dialog to close when a valid
        // amount address was entered. Default behavior is to always call dismiss().
        final Button positiveButton = dialog.getButton(Dialog.BUTTON_POSITIVE);
        positiveButton.setOnClickListener(v -> {
            final String amountString = binding.etDialogCompConfirmAmount.getText().toString();
            final double amount1 = MoneyUtils.parsePrice(amountString);
            if (amount1 > 0) {
                if (amount1 > (CompConfirmAmountDialogFragment.this.amount * 2)) {
                    binding.tilDialogCompConfirmAmount.setError(getString(R.string.error_valid_amount_too_big));
                } else {
                    binding.tilDialogCompConfirmAmount.setErrorEnabled(false);
                    activity.onAmountConfirmed(amount1);
                    dismiss();
                }
            } else {
                binding.tilDialogCompConfirmAmount.setError(getString(R.string.error_valid_amount));
            }
        });
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
        void onAmountConfirmed(double amount);
    }
}
