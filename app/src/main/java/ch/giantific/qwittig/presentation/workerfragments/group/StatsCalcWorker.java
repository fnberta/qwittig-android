/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.presentation.workerfragments.group;

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

import javax.inject.Inject;

import ch.giantific.qwittig.domain.models.parse.Group;
import ch.giantific.qwittig.domain.models.parse.User;
import ch.giantific.qwittig.domain.models.stats.Stats;
import ch.giantific.qwittig.domain.repositories.ApiRepository;
import ch.giantific.qwittig.presentation.workerfragments.BaseWorker;
import rx.Observable;
import rx.Single;
import rx.functions.Func1;

/**
 * Calculates different statistics by calling Parse.com cloud functions.
 * <p/>
 * Currently handles spending, stores and currency statistics, as defined in {@link StatsType}.
 * <p/>
 * Subclass of {@link BaseWorker}.
 */
public class StatsCalcWorker extends BaseWorker<Stats, StatsCalcListener> {

    public static final String WORKER_TAG = "STATS_CALC_WORKER";
    @IntDef({TYPE_SPENDING, TYPE_STORES, TYPE_CURRENCIES})
    @Retention(RetentionPolicy.SOURCE)
    public @interface StatsType {}
    public static final int TYPE_SPENDING = 1;
    public static final int TYPE_STORES = 2;
    public static final int TYPE_CURRENCIES = 3;
    private static final String BUNDLE_STATS_TYPE = "BUNDLE_STATS_TYPE";
    private static final String BUNDLE_YEAR = "BUNDLE_YEAR";
    private static final String BUNDLE_MONTH = "BUNDLE_MONTH";
    private static final String LOG_TAG = StatsCalcWorker.class.getSimpleName();
    @Inject
    ApiRepository mApiRepo;
    @Inject
    User mCurrentUser;
    private int mStatsType;

    public StatsCalcWorker() {
        // empty default constructor
    }

    /**
     * Returns a new instance of {@link StatsCalcWorker}.
     *
     * @param statsType the type of statistic to calculate
     * @param year      the year to calculate statistics for
     * @param month     the month to calculate statistics for (1-12), if 0 statistics for whole year
     *                  will be calculated
     * @return a new instance of {@link StatsCalcWorker}
     */
    @NonNull
    public static StatsCalcWorker newInstance(@StatsType int statsType, @NonNull String year,
                                              int month) {
        StatsCalcWorker fragment = new StatsCalcWorker();
        Bundle args = new Bundle();
        args.putInt(BUNDLE_STATS_TYPE, statsType);
        args.putString(BUNDLE_YEAR, year);
        args.putInt(BUNDLE_MONTH, month);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected String getWorkerTag() {
        return WORKER_TAG;
    }

    @Nullable
    @Override
    protected Observable<Stats> getObservable(@NonNull Bundle args) {
        mStatsType = args.getInt(BUNDLE_STATS_TYPE, 0);
        final String year = args.getString(BUNDLE_YEAR, "");
        final int month = args.getInt(BUNDLE_MONTH, 0);
        final Group currentGroup = mCurrentUser.getCurrentGroup();
        if (!TextUtils.isEmpty(year) && currentGroup != null) {
            final String groupId = currentGroup.getObjectId();
            switch (mStatsType) {
                case TYPE_SPENDING:
                    return mApiRepo.calcStatsSpending(groupId, year, month).toObservable();
                case TYPE_STORES:
                    return mApiRepo.calcStatsStores(groupId, year, month).toObservable();
                case TYPE_CURRENCIES:
                    return mApiRepo.calcStatsCurrencies(groupId, year, month).toObservable();
            }
        }

        return null;
    }

    @Override
    protected void setStream(@NonNull Observable<Stats> observable, @NonNull String workerTag) {
        mActivity.setStatsCalcStream(observable.toSingle(), mStatsType, workerTag);
    }
}
