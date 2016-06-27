/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.stats.pie.stores;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.Loader;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.presentation.stats.BaseStatsFragment;
import ch.giantific.qwittig.presentation.stats.StatsLoader;
import ch.giantific.qwittig.presentation.stats.StatsViewModel;
import ch.giantific.qwittig.presentation.stats.di.StatsSubcomponent;
import ch.giantific.qwittig.presentation.stats.models.Stats;
import ch.giantific.qwittig.presentation.stats.pie.BaseStatsPieFragment;
import ch.giantific.qwittig.presentation.stats.widgets.PieChart;
import rx.Observable;

/**
 * Displays the stores statistics in a {@link PieChart}. Shows the percentages of the stores
 * used in all purchases.
 * <p/>
 * Subclass of {@link BaseStatsPieFragment}.
 */
public class StatsStoresFragment extends BaseStatsPieFragment<StatsStoresViewModel, BaseStatsFragment.ActivityListener> {

    private StatsSubcomponent mComponent;

    public StatsStoresFragment() {
        // Required empty public constructor
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mViewModel.attachView(this);
        getLoaderManager().initLoader(StatsViewModel.StatsType.STORES, null, this);
    }

    @Override
    protected void injectDependencies(@NonNull StatsSubcomponent component) {
        mComponent = component;
        mComponent.inject(this);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_stats_stores, menu);

        final MenuItem sortByUsers = menu.findItem(R.id.action_sort_by_user);
        sortByUsers.setChecked(mViewModel.isSortUsers());

        final MenuItem showPercent = menu.findItem(R.id.action_show_percent);
        showPercent.setChecked(mViewModel.isShowPercentage());

        final MenuItem showAverage = menu.findItem(R.id.action_show_average);
        showAverage.setChecked(mViewModel.isShowAverage());
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_sort_by_user:
                item.setChecked(!item.isChecked());
                mViewModel.onToggleSortUsersMenuClick();
                return true;
            case R.id.action_show_percent:
                item.setChecked(!item.isChecked());
                mViewModel.onTogglePercentMenuClick();
                return true;
            case R.id.action_show_average:
                item.setChecked(!item.isChecked());
                mViewModel.onToggleAverageMenuClick();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public Loader<Observable<Stats>> onCreateLoader(int id, Bundle args) {
        return new StatsLoader(getActivity(), mComponent.getUserRepository(),
                mComponent.getStatsRepository(), StatsViewModel.StatsType.STORES, mViewModel.getYear(),
                mViewModel.getMonth().getNumber());
    }
}