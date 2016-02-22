/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.finance.paid;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mugen.Mugen;
import com.mugen.MugenCallbacks;

import ch.giantific.qwittig.databinding.FragmentFinanceCompensationsPaidBinding;
import ch.giantific.qwittig.presentation.finance.paid.di.DaggerFinanceCompsPaidComponent;
import ch.giantific.qwittig.presentation.finance.paid.di.FinanceCompsPaidViewModelModule;
import ch.giantific.qwittig.presentation.common.fragments.BaseRecyclerViewOnlineFragment;
import ch.giantific.qwittig.presentation.finance.unpaid.CompsUpdateWorker;

/**
 * Displays recent paid compensations in a {@link RecyclerView} list.
 * <p/>
 * Subclass of {@link BaseRecyclerViewOnlineFragment}.
 */
public class CompsPaidFragment extends BaseRecyclerViewOnlineFragment<CompsPaidViewModel, CompsPaidFragment.ActivityListener>
        implements CompsPaidViewModel.ViewListener {

    private FragmentFinanceCompensationsPaidBinding mBinding;

    public CompsPaidFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DaggerFinanceCompsPaidComponent.builder()
                .financeCompsPaidViewModelModule(new FinanceCompsPaidViewModelModule(savedInstanceState, this))
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
        return new CompsPaidRecyclerAdapter(mViewModel);
    }

    @Override
    protected SwipeRefreshLayout getSrl() {
        return mBinding.srlRv.srlBase;
    }

    @Override
    protected void setViewModelToActivity() {
        mActivity.setCompsPaidViewModel(mViewModel);
    }

    @Override
    public void loadUpdateCompensationsPaidWorker() {
        CompsUpdateWorker.attach(getFragmentManager(), true);
    }

    @Override
    public void loadQueryMoreCompensationsPaidWorker(int skip) {
        CompsQueryMoreWorker.attach(getFragmentManager(), skip);
    }


    public interface ActivityListener extends BaseRecyclerViewOnlineFragment.ActivityListener {
        void setCompsPaidViewModel(@NonNull CompsPaidViewModel viewModel);
    }
}
