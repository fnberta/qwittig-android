/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.workerfragments.group;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.parse.ParseUser;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import ch.giantific.qwittig.workerfragments.BaseWorker;
import ch.giantific.qwittig.domain.models.parse.Group;
import ch.giantific.qwittig.domain.models.parse.User;
import ch.giantific.qwittig.domain.models.stats.Stats;
import ch.giantific.qwittig.data.rest.CloudCodeClient;

/**
 * Calculates different statistics by calling Parse.com cloud functions.
 * <p/>
 * Currently handles spending, stores and currency statistics, as defined in {@link StatsType}.
 * <p/>
 * Subclass of {@link BaseWorker}.
 */
public class StatsWorker extends BaseWorker implements
        CloudCodeClient.CloudCodeListener {

    @IntDef({TYPE_SPENDING, TYPE_STORES, TYPE_CURRENCIES})
    @Retention(RetentionPolicy.SOURCE)
    public @interface StatsType {}
    public static final int TYPE_SPENDING = 1;
    public static final int TYPE_STORES = 2;
    public static final int TYPE_CURRENCIES = 3;
    private static final String BUNDLE_STATS_TYPE = "BUNDLE_STATS_TYPE";
    private static final String BUNDLE_YEAR = "BUNDLE_YEAR";
    private static final String BUNDLE_MONTH = "BUNDLE_MONTH";
    private static final String LOG_TAG = StatsWorker.class.getSimpleName();
    @Nullable
    private WorkerInteractionListener mListener;
    private int mStatsType;
    public StatsWorker() {
        // empty default constructor
    }

    /**
     * Returns a new instance of {@link StatsWorker}.
     *
     * @param statsType the type of statistic to calculate
     * @param year      the year to calculate statistics for
     * @param month     the month to calculate statistics for (1-12), if 0 statistics for whole year
     *                  will be calculated
     * @return a new instance of {@link StatsWorker}
     */
    @NonNull
    public static StatsWorker newInstance(@StatsType int statsType, @NonNull String year,
                                          int month) {
        StatsWorker fragment = new StatsWorker();
        Bundle args = new Bundle();
        args.putInt(BUNDLE_STATS_TYPE, statsType);
        args.putString(BUNDLE_YEAR, year);
        args.putInt(BUNDLE_MONTH, month);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (WorkerInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement DialogInteractionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String year = "";
        int month = 0;
        Bundle args = getArguments();
        if (args != null) {
            mStatsType = args.getInt(BUNDLE_STATS_TYPE, 0);
            year = args.getString(BUNDLE_YEAR, "");
            month = args.getInt(BUNDLE_MONTH, 0);
        }

        if (TextUtils.isEmpty(year)) {
            if (mListener != null) {
                mListener.onStatsCalculationFailed(mStatsType, 0);
            }
            return;
        }

        final User currentUser = (User) ParseUser.getCurrentUser();
        final Group currentGroup = currentUser.getCurrentGroup();
        if (currentGroup == null) {
            if (mListener != null) {
                mListener.onStatsCalculationFailed(mStatsType, 0);
            }
            return;
        }

        CloudCodeClient cloudCode = new CloudCodeClient(getActivity());
        String groupId = currentGroup.getObjectId();
        switch (mStatsType) {
            case TYPE_SPENDING:
                cloudCode.calcStatsSpending(groupId, year, month, this);
                break;
            case TYPE_STORES:
                cloudCode.calcStatsStores(groupId, year, month, this);
                break;
            case TYPE_CURRENCIES:
                cloudCode.calcStatsCurrencies(groupId, year, month, this);
                break;
        }
    }

    @Override
    public void onCloudFunctionReturned(Object result) {
        Stats stats = parseJson((String) result);
        if (mListener != null) {
            if (stats != null) {
                mListener.onStatsCalculated(mStatsType, stats);
            } else {
                mListener.onStatsCalculationFailed(mStatsType, 0);
            }
        }
    }

    @Override
    public void onCloudFunctionFailed(@StringRes int errorMessage) {
        if (mListener != null) {
            mListener.onStatsCalculationFailed(mStatsType, errorMessage);
        }
    }

    private Stats parseJson(@NonNull String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, Stats.class);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * Defines the actions to take after statistics were calculated or after the calculation failed.
     */
    public interface WorkerInteractionListener {
        /**
         * Handles the successful calculation of statistics.
         *
         * @param statsType the type of stats calculated, one of {@link StatsType}
         * @param stats     the calculated and parsed statistics
         */
        void onStatsCalculated(int statsType, @NonNull Stats stats);

        /**
         * Handles the failed calculation of statistics.
         *
         * @param statsType    the type of stats that failed to calculate, one of {@link StatsType}
         * @param errorMessage the error message from the exception thrown in the process
         */
        void onStatsCalculationFailed(int statsType, @StringRes int errorMessage);
    }
}
