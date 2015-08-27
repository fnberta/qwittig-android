package ch.giantific.qwittig.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.parse.ParseException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.models.Month;
import ch.giantific.qwittig.data.stats.models.Stats;
import ch.giantific.qwittig.helpers.StatsHelper;
import ch.giantific.qwittig.utils.Utils;

public class StatsActivity extends BaseNavDrawerActivity implements
        StatsBaseFragment.FragmentInteractionListener,
        StatsHelper.HelperInteractionListener {

    private static final String LOG_TAG = StatsActivity.class.getSimpleName();
    private static final String STATS_FRAGMENT = "stats_fragment";
    private static final int NUMBER_OF_MONTHS = 12;
    private Spinner mSpinnerStatsType;
    private Spinner mSpinnerYear;
    private Spinner mSpinnerMonth;
    private StatsBaseFragment mStatsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        // check item in NavDrawer
        checkNavDrawerItem(R.id.nav_stats);

        mSpinnerStatsType = (Spinner) findViewById(R.id.sp_stats_type);
        setupTypeSpinner();
        mSpinnerYear = (Spinner) findViewById(R.id.sp_year);
        setupYearSpinner();
        mSpinnerMonth = (Spinner) findViewById(R.id.sp_month);
        setupMonthSpinner();
    }

    private void setupTypeSpinner() {
        String[] types = new String[]{
                getString(R.string.tab_stats_spending),
                getString(R.string.tab_stats_stores),
                getString(R.string.tab_stats_currencies)};
        final ArrayAdapter<String> typesAdapter =
                new ArrayAdapter<>(this, R.layout.spinner_item_stats, types);
        typesAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        mSpinnerStatsType.setAdapter(typesAdapter);
        mSpinnerStatsType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String statsType = (String) parent.getItemAtPosition(position);
                switchFragment(statsType);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void setupYearSpinner() {
        ArrayAdapter<String> spinnerYearAdapter =
                new ArrayAdapter<>(this, R.layout.spinner_item_stats, getLastYears(5));
        spinnerYearAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        mSpinnerYear.setAdapter(spinnerYearAdapter);
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

    private void onPeriodSelected() {
        if (mStatsFragment != null) {
            mStatsFragment.loadData();
        }
    }

    private void setupMonthSpinner() {
        List<Month> months = new ArrayList<>();
        months.add(new Month(getString(R.string.stats_month_all)));
        for (int i = 1; i <= NUMBER_OF_MONTHS; i++) {
            months.add(new Month(i));
        }

        ArrayAdapter<Month> spinnerMonthAdapter =
                new ArrayAdapter<>(this, R.layout.spinner_item_stats, months);
        spinnerMonthAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        mSpinnerMonth.setAdapter(spinnerMonthAdapter);
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

    private void switchFragment(String statsType) {
        StatsBaseFragment fragment = null;
        if (statsType.equals(getString(R.string.tab_stats_spending))) {
            fragment = new StatsSpendingFragment();
        } else if (statsType.equals(getString(R.string.tab_stats_stores))) {
            fragment = new StatsStoresFragment();
        } else if (statsType.equals(getString(R.string.tab_stats_currencies))) {
            fragment = new StatsCurrenciesFragment();
        }

        if (fragment != null) {
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.container, fragment, STATS_FRAGMENT)
                    .commit();
            fragmentManager.executePendingTransactions();
            mStatsFragment = fragment;
        }
    }

    @Override
    void afterLoginSetup() {

        super.afterLoginSetup();
    }

    @Override
    public String getYear() {
        return (String) mSpinnerYear.getSelectedItem();
    }

    @Override
    public Month getMonth() {
        return (Month) mSpinnerMonth.getSelectedItem();
    }

    @Override
    public void onStatsCalculated(int statsType, Stats stats) {
        mStatsFragment.onStatsCalculated(stats);
    }

    @Override
    public void onFailedToCalculateStats(int statsType, ParseException e) {
        mStatsFragment.onFailedToCalculateStats(e);
    }

    @Override
    protected void onNewGroupSet() {
        mStatsFragment.updateData();
    }
}
