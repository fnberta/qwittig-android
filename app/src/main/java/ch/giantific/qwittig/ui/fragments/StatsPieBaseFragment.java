/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.ui.fragments;


import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.parse.ParseObject;

import java.util.ArrayList;
import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.parse.models.User;
import ch.giantific.qwittig.data.stats.models.Stats;
import ch.giantific.qwittig.ui.adapters.StatsPieChartRecyclerAdapter;
import ch.giantific.qwittig.ui.widgets.PieChart;

/**
 * Provides an abstract base class for the display of stats data in a {@link PieChart}.
 * <p/>
 * Subclass of {@link StatsBaseFragment}.
 */
public abstract class StatsPieBaseFragment extends StatsBaseFragment {

    private static final String LOG_TAG = StatsPieBaseFragment.class.getSimpleName();
    private static final String STATE_SORT_BY_USER = "STATE_SORT_BY_USER";
    PieChart mPieChart;
    StatsPieChartRecyclerAdapter mRecyclerAdapter;
    boolean mSortByUser;
    private RecyclerView mRecyclerView;
    @NonNull
    private List<PieData> mUserPieData = new ArrayList<>();
    @NonNull
    private List<String> mUserNicknames = new ArrayList<>();

    public StatsPieBaseFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        if (savedInstanceState != null) {
            mSortByUser = savedInstanceState.getBoolean(STATE_SORT_BY_USER, false);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(STATE_SORT_BY_USER, mSortByUser);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_stats_pie, container, false);

        findBaseViews(rootView);

        return rootView;
    }

    @Override
    void findBaseViews(@NonNull View rootView) {
        super.findBaseViews(rootView);

        mPieChart = (PieChart) rootView.findViewById(R.id.pc_stores);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.rv_stats_stores);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRecyclerAdapter = new StatsPieChartRecyclerAdapter(mUserPieData, mUserNicknames);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(mRecyclerAdapter);
    }

    @CallSuper
    void setMenuValues(@NonNull Menu menu) {
        MenuItem sortByUsers = menu.findItem(R.id.action_sort_by_user);
        sortByUsers.setChecked(mSortByUser);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_sort_by_user:
                item.setChecked(!item.isChecked());
                mSortByUser = !mSortByUser;
                setChartData();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void showChart() {
        if (mSortByUser) {
            mPieChart.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
        } else {
            mPieChart.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);
        }
    }

    @Override
    protected void hideChart() {
        mPieChart.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.GONE);
    }

    protected void setChartData() {
        if (!mDataIsLoaded || mStatsData == null) {
            return;
        }

        List<Stats.Member> userData = mStatsData.getMembers();
        Stats.Group groupData = mStatsData.getGroup();

        if (mSortByUser) {
            setUserChartData(userData);
        } else {
            setGroupChartData(groupData);
        }
    }

    private void setUserChartData(@NonNull List<Stats.Member> userData) {
        mUserPieData.clear();
        mUserNicknames.clear();
        boolean hasNoData = true;

        for (Stats.Member member : userData) {
            List<Entry> yVals = new ArrayList<>();
            List<String> xVals = new ArrayList<>();

            String userId = member.getMemberId();
            User user = (User) ParseObject.createWithoutData(User.CLASS, userId);
            mUserNicknames.add(user.getNicknameOrMe(getActivity()));

            List<Stats.Unit> units = member.getUnits();
            for (int i = 0, unitsSize = units.size(); i < unitsSize; i++) {
                Stats.Unit unit = units.get(i);
                float value = getValue(unit);

                if (value > 0) {
                    xVals.add(unit.getIdentifier());
                    yVals.add(new Entry(value, i));

                    hasNoData = false;
                }
            }

            PieDataSet pieDataSet = new PieDataSet(yVals, "");
            setDataSetOptions(pieDataSet);

            PieData pieData = new PieData(xVals, pieDataSet);
            setDataOptions(pieData);
            mUserPieData.add(pieData);
        }

        mIsLoading = false;
        toggleProgressBarVisibility();

        if (hasNoData) {
            setEmptyViewVisibility(true);
        } else {
            setEmptyViewVisibility(false);
            setChartOptions();
            mRecyclerAdapter.notifyDataSetChanged();
        }
    }

    float getValue(@NonNull Stats.Unit unit) {
        return unit.getTotal();
    }

    private void setGroupChartData(@NonNull Stats.Group groupData) {
        List<Entry> yVals = new ArrayList<>();
        List<String> xVals = new ArrayList<>();

        List<Stats.Unit> units = groupData.getUnits();
        for (int i = 0, unitsSize = units.size(); i < unitsSize; i++) {
            Stats.Unit unit = units.get(i);
            float value = getValue(unit);

            if (value > 0) {
                xVals.add(unit.getIdentifier());
                yVals.add(new Entry(value, i));
            }
        }

        PieDataSet pieDataSet = new PieDataSet(yVals, "");
        setDataSetOptions(pieDataSet);

        PieData pieData = new PieData(xVals, pieDataSet);
        setDataOptions(pieData);

        setPieCenterText();
        mPieChart.setData(pieData);
        setChartOptions();

        mIsLoading = false;
        toggleProgressBarVisibility();

        if (mPieChart.isEmpty()) {
            setEmptyViewVisibility(true);
        } else {
            setEmptyViewVisibility(false);
            mPieChart.animateY(PieChart.ANIMATION_Y_TIME);
        }
    }

    private void setDataOptions(@NonNull PieData pieData) {
        pieData.setValueTextSize(14f);
        pieData.setValueTextColor(Color.WHITE);
    }

    @CallSuper
    void setDataSetOptions(@NonNull PieDataSet pieDataSet) {
        pieDataSet.setColors(getColors());
        pieDataSet.setSliceSpace(3f);
    }

    @CallSuper
    void setChartOptions() {
        // nothing globally
    }

    private void setPieCenterText() {
        mPieChart.setCenterText(mCurrentGroup.getName());
    }
}
