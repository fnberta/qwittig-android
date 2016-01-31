/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.presentation.ui.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;

import ch.giantific.qwittig.databinding.FragmentPurchaseShowReceiptBinding;
import ch.giantific.qwittig.presentation.viewmodels.PurchaseReceiptViewModel;

/**
 * Provides an abstract base class for screens that display purchase receipt images.
 * <p/>
 * Subclass of {@link BaseFragment}.
 */
public abstract class PurchaseReceiptBaseFragment<T extends PurchaseReceiptViewModel, S extends PurchaseReceiptBaseFragment.ActivityListener>
        extends BaseFragment<T, S>
        implements PurchaseReceiptViewModel.ViewListener {

    FragmentPurchaseShowReceiptBinding mBinding;

    public PurchaseReceiptBaseFragment() {
        // required empty constructor
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
    public void setReceiptImage(@NonNull byte[] receiptImage) {
        Glide.with(this)
                .load(receiptImage)
                .into(mBinding.ivReceipt);
    }

    @Override
    public void setReceiptImage(@NonNull String receiptPath) {
        Glide.with(this)
                .load(receiptPath)
                .into(mBinding.ivReceipt);
    }

    public interface ActivityListener extends BaseFragment.ActivityListener {
    }
}
