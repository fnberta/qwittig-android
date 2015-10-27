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

import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.parse.ParseObject;

import java.util.ArrayList;
import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.parse.models.User;
import ch.giantific.qwittig.data.stats.models.Stats;
import ch.giantific.qwittig.helpers.StatsHelper;
import ch.giantific.qwittig.ui.widgets.BarChart;
import ch.giantific.qwittig.utils.CurrencyFormatter;
import ch.giantific.qwittig.utils.DateUtils;

/**
 * Displays the spending statistics in a {@link BarChart}. Shows which user spent how much money in
 * a specified period of time.
 * <p/>
 * Subclass of {@link StatsBaseFragment}.
 */
public class StatsSpendingFragment extends StatsBaseFragment {

    private static final String LOG_TAG = StatsSpendingFragment.class.getSimpleName();
    private static final String STATS_HELPER_SPENDING = "STATS_HELPER_SPENDING";
    private static final String STATE_SHOW_GROUP = "STATE_SHOW_GROUP";
    private static final String STATE_SHOW_AVERAGE = "STATE_SHOW_AVERAGE";
    private BarChart mBarChart;
    private boolean mShowGroup;
    private boolean mShowAverage;
    private boolean mHasNoData = true;

    public StatsSpendingFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        if (savedInstanceState != null) {
            mShowGroup = savedInstanceState.getBoolean(STATE_SHOW_GROUP, false);
            mShowAverage = savedInstanceState.getBoolean(STATE_SHOW_AVERAGE, false);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(STATE_SHOW_GROUP, mShowGroup);
        outState.putBoolean(STATE_SHOW_AVERAGE, mShowAverage);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_stats_spending, container, false);

        mBarChart = (BarChart) rootView.findViewById(R.id.bc_spending);
        findBaseViews(rootView);

        return rootView;
    }

    @Override
    void setStuffWithGroupData() {
        super.setStuffWithGroupData();

        YAxis yAxis = mBarChart.getAxisLeft();
        yAxis.setValueFormatter(new CurrencyFormatter(mCurrentGroup.getCurrency()));
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_stats_spending, menu);

        MenuItem sortByUsers = menu.findItem(R.id.action_show_average);
        sortByUsers.setChecked(mShowAverage);

        MenuItem showGroupValues = menu.findItem(R.id.action_show_group);
        showGroupValues.setChecked(mShowGroup);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_show_group:
                item.setChecked(!item.isChecked());
                mShowGroup = !mShowGroup;
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
        return STATS_HELPER_SPENDING;
    }

    @Override
    void calcStats(@NonNull String year, int month) {
        super.calcStats(year, month);

        calcStatsWithHelper(StatsHelper.TYPE_SPENDING, year, month);
    }

    @Override
    protected void showChart() {
        mBarChart.setVisibility(View.VISIBLE);
    }

    @Override
    protected void hideChart() {
        mBarChart.setVisibility(View.GONE);
    }

    protected void setChartData() {
        if (!mDataIsLoaded || mStatsData == null) {
            return;
        }

        List<Stats.Member> userData = mStatsData.getMembers();
        Stats.Group groupData = mStatsData.getGroup();
        int unitSize = mStatsData.getNumberOfUnits();
        List<String> xVals = getXvals(unitSize);

        BarData dataToShow = mShowGroup ?
                getGroupBarData(groupData, xVals) :
                getUserBarData(userData, xVals);

        mBarChart.setData(dataToShow);

        mIsLoading = false;
        toggleProgressBarVisibility();

        if (mBarChart.isEmpty() || mHasNoData) {
            setEmptyViewVisibility(true);
        } else {
            setEmptyViewVisibility(false);
            mBarChart.animateY(BarChart.ANIMATION_Y_TIME);
            mHasNoData = true;
        }
    }

    @NonNull
    private List<String> getXvals(int unitSize) {
        List<String> xVals = new ArrayList<>(unitSize);
        for (int i = 1; i <= unitSize; i++) {
            switch (mPeriodType) {
                case PERIOD_YEAR:
                    xVals.add(DateUtils.getMonthNameShort(i));
                    break;
                case PERIOD_MONTH:
                    xVals.add(String.valueOf(i));
                    break;
            }
        }
        return xVals;
    }

    @NonNull
    private BarData getUserBarData(@NonNull List<Stats.Member> userData, List<String> xVals) {
        int userDataSize = userData.size();
        List<BarDataSet> barDataSets = new ArrayList<>(userDataSize);
        for (int i = 0; i < userDataSize; i++) {
            Stats.Member user = userData.get(i);
            List<Stats.Unit> units = user.getUnits();
            List<BarEntry> barEntries = getBarEntries(units);

            String userId = user.getMemberId();
            User buyer = (User) ParseObject.createWithoutData(User.CLASS, userId);
            BarDataSet barDataSet = new BarDataSet(barEntries, buyer.getNicknameOrMe(getActivity()));
            barDataSet.setColor(getColor(i));

            barDataSets.add(barDataSet);
        }

        return new BarData(xVals, barDataSets);
    }

    @NonNull
    private List<BarEntry> getBarEntries(@NonNull List<Stats.Unit> units) {
        List<BarEntry> barEntries = new ArrayList<>(units.size());

        for (Stats.Unit unit : units) {
            float value = mShowAverage ? unit.getAverage() : unit.getTotal();
            int identifier = Integer.parseInt(unit.getIdentifier());
            BarEntry barEntry = new BarEntry(value, identifier);
            barEntries.add(barEntry);

            if (value > 0) {
                mHasNoData = false;
            }
        }

        return barEntries;
    }

    @NonNull
    private BarData getGroupBarData(@NonNull Stats.Group groupData, List<String> xVals) {
        List<Stats.Unit> units = groupData.getUnits();
        List<BarEntry> barEntries = getBarEntries(units);

        BarDataSet barDataSet = new BarDataSet(barEntries, mCurrentGroup.getName());
        barDataSet.setColors(getColors());

        return new BarData(xVals, barDataSet);
    }
}
