/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.ui.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import ch.giantific.qwittig.R;

/**
 * Shows the receipt image taken by the user when adding a new purchase.
 * <p/>
 * Subclass of {@link PurchaseReceiptBaseFragment}.
 */
public class PurchaseReceiptAddFragment extends PurchaseReceiptBaseFragment {

    private static final String BUNDLE_IMAGE_PATH = "BUNDLE_IMAGE_PATH";
    private FragmentInteractionListener mListener;
    private String mReceiptPath;

    public PurchaseReceiptAddFragment() {
    }

    /**
     * Returns a new instance of {@link PurchaseReceiptAddFragment}.
     *
     * @param imagePath the path to the receipt image taken
     * @return a new instance of {@link PurchaseReceiptAddFragment}
     */
    @NonNull
    public static PurchaseReceiptAddFragment newInstance(@NonNull String imagePath) {
        PurchaseReceiptAddFragment purchaseReceiptAddFragment = new PurchaseReceiptAddFragment();
        Bundle args = new Bundle();
        args.putString(BUNDLE_IMAGE_PATH, imagePath);
        purchaseReceiptAddFragment.setArguments(args);
        return purchaseReceiptAddFragment;
    }

    @Override
    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (FragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement DialogInteractionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
        getDataFromBundle();
    }

    void getDataFromBundle() {
        Bundle args = getArguments();
        if (args != null) {
            mReceiptPath = args.getString(BUNDLE_IMAGE_PATH, "");
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setData();
    }

    void setData() {
        setReceiptImage(mReceiptPath);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_purchase_edit_receipt, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_purchase_edit_receipt_edit:
                mListener.captureImage();
                return true;
            case R.id.action_purchase_edit_receipt_delete:
                mListener.deleteReceipt();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * Defines the interaction with the hosting {@link Activity}.
     */
    public interface FragmentInteractionListener {
        /**
         * Handles the request to delete the receipt image file from online Parse.com database.
         */
        void deleteReceipt();

        /**
         * Handles the request to capture a new image.
         */
        void captureImage();
    }
}
