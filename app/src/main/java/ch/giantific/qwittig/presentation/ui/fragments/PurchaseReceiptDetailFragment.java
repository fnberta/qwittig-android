/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.presentation.ui.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;


/**
 * Shows the receipt image of a purchase when viewing its details.
 * <p/>
 * Subclass of {@link PurchaseReceiptBaseFragment}.
 */
public class PurchaseReceiptDetailFragment extends PurchaseReceiptBaseFragment {

    static final String BUNDLE_PURCHASE_ID = "BUNDLE_PURCHASE_ID";
    private String mPurchaseId;

    public PurchaseReceiptDetailFragment() {
    }

    /**
     * Returns a new instance of {@link PurchaseReceiptDetailFragment}.
     *
     * @param purchaseId the object id of the purchase of which the receipt image should be
     *                   displayed
     * @return a new instance of {@link PurchaseReceiptDetailFragment}
     */
    @NonNull
    public static PurchaseReceiptDetailFragment newInstance(@NonNull String purchaseId) {
        PurchaseReceiptDetailFragment fragment = new PurchaseReceiptDetailFragment();
        Bundle args = new Bundle();
        args.putString(BUNDLE_PURCHASE_ID, purchaseId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            mPurchaseId = args.getString(BUNDLE_PURCHASE_ID, "");
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        loadData();
    }

    /**
     * Fetches the purchase from the object id and displays the receipt.
     */
    public void loadData() {
        mPurchaseRepo.fetchPurchaseDataLocalAsync(mPurchaseId);
    }
}
