package ch.giantific.qwittig.ui;


import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.utils.PercentFormatter;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.parse.CloudCode;
import ch.giantific.qwittig.data.stats.models.Stats;
import ch.giantific.qwittig.ui.widgets.CurrencyFormatter;

/**
 * A simple {@link Fragment} subclass.
 */
public class StatsStoresFragment extends StatsPieBaseFragment {

    private static final String LOG_TAG = StatsStoresFragment.class.getSimpleName();
    private static final String STATE_SHOW_PERCENT = "state_show_percent";
    private static final String STATE_SHOW_AVERAGE = "state_show_average";
    private boolean mShowPercent = true;
    private boolean mShowAverage;

    public StatsStoresFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        if (savedInstanceState != null) {
            mShowPercent = savedInstanceState.getBoolean(STATE_SHOW_PERCENT, true);
            mShowAverage = savedInstanceState.getBoolean(STATE_SHOW_AVERAGE, false);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(STATE_SHOW_PERCENT, mShowPercent);
        outState.putBoolean(STATE_SHOW_AVERAGE, mShowAverage);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_stats_pie, container, false);

        findBaseViews(rootView);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_stats_stores, menu);

        setMenuValues(menu);
    }

    @Override
    void setMenuValues(Menu menu) {
        super.setMenuValues(menu);

        MenuItem showPercent = menu.findItem(R.id.action_show_percent);
        showPercent.setChecked(mShowPercent);

        MenuItem showAverage = menu.findItem(R.id.action_show_average);
        showAverage.setChecked(mShowAverage);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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

    @Override
    void calcStats(String year, int month) {
        super.calcStats(year, month);

        String groupId = mCurrentGroup.getObjectId();
        CloudCode.statsStores(getActivity(), this, groupId, year, month);
    }

    @Override
    public void onCloudFunctionReturned(String cloudFunction, Object o) {
        switch (cloudFunction) {
            case CloudCode.STATS_STORES: {
                String dataJson = (String) o;
                mStatsData = parseJson(dataJson);

                setChartData();
                break;
            }
        }
    }

    @Override
    float getValue(Stats.Unit unit) {
        if (mShowAverage) {
            return unit.getAverage();
        } else {
            return super.getValue(unit);
        }
    }

    @Override
    void setDataSetOptions(PieDataSet pieDataSet) {
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
