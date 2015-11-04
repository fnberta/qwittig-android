/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.ui.activities;

import android.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.domain.models.Month;
import ch.giantific.qwittig.domain.models.stats.Stats;
import ch.giantific.qwittig.data.helpers.group.StatsHelper;
import ch.giantific.qwittig.ui.adapters.StringResSpinnerAdapter;
import ch.giantific.qwittig.ui.adapters.ThemedArrayAdapter;
import ch.giantific.qwittig.ui.fragments.StatsBaseFragment;
import ch.giantific.qwittig.ui.fragments.StatsCurrenciesFragment;
import ch.giantific.qwittig.ui.fragments.StatsSpendingFragment;
import ch.giantific.qwittig.ui.fragments.StatsStoresFragment;

/**
 * Hosts the different stats fragments, {@link StatsSpendingFragment}, {@link StatsStoresFragment}
 * and {@link StatsCurrenciesFragment}. They display statistical information about the behaviour in
 * the current group.
 * <p/>
 * Handles the stats type and period selection with spinners in the {@link Toolbar}.
 * <p/>
 * Subclass of {@link BaseNavDrawerActivity}.
 */
public class StatsActivity extends BaseNavDrawerActivity implements
        StatsBaseFragment.FragmentInteractionListener,
        StatsHelper.HelperInteractionListener {

    private static final String LOG_TAG = StatsActivity.class.getSimpleName();
    private static final String STATE_STATS_FRAGMENT = "STATE_STATS_FRAGMENT";
    private static final int NUMBER_OF_MONTHS = 12;
    private Spinner mSpinnerStatsType;
    private Spinner mSpinnerYear;
    private Spinner mSpinnerMonth;
    @Nullable
    private StatsBaseFragment mStatsFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        // check item in NavDrawer
        checkNavDrawerItem(R.id.nav_stats);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(null);
        }

        mSpinnerStatsType = (Spinner) findViewById(R.id.sp_stats_type);
        setupTypeSpinner();
        mSpinnerYear = (Spinner) findViewById(R.id.sp_year);
        setupYearSpinner();
        mSpinnerMonth = (Spinner) findViewById(R.id.sp_month);
        setupMonthSpinner();

        if (mUserIsLoggedIn) {
            if (savedInstanceState == null) {
                addFirstFragment();
            } else {
                mStatsFragment = (StatsBaseFragment) getFragmentManager()
                        .getFragment(savedInstanceState, STATE_STATS_FRAGMENT);
            }
        }
    }

    private void setupTypeSpinner() {
        final int[] types = new int[]{
                R.string.tab_stats_spending,
                R.string.tab_stats_stores,
                R.string.tab_stats_currencies};
        final StringResSpinnerAdapter typesAdapter =
                new StringResSpinnerAdapter(this, R.layout.spinner_item_toolbar, types);
        mSpinnerStatsType.setAdapter(typesAdapter);
    }

    private void setupYearSpinner() {
        ThemedArrayAdapter<String> spinnerYearAdapter =
                new ThemedArrayAdapter<>(this, R.layout.spinner_item_stats_period, getLastYears(5));
        spinnerYearAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        mSpinnerYear.setAdapter(spinnerYearAdapter);
    }

    @NonNull
    private List<String> getLastYears(int timeToGoBack) {
        List<String> lastYears = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        int yearNow = calendar.get(Calendar.YEAR);
        int year = yearNow - timeToGoBack;

        while (year <= yearNow) {
            lastYears.add(String.valueOf(year));
            year++;
        }

        Collections.reverse(lastYears);
        return lastYears;
    }

    private void setupMonthSpinner() {
        List<Month> months = new ArrayList<>();
        months.add(new Month(getString(R.string.stats_month_all)));
        for (int i = 1; i <= NUMBER_OF_MONTHS; i++) {
            months.add(new Month(i));
        }

        ThemedArrayAdapter<Month> spinnerMonthAdapter =
                new ThemedArrayAdapter<>(this, R.layout.spinner_item_stats_period, months);
        spinnerMonthAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        mSpinnerMonth.setAdapter(spinnerMonthAdapter);
    }

    private void addFirstFragment() {
        mStatsFragment = new StatsSpendingFragment();
        getFragmentManager().beginTransaction()
                .add(R.id.container, mStatsFragment)
                .commit();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mUserIsLoggedIn) {
            getFragmentManager().putFragment(outState, STATE_STATS_FRAGMENT, mStatsFragment);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mUserIsLoggedIn) {
            setSpinnerListeners();
        }
    }

    /**
     * Adds itemSelectedListeners for spinners with a Runnable. The reason being that they should
     * not fire when the spinners are first laid out. Maybe use GlobalLayoutListener for a more
     * elegant solution.
     */
    private void setSpinnerListeners() {
        mSpinnerStatsType.post(new Runnable() {
            @Override
            public void run() {
                mSpinnerStatsType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(@NonNull AdapterView<?> parent, View view, int position, long id) {
                        int statsType = (int) parent.getItemAtPosition(position);
                        switchFragment(statsType);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
            }
        });

        mSpinnerYear.post(new Runnable() {
            @Override
            public void run() {
                mSpinnerYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        onPeriodSelected();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
            }
        });

        mSpinnerMonth.post(new Runnable() {
            @Override
            public void run() {
                mSpinnerMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        onPeriodSelected();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
            }
        });
    }

    private void switchFragment(int statsType) {
        StatsBaseFragment fragment = null;
        switch (statsType) {
            case R.string.tab_stats_spending:
                fragment = new StatsSpendingFragment();
                break;
            case R.string.tab_stats_stores:
                fragment = new StatsStoresFragment();
                break;
            case R.string.tab_stats_currencies:
                fragment = new StatsCurrenciesFragment();
                break;
        }

        if (fragment != null) {
            mStatsFragment = fragment;
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.container, mStatsFragment)
                    .commit();
        }
    }

    private void onPeriodSelected() {
        if (mStatsFragment != null) {
            mStatsFragment.loadData();
        }
    }

    @Override
    void afterLoginSetup() {
        super.afterLoginSetup();

        addFirstFragment();
        setSpinnerListeners();
    }

    @NonNull
    @Override
    public String getYear() {
        return (String) mSpinnerYear.getSelectedItem();
    }

    @NonNull
    @Override
    public Month getMonth() {
        return (Month) mSpinnerMonth.getSelectedItem();
    }

    @Override
    public void onStatsCalculated(int statsType, @NonNull Stats stats) {
        if (mStatsFragment != null) {
            mStatsFragment.onStatsCalculated(stats);
        }
    }

    @Override
    public void onStatsCalculationFailed(int statsType, int errorCode) {
        if (mStatsFragment != null) {
            mStatsFragment.onStatsCalculationFailed(errorCode);
        }
    }

    @Override
    protected void onNewGroupSet() {
        if (mStatsFragment != null) {
            mStatsFragment.updateFragment();
        }
    }
}
