package ch.giantific.qwittig.ui;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.models.Month;
import ch.giantific.qwittig.data.parse.LocalQuery;
import ch.giantific.qwittig.data.parse.models.Group;
import ch.giantific.qwittig.data.parse.models.User;
import ch.giantific.qwittig.data.stats.models.Stats;
import ch.giantific.qwittig.helper.StatsHelper;
import ch.giantific.qwittig.utils.MessageUtils;
import ch.giantific.qwittig.utils.ParseErrorHandler;
import ch.giantific.qwittig.utils.ParseUtils;
import ch.giantific.qwittig.utils.Utils;

/**
 * A simple {@link Fragment} subclass.
 */
public abstract class StatsBaseFragment extends BaseFragment implements
        LocalQuery.ObjectLocalFetchListener {

    static final int PERIOD_YEAR = 0;
    static final int PERIOD_MONTH = 1;
    static final int NUMBER_OF_MONTHS = 12;
    private static final String LOG_TAG = StatsBaseFragment.class.getSimpleName();
    FragmentInteractionListener mListener;
    User mCurrentUser;
    Group mCurrentGroup;
    int mPeriodType;
    boolean mIsLoading;
    Stats mStatsData;
    boolean mDataIsLoaded;
    private Spinner mSpinnerPeriod;
    private Spinner mSpinnerYear;
    private Spinner mSpinnerMonth;
    private TextView mTextViewEmptyView;
    private ProgressBar mProgressBar;
    private boolean mListenersAreSet;

    public StatsBaseFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (FragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement FragmentInteractionListener");
        }
    }

    @CallSuper
    void findBaseViews(View rootView) {
        mSpinnerPeriod = (Spinner) rootView.findViewById(R.id.sp_period);
        mSpinnerYear = (Spinner) rootView.findViewById(R.id.sp_year);
        mSpinnerMonth = (Spinner) rootView.findViewById(R.id.sp_month);
        mTextViewEmptyView = (TextView) rootView.findViewById(R.id.tv_empty_view);
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.pb_stats);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupPeriodAdapter();
        setupYearAdapter();
        setupMonthAdapter();
    }

    private void setupPeriodAdapter() {
        String[] periods = getResources().getStringArray(R.array.stats_periods);
        final ArrayAdapter<String> spinnerPeriodAdapter = new ArrayAdapter<>(getActivity(),
                R.layout.spinner_item_stats, periods);
        spinnerPeriodAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        mSpinnerPeriod.setAdapter(spinnerPeriodAdapter);
    }

    private void setupYearAdapter() {
        ArrayAdapter<String> spinnerYearAdapter = new ArrayAdapter<>(getActivity(),
                R.layout.spinner_item_stats, getLastYears(5));
        spinnerYearAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        mSpinnerYear.setAdapter(spinnerYearAdapter);
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

    private void setupMonthAdapter() {
        List<Month> months = new ArrayList<>();
        int i = 1;
        while (i <= NUMBER_OF_MONTHS) {
            months.add(new Month(i));
            i++;
        }

        ArrayAdapter<Month> spinnerMonthAdapter = new ArrayAdapter<>(getActivity(),
                R.layout.spinner_item_stats, months);
        spinnerMonthAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        mSpinnerMonth.setAdapter(spinnerMonthAdapter);
    }

    @Override
    public void onStart() {
        super.onStart();

        updateData();
    }

    @CallSuper
    void updateData() {
        updateCurrentUserAndGroup();
        checkCurrentGroup();
    }

    private void updateCurrentUserAndGroup() {
        mCurrentUser = (User) ParseUser.getCurrentUser();
        if (mCurrentUser != null) {
            mCurrentGroup = mCurrentUser.getCurrentGroup();
        }
    }

    private void checkCurrentGroup() {
        if (mCurrentGroup != null) {
            if (mCurrentGroup.isDataAvailable()) {
                setStuffWithGroupData();
            } else {
                LocalQuery.fetchObjectData(this, mCurrentGroup);
            }
        } else {
            setEmptyViewVisibility(true);
            toggleProgressBarVisibility();
        }
    }

    @CallSuper
    void setStuffWithGroupData() {
        if (!mListenersAreSet) {
            setPeriodSelectedListener();
            setYearSelectedListener();
            setMonthSelectedListener();

            mListenersAreSet = true;
        } else {
            if (userIsInGroup()) {
                loadData();
            } else {
                setEmptyViewVisibility(true);
                toggleProgressBarVisibility();
            }
        }
    }

    @Override
    public void onObjectFetched(ParseObject object) {
        setStuffWithGroupData();
    }

    private void setPeriodSelectedListener() {
        mSpinnerPeriod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String period = (String) parent.getItemAtPosition(position);
                if (userIsInGroup()) {
                    onPeriodSelected(period);
                } else {
                    setEmptyViewVisibility(true);
                    toggleProgressBarVisibility();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private boolean userIsInGroup() {
        return mCurrentUser != null && mCurrentGroup != null;
    }

    private void onPeriodSelected(String period) {
        if (period.equals(getString(R.string.period_year))) {
            mPeriodType = PERIOD_YEAR;

            if (mSpinnerMonth.getVisibility() == View.VISIBLE) {
                mSpinnerMonth.setVisibility(View.GONE);
            }
        } else if (period.equals(getString(R.string.period_month))) {
            mPeriodType = PERIOD_MONTH;

            if (mSpinnerMonth.getVisibility() == View.GONE) {
                mSpinnerMonth.setVisibility(View.VISIBLE);
            }
        }

        loadData();
    }

    private void setYearSelectedListener() {
        mSpinnerYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (userIsInGroup()) {
                    loadData();
                } else {
                    setEmptyViewVisibility(true);
                    toggleProgressBarVisibility();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void setMonthSelectedListener() {
        mSpinnerMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (userIsInGroup()) {
                    loadData();
                } else {
                    setEmptyViewVisibility(true);
                    toggleProgressBarVisibility();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public void loadData() {
        if (mIsLoading) {
            return;
        }

        String year = (String) mSpinnerYear.getSelectedItem();
        int monthNumber = 0;

        if (mPeriodType == PERIOD_MONTH) {
            Month month = (Month) mSpinnerMonth.getSelectedItem();
            monthNumber = month.getNumber();
        }

        calcStats(year, monthNumber);
    }

    @CallSuper
    void calcStats(String year, int month) {
        if (!Utils.isConnected(getActivity())) {
            showErrorSnackbar(ParseErrorHandler.getErrorMessage(getActivity(),
                    ParseUtils.getNoConnectionException()));

            setEmptyViewVisibility(true);
            toggleProgressBarVisibility();
            return;
        }

        mIsLoading = true;
        toggleProgressBarVisibility();
    }

    final void toggleProgressBarVisibility() {
        if (mIsLoading) {
            mProgressBar.setVisibility(View.VISIBLE);
            mTextViewEmptyView.setVisibility(View.GONE);
            hideChart();
        } else {
            mProgressBar.setVisibility(View.GONE);
        }
    }

    protected abstract void showChart();

    protected abstract void hideChart();

    @CallSuper
    void setEmptyViewVisibility(boolean showEmptyView) {
        if (showEmptyView) {
            hideChart();
            mTextViewEmptyView.setVisibility(View.VISIBLE);
        } else {
            showChart();
            mTextViewEmptyView.setVisibility(View.GONE);
        }
    }

    final List<Integer> getColors() {
        List<Integer> colors = new ArrayList<>();

        for (int i = 0; i <= 11; i++) {
            colors.add(getColor(i));
        }

        return colors;
    }

    final int getColor(int position) {
        int[] colors = getResources().getIntArray(R.array.stats_colors);
        if (position >= 0 && position <= 11) {
            return colors[position];
        } else if (position > 11) {
            return getColor(position - 12);
        }

        return -1;
    }

    final void calcStatsWithHelper(@StatsHelper.StatsType int statsType,
                                     String year, int month) {
        FragmentManager fragmentManager = getFragmentManager();
        StatsHelper statsHelper = findStatsHelper(fragmentManager);

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (statsHelper == null) {
            statsHelper = StatsHelper.newInstance(statsType, year, month);

            fragmentManager.beginTransaction()
                    .add(statsHelper, getHelperTag())
                    .commit();
        }
    }

    protected abstract String getHelperTag();

    private StatsHelper findStatsHelper(FragmentManager fragmentManager) {
        return (StatsHelper) fragmentManager.findFragmentByTag(getHelperTag());
    }

    private void removeStatsHelper() {
        FragmentManager fragmentManager = getFragmentManager();
        StatsHelper statsHelper = findStatsHelper(fragmentManager);

        if (statsHelper != null) {
            fragmentManager.beginTransaction().remove(statsHelper).commitAllowingStateLoss();
        }
    }

    /**
     * Called from activity when helper successfully calculated new stats
     * @param stats
     */
    @CallSuper
    public void onStatsCalculated(Stats stats) {
        removeStatsHelper();

        mDataIsLoaded = true;
        mStatsData = stats;
        setChartData();
    }

    protected abstract void setChartData();

    /**
     * Called from activity when helper failed to calculate stats
     * @param e
     */
    @CallSuper
    public void onFailedToCalculateStats(ParseException e) {
        ParseErrorHandler.handleParseError(getActivity(), e);
        showErrorSnackbar(ParseErrorHandler.getErrorMessage(getActivity(), e));
        removeStatsHelper();
                
        mIsLoading = false;
    }

    private void showErrorSnackbar(String message) {
        Snackbar snackbar = MessageUtils.getBasicSnackbar(mSpinnerPeriod, message);
        snackbar.setAction(R.string.action_retry, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadData();
            }
        });
        snackbar.show();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface FragmentInteractionListener {
    }

}
