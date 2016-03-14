/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.stats;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.databinding.ActivityStatsBinding;
import ch.giantific.qwittig.presentation.navdrawer.BaseNavDrawerActivity;
import ch.giantific.qwittig.presentation.navdrawer.di.NavDrawerComponent;
import ch.giantific.qwittig.presentation.stats.StatsViewModel.StatsType;
import ch.giantific.qwittig.presentation.stats.models.Month;
import ch.giantific.qwittig.presentation.stats.models.StatsPage;
import ch.giantific.qwittig.presentation.stats.pie.currencies.StatsCurrenciesFragment;
import ch.giantific.qwittig.presentation.stats.pie.stores.StatsStoresFragment;
import ch.giantific.qwittig.presentation.stats.spending.StatsSpendingFragment;

/**
 * Hosts the different stats fragments, {@link StatsSpendingFragment}, {@link StatsStoresFragment}
 * and {@link StatsCurrenciesFragment}. They display statistical information about the behaviour in
 * the current group.
 * <p/>
 * Handles the stats type and period selection with spinners in the {@link Toolbar}.
 * <p/>
 * Subclass of {@link BaseNavDrawerActivity}.
 */
public class StatsActivity extends BaseNavDrawerActivity<StatsViewModel> implements
        StatsSpendingFragment.ActivityListener {

    private static final int NUMBER_OF_MONTHS = 12;
    private ActivityStatsBinding mBinding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_stats);

        // check item in NavDrawer
        checkNavDrawerItem(R.id.nav_stats);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(null);
        }

        setupTypeSpinner();
        setupYearSpinner();
        setupMonthSpinner();

        if (mUserLoggedIn && savedInstanceState == null) {
            addFirstFragment();
        }
    }

    @Override
    protected void injectNavDrawerDependencies(@NonNull NavDrawerComponent navComp) {
        navComp.inject(this);
    }

    @SuppressWarnings("ConstantConditions")
    private void setupTypeSpinner() {
        final StatsPage[] pages = new StatsPage[]{
                new StatsPage(getString(R.string.tab_stats_spending), StatsType.SPENDING),
                new StatsPage(getString(R.string.tab_stats_stores), StatsType.STORES),
                new StatsPage(getString(R.string.tab_stats_currencies), StatsType.CURRENCIES)
        };
        final Context themedContext = getSupportActionBar().getThemedContext();
        final ArrayAdapter<StatsPage> adapter =
                new ArrayAdapter<>(themedContext, R.layout.spinner_item_toolbar, pages);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mBinding.spStatsType.setAdapter(adapter);
    }

    private void setupYearSpinner() {
        final ArrayAdapter<String> adapter =
                new ArrayAdapter<>(this, R.layout.spinner_item_stats_period, getLastYears(5));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mBinding.spYear.setAdapter(adapter);
    }

    @NonNull
    private List<String> getLastYears(int timeToGoBack) {
        final List<String> lastYears = new ArrayList<>();
        final Calendar calendar = Calendar.getInstance();
        final int yearNow = calendar.get(Calendar.YEAR);

        int year = yearNow - timeToGoBack;
        while (year <= yearNow) {
            lastYears.add(String.valueOf(year));
            year++;
        }

        Collections.reverse(lastYears);
        return lastYears;
    }

    private void setupMonthSpinner() {
        final ArrayAdapter<Month> adapter =
                new ArrayAdapter<>(this, R.layout.spinner_item_stats_period, getMonths());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mBinding.spMonth.setAdapter(adapter);
    }

    @NonNull
    private List<Month> getMonths() {
        final List<Month> months = new ArrayList<>();
        months.add(new Month(getString(R.string.stats_month_all)));
        for (int i = 1; i <= NUMBER_OF_MONTHS; i++) {
            months.add(new Month(i));
        }
        return months;
    }

    private void addFirstFragment() {
        final String year = (String) mBinding.spYear.getSelectedItem();
        final Month month = (Month) mBinding.spMonth.getSelectedItem();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.container, StatsSpendingFragment.newInstance(year, month))
                .commit();
    }

    @Override
    protected void onLoginSuccessful() {
        super.onLoginSuccessful();

        addFirstFragment();
    }

    @Override
    public void setStatsViewModel(@NonNull StatsViewModel viewModel) {
        mViewModel = viewModel;
        mBinding.setViewModel(mViewModel);
    }

    @Override
    public void switchFragment(@StatsType int statsType) {
        final String year = (String) mBinding.spYear.getSelectedItem();
        final Month month = (Month) mBinding.spMonth.getSelectedItem();
        Fragment fragment;
        switch (statsType) {
            case StatsType.CURRENCIES:
                fragment = StatsCurrenciesFragment.newInstance(year, month);
                break;
            case StatsType.SPENDING:
                fragment = StatsSpendingFragment.newInstance(year, month);
                break;
            case StatsType.STORES:
                fragment = StatsStoresFragment.newInstance(year, month);
                break;
            default:
                fragment = null;
        }

        if (fragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, fragment)
                    .commit();
        }
    }
}
