/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.ui.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.repositories.ParseGroupRepository;
import ch.giantific.qwittig.domain.models.Month;
import ch.giantific.qwittig.domain.models.parse.Group;
import ch.giantific.qwittig.domain.models.stats.Stats;
import ch.giantific.qwittig.domain.repositories.GroupRepository;
import ch.giantific.qwittig.utils.Utils;
import ch.giantific.qwittig.utils.WorkerUtils;
import ch.giantific.qwittig.workerfragments.group.StatsWorker;

/**
 * Provides an abstract base class for the display of statistical information related to a group.
 * <p/>
 * Subclass of {@link BaseFragment}.
 */
public abstract class StatsBaseFragment extends BaseFragment {

    static final int PERIOD_YEAR = 0;
    static final int PERIOD_MONTH = 1;
    private static final String STATE_STATS_DATA = "STATE_STATS_DATA";
    private static final String STATE_PERIOD_TYPE = "STATE_PERIOD_TYPE";
    private static final String LOG_TAG = StatsBaseFragment.class.getSimpleName();
    int mPeriodType;
    boolean mIsLoading;
    @Nullable
    Stats mStatsData;
    boolean mDataIsLoaded;
    private FragmentInteractionListener mListener;
    private TextView mTextViewEmptyView;
    private ContentLoadingProgressBar mProgressBar;
    private boolean mIsRecreating;

    public StatsBaseFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (FragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement DialogInteractionListener");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        updateCurrentUserAndGroup();

        if (savedInstanceState != null) {
            mStatsData = savedInstanceState.getParcelable(STATE_STATS_DATA);
            mPeriodType = savedInstanceState.getInt(STATE_PERIOD_TYPE);
            mIsRecreating = true;
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mStatsData != null) {
            outState.putParcelable(STATE_STATS_DATA, mStatsData);
            outState.putInt(STATE_PERIOD_TYPE, mPeriodType);
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mTextViewEmptyView = (TextView) view.findViewById(R.id.tv_empty_view);
        mProgressBar = (ContentLoadingProgressBar) view.findViewById(R.id.pb_base);
    }

    /**
     * Starts the stats loading process by checking if data is available for the current group. If
     * yes, loads the stats data and if not, fetches the data for the group and then starts the
     * stats data loading.
     * <p/>
     * Called in subclasses after their view is ready.
     */
    final void checkCurrentGroup() {
        if (mCurrentGroup != null) {
            if (mCurrentGroup.isDataAvailable()) {
                setStuffWithGroupData();
                loadData();
            } else {
                GroupRepository repo = new ParseGroupRepository(getActivity());
                repo.fetchGroupDataAsync(mCurrentGroup, new GroupRepository.GetGroupLocalListener() {
                    @Override
                    public void onGroupLocalLoaded(@NonNull Group group) {
                        setStuffWithGroupData();
                        loadData();
                    }
                });
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

    private boolean userIsInGroup() {
        return mCurrentUser != null && mCurrentGroup != null;
    }

    /**
     * Reads the user set year and month and calls method to load the stats data from the server.
     */
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
            showErrorSnackbar(R.string.toast_no_connection);

            setEmptyViewVisibility(true);
            toggleProgressBarVisibility();
            return;
        }

        mIsLoading = true;
        toggleProgressBarVisibility();
    }

    final void toggleProgressBarVisibility() {
        if (mIsLoading) {
            mProgressBar.show();
            mTextViewEmptyView.setVisibility(View.GONE);
            hideChart();
        } else {
            mProgressBar.hide();
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

    @NonNull
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

    final void calcStatsWithWorker(@StatsWorker.StatsType int statsType,
                                   @NonNull String year, int month) {
        FragmentManager fragmentManager = getFragmentManager();
        Fragment statsWorker = WorkerUtils.findWorker(fragmentManager, getWorkerTag());

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (statsWorker == null) {
            statsWorker = StatsWorker.newInstance(statsType, year, month);

            fragmentManager.beginTransaction()
                    .add(statsWorker, getWorkerTag())
                    .commit();
        }
    }

    protected abstract String getWorkerTag();

    /**
     * Removes the retained worker fragment und sets the loaded chart data for display to the user.
     *
     * @param stats the stats data to set
     */
    @CallSuper
    public void onStatsCalculated(@Nullable Stats stats) {
        WorkerUtils.removeWorker(getFragmentManager(), getWorkerTag());

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
     * Shows the user the error message and removes the retained worker fragment and loading
     * indicators.
     *
     * @param errorMessage the error message from the exception thrown during the process
     */
    @CallSuper
    public void onStatsCalculationFailed(@StringRes int errorMessage) {
        showErrorSnackbar(errorMessage);
        WorkerUtils.removeWorker(getFragmentManager(), getWorkerTag());

        mIsLoading = false;
        toggleProgressBarVisibility();
        setEmptyViewVisibility(true);
    }

    private void showErrorSnackbar(@StringRes int message) {
        Snackbar snackbar = Snackbar.make(mTextViewEmptyView, message, Snackbar.LENGTH_LONG);
        snackbar.setAction(R.string.action_retry, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadData();
            }
        });
        snackbar.show();
    }

    /**
     * Updates the current user and current group fields and loads the stats data.
     */
    public void updateFragment() {
        updateCurrentUserAndGroup();
        checkCurrentGroup();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * Defines the interaction with the hosting {@link Activity}.
     */
    public interface FragmentInteractionListener {
        /**
         * Gets the year the user selected to calculate stats for.
         *
         * @return the year to calculate stats for
         */
        @NonNull
        String getYear();

        /**
         * Gets the month the user selected to calculate stats for.
         *
         * @return the month to calculate stats for
         */
        @NonNull
        Month getMonth();
    }

}
