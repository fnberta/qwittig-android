package ch.giantific.qwittig.ui;


import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.CallSuper;
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
import ch.giantific.qwittig.ui.adapter.StatsPieChartRecyclerAdapter;
import ch.giantific.qwittig.ui.widgets.PieChart;

/**
 * A simple {@link Fragment} subclass.
 */
public abstract class StatsPieBaseFragment extends StatsBaseFragment {

    private static final String LOG_TAG = StatsPieBaseFragment.class.getSimpleName();
    private static final String STATE_SORT_BY_USER = "state_sort_by_user";
    PieChart mPieChart;
    RecyclerView mRecyclerView;
    StatsPieChartRecyclerAdapter mRecyclerAdapter;
    List<PieData> mUserPieData = new ArrayList<>();
    List<String> mUserNicknames = new ArrayList<>();
    boolean mSortByUser;

    public StatsPieBaseFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        if (savedInstanceState != null) {
            mSortByUser = savedInstanceState.getBoolean(STATE_SORT_BY_USER, false);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(STATE_SORT_BY_USER, mSortByUser);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_stats_pie, container, false);

        findBaseViews(rootView);

        return rootView;
    }

    @Override
    void findBaseViews(View rootView) {
        super.findBaseViews(rootView);

        mPieChart = (PieChart) rootView.findViewById(R.id.pc_stores);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.rv_stats_stores);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRecyclerAdapter = new StatsPieChartRecyclerAdapter(getActivity(),
                R.layout.row_stats_stores_user, mUserPieData, mUserNicknames);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(mRecyclerAdapter);
    }

    @CallSuper
    void setMenuValues(Menu menu) {
        MenuItem sortByUsers = menu.findItem(R.id.action_sort_by_user);
        sortByUsers.setChecked(mSortByUser);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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

    final void setChartData() {
        List<Stats.Member> userData = mStatsData.getMembers();
        Stats.Group groupData = mStatsData.getGroup();

        if (mSortByUser) {
            setUserChartData(userData);
        } else {
            setGroupChartData(groupData);
        }
    }

    private void setUserChartData(List<Stats.Member> userData) {
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

    float getValue(Stats.Unit unit) {
        return unit.getTotal();
    }

    private void setGroupChartData(Stats.Group groupData) {
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

    @CallSuper
    void setDataOptions(PieData pieData) {
        pieData.setValueTextSize(14f);
        pieData.setValueTextColor(Color.WHITE);
    }

    @CallSuper
    void setDataSetOptions(PieDataSet pieDataSet) {
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
