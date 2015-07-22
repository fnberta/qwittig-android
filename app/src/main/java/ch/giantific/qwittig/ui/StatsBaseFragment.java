package ch.giantific.qwittig.ui;

import android.app.Activity;
import android.app.Fragment;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.Gson;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.models.Month;
import ch.giantific.qwittig.data.parse.CloudCode;
import ch.giantific.qwittig.data.parse.models.Group;
import ch.giantific.qwittig.data.parse.models.User;
import ch.giantific.qwittig.data.stats.models.Stats;
import ch.giantific.qwittig.utils.MessageUtils;
import ch.giantific.qwittig.utils.ParseErrorHandler;
import ch.giantific.qwittig.utils.ParseUtils;
import ch.giantific.qwittig.utils.Utils;

/**
 * A simple {@link Fragment} subclass.
 */
public abstract class StatsBaseFragment extends BaseFragment implements
        CloudCode.CloudFunctionListener {

    static final int PERIOD_YEAR = 0;
    static final int PERIOD_MONTH = 1;
    static final int NUMBER_OF_MONTHS = 12;
    FragmentInteractionListener mListener;
    User mCurrentUser;
    Group mCurrentGroup;
    int mPeriodType;
    Spinner mSpinnerPeriod;
    Spinner mSpinnerYear;
    Spinner mSpinnerMonth;
    TextView mTextViewEmptyView;
    ProgressBar mProgressBar;
    boolean mIsLoading;
    Stats mStatsData;


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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        updateCurrentUserAndGroup();
    }

    private void updateCurrentUserAndGroup() {
        mCurrentUser = (User) ParseUser.getCurrentUser();
        if (mCurrentUser != null) {
            mCurrentGroup = mCurrentUser.getCurrentGroup();
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
        mSpinnerPeriod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String period = (String) parent.getItemAtPosition(position);
                if (userIsInGroup()) {
                    onPeriodSelected(period);
                } else {
                    setEmptyViewVisibility(true);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void onPeriodSelected(String period) {
        if (period.equals(getString(R.string.period_year))) {
            mPeriodType = PERIOD_YEAR;

            if (mSpinnerMonth.getVisibility() == View.VISIBLE) {
                mSpinnerMonth.setVisibility(View.GONE);
            }

            onYearSelected((String) mSpinnerYear.getSelectedItem());
        } else if (period.equals(getString(R.string.period_month))) {
            mPeriodType = PERIOD_MONTH;

            if (mSpinnerMonth.getVisibility() == View.GONE) {
                mSpinnerMonth.setVisibility(View.VISIBLE);
            }

            Month month = (Month) mSpinnerMonth.getSelectedItem();
            onMonthSelected(month.getNumber());
        }
    }

    private boolean userIsInGroup() {
        return mCurrentUser != null && mCurrentGroup != null;
    }

    private void setupYearAdapter() {
        ArrayAdapter<String> spinnerYearAdapter = new ArrayAdapter<>(getActivity(),
                R.layout.spinner_item_stats, getLastYears(5));
        spinnerYearAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        mSpinnerYear.setAdapter(spinnerYearAdapter);
        mSpinnerYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String year = (String) parent.getItemAtPosition(position);
                if (userIsInGroup()) {
                    onYearSelected(year);
                } else {
                    setEmptyViewVisibility(true);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void onYearSelected(String year) {
        if (!mIsLoading) {
            switch (mPeriodType) {
                case PERIOD_YEAR:
                    calcStats(year);
                    break;
                case PERIOD_MONTH:
                    Month month = (Month) mSpinnerMonth.getSelectedItem();
                    calcStats(year, month.getNumber());
                    break;
            }
        }
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
        mSpinnerMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Month month = (Month) parent.getItemAtPosition(position);
                if (userIsInGroup()) {
                    onMonthSelected(month.getNumber());
                } else {
                    setEmptyViewVisibility(true);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void onMonthSelected(int month) {
        if (!mIsLoading) {
            String year = (String) mSpinnerYear.getSelectedItem();
            calcStats(year, month);
        }
    }

    @CallSuper
    void calcStats(String year) {
        calcStats(year, 0);
    }

    @CallSuper
    void calcStats(String year, int month) {
        if (!Utils.isConnected(getActivity())) {
            showErrorSnackbar(ParseErrorHandler.getErrorMessage(getActivity(),
                    ParseUtils.getNoConnectionException()));
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

    public void reloadData() {
        updateCurrentUserAndGroup();

        String year = (String) mSpinnerYear.getSelectedItem();
        int monthNumber = 0;

        if (mPeriodType == PERIOD_MONTH) {
            Month month = (Month) mSpinnerMonth.getSelectedItem();
            monthNumber = month.getNumber();
        }

        calcStats(year, monthNumber);
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

    final Stats parseJson(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, Stats.class);
    }

    @Override
    public void onCloudFunctionError(ParseException e) {
        mIsLoading = false;
        showErrorSnackbar(ParseErrorHandler.getErrorMessage(getActivity(), e));
    }

    private void showErrorSnackbar(String message) {
        Snackbar snackbar = MessageUtils.getBasicSnackbar(mSpinnerPeriod, message);
        snackbar.setAction(R.string.action_retry, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reloadData();
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
