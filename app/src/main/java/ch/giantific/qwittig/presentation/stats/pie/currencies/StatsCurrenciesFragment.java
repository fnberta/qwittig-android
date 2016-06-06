/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.stats.pie.currencies;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.Loader;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import ch.giantific.qwittig.Qwittig;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.presentation.stats.StatsLoader;
import ch.giantific.qwittig.presentation.stats.StatsViewModel;
import ch.giantific.qwittig.presentation.stats.models.Month;
import ch.giantific.qwittig.presentation.stats.models.Stats;
import ch.giantific.qwittig.presentation.stats.pie.StatsPieBaseFragment;
import ch.giantific.qwittig.presentation.stats.pie.StatsPieViewModel;
import ch.giantific.qwittig.presentation.stats.pie.currencies.di.DaggerStatsCurrenciesComponent;
import ch.giantific.qwittig.presentation.stats.pie.currencies.di.StatsCurrenciesComponent;
import ch.giantific.qwittig.presentation.stats.pie.currencies.di.StatsCurrenciesViewModelModule;
import ch.giantific.qwittig.presentation.stats.widgets.PieChart;
import rx.Observable;

/**
 * Displays the currency statistics in a {@link PieChart}. Shows the percentages of the currencies
 * used in all purchases.
 * <p/>
 * Subclass of {@link StatsPieBaseFragment}.
 */
public class StatsCurrenciesFragment extends StatsPieBaseFragment<StatsPieViewModel, StatsPieBaseFragment.ActivityListener> {

    private StatsCurrenciesComponent mComponent;

    public StatsCurrenciesFragment() {
        // Required empty public constructor
    }

    public static StatsCurrenciesFragment newInstance(@NonNull String year, @NonNull Month month) {
        final StatsCurrenciesFragment fragment = new StatsCurrenciesFragment();
        final Bundle args = new Bundle();
        args.putString(KEY_YEAR, year);
        args.putParcelable(KEY_MONTH, month);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void injectDependencies(@Nullable Bundle savedState, @NonNull String year, @NonNull Month month) {
        mComponent = DaggerStatsCurrenciesComponent.builder()
                .applicationComponent(Qwittig.getAppComponent(getActivity()))
                .statsCurrenciesViewModelModule(new StatsCurrenciesViewModelModule(savedState, this, year, month))
                .build();
        mComponent.inject(this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getLoaderManager().initLoader(StatsViewModel.StatsType.CURRENCIES, null, this);
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
        return new StatsLoader(getActivity(), mComponent.getUserRepo(),
                mComponent.getStatsRepo(), StatsViewModel.StatsType.CURRENCIES, mViewModel.getYear(),
                mViewModel.getMonth().getNumber());
    }
}
