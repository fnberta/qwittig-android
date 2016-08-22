/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.stats;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.widget.ArrayAdapter;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.rest.StatsResult;
import ch.giantific.qwittig.databinding.ActivityStatsBinding;
import ch.giantific.qwittig.presentation.common.adapters.TabsAdapter;
import ch.giantific.qwittig.presentation.common.viewmodels.ViewModel;
import ch.giantific.qwittig.presentation.navdrawer.BaseNavDrawerActivity;
import ch.giantific.qwittig.presentation.navdrawer.di.NavDrawerComponent;
import ch.giantific.qwittig.presentation.stats.StatsViewModel.StatsPeriod;
import ch.giantific.qwittig.presentation.stats.StatsViewModel.StatsType;
import ch.giantific.qwittig.presentation.stats.di.StatsLoaderModule;
import ch.giantific.qwittig.presentation.stats.di.StatsSubcomponent;
import ch.giantific.qwittig.presentation.stats.di.StatsViewModelModule;
import ch.giantific.qwittig.presentation.stats.models.StatsPeriodItem;
import ch.giantific.qwittig.presentation.stats.models.StatsTypeItem;
import rx.Observable;

/**
 * Hosts the different stats fragments, {@link StatsPieFragment} and
 * {@link StatsBarFragment}. They display statistical information about the behaviour in
 * the current group.
 * <p/>
 * Handles the stats type and period selection with spinners in the {@link Toolbar}.
 * <p/>
 * Subclass of {@link BaseNavDrawerActivity}.
 */
public class StatsActivity extends BaseNavDrawerActivity<StatsSubcomponent>
        implements LoaderManager.LoaderCallbacks<Observable<StatsResult>>,
        StatsViewModel.ViewListener {

    @Inject
    StatsViewModel statsViewModel;
    private ActivityStatsBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_stats);

        // check item in NavDrawer
        checkNavDrawerItem(R.id.nav_stats);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(null);
        }

        setupTypeSpinner();
        setupPeriodSpinner();

        if (userLoggedIn) {
            setupTabs();
            getSupportLoaderManager().initLoader(0, null, this);
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void setupTypeSpinner() {
        final StatsTypeItem[] pages = new StatsTypeItem[]{
                new StatsTypeItem(getString(R.string.tab_stats_group), StatsType.GROUP),
                new StatsTypeItem(getString(R.string.tab_stats_user), StatsType.USER),
        };
        final Context themedContext = getSupportActionBar().getThemedContext();
        final ArrayAdapter<StatsTypeItem> adapter =
                new ArrayAdapter<>(themedContext, R.layout.spinner_item_toolbar, pages);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spStatsType.setAdapter(adapter);
    }

    private void setupPeriodSpinner() {
        final StatsPeriodItem[] types = new StatsPeriodItem[]{
                new StatsPeriodItem(getString(R.string.stats_period_month_this), StatsPeriod.THIS_MONTH),
                new StatsPeriodItem(getString(R.string.stats_period_month_last), StatsPeriod.LAST_MONTH),
                new StatsPeriodItem(getString(R.string.stats_period_year_this), StatsPeriod.THIS_YEAR),
                new StatsPeriodItem(getString(R.string.stats_period_custom), StatsPeriod.CUSTOM)
        };
        final ArrayAdapter<StatsPeriodItem> adapter =
                new ArrayAdapter<>(this, R.layout.spinner_item_stats_period, types);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spStatsPeriod.setAdapter(adapter);
    }

    private void setupTabs() {
        final StatsTypeItem type = (StatsTypeItem) binding.spStatsType.getSelectedItem();
        statsViewModel.setType(type);
        final StatsPeriodItem period = (StatsPeriodItem) binding.spStatsPeriod.getSelectedItem();
        statsViewModel.setPeriod(period);
        binding.setViewModel(statsViewModel);

        final TabsAdapter tabsAdapter = new TabsAdapter(getSupportFragmentManager());
        tabsAdapter.addInitialFragment(new StatsPieFragment(), getString(R.string.tab_stats_pie));
        tabsAdapter.addInitialFragment(new StatsBarFragment(), getString(R.string.tab_stats_bar));
        binding.viewpager.setAdapter(tabsAdapter);
    }

    @Override
    protected void injectDependencies(@NonNull NavDrawerComponent navComp,
                                      @Nullable Bundle savedInstanceState) {
        component = navComp.plus(new StatsViewModelModule(savedInstanceState),
                new StatsLoaderModule(this));
        component.inject(this);
        statsViewModel.attachView(this);
    }

    @Override
    protected List<ViewModel> getViewModels() {
        return Arrays.asList(new ViewModel[]{statsViewModel});
    }

    @Override
    public Loader<Observable<StatsResult>> onCreateLoader(int id, Bundle args) {
        final StatsLoader loader = component.getStatsLoader();
        loader.setStartDate(statsViewModel.getStartDate());
        loader.setEndDAte(statsViewModel.getEndDate());
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Observable<StatsResult>> loader,
                               Observable<StatsResult> data) {
        statsViewModel.onDataLoaded(data);
    }

    @Override
    public void onLoaderReset(Loader<Observable<StatsResult>> loader) {
        // do nothing
    }

    @Override
    public void setupScreenAfterLogin() {
        super.setupScreenAfterLogin();

        setupTabs();
        getSupportLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void reloadData() {
        getSupportLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public int[] getStatsColors() {
        return getResources().getIntArray(R.array.stats_colors);
    }
}
