/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.ui.fragments;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.utils.PercentFormatter;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.helpers.StatsHelper;
import ch.giantific.qwittig.ui.widgets.PieChart;

/**
 * Displays the currency statistics in a {@link PieChart}. Shows the percentages of the currencies
 * used in all purchases.
 * <p/>
 * Subclass of {@link StatsPieBaseFragment}.
 */
public class StatsCurrenciesFragment extends StatsPieBaseFragment {

    private static final String LOG_TAG = StatsCurrenciesFragment.class.getSimpleName();
    private static final String STATS_HELPER_CURRENCIES = "STATS_HELPER_CURRENCIES";

    public StatsCurrenciesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_stats_pie, container, false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_stats_currencies, menu);

        setMenuValues(menu);
    }

    @NonNull
    @Override
    protected String getHelperTag() {
        return STATS_HELPER_CURRENCIES;
    }

    @Override
    void calcStats(@NonNull String year, int month) {
        super.calcStats(year, month);

        calcStatsWithHelper(StatsHelper.TYPE_CURRENCIES, year, month);
    }

    @Override
    void setChartOptions() {
        super.setChartOptions();

        if (!mSortByUser) {
            mPieChart.setUsePercentValues(true);
        } else {
            mRecyclerAdapter.showPercentValues(true);
        }
    }

    @Override
    void setDataSetOptions(@NonNull PieDataSet pieDataSet) {
        super.setDataSetOptions(pieDataSet);

        pieDataSet.setValueFormatter(new PercentFormatter());
    }
}
