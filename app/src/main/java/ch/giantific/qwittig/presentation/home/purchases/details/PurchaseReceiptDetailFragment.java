/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home.purchases.details;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ch.giantific.qwittig.Qwittig;
import ch.giantific.qwittig.databinding.FragmentPurchaseShowReceiptBinding;
import ch.giantific.qwittig.presentation.common.fragments.BaseFragment;
import ch.giantific.qwittig.presentation.home.purchases.common.PurchaseReceiptViewModel;
import ch.giantific.qwittig.presentation.home.purchases.common.di.DaggerPurchaseReceiptComponent;
import ch.giantific.qwittig.presentation.home.purchases.common.di.PurchaseReceiptViewModelModule;


/**
 * Shows the receipt image of a purchase when viewing its details.
 * <p/>
 * Subclass of {@link BaseFragment}.
 */
public class PurchaseReceiptDetailFragment extends BaseFragment<PurchaseReceiptViewModel, BaseFragment.ActivityListener>
        implements PurchaseReceiptViewModel.ViewListener {

    private static final String KEY_RECEIPT_IMAGE_URI = "RECEIPT_IMAGE_URI";
    private FragmentPurchaseShowReceiptBinding mBinding;

    public PurchaseReceiptDetailFragment() {
        // required empty constructor
    }

    /**
     * Returns a new instance of {@link PurchaseReceiptDetailFragment}.
     *
     * @param receiptImageUri the object id of the purchase of which the receipt image should be
     *                        displayed
     * @return a new instance of {@link PurchaseReceiptDetailFragment}
     */
    @NonNull
    public static PurchaseReceiptDetailFragment newInstance(@NonNull String receiptImageUri) {
        final PurchaseReceiptDetailFragment fragment = new PurchaseReceiptDetailFragment();
        final Bundle args = new Bundle();
        args.putString(KEY_RECEIPT_IMAGE_URI, receiptImageUri);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final String receiptImageUri = getArguments().getString(KEY_RECEIPT_IMAGE_URI, "");
        DaggerPurchaseReceiptComponent.builder()
                .applicationComponent(Qwittig.getAppComponent(getActivity()))
                .purchaseReceiptViewModelModule(new PurchaseReceiptViewModelModule(savedInstanceState, this, receiptImageUri))
                .build()
                .inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentPurchaseShowReceiptBinding.inflate(inflater, container, false);
        mBinding.setViewModel(mViewModel);
        return mBinding.getRoot();
    }

    @Override
    protected void setViewModelToActivity() {
        // do nothing
    }

    @Override
    protected View getSnackbarView() {
        return mBinding.ivReceipt;
    }

    @Override
    public void captureImage() {
        // not relevant
    }

    @Override
    public void showPurchaseScreen() {
        // not relevant
    }
}
