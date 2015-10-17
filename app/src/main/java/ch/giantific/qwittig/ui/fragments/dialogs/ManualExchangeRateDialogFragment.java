package ch.giantific.qwittig.ui.fragments.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;

import ch.giantific.qwittig.R;

/**
 * Created by fabio on 20.11.14.
 */
public class ManualExchangeRateDialogFragment extends DialogFragment {

    private DialogInteractionListener mListener;
    private TextInputLayout mTextInputLayoutExchangeRate;
    private String mAutoExchangeRate;
    private static final String BUNDLE_EXCHANGE_RATE = "bundle_exchange_rate";

    public static ManualExchangeRateDialogFragment newInstance(String exchangeRate) {
        ManualExchangeRateDialogFragment fragment = new ManualExchangeRateDialogFragment();

        Bundle args = new Bundle();
        args.putString(BUNDLE_EXCHANGE_RATE, exchangeRate);
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
                            mListener.setExchangeRate(Float.parseFloat(exchangeRate));
                        }
                    }
                })
                .setNegativeButton(android.R.string.no, null);

        return dialogBuilder.create();
    }

    public interface DialogInteractionListener {
        void setExchangeRate(float exchangeRate);
    }
}
