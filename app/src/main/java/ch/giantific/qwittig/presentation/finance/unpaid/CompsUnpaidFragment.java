/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.finance.unpaid;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.apache.commons.math3.fraction.BigFraction;

import ch.giantific.qwittig.databinding.FragmentFinanceCompensationsUnpaidBinding;
import ch.giantific.qwittig.presentation.common.adapters.BaseRecyclerAdapter;
import ch.giantific.qwittig.presentation.common.fragments.BaseRecyclerViewFragment;
import ch.giantific.qwittig.presentation.finance.di.FinanceSubcomponent;

/**
 * Displays all currently open unpaid compensations in the group in card based {@link RecyclerView}
 * list.
 * <p/>
 * Allows the user to create a new settlement if there are no open unpaid compensations.
 * <p/>
 * Subclass of {@link BaseRecyclerViewFragment}.
 */
public class CompsUnpaidFragment extends BaseRecyclerViewFragment<FinanceSubcomponent, CompsUnpaidViewModel, BaseRecyclerViewFragment.ActivityListener<FinanceSubcomponent>>
        implements CompsUnpaidViewModel.ViewListener {

    private FragmentFinanceCompensationsUnpaidBinding mBinding;

    public CompsUnpaidFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentFinanceCompensationsUnpaidBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mViewModel.attachView(this);
        mViewModel.setListInteraction(mRecyclerAdapter);
        mBinding.setViewModel(mViewModel);
    }

    @Override
    protected void injectDependencies(@NonNull FinanceSubcomponent component) {
        component.inject(this);
    }

    @Override
    protected RecyclerView getRecyclerView() {
        return mBinding.srlRv.rvBase;
    }

    @Override
    protected BaseRecyclerAdapter getRecyclerAdapter() {
        return new CompsUnpaidRecyclerAdapter(mViewModel);
    }

    @Override
    public void showCompensationAmountConfirmDialog(@NonNull BigFraction amount,
                                                    @NonNull String debtorNickname,
                                                    @NonNull String currency) {
        CompConfirmAmountDialogFragment.display(getFragmentManager(), amount,
                debtorNickname, currency);
    }
}
