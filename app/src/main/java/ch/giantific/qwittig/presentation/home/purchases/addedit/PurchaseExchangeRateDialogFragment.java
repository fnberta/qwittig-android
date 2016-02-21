/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home.purchases.addedit;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.databinding.DialogExchangeRateManualBinding;
import ch.giantific.qwittig.presentation.common.fragments.BaseDialogFragment;
import ch.giantific.qwittig.utils.MoneyUtils;

/**
 * Provides a dialog that allows the user to manually edit the currency exchange rate of a purchase.
 * <p/>
 * Subclass of {@link BaseDialogFragment}.
 */
public class PurchaseExchangeRateDialogFragment extends BaseDialogFragment<PurchaseExchangeRateDialogFragment.DialogInteractionListener> {

    private static final String DIALOG_TAG = PurchaseExchangeRateDialogFragment.class.getCanonicalName();
    private static final String KEY_EXCHANGE_RATE = "EXCHANGE_RATE";
    @Nullable
    private String mAutoExchangeRate;

    /**
     * Displays a new instance of {@link PurchaseExchangeRateDialogFragment}.
     *
     * @param fm           the fragment manager to use for the transaction
     * @param exchangeRate the currently set currency exchange rate
     */
    public static void display(@NonNull FragmentManager fm, @NonNull String exchangeRate) {
        final PurchaseExchangeRateDialogFragment dialog = new PurchaseExchangeRateDialogFragment();

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
            mAutoExchangeRate = args.getString(KEY_EXCHANGE_RATE);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final FragmentActivity activity = getActivity();
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
        final DialogExchangeRateManualBinding binding =
                DialogExchangeRateManualBinding.inflate(activity.getLayoutInflater());
        if (!TextUtils.isEmpty(mAutoExchangeRate)) {
            binding.etExchangeRate.setText(mAutoExchangeRate);
        }

        dialogBuilder.setTitle(R.string.hint_exchange_rate)
                .setView(binding.getRoot())
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final String exchangeRate = binding.etExchangeRate.getText().toString().trim();
                        if (!TextUtils.isEmpty(exchangeRate)) {
                            mActivity.onExchangeRateManuallySet(MoneyUtils.parsePrice(exchangeRate));
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
        void onExchangeRateManuallySet(double exchangeRate);
    }
}
