/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.finance.unpaid;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.apache.commons.math3.fraction.BigFraction;

import javax.inject.Inject;

import ch.giantific.qwittig.databinding.FragmentFinanceCompensationsUnpaidBinding;
import ch.giantific.qwittig.presentation.common.BaseFragment;
import ch.giantific.qwittig.presentation.common.BaseSortedListFragment;
import ch.giantific.qwittig.presentation.common.listadapters.BaseSortedListRecyclerAdapter;
import ch.giantific.qwittig.presentation.finance.di.FinanceSubcomponent;
import ch.giantific.qwittig.presentation.finance.unpaid.viewmodels.CompsUnpaidViewModel;
import ch.giantific.qwittig.presentation.finance.unpaid.viewmodels.items.CompUnpaidItemViewModel;

/**
 * Displays all currently open unpaid compensations in the group in card based {@link RecyclerView}
 * list.
 * <p/>
 * Allows the user to create a new settlement if there are no open unpaid compensations.
 * <p/>
 * Subclass of {@link BaseFragment}.
 */
public class CompsUnpaidFragment extends BaseSortedListFragment<FinanceSubcomponent,
        CompsUnpaidContract.Presenter,
        BaseSortedListFragment.ActivityListener<FinanceSubcomponent>,
        CompUnpaidItemViewModel>
        implements CompsUnpaidContract.ViewListener {

    @Inject
    CompsUnpaidViewModel viewModel;
    private FragmentFinanceCompensationsUnpaidBinding binding;

    public CompsUnpaidFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentFinanceCompensationsUnpaidBinding.inflate(inflater, container, false);
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
    protected BaseSortedListRecyclerAdapter<CompUnpaidItemViewModel, CompsUnpaidContract.Presenter, ? extends RecyclerView.ViewHolder> getRecyclerAdapter() {
        return new CompsUnpaidRecyclerAdapter(presenter);
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

    @Override
    public void showConfirmAmountDialog(@NonNull BigFraction amount,
                                        @NonNull String debtorNickname,
                                        @NonNull String currency) {
        CompConfirmAmountDialogFragment.display(getFragmentManager(), amount,
                debtorNickname, currency);
    }

    @Override
    public CompUnpaidItemViewModel getItemForId(@NonNull String id) {
        final int pos = getItemPositionForId(id);
        return recyclerAdapter.getItemAtPosition(pos);
    }
}
