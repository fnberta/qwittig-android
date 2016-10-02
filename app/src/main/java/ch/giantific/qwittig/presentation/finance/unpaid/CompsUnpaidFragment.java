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

import ch.giantific.qwittig.databinding.FragmentFinanceCompensationsUnpaidBinding;
import ch.giantific.qwittig.presentation.about.AboutRecyclerAdapter;
import ch.giantific.qwittig.presentation.common.BaseFragment;
import ch.giantific.qwittig.presentation.common.listadapters.BaseRecyclerAdapter;
import ch.giantific.qwittig.presentation.finance.di.FinanceSubcomponent;

/**
 * Displays all currently open unpaid compensations in the group in card based {@link RecyclerView}
 * list.
 * <p/>
 * Allows the user to create a new settlement if there are no open unpaid compensations.
 * <p/>
 * Subclass of {@link BaseFragment}.
 */
public class CompsUnpaidFragment extends BaseFragment<FinanceSubcomponent, CompsUnpaidContract.Presenter, BaseFragment.ActivityListener<FinanceSubcomponent>>
        implements CompsUnpaidContract.ViewListener {

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

        final CompsUnpaidRecyclerAdapter adapter = setupRecyclerView();
        presenter.attachView(this);
        presenter.setListInteraction(adapter);
        binding.setViewModel(presenter.getViewModel());
    }

    @Override
    protected void injectDependencies(@NonNull FinanceSubcomponent component) {
        component.inject(this);
    }

    private CompsUnpaidRecyclerAdapter setupRecyclerView() {
        binding.rvPb.rvBase.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.rvPb.rvBase.setHasFixedSize(true);
        final CompsUnpaidRecyclerAdapter adapter = new CompsUnpaidRecyclerAdapter(presenter);
        binding.rvPb.rvBase.setAdapter(adapter);

        return adapter;
    }

    @Override
    protected View getSnackbarView() {
        return binding.rvPb.rvBase;
    }

    @Override
    public void showCompensationAmountConfirmDialog(@NonNull BigFraction amount,
                                                    @NonNull String debtorNickname,
                                                    @NonNull String currency) {
        CompConfirmAmountDialogFragment.display(getFragmentManager(), amount,
                debtorNickname, currency);
    }
}
