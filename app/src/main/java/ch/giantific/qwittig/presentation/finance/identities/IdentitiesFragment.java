/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.finance.identities;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.apache.commons.math3.fraction.BigFraction;

import ch.giantific.qwittig.databinding.FragmentFinanceIdentitiesBinding;
import ch.giantific.qwittig.di.components.DaggerFinanceUsersComponent;
import ch.giantific.qwittig.di.modules.FinanceUsersViewModelModule;
import ch.giantific.qwittig.presentation.common.fragments.BaseRecyclerViewOnlineFragment;

/**
 * Displays the users of a group and their current balances in a {@link RecyclerView} list. Does not
 * include the current user whose balance is displayed in the {@link Toolbar} of the hosting
 * {@link Activity}.
 * <p/>
 * Subclass of {@link BaseRecyclerViewOnlineFragment}.
 */
public class IdentitiesFragment extends BaseRecyclerViewOnlineFragment<IdentitiesViewModel, IdentitiesFragment.ActivityListener>
        implements IdentitiesViewModel.ViewListener {

    private FragmentFinanceIdentitiesBinding mBinding;

    public IdentitiesFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DaggerFinanceUsersComponent.builder()
                .financeUsersViewModelModule(new FinanceUsersViewModelModule(savedInstanceState, this))
                .build()
                .inject(this);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBinding = FragmentFinanceIdentitiesBinding.inflate(inflater, container, false);
        mBinding.setViewModel(mViewModel);
        return mBinding.getRoot();
    }

    @Override
    protected RecyclerView getRecyclerView() {
        return mBinding.srlRv.rvBase;
    }

    @Override
    protected RecyclerView.Adapter getRecyclerAdapter() {
        return new IdentitiesRecyclerAdapter(mViewModel);
    }

    @Override
    protected SwipeRefreshLayout getSrl() {
        return mBinding.srlRv.srlBase;
    }

    @Override
    protected void setViewModelToActivity() {
        mActivity.setIdentitiesViewModel(mViewModel);
    }

    @Override
    public void loadUpdateUsersWorker() {
        IdentitiesUpdateWorker.attach(getFragmentManager());
    }

    @Override
    public void setColorTheme(@NonNull BigFraction balance) {
        mActivity.setColorTheme(balance);
    }

    public interface ActivityListener extends BaseRecyclerViewOnlineFragment.ActivityListener {

        void setIdentitiesViewModel(@NonNull IdentitiesViewModel viewModel);

        void setColorTheme(@NonNull BigFraction balance);
    }
}
