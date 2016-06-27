/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.details;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ch.giantific.qwittig.databinding.FragmentPurchaseShowReceiptBinding;
import ch.giantific.qwittig.presentation.common.fragments.BaseFragment;
import ch.giantific.qwittig.presentation.purchases.details.di.PurchaseDetailsSubcomponent;


/**
 * Shows the receipt image of a purchase when viewing its details.
 * <p/>
 * Subclass of {@link BaseFragment}.
 */
public class PurchaseDetailsReceiptFragment extends BaseFragment<PurchaseDetailsSubcomponent, PurchaseDetailsViewModel, BaseFragment.ActivityListener<PurchaseDetailsSubcomponent>> {

    private FragmentPurchaseShowReceiptBinding mBinding;

    public PurchaseDetailsReceiptFragment() {
        // required empty constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentPurchaseShowReceiptBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mBinding.setViewModel(mViewModel);
    }

    @Override
    protected void injectDependencies(@NonNull PurchaseDetailsSubcomponent component) {
        component.inject(this);
    }

    @Override
    protected View getSnackbarView() {
        return mBinding.ivReceipt;
    }
}
