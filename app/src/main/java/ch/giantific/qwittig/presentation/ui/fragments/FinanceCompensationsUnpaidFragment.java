/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.presentation.ui.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.apache.commons.math3.fraction.BigFraction;

import ch.giantific.qwittig.databinding.FragmentFinanceCompensationsUnpaidBinding;
import ch.giantific.qwittig.di.components.DaggerFinanceCompsUnpaidComponent;
import ch.giantific.qwittig.di.modules.FinanceCompsUnpaidViewModelModule;
import ch.giantific.qwittig.presentation.ui.adapters.CompensationsUnpaidRecyclerAdapter;
import ch.giantific.qwittig.presentation.ui.fragments.dialogs.CompensationConfirmAmountDialogFragment;
import ch.giantific.qwittig.presentation.viewmodels.FinanceCompsUnpaidViewModel;
import ch.giantific.qwittig.presentation.workerfragments.query.CompensationsUpdateWorker;
import ch.giantific.qwittig.presentation.workerfragments.reminder.CompensationRemindWorker;

/**
 * Displays all currently open unpaid compensations in the group in card based {@link RecyclerView}
 * list.
 * <p/>
 * Allows the user to create a new settlement if there are no open unpaid compensations.
 * <p/>
 * Subclass of {@link BaseRecyclerViewOnlineFragment}.
 */
public class FinanceCompensationsUnpaidFragment extends BaseRecyclerViewOnlineFragment<FinanceCompsUnpaidViewModel, FinanceCompensationsUnpaidFragment.ActivityListener>
        implements FinanceCompsUnpaidViewModel.ViewListener {

    private FragmentFinanceCompensationsUnpaidBinding mBinding;

    public FinanceCompensationsUnpaidFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DaggerFinanceCompsUnpaidComponent.builder()
                .financeCompsUnpaidViewModelModule(new FinanceCompsUnpaidViewModelModule(savedInstanceState, this))
                .build()
                .inject(this);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentFinanceCompensationsUnpaidBinding.inflate(inflater, container, false);
        mBinding.setViewModel(mViewModel);
        return mBinding.getRoot();
    }

    @Override
    protected RecyclerView getRecyclerView() {
        return mBinding.srlRv.rvBase;
    }

    @Override
    protected RecyclerView.Adapter getRecyclerAdapter() {
        return new CompensationsUnpaidRecyclerAdapter(mViewModel);
    }

    @Override
    protected void setViewModelToActivity() {
        mActivity.setCompsUnpaidViewModel(mViewModel);
    }

    @Override
    public void loadUpdateCompensationsUnpaidWorker() {
        CompensationsUpdateWorker.attach(getFragmentManager(), false);
    }

    @Override
    public void loadCompensationRemindWorker(@NonNull String compensationId) {
        CompensationRemindWorker.attach(getFragmentManager(), compensationId);
    }

    @Override
    public void showCompensationAmountConfirmDialog(@NonNull BigFraction amount,
                                                    @NonNull String debtorNickname,
                                                    @NonNull String currency) {
        CompensationConfirmAmountDialogFragment.show(getFragmentManager(), amount, debtorNickname,
                currency);
    }

    @Override
    public void setColorTheme(@NonNull BigFraction balance) {
        mActivity.setColorTheme(balance);
    }

    public interface ActivityListener extends BaseRecyclerViewOnlineFragment.ActivityListener {
        void setCompsUnpaidViewModel(@NonNull FinanceCompsUnpaidViewModel viewModel);

        void setColorTheme(@NonNull BigFraction balance);
    }
}
