/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.details;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ch.giantific.qwittig.databinding.FragmentPurchaseDetailsBinding;
import ch.giantific.qwittig.presentation.common.BaseFragment;
import ch.giantific.qwittig.presentation.purchases.details.di.PurchaseDetailsSubcomponent;


/**
 * Displays all items of a purchase, the users involved the total price and the share of the
 * current user. The store and date of the purchase are displayed in the hosting activity.
 *
 * @see PurchaseDetailsActivity
 */
public class PurchaseDetailsFragment extends BaseFragment<PurchaseDetailsSubcomponent, PurchaseDetailsContract.Presenter, BaseFragment.ActivityListener<PurchaseDetailsSubcomponent>> {

    private FragmentPurchaseDetailsBinding binding;

    public PurchaseDetailsFragment() {
        // required empty constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPurchaseDetailsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final PurchaseDetailsRecyclerAdapter adapter = setupRecyclerView();
        setupIdentitiesList();
        presenter.setListInteraction(adapter);
        binding.setViewModel(presenter.getViewModel());
    }

    @Override
    protected void injectDependencies(@NonNull PurchaseDetailsSubcomponent component) {
        component.inject(this);
    }

    private PurchaseDetailsRecyclerAdapter setupRecyclerView() {
        binding.rvPurchaseDetails.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.rvPurchaseDetails.setHasFixedSize(true);
        final PurchaseDetailsRecyclerAdapter adapter = new PurchaseDetailsRecyclerAdapter(presenter);
        binding.rvPurchaseDetails.setAdapter(adapter);

        return adapter;
    }

    private void setupIdentitiesList() {
        binding.rvPurchaseDetailsIdentities.setHasFixedSize(true);
        binding.rvPurchaseDetailsIdentities.setLayoutManager(new LinearLayoutManager(getActivity(),
                LinearLayoutManager.HORIZONTAL, false));
        final PurchaseDetailsIdentitiesRecyclerAdapter adapter =
                new PurchaseDetailsIdentitiesRecyclerAdapter(presenter);
        binding.rvPurchaseDetailsIdentities.setAdapter(adapter);
        presenter.setIdentitiesListInteraction(adapter);
    }

    @Override
    protected View getSnackbarView() {
        return binding.rvPurchaseDetails;
    }
}
