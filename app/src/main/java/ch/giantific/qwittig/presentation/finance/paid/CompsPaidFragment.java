/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.finance.paid;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ch.giantific.qwittig.databinding.FragmentFinanceCompensationsPaidBinding;
import ch.giantific.qwittig.presentation.common.BaseFragment;
import ch.giantific.qwittig.presentation.common.listadapters.BaseRecyclerAdapter;
import ch.giantific.qwittig.presentation.finance.di.FinanceSubcomponent;
import ch.giantific.qwittig.presentation.finance.unpaid.CompsUnpaidRecyclerAdapter;

/**
 * Displays recent paid compensations in a {@link RecyclerView} list.
 * <p/>
 * Subclass of {@link BaseFragment}.
 */
public class CompsPaidFragment extends BaseFragment<FinanceSubcomponent, CompsPaidContract.Presenter, BaseFragment.ActivityListener<FinanceSubcomponent>>
        implements CompsPaidContract.ViewListener {

    private FragmentFinanceCompensationsPaidBinding binding;

    public CompsPaidFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentFinanceCompensationsPaidBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final CompsPaidRecyclerAdapter adapter = setupRecyclerView();
        presenter.attachView(this);
        presenter.setListInteraction(adapter);
        binding.setViewModel(presenter.getViewModel());
    }

    @Override
    protected void injectDependencies(@NonNull FinanceSubcomponent component) {
        component.inject(this);
    }

    private CompsPaidRecyclerAdapter setupRecyclerView() {
        binding.rvPb.rvBase.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.rvPb.rvBase.setHasFixedSize(true);
        final CompsPaidRecyclerAdapter adapter = new CompsPaidRecyclerAdapter(presenter);
        binding.rvPb.rvBase.setAdapter(adapter);

        return adapter;
    }

    @Override
    protected View getSnackbarView() {
        return binding.rvPb.rvBase;
    }
}
