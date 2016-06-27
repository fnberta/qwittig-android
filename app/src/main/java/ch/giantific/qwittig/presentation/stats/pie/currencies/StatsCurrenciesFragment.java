/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.stats.pie.currencies;


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
 * Displays the currency statistics in a {@link PieChart}. Shows the percentages of the currencies
 * used in all purchases.
 * <p/>
 * Subclass of {@link BaseStatsPieFragment}.
 */
public class StatsCurrenciesFragment extends BaseStatsPieFragment<StatsCurrenciesViewModel, BaseStatsFragment.ActivityListener> {

    private StatsSubcomponent mComponent;

    public StatsCurrenciesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mViewModel.attachView(this);
        getLoaderManager().initLoader(StatsViewModel.StatsType.CURRENCIES, null, this);
    }

    @Override
    protected void injectDependencies(@NonNull StatsSubcomponent component) {
        mComponent = component;
        mComponent.inject(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_stats_currencies, menu);

        MenuItem sortByUsers = menu.findItem(R.id.action_sort_by_user);
        sortByUsers.setChecked(mViewModel.isSortUsers());
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_sort_by_user:
                item.setChecked(!item.isChecked());
                mViewModel.onToggleSortUsersMenuClick();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public Loader<Observable<Stats>> onCreateLoader(int id, Bundle args) {
        return new StatsLoader(getActivity(), mComponent.getUserRepository(),
                mComponent.getStatsRepository(), StatsViewModel.StatsType.CURRENCIES, mViewModel.getYear(),
                mViewModel.getMonth().getNumber());
    }
}