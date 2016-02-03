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

import com.mugen.Mugen;
import com.mugen.MugenCallbacks;

import ch.giantific.qwittig.databinding.FragmentFinanceCompensationsPaidBinding;
import ch.giantific.qwittig.di.components.DaggerFinanceComponent;
import ch.giantific.qwittig.di.modules.FinanceViewModelModule;
import ch.giantific.qwittig.presentation.ui.adapters.CompensationsPaidRecyclerAdapter;
import ch.giantific.qwittig.presentation.viewmodels.FinanceCompsPaidViewModel;
import ch.giantific.qwittig.presentation.workerfragments.query.CompensationsQueryMoreWorker;
import ch.giantific.qwittig.presentation.workerfragments.query.CompensationsUpdateWorker;

/**
 * Displays recent paid compensations in a {@link RecyclerView} list.
 * <p/>
 * Subclass of {@link BaseRecyclerViewOnlineFragment}.
 */
public class FinanceCompensationsPaidFragment extends BaseRecyclerViewOnlineFragment<FinanceCompsPaidViewModel, FinanceCompensationsPaidFragment.ActivityListener>
        implements FinanceCompsPaidViewModel.ViewListener {

    private FragmentFinanceCompensationsPaidBinding mBinding;

    public FinanceCompensationsPaidFragment() {
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
        mBinding = FragmentFinanceCompensationsPaidBinding.inflate(inflater, container, false);
        mBinding.setViewModel(mViewModel);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Mugen.with(mRecyclerView, new MugenCallbacks() {
            @Override
            public void onLoadMore() {
                mViewModel.onLoadMore();
            }

            @Override
            public boolean isLoading() {
                return mViewModel.isLoadingMore();
            }

            @Override
            public boolean hasLoadedAllItems() {
                return false;
            }
        }).start();
    }

    @Override
    protected RecyclerView getRecyclerView() {
        return mBinding.srlRv.rvBase;
    }

    @Override
    protected RecyclerView.Adapter getRecyclerAdapter() {
        return new CompensationsPaidRecyclerAdapter(mViewModel);
    }

    @Override
    protected void setViewModelToActivity() {
        mActivity.setCompsPaidViewModel(mViewModel);
    }

    @Override
    public void loadUpdateCompensationsPaidWorker() {
        CompensationsUpdateWorker.attach(getFragmentManager(), true);
    }

    @Override
    public void loadQueryMoreCompensationsPaidWorker(int skip) {
        CompensationsQueryMoreWorker.attach(getFragmentManager(), skip);
    }


    public interface ActivityListener extends BaseRecyclerViewOnlineFragment.ActivityListener {
        void setCompsPaidViewModel(@NonNull FinanceCompsPaidViewModel viewModel);
    }
}
