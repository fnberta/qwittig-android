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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.databinding.ActivityStatsBinding;
import ch.giantific.qwittig.presentation.common.viewmodels.ViewModel;
import ch.giantific.qwittig.presentation.navdrawer.BaseNavDrawerActivity;
import ch.giantific.qwittig.presentation.navdrawer.di.NavDrawerComponent;
import ch.giantific.qwittig.presentation.stats.StatsViewModel.StatsType;
import ch.giantific.qwittig.presentation.stats.di.StatsCurrenciesViewModelModule;
import ch.giantific.qwittig.presentation.stats.di.StatsSpendingViewModelModule;
import ch.giantific.qwittig.presentation.stats.di.StatsStoresViewModelModule;
import ch.giantific.qwittig.presentation.stats.di.StatsSubcomponent;
import ch.giantific.qwittig.presentation.stats.models.Month;
import ch.giantific.qwittig.presentation.stats.models.StatsPage;
import ch.giantific.qwittig.presentation.stats.pie.StatsPieViewModel;
import ch.giantific.qwittig.presentation.stats.pie.currencies.StatsCurrenciesFragment;
import ch.giantific.qwittig.presentation.stats.pie.currencies.StatsCurrenciesViewModel;
import ch.giantific.qwittig.presentation.stats.pie.stores.StatsStoresFragment;
import ch.giantific.qwittig.presentation.stats.pie.stores.StatsStoresViewModel;
import ch.giantific.qwittig.presentation.stats.spending.StatsSpendingFragment;
import ch.giantific.qwittig.presentation.stats.spending.StatsSpendingViewModel;

/**
 * Hosts the different stats fragments, {@link StatsSpendingFragment}, {@link StatsStoresFragment}
 * and {@link StatsCurrenciesFragment}. They display statistical information about the behaviour in
 * the current group.
 * <p/>
 * Handles the stats type and period selection with spinners in the {@link Toolbar}.
 * <p/>
 * Subclass of {@link BaseNavDrawerActivity}.
 */
public class StatsActivity extends BaseNavDrawerActivity<StatsSubcomponent> implements
        StatsSpendingFragment.ActivityListener {

    private static final int NUMBER_OF_MONTHS = 12;
    @Inject
    StatsSpendingViewModel mSpendingViewModel;
    @Inject
    StatsStoresViewModel mStoresViewModel;
    @Inject
    StatsCurrenciesViewModel mCurrenciesViewModel;
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
    protected void injectDependencies(@NonNull NavDrawerComponent navComp,
                                      @Nullable Bundle savedInstanceState) {
        mComponent = navComp.plus(new StatsSpendingViewModelModule(savedInstanceState),
                new StatsCurrenciesViewModelModule(savedInstanceState),
                new StatsStoresViewModelModule(savedInstanceState));
        mComponent.inject(this);
    }

    @Override
    protected List<ViewModel> getViewModels() {
        return Arrays.asList(new ViewModel[]{mSpendingViewModel, mStoresViewModel, mCurrenciesViewModel});
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
        mSpendingViewModel.setYear(year);
        mStoresViewModel.setMonth(month);
        mBinding.setViewModel(mSpendingViewModel);

        getSupportFragmentManager().beginTransaction()
                .add(R.id.container, new StatsSpendingFragment())
                .commit();
    }

    @Override
    protected void onLoginSuccessful() {
        super.onLoginSuccessful();

        addFirstFragment();
    }

    @Override
    public void switchFragment(@StatsType int statsType) {
        final String year = (String) mBinding.spYear.getSelectedItem();
        final Month month = (Month) mBinding.spMonth.getSelectedItem();
        Fragment fragment;
        switch (statsType) {
            case StatsType.CURRENCIES:
                mCurrenciesViewModel.setYear(year);
                mCurrenciesViewModel.setMonth(month);
                mBinding.setViewModel(mCurrenciesViewModel);
                fragment = new StatsCurrenciesFragment();
                break;
            case StatsType.SPENDING:
                mStoresViewModel.setYear(year);
                mStoresViewModel.setMonth(month);
                mBinding.setViewModel(mSpendingViewModel);
                fragment = new StatsSpendingFragment();
                break;
            case StatsType.STORES:
                mStoresViewModel.setYear(year);
                mStoresViewModel.setMonth(month);
                mBinding.setViewModel(mStoresViewModel);
                fragment = new StatsStoresFragment();
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
