/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.finance.unpaid;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.apache.commons.math3.fraction.BigFraction;

import ch.giantific.qwittig.data.services.ParseQueryService;
import ch.giantific.qwittig.databinding.FragmentFinanceCompensationsUnpaidBinding;
import ch.giantific.qwittig.presentation.common.fragments.BaseRecyclerViewFragment;
import ch.giantific.qwittig.presentation.finance.unpaid.di.DaggerFinanceCompsUnpaidComponent;
import ch.giantific.qwittig.presentation.finance.unpaid.di.FinanceCompsUnpaidViewModelModule;

/**
 * Displays all currently open unpaid compensations in the group in card based {@link RecyclerView}
 * list.
 * <p/>
 * Allows the user to create a new settlement if there are no open unpaid compensations.
 * <p/>
 * Subclass of {@link BaseRecyclerViewFragment}.
 */
public class CompsUnpaidFragment extends BaseRecyclerViewFragment<CompsUnpaidViewModel, CompsUnpaidFragment.ActivityListener>
        implements CompsUnpaidViewModel.ViewListener {

    private FragmentFinanceCompensationsUnpaidBinding mBinding;

    public CompsUnpaidFragment() {
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
        return new CompsUnpaidRecyclerAdapter(mViewModel);
    }

    @Override
    protected void setViewModelToActivity() {
        mActivity.setCompsUnpaidViewModel(mViewModel);
    }

    @Override
    public void startUpdateCompensationsUnpaidService() {
        ParseQueryService.startUpdateIdentities(getActivity());
        ParseQueryService.startUpdateCompensationsUnpaid(getActivity());
    }

    @Override
    public void loadCompensationRemindWorker(@NonNull String compensationId) {
        CompRemindWorker.attach(getFragmentManager(), compensationId);
    }

    @Override
    public void showCompensationAmountConfirmDialog(@NonNull BigFraction amount,
                                                    @NonNull String debtorNickname,
                                                    @NonNull String currency) {
        CompConfirmAmountDialogFragment.display(getFragmentManager(), amount, debtorNickname,
                currency);
    }

    @Override
    public void onCompensationConfirmed() {
        mActivity.reloadCompsPaid();
    }

    public interface ActivityListener extends BaseRecyclerViewFragment.ActivityListener {
        void setCompsUnpaidViewModel(@NonNull CompsUnpaidViewModel viewModel);

        void reloadCompsPaid();
    }
}
