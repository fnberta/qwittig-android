/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.details;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ch.giantific.qwittig.databinding.FragmentPurchaseDetailsBinding;
import ch.giantific.qwittig.presentation.common.adapters.BaseRecyclerAdapter;
import ch.giantific.qwittig.presentation.common.fragments.BaseFragment;
import ch.giantific.qwittig.presentation.common.fragments.BaseRecyclerViewFragment;
import ch.giantific.qwittig.presentation.purchases.details.di.PurchaseDetailsSubcomponent;


/**
 * Displays all items of a purchase, the users involved the total price and the share of the
 * current user. The store and date of the purchase are displayed in the hosting activity.
 * <p/>
 * Subclass of {@link BaseRecyclerViewFragment}.
 *
 * @see PurchaseDetailsActivity
 */
public class PurchaseDetailsFragment extends BaseRecyclerViewFragment<PurchaseDetailsSubcomponent, PurchaseDetailsViewModel, BaseFragment.ActivityListener<PurchaseDetailsSubcomponent>> {

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

        setupIdentitiesList();
        viewModel.setListInteraction(recyclerAdapter);
        binding.setViewModel(viewModel);
    }

    @Override
    protected void injectDependencies(@NonNull PurchaseDetailsSubcomponent component) {
        component.inject(this);
    }

    private void setupIdentitiesList() {
        binding.rvPurchaseDetailsIdentities.setHasFixedSize(true);
        binding.rvPurchaseDetailsIdentities.setLayoutManager(new LinearLayoutManager(getActivity(),
                LinearLayoutManager.HORIZONTAL, false));
        final PurchaseDetailsIdentitiesRecyclerAdapter adapter =
                new PurchaseDetailsIdentitiesRecyclerAdapter(viewModel);
        binding.rvPurchaseDetailsIdentities.setAdapter(adapter);
        viewModel.setIdentitiesListInteraction(adapter);
    }

    @Override
    protected RecyclerView getRecyclerView() {
        return binding.rvPurchaseDetails;
    }

    @Override
    protected BaseRecyclerAdapter getRecyclerAdapter() {
        return new PurchaseDetailsRecyclerAdapter(viewModel);
    }
}
