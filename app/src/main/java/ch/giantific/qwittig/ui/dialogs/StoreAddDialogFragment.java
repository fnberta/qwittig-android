package ch.giantific.qwittig.ui.dialogs;

import android.app.Activity;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;

import ch.giantific.qwittig.R;

/**
 * Created by fabio on 20.11.14.
 */
public class StoreAddDialogFragment extends DialogFragment {

    private DialogInteractionListener mListener;
    private TextInputLayout mTextInputLayoutStore;

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
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_store_add, null);
        mTextInputLayoutStore = (TextInputLayout) view.findViewById(R.id.til_store);

        dialogBuilder.setTitle(R.string.add_store)
                .setView(view)
                .setPositiveButton(R.string.dialog_positive_add, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String storeName = mTextInputLayoutStore.getEditText().getText().toString().trim();
                        if (!TextUtils.isEmpty(storeName)) {
                            mListener.addStore(storeName);
                        }
                    }
                })
                .setNegativeButton(android.R.string.no, null);

        return dialogBuilder.create();
    }

    public interface DialogInteractionListener {
        void addStore(String storeName);
    }
}
