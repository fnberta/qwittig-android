/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.stats;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.presentation.common.fragments.BaseFragment;
import ch.giantific.qwittig.presentation.stats.StatsViewModel.StatsType;
import ch.giantific.qwittig.presentation.stats.di.StatsSubcomponent;
import ch.giantific.qwittig.presentation.stats.models.Stats;
import rx.Observable;

/**
 * Provides an abstract base class for the display of statistical information related to a group.
 * <p/>
 * Subclass of {@link BaseFragment}.
 */
public abstract class BaseStatsFragment<T extends StatsViewModel, S extends BaseStatsFragment.ActivityListener>
        extends BaseFragment<StatsSubcomponent, T, S>
        implements LoaderManager.LoaderCallbacks<Observable<Stats>>, StatsViewModel.ViewListener {

    // boolean flag to avoid delivering the result twice. Calling initLoader in onActivityCreated
    // makes onLoadFinished be called twice during configuration change.
    private boolean mLoadFinished;

    public BaseStatsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public void onLoadFinished(Loader<Observable<Stats>> loader, Observable<Stats> data) {
        if (!mLoadFinished) {
            mLoadFinished = true;
            mViewModel.onDataLoaded(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<Observable<Stats>> loader) {
        // do nothing
    }

    @Override
    public void reloadData(@StatsType int type) {
        mLoadFinished = false;
        getLoaderManager().restartLoader(type, null, this);
    }

    @Override
    public int[] getStatsColors() {
        return getResources().getIntArray(R.array.stats_colors);
    }

    @Override
    public void switchStatsScreen(@StatsType int statsType) {
        mActivity.switchFragment(statsType);
    }

    /**
     * Defines the interaction with the hosting activity.
     */
    public interface ActivityListener extends BaseFragment.ActivityListener<StatsSubcomponent> {
        void switchFragment(int statsType);
    }
}
