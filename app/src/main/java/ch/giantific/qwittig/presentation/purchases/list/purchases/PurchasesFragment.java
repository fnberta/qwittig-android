/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.list.purchases;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

import ch.giantific.qwittig.databinding.FragmentHomePurchasesBinding;
import ch.giantific.qwittig.presentation.common.BaseFragment;
import ch.giantific.qwittig.presentation.common.BaseSortedListFragment;
import ch.giantific.qwittig.presentation.common.listadapters.BaseSortedListRecyclerAdapter;
import ch.giantific.qwittig.presentation.purchases.list.di.HomeSubcomponent;
import ch.giantific.qwittig.presentation.purchases.list.purchases.viewmodels.PurchasesViewModel;
import ch.giantific.qwittig.presentation.purchases.list.purchases.viewmodels.items.PurchaseItemViewModel;

/**
 * Displays recent purchases in a {@link RecyclerView} list.
 */
public class PurchasesFragment extends BaseSortedListFragment<HomeSubcomponent,
        PurchasesContract.Presenter,
        BaseFragment.ActivityListener<HomeSubcomponent>,
        PurchaseItemViewModel>
        implements PurchasesContract.ViewListener {

    @Inject
    PurchasesViewModel viewModel;
    private FragmentHomePurchasesBinding binding;

    public PurchasesFragment() {
        // required empty constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomePurchasesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        presenter.attachView(this);
        binding.setViewModel(viewModel);
    }

    @Override
    protected void injectDependencies(@NonNull HomeSubcomponent component) {
        component.inject(this);
    }

    @Override
    protected BaseSortedListRecyclerAdapter<PurchaseItemViewModel, PurchasesContract.Presenter, ? extends RecyclerView.ViewHolder> getRecyclerAdapter() {
        return new PurchasesRecyclerAdapter(presenter);
    }

    @Override
    protected void setupRecyclerView() {
        binding.rvPb.rvBase.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.rvPb.rvBase.setHasFixedSize(true);
        binding.rvPb.rvBase.setAdapter(recyclerAdapter);
    }

    @Override
    protected View getSnackbarView() {
        return binding.rvPb.rvBase;
    }
}
