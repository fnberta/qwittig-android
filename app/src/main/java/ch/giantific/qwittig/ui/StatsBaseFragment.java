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
import ch.giantific.qwittig.helpers.StatsHelper;
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
    private static final String STATE_STATS_DATA = "state_stats_data";
    private static final String STATE_PERIOD_TYPE = "state_period_type";
    private static final String LOG_TAG = StatsBaseFragment.class.getSimpleName();
    FragmentInteractionListener mListener;
    User mCurrentUser;
    Group mCurrentGroup;
    int mPeriodType;
    boolean mIsLoading;
    Stats mStatsData;
    boolean mDataIsLoaded;
    private TextView mTextViewEmptyView;
    private ProgressBar mProgressBar;
    private boolean mIsRecreating;

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

        if (savedInstanceState != null) {
            mStatsData = savedInstanceState.getParcelable(STATE_STATS_DATA);
            mPeriodType = savedInstanceState.getInt(STATE_PERIOD_TYPE);
            mIsRecreating = true;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mStatsData != null) {
            outState.putParcelable(STATE_STATS_DATA, mStatsData);
            outState.putInt(STATE_PERIOD_TYPE, mPeriodType);
        }
    }

    @CallSuper
    void findBaseViews(View rootView) {
        mTextViewEmptyView = (TextView) rootView.findViewById(R.id.tv_empty_view);
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.pb_stats);
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
                loadData();
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
        // empty default implementation
    }

    @Override
    public void onObjectFetched(ParseObject object) {
        setStuffWithGroupData();
        loadData();
    }

    private boolean userIsInGroup() {
        return mCurrentUser != null && mCurrentGroup != null;
    }

    public void loadData() {
        if (mIsLoading) {
            return;
        }

        if (!userIsInGroup()) {
            setEmptyViewVisibility(true);
            toggleProgressBarVisibility();
            return;
        }

        if (mIsRecreating && mStatsData != null) {
            mIsRecreating = false;
            mDataIsLoaded = true;
            setChartData();
        } else {
            String year = mListener.getYear();
            Month month = mListener.getMonth();
            int monthNumber = month.getNumber();
            mPeriodType = monthNumber == 0 ? PERIOD_YEAR : PERIOD_MONTH;

            calcStats(year, monthNumber);
        }
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
        int colorsSize = colors.length;
        if (position >= 0 && position < colorsSize) {
            return colors[position];
        } else if (position >= colorsSize) {
            return getColor(position - colorsSize);
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

        if (stats == null) {
            mIsLoading = false;
            toggleProgressBarVisibility();
            setEmptyViewVisibility(true);
            return;
        }

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
        toggleProgressBarVisibility();
        setEmptyViewVisibility(true);
    }

    private void showErrorSnackbar(String message) {
        Snackbar snackbar = MessageUtils.getBasicSnackbar(mTextViewEmptyView, message);
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
        String getYear();

        Month getMonth();
    }

}
