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

import ch.giantific.qwittig.databinding.FragmentHomePurchasesBinding;
import ch.giantific.qwittig.presentation.common.listadapters.BaseRecyclerAdapter;
import ch.giantific.qwittig.presentation.common.BaseFragment;
import ch.giantific.qwittig.presentation.purchases.list.di.HomeSubcomponent;

/**
 * Displays recent purchases in a {@link RecyclerView} list.
 */
public class PurchasesFragment extends BaseFragment<HomeSubcomponent, PurchasesContract.Presenter, BaseFragment.ActivityListener<HomeSubcomponent>>
        implements PurchasesContract.ViewListener {

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

        final PurchasesRecyclerAdapter adapter = setupRecyclerView();
        presenter.attachView(this);
        presenter.setListInteraction(adapter);
        binding.setViewModel(presenter.getViewModel());
    }

    @Override
    protected void injectDependencies(@NonNull HomeSubcomponent component) {
        component.inject(this);
    }

    private PurchasesRecyclerAdapter setupRecyclerView() {
        binding.rvPb.rvBase.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.rvPb.rvBase.setHasFixedSize(true);
        final PurchasesRecyclerAdapter adapter = new PurchasesRecyclerAdapter(presenter);
        binding.rvPb.rvBase.setAdapter(adapter);

        return adapter;
    }

    @Override
    protected View getSnackbarView() {
        return binding.rvPb.rvBase;
    }
}
