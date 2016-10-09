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

import javax.inject.Inject;

import ch.giantific.qwittig.databinding.FragmentPurchaseDetailsBinding;
import ch.giantific.qwittig.presentation.common.BaseFragment;
import ch.giantific.qwittig.presentation.purchases.details.di.PurchaseDetailsSubcomponent;
import ch.giantific.qwittig.presentation.purchases.details.viewmodels.PurchaseDetailsViewModel;


/**
 * Displays all items of a purchase, the users involved the total price and the share of the
 * current user. The store and date of the purchase are displayed in the hosting activity.
 *
 * @see PurchaseDetailsActivity
 */
public class PurchaseDetailsFragment extends BaseFragment<PurchaseDetailsSubcomponent,
        PurchaseDetailsContract.Presenter,
        BaseFragment.ActivityListener<PurchaseDetailsSubcomponent>> {

    @Inject
    PurchaseDetailsViewModel viewModel;
    private FragmentPurchaseDetailsBinding binding;
    private PurchaseDetailsRecyclerAdapter articlesRecyclerAdapter;
    private PurchaseDetailsIdentitiesRecyclerAdapter identitiesRecyclerAdapter;

    public PurchaseDetailsFragment() {
        // required empty constructor
    }

    public PurchaseDetailsRecyclerAdapter getArticlesRecyclerAdapter() {
        return articlesRecyclerAdapter;
    }

    public PurchaseDetailsIdentitiesRecyclerAdapter getIdentitiesRecyclerAdapter() {
        return identitiesRecyclerAdapter;
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

        setupArticleRecyclerView();
        setupIdentitiesRecyclerView();
        binding.setViewModel(viewModel);
    }

    @Override
    protected void injectDependencies(@NonNull PurchaseDetailsSubcomponent component) {
        component.inject(this);
    }

    private void setupArticleRecyclerView() {
        binding.rvPurchaseDetails.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.rvPurchaseDetails.setHasFixedSize(true);
        articlesRecyclerAdapter = new PurchaseDetailsRecyclerAdapter();
        binding.rvPurchaseDetails.setAdapter(articlesRecyclerAdapter);
    }

    private void setupIdentitiesRecyclerView() {
        binding.rvPurchaseDetailsIdentities.setHasFixedSize(true);
        binding.rvPurchaseDetailsIdentities.setLayoutManager(new LinearLayoutManager(getActivity(),
                LinearLayoutManager.HORIZONTAL, false));
        identitiesRecyclerAdapter = new PurchaseDetailsIdentitiesRecyclerAdapter();
        binding.rvPurchaseDetailsIdentities.setAdapter(identitiesRecyclerAdapter);
    }

    @Override
    protected View getSnackbarView() {
        return binding.rvPurchaseDetails;
    }
}
