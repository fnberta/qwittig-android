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

import javax.inject.Inject;

import ch.giantific.qwittig.databinding.FragmentFinanceCompensationsPaidBinding;
import ch.giantific.qwittig.presentation.common.BaseFragment;
import ch.giantific.qwittig.presentation.common.BaseSortedListFragment;
import ch.giantific.qwittig.presentation.common.listadapters.BaseSortedListRecyclerAdapter;
import ch.giantific.qwittig.presentation.finance.di.FinanceSubcomponent;
import ch.giantific.qwittig.presentation.finance.paid.viewmodels.CompsPaidViewModel;
import ch.giantific.qwittig.presentation.finance.paid.viewmodels.items.CompPaidItemViewModel;

/**
 * Displays recent paid compensations in a {@link RecyclerView} list.
 * <p/>
 * Subclass of {@link BaseFragment}.
 */
public class CompsPaidFragment extends BaseSortedListFragment<FinanceSubcomponent,
        CompsPaidContract.Presenter,
        BaseFragment.ActivityListener<FinanceSubcomponent>,
        CompPaidItemViewModel>
        implements CompsPaidContract.ViewListener {

    @Inject
    CompsPaidViewModel viewModel;
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

        setupRecyclerView();
        presenter.attachView(this);
        binding.setViewModel(viewModel);
    }

    @Override
    protected void injectDependencies(@NonNull FinanceSubcomponent component) {
        component.inject(this);
    }

    @Override
    protected BaseSortedListRecyclerAdapter<CompPaidItemViewModel, CompsPaidContract.Presenter,
            ? extends RecyclerView.ViewHolder> getRecyclerAdapter() {
        return new CompsPaidRecyclerAdapter(presenter);
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
