/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.presentation.ui.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.apache.commons.math3.fraction.BigFraction;

import ch.giantific.qwittig.databinding.FragmentFinanceUsersBinding;
import ch.giantific.qwittig.di.components.DaggerFinanceComponent;
import ch.giantific.qwittig.di.modules.FinanceViewModelModule;
import ch.giantific.qwittig.presentation.ui.adapters.UsersRecyclerAdapter;
import ch.giantific.qwittig.presentation.viewmodels.FinanceUsersViewModel;
import ch.giantific.qwittig.presentation.workerfragments.query.UsersUpdateWorker;

/**
 * Displays the users of a group and their current balances in a {@link RecyclerView} list. Does not
 * include the current user whose balance is displayed in the {@link Toolbar} of the hosting
 * {@link Activity}.
 * <p/>
 * Subclass of {@link BaseRecyclerViewOnlineFragment}.
 */
public class FinanceUserBalancesFragment extends BaseRecyclerViewOnlineFragment<FinanceUsersViewModel, FinanceUserBalancesFragment.ActivityListener>
        implements FinanceUsersViewModel.ViewListener {

    private FragmentFinanceUsersBinding mBinding;

    public FinanceUserBalancesFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DaggerFinanceComponent.builder()
                .financeViewModelModule(new FinanceViewModelModule(savedInstanceState))
                .build()
                .inject(this);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBinding = FragmentFinanceUsersBinding.inflate(inflater, container, false);
        mBinding.setViewModel(mViewModel);
        return mBinding.getRoot();
    }

    @Override
    protected RecyclerView getRecyclerView() {
        return mBinding.srlRv.rvBase;
    }

    @Override
    protected RecyclerView.Adapter getRecyclerAdapter() {
        return new UsersRecyclerAdapter(mViewModel);
    }

    @Override
    protected void setViewModelToActivity() {
        mActivity.setUsersViewModel(mViewModel);
    }

    @Override
    public void loadUpdateUsersWorker() {
        UsersUpdateWorker.attach(getFragmentManager());
    }

    @Override
    public void setColorTheme(@NonNull BigFraction balance) {
        mActivity.setColorTheme(balance);
    }

    public interface ActivityListener extends BaseRecyclerViewOnlineFragment.ActivityListener {

        void setUsersViewModel(@NonNull FinanceUsersViewModel viewModel);

        void setColorTheme(@NonNull BigFraction balance);
    }
}
