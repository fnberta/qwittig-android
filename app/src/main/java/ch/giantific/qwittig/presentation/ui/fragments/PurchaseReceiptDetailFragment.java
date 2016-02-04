/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.presentation.ui.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;

import ch.giantific.qwittig.Qwittig;
import ch.giantific.qwittig.di.components.DaggerPurchaseReceiptIdComponent;
import ch.giantific.qwittig.di.modules.PurchaseReceiptIdViewModelModule;
import ch.giantific.qwittig.presentation.viewmodels.PurchaseReceiptViewModel;


/**
 * Shows the receipt image of a purchase when viewing its details.
 * <p/>
 * Subclass of {@link PurchaseReceiptBaseFragment}.
 */
public class PurchaseReceiptDetailFragment extends PurchaseReceiptBaseFragment<PurchaseReceiptViewModel, PurchaseReceiptBaseFragment.ActivityListener> {

    private static final String KEY_PURCHASE_ID = "PURCHASE_ID";

    public PurchaseReceiptDetailFragment() {
        // required empty constructor
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
        args.putString(KEY_PURCHASE_ID, purchaseId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final String purchaseId = getArguments().getString(KEY_PURCHASE_ID, "");
        DaggerPurchaseReceiptIdComponent.builder()
                .applicationComponent(Qwittig.getAppComponent(getActivity()))
                .purchaseReceiptIdViewModelModule(new PurchaseReceiptIdViewModelModule(savedInstanceState, this, purchaseId, false))
                .build()
                .inject(this);
    }
}
