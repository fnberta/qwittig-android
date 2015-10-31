/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.ui.fragments;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.utils.PercentFormatter;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.domain.models.stats.Stats;
import ch.giantific.qwittig.data.helpers.group.StatsHelper;
import ch.giantific.qwittig.ui.widgets.PieChart;
import ch.giantific.qwittig.CurrencyFormatter;

/**
 * Displays the stores statistics in a {@link PieChart}. Shows the percentages of the stores
 * used in all purchases.
 * <p/>
 * Subclass of {@link StatsPieBaseFragment}.
 */
public class StatsStoresFragment extends StatsPieBaseFragment {

    private static final String LOG_TAG = StatsStoresFragment.class.getSimpleName();
    private static final String STATS_HELPER_STORES = "STATS_HELPER_STORES";
    private static final String STATE_SHOW_PERCENT = "STATE_SHOW_PERCENT";
    private static final String STATE_SHOW_AVERAGE = "STATE_SHOW_AVERAGE";
    private boolean mShowPercent = true;
    private boolean mShowAverage;

    public StatsStoresFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        if (savedInstanceState != null) {
            mShowPercent = savedInstanceState.getBoolean(STATE_SHOW_PERCENT, true);
            mShowAverage = savedInstanceState.getBoolean(STATE_SHOW_AVERAGE, false);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(STATE_SHOW_PERCENT, mShowPercent);
        outState.putBoolean(STATE_SHOW_AVERAGE, mShowAverage);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_stats_pie, container, false);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_stats_stores, menu);

        setMenuValues(menu);
    }

    @Override
    void setMenuValues(@NonNull Menu menu) {
        super.setMenuValues(menu);

        MenuItem showPercent = menu.findItem(R.id.action_show_percent);
        showPercent.setChecked(mShowPercent);

        MenuItem showAverage = menu.findItem(R.id.action_show_average);
        showAverage.setChecked(mShowAverage);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_show_percent:
                item.setChecked(!item.isChecked());
                mShowPercent = !mShowPercent;
                setChartData();
                return true;
            case R.id.action_show_average:
                item.setChecked(!item.isChecked());
                mShowAverage = !mShowAverage;
                setChartData();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @NonNull
    @Override
    protected String getHelperTag() {
        return STATS_HELPER_STORES;
    }

    @Override
    void calcStats(@NonNull String year, int month) {
        super.calcStats(year, month);

        calcStatsWithHelper(StatsHelper.TYPE_STORES, year, month);
    }

    @Override
    float getValue(@NonNull Stats.Unit unit) {
        if (mShowAverage) {
            return unit.getAverage();
        } else {
            return super.getValue(unit);
        }
    }

    @Override
    void setDataSetOptions(@NonNull PieDataSet pieDataSet) {
        super.setDataSetOptions(pieDataSet);

        if (mShowPercent) {
            pieDataSet.setValueFormatter(new PercentFormatter());
        } else {
            pieDataSet.setValueFormatter(new CurrencyFormatter(mCurrentGroup.getCurrency()));
        }
    }

    @Override
    void setChartOptions() {
        super.setChartOptions();

        if (!mSortByUser) {
            mPieChart.setUsePercentValues(mShowPercent);
        } else {
            mRecyclerAdapter.showPercentValues(mShowPercent);
        }
    }
}
