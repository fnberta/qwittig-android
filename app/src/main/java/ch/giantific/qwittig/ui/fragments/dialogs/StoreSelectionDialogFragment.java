/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.ui.fragments.dialogs;

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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.domain.models.parse.User;

/**
 * Provides a dialog that allows the user to make the store selection for a purchase by either
 * selecting one in the list or adding a new one.
 * <p/>
 * If the user chose to add a new store, a valid name must be entered. Otherwise the dialog will
 * only be dismissed by the cancel action. Therefore overrides the default positive button
 * onClickListener because the default behaviour is to always call dismiss().
 * <p/>
 * Subclass of {@link DialogFragment}.
 */
public class StoreSelectionDialogFragment extends DialogFragment {

    private static final String LOG_TAG = StoreSelectionDialogFragment.class.getSimpleName();
    private static final String BUNDLE_STORE = "BUNDLE_STORE";
    private DialogInteractionListener mListener;
    @Nullable
    private String mStoreSelected;
    private TextInputLayout mTextInputLayoutStoreManual;
    private Spinner mSpinnerStore;
    private String mOtherStore;

    /**
     * Returns a new instance of {@link StoreSelectionDialogFragment}.
     *
     * @param store the currently selected store
     * @return a new instance of {@link StoreSelectionDialogFragment}
     */
    @NonNull
    public static StoreSelectionDialogFragment newInstance(@NonNull String store) {
        StoreSelectionDialogFragment fragment = new StoreSelectionDialogFragment();

        Bundle args = new Bundle();
        args.putString(BUNDLE_STORE, store);
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
            mStoreSelected = getArguments().getString(BUNDLE_STORE);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_store_selection, null);
        mSpinnerStore = (Spinner) view.findViewById(R.id.sp_store);
        mTextInputLayoutStoreManual = (TextInputLayout) view.findViewById(R.id.til_store);
        setupSpinner();

        dialogBuilder.setMessage(getString(R.string.dialog_store_selector, mOtherStore))
                .setView(view)
                .setPositiveButton(android.R.string.yes, null)
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(@NonNull DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        return dialogBuilder.create();
    }

    private void setupSpinner() {
        List<String> stores;
        User currentUser = (User) ParseUser.getCurrentUser();
        List<String> storesFavorites = currentUser.getStoresFavorites();
        if (!storesFavorites.isEmpty()) {
            stores = new ArrayList<>(storesFavorites.size() + 1);
            stores.addAll(storesFavorites);
        } else {
            stores = new ArrayList<>(1);
        }
        mOtherStore = getString(R.string.dialog_store_other);
        stores.add(mOtherStore);

        final ArrayAdapter<String> spinnerStoreDataAdapter = new ArrayAdapter<>(getActivity(),
                R.layout.spinner_item, stores);
        spinnerStoreDataAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        mSpinnerStore.setAdapter(spinnerStoreDataAdapter);
        mSpinnerStore.setSelection(spinnerStoreDataAdapter.getPosition(mStoreSelected));
        mSpinnerStore.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String storeSelected = spinnerStoreDataAdapter.getItem(position);
                if (storeSelected.equals(mOtherStore)) {
                    mTextInputLayoutStoreManual.setVisibility(View.VISIBLE);
                } else {
                    mTextInputLayoutStoreManual.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        // Override View onClickListener because we only want the dialog to close when a valid
        // store name was entered. Default behavior is to always call dismiss().
        AlertDialog dialog = (AlertDialog) getDialog();
        if (dialog != null) {
            Button positiveButton = dialog.getButton(Dialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setStore();
                }
            });
        }
    }

    private void setStore() {
        String storeSelected = mSpinnerStore.getSelectedItem().toString();
        if (storeSelected.equals(mOtherStore)) {
            mStoreSelected = mTextInputLayoutStoreManual.getEditText().getText().toString().trim();
            if (TextUtils.isEmpty(mStoreSelected)) {
                mTextInputLayoutStoreManual.setError(getString(R.string.error_store));
            } else {
                mTextInputLayoutStoreManual.setErrorEnabled(false);
                mListener.onStoreSet(mStoreSelected, true);
                dismiss();
            }
        } else {
            mStoreSelected = storeSelected;
            mListener.onStoreSet(mStoreSelected, false);
            dismiss();
        }
    }

    /**
     * Defines the actions to take when user clicks on one of the dialog's buttons.
     */
    public interface DialogInteractionListener {
        /**
         * Handles the click on the store selected button
         *
         * @param store           the store selected
         * @param manuallyEntered whether the store was newly added manually or chosen from the list
         */
        void onStoreSet(@NonNull String store, boolean manuallyEntered);
    }
}