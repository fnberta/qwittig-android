/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.addedit;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.databinding.DialogExchangeRateManualBinding;
import ch.giantific.qwittig.presentation.common.dialogs.BaseDialogFragment;
import ch.giantific.qwittig.utils.MoneyUtils;

/**
 * Provides a dialog that allows the user to manually edit the currency exchange rate of a purchase.
 * <p/>
 * Subclass of {@link BaseDialogFragment}.
 */
public class ExchangeRateDialogFragment extends BaseDialogFragment<ExchangeRateDialogFragment.DialogInteractionListener> {

    private static final String DIALOG_TAG = ExchangeRateDialogFragment.class.getCanonicalName();
    private static final String KEY_EXCHANGE_RATE = "EXCHANGE_RATE";
    private String exchangeRate;

    /**
     * Displays a new instance of {@link ExchangeRateDialogFragment}.
     *
     * @param fm           the fragment manager to use for the transaction
     * @param exchangeRate the currently set currency exchange rate
     */
    public static void display(@NonNull FragmentManager fm, @NonNull String exchangeRate) {
        final ExchangeRateDialogFragment dialog = new ExchangeRateDialogFragment();

        final Bundle args = new Bundle();
        args.putString(KEY_EXCHANGE_RATE, exchangeRate);
        dialog.setArguments(args);

        dialog.show(fm, DIALOG_TAG);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            exchangeRate = args.getString(KEY_EXCHANGE_RATE);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final FragmentActivity activity = getActivity();
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
        final DialogExchangeRateManualBinding binding =
                DialogExchangeRateManualBinding.inflate(activity.getLayoutInflater());
        if (!TextUtils.isEmpty(exchangeRate)) {
            binding.etExchangeRate.setText(exchangeRate);
        }

        dialogBuilder.setTitle(R.string.hint_exchange_rate)
                .setView(binding.getRoot())
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    final String exchangeRate1 = binding.etExchangeRate.getText().toString().trim();
                    if (!TextUtils.isEmpty(exchangeRate1)) {
                        ExchangeRateDialogFragment.this.activity.onExchangeRateManuallySet(MoneyUtils.parsePrice(exchangeRate1));
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
        void onExchangeRateManuallySet(double exchangeRate);
    }
}
