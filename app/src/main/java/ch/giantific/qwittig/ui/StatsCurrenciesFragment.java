package ch.giantific.qwittig.ui;


import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.utils.PercentFormatter;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.parse.CloudCode;

/**
 * A simple {@link Fragment} subclass.
 */
public class StatsCurrenciesFragment extends StatsPieBaseFragment {

    private static final String LOG_TAG = StatsCurrenciesFragment.class.getSimpleName();

    public StatsCurrenciesFragment() {
        // Required empty public constructor
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
        inflater.inflate(R.menu.menu_stats_currencies, menu);

        setMenuValues(menu);
    }

    @Override
    void calcStats(String year, int month) {
        super.calcStats(year, month);

        String groupId = mCurrentGroup.getObjectId();
        CloudCode.statsCurrencies(getActivity(), this, groupId, year, month);
    }

    @Override
    public void onCloudFunctionReturned(String cloudFunction, Object o) {
        super.onCloudFunctionReturned(cloudFunction, o);

        switch (cloudFunction) {
            case CloudCode.STATS_CURRENCIES: {
                String dataJson = (String) o;
                mStatsData = parseJson(dataJson);

                setChartData();
                break;
            }
        }
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
    void setDataSetOptions(PieDataSet pieDataSet) {
        super.setDataSetOptions(pieDataSet);

        pieDataSet.setValueFormatter(new PercentFormatter());
    }
}
