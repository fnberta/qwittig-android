/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.stats;

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
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.presentation.stats.StatsCalcWorker.StatsType;
import ch.giantific.qwittig.presentation.stats.models.Stats;
import ch.giantific.qwittig.presentation.stats.widgets.BarChart;
import ch.giantific.qwittig.utils.DateUtils;

/**
 * Displays the spending statistics in a {@link BarChart}. Shows which user spent how much money in
 * a specified period of time.
 * <p/>
 * Subclass of {@link StatsBaseFragment}.
 */
public class StatsSpendingFragment extends StatsBaseFragment {

    private static final String STATS_WORKER_SPENDING = "STATS_WORKER_SPENDING";
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
        return inflater.inflate(R.layout.fragment_stats_spending, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mBarChart = (BarChart) view.findViewById(R.id.bc_spending);

        checkCurrentGroup();
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
    protected String getWorkerTag() {
        return STATS_WORKER_SPENDING;
    }

    @Override
    void calcStats(@NonNull String year, int month) {
        super.calcStats(year, month);

        calcStatsWithWorker(StatsType.SPENDING, year, month);
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
        final int userDataSize = userData.size();
        final List<BarDataSet> barDataSets = new ArrayList<>(userDataSize);
        for (int i = 0; i < userDataSize; i++) {
            final Stats.Member user = userData.get(i);
            final List<Stats.Unit> units = user.getUnits();
            final List<BarEntry> barEntries = getBarEntries(units);

            final String userId = user.getMemberId();
            final Identity buyer = (Identity) ParseObject.createWithoutData(Identity.CLASS, userId);
            final BarDataSet barDataSet = new BarDataSet(barEntries, buyer.getNickname());
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
