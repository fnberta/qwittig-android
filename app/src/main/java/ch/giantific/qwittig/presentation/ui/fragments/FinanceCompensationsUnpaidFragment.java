/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.presentation.ui.fragments;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.apache.commons.math3.fraction.BigFraction;

import ch.giantific.qwittig.databinding.FragmentFinanceCompensationsUnpaidBinding;
import ch.giantific.qwittig.di.components.DaggerFinanceComponent;
import ch.giantific.qwittig.di.modules.FinanceViewModelModule;
import ch.giantific.qwittig.presentation.ui.adapters.CompensationsUnpaidRecyclerAdapter;
import ch.giantific.qwittig.presentation.ui.fragments.dialogs.CompensationChangeAmountDialogFragment;
import ch.giantific.qwittig.presentation.viewmodels.FinanceCompsUnpaidViewModel;
import ch.giantific.qwittig.presentation.workerfragments.query.CompensationsUpdateWorker;
import ch.giantific.qwittig.presentation.workerfragments.reminder.CompensationRemindWorker;
import ch.giantific.qwittig.utils.WorkerUtils;

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

    private static final String LOG_TAG = FinanceCompensationsUnpaidFragment.class.getSimpleName();
    private static final String CHANGE_AMOUNT_DIALOG = "CHANGE_AMOUNT_DIALOG";
    private FragmentFinanceCompensationsUnpaidBinding mBinding;

    public FinanceCompensationsUnpaidFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DaggerFinanceComponent.builder()
                .financeViewModelModule(new FinanceViewModelModule(savedInstanceState))
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
        final FragmentManager fragmentManager = getFragmentManager();
        Fragment queryWorker = WorkerUtils.findWorker(fragmentManager, CompensationsUpdateWorker.WORKER_TAG);
        if (queryWorker == null) {
            queryWorker = CompensationsUpdateWorker.newInstance(false);

            fragmentManager.beginTransaction()
                    .add(queryWorker, CompensationsUpdateWorker.WORKER_TAG)
                    .commit();
        }
    }

    @Override
    public void loadCompensationRemindWorker(@NonNull String compensationId) {
        final FragmentManager fragmentManager = getFragmentManager();
        Fragment compensationRemindWorker = WorkerUtils.findWorker(fragmentManager,
                CompensationRemindWorker.WORKER_TAG);
        if (compensationRemindWorker == null) {
            compensationRemindWorker = CompensationRemindWorker.newInstance(compensationId);

            fragmentManager.beginTransaction()
                    .add(compensationRemindWorker, CompensationRemindWorker.WORKER_TAG)
                    .commit();
        }
    }

    @Override
    public void showChangeCompensationAmountDialog(@NonNull BigFraction amount, @NonNull String currency) {
        final CompensationChangeAmountDialogFragment dialog =
                CompensationChangeAmountDialogFragment.newInstance(amount, currency);
        dialog.show(getFragmentManager(), CHANGE_AMOUNT_DIALOG);
    }

    public interface ActivityListener extends BaseRecyclerViewOnlineFragment.ActivityListener {
        void setCompsUnpaidViewModel(@NonNull FinanceCompsUnpaidViewModel viewModel);
    }
}
