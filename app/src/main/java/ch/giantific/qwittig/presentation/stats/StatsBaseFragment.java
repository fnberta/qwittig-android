/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.stats;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.presentation.common.fragments.BaseFragment;
import ch.giantific.qwittig.presentation.stats.StatsViewModel.StatsType;
import ch.giantific.qwittig.presentation.stats.models.Month;
import ch.giantific.qwittig.presentation.stats.models.Stats;
import rx.Observable;

/**
 * Provides an abstract base class for the display of statistical information related to a group.
 * <p/>
 * Subclass of {@link BaseFragment}.
 */
public abstract class StatsBaseFragment<T extends StatsViewModel, S extends StatsBaseFragment.ActivityListener>
        extends BaseFragment<T, S>
        implements LoaderManager.LoaderCallbacks<Observable<Stats>>, StatsViewModel.ViewListener {

    protected static final String KEY_YEAR = "YEAR";
    protected static final String KEY_MONTH = "MONTH";
    // boolean flag to avoid delivering the result twice. Calling initLoader in onActivityCreated
    // makes onLoadFinished will be called twice during configuration change.
    private boolean mLoadFinished;

    public StatsBaseFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        final Bundle args = getArguments();
        final String year = args.getString(KEY_YEAR);
        final Month month = args.getParcelable(KEY_MONTH);
        injectDependencies(savedInstanceState, year, month);
    }

    protected abstract void injectDependencies(@Nullable Bundle savedState,
                                               @NonNull String year, @NonNull Month month);

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
    protected void setViewModelToActivity() {
        mActivity.setStatsViewModel(mViewModel);
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
     * Defines the interaction with the hosting {@link Activity}.
     */
    public interface ActivityListener extends BaseFragment.ActivityListener {
        void setStatsViewModel(@NonNull StatsViewModel viewModel);

        void switchFragment(int statsType);
    }
}
