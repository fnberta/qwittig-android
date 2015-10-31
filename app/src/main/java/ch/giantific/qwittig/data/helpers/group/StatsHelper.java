/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.data.helpers.group;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.parse.ParseUser;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import ch.giantific.qwittig.data.helpers.BaseHelper;
import ch.giantific.qwittig.domain.models.parse.Group;
import ch.giantific.qwittig.domain.models.parse.User;
import ch.giantific.qwittig.domain.models.stats.Stats;
import ch.giantific.qwittig.data.rest.CloudCodeClient;

/**
 * Calculates different statistics by calling Parse.com cloud functions.
 * <p/>
 * Currently handles spending, stores and currency statistics, as defined in {@link StatsType}.
 * <p/>
 * Subclass of {@link BaseHelper}.
 */
public class StatsHelper extends BaseHelper implements
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
    private static final String LOG_TAG = StatsHelper.class.getSimpleName();
    @Nullable
    private HelperInteractionListener mListener;
    private int mStatsType;
    public StatsHelper() {
        // empty default constructor
    }

    /**
     * Returns a new instance of {@link StatsHelper}.
     *
     * @param statsType the type of statistic to calculate
     * @param year      the year to calculate statistics for
     * @param month     the month to calculate statistics for (1-12), if 0 statistics for whole year
     *                  will be calculated
     * @return a new instance of {@link StatsHelper}
     */
    @NonNull
    public static StatsHelper newInstance(@StatsType int statsType, @NonNull String year,
                                          int month) {
        StatsHelper fragment = new StatsHelper();
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
            mListener = (HelperInteractionListener) activity;
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
            mStatsType = args.getInt(BUNDLE_STATS_TYPE);
            year = args.getString(BUNDLE_YEAR);
            month = args.getInt(BUNDLE_MONTH);
        }

        if (TextUtils.isEmpty(year)) {
            if (mListener != null) {
                mListener.onStatsCalculationFailed(mStatsType, 0);
            }
            return;
        }

        CloudCodeClient cloudCode = new CloudCodeClient();
        String groupId = getCurrentGroupId();
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

    private String getCurrentGroupId() {
        User currentUser = (User) ParseUser.getCurrentUser();
        Group currentGroup = currentUser.getCurrentGroup();
        return currentGroup.getObjectId();
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
    public void onCloudFunctionFailed(int errorCode) {
        if (mListener != null) {
            mListener.onStatsCalculationFailed(mStatsType, errorCode);
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
    public interface HelperInteractionListener {
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
         * @param statsType the type of stats that failed to calculate, one of {@link StatsType}
         * @param errorCode the error code of the exception thrown in the process
         */
        void onStatsCalculationFailed(int statsType, int errorCode);
    }
}
