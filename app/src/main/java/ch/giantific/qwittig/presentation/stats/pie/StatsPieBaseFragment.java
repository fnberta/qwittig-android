/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.stats.pie;


import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ch.giantific.qwittig.databinding.FragmentStatsPieBinding;
import ch.giantific.qwittig.presentation.stats.StatsBaseFragment;
import ch.giantific.qwittig.presentation.stats.widgets.PieChart;

/**
 * Provides an abstract base class for the display of stats data in a {@link PieChart}.
 * <p/>
 * Subclass of {@link StatsBaseFragment}.
 */
public abstract class StatsPieBaseFragment<T extends StatsPieViewModel, S extends StatsPieBaseFragment.ActivityListener>
        extends StatsBaseFragment<T, S>
        implements StatsPieViewModel.ViewListener {

    private StatsPieChartRecyclerAdapter mRecyclerAdapter;
    private FragmentStatsPieBinding mBinding;

    public StatsPieBaseFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentStatsPieBinding.inflate(inflater, container, false);
        mBinding.setViewModel(mViewModel);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRecyclerAdapter = new StatsPieChartRecyclerAdapter(mViewModel);
        mBinding.rvStatsPie.setLayoutManager(new LinearLayoutManager(getActivity()));
        mBinding.rvStatsPie.setHasFixedSize(true);
        mBinding.rvStatsPie.setAdapter(mRecyclerAdapter);
    }

    @Override
    protected View getSnackbarView() {
        return mBinding.pcStores;
    }

    @Override
    public void notifyDataSetChanged() {
        mRecyclerAdapter.notifyDataSetChanged();
    }

    /**
     * Defines the interaction with the hosting {@link Activity}.
     */
    public interface ActivityListener extends StatsBaseFragment.ActivityListener {
        // empty
    }
}
