/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.details;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

import ch.giantific.qwittig.databinding.FragmentPurchaseShowReceiptBinding;
import ch.giantific.qwittig.presentation.common.BaseFragment;
import ch.giantific.qwittig.presentation.purchases.details.di.PurchaseDetailsSubcomponent;
import ch.giantific.qwittig.presentation.purchases.details.viewmodels.PurchaseDetailsViewModel;


/**
 * Shows the receipt image of a purchase when viewing its details.
 * <p/>
 * Subclass of {@link BaseFragment}.
 */
public class PurchaseDetailsReceiptFragment extends BaseFragment<PurchaseDetailsSubcomponent,
        PurchaseDetailsContract.Presenter,
        BaseFragment.ActivityListener<PurchaseDetailsSubcomponent>> {

    @Inject
    PurchaseDetailsViewModel viewModel;
    private FragmentPurchaseShowReceiptBinding binding;

    public PurchaseDetailsReceiptFragment() {
        // required empty constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPurchaseShowReceiptBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        binding.setViewModel(viewModel);
    }

    @Override
    protected void injectDependencies(@NonNull PurchaseDetailsSubcomponent component) {
        component.inject(this);
    }

    @Override
    protected View getSnackbarView() {
        return binding.ivReceipt;
    }
}
