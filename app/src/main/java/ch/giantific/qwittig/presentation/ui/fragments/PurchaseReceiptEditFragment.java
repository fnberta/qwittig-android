/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.presentation.ui.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;

import ch.giantific.qwittig.domain.models.parse.Purchase;
import ch.giantific.qwittig.domain.repositories.PurchaseRepository;

/**
 * Shows the receipt image taken by the user when editing a purchase.
 * <p/>
 * Subclass of {@link PurchaseReceiptBaseFragment}.
 */
public class PurchaseReceiptEditFragment extends PurchaseReceiptAddFragment implements
        PurchaseRepository.GetPurchaseLocalListener {

    private static final String BUNDLE_PURCHASE_ID = "BUNDLE_PURCHASE_ID";
    private static final String BUNDLE_IS_DRAFT = "BUNDLE_IS_DRAFT";
    private String mPurchaseId;
    private boolean mIsDraft;

    public PurchaseReceiptEditFragment() {
    }

    /**
     * Returns a new instance of {@link PurchaseReceiptEditFragment}.
     *
     * @param purchaseId the object id of the purchase of which the receipt image should be shown
     * @param isDraft    whether the purchase is a draft or not
     * @return a new instance of {@link PurchaseReceiptEditFragment}
     */
    @NonNull
    public static PurchaseReceiptEditFragment newInstance(@NonNull String purchaseId,
                                                          boolean isDraft) {
        PurchaseReceiptEditFragment fragment = new PurchaseReceiptEditFragment();

        Bundle args = new Bundle();
        args.putString(BUNDLE_PURCHASE_ID, purchaseId);
        args.putBoolean(BUNDLE_IS_DRAFT, isDraft);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    void getDataFromBundle() {
        Bundle args = getArguments();
        if (args != null) {
            mPurchaseId = args.getString(BUNDLE_PURCHASE_ID, "");
            mIsDraft = args.getBoolean(BUNDLE_IS_DRAFT);
        }
    }

    @Override
    void setData() {
        if (mIsDraft) {
            mPurchaseRepo.getPurchaseLocalAsync(mPurchaseId, true);
        } else {
            mPurchaseRepo.fetchPurchaseDataLocalAsync(mPurchaseId);
        }
    }

    @Override
    public void onPurchaseLocalLoaded(@NonNull Purchase purchase) {
        if (mIsDraft) {
            setImage(purchase.getReceiptData());
        } else {
            setReceiptImage(purchase.getReceiptParseFile());
        }
    }
}
