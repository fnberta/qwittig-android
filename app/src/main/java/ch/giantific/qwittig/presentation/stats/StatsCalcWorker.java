/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.stats;

import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Inject;

import ch.giantific.qwittig.di.components.WorkerComponent;
import ch.giantific.qwittig.domain.models.Group;
import ch.giantific.qwittig.domain.models.User;
import ch.giantific.qwittig.domain.repositories.StatsRepository;
import ch.giantific.qwittig.presentation.common.workers.BaseWorker;
import ch.giantific.qwittig.presentation.stats.models.Stats;
import rx.Observable;


/**
 * Calculates different statistics by calling Parse.com cloud functions.
 * <p/>
 * Currently handles spending, stores and currency statistics, as defined in {@link StatsType}.
 * <p/>
 * Subclass of {@link BaseWorker}.
 */
public class StatsCalcWorker extends BaseWorker<Stats, StatsCalcListener> {

    private static final String WORKER_TAG = StatsCalcWorker.class.getCanonicalName();
    private static final String KEY_STATS_TYPE = "STATS_TYPE";
    private static final String KEY_YEAR = "YEAR";
    private static final String KEY_MONTH = "MONTH";
    @Inject
    StatsRepository mStatsRepo;
    private int mStatsType;

    public StatsCalcWorker() {
        // empty default constructor
    }

    /**
     * Attaches a new instance of {@link StatsCalcWorker}.
     *
     * @param fm        the fragment manger to use for the transaction
     * @param statsType the type of statistic to calculate
     * @param year      the year to calculate statistics for
     * @param month     the month to calculate statistics for (1-12), if 0 statistics for whole year
     *                  will be calculated
     * @return a new instance of {@link StatsCalcWorker}
     */
    public static StatsCalcWorker attach(@NonNull FragmentManager fm, @StatsType int statsType,
                                         @NonNull String year, int month) {
        StatsCalcWorker worker = (StatsCalcWorker) fm.findFragmentByTag(WORKER_TAG);
        if (worker == null) {
            worker = StatsCalcWorker.newInstance(statsType, year, month);
            fm.beginTransaction()
                    .add(worker, WORKER_TAG)
                    .commit();
        }

        return worker;
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
        args.putInt(KEY_STATS_TYPE, statsType);
        args.putString(KEY_YEAR, year);
        args.putInt(KEY_MONTH, month);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void injectWorkerDependencies(@NonNull WorkerComponent component) {
        component.inject(this);
    }

    @Override
    protected void onError() {
        mActivity.onWorkerError(WORKER_TAG);
    }

    @Nullable
    @Override
    protected Observable<Stats> getObservable(@NonNull Bundle args) {
        mStatsType = args.getInt(KEY_STATS_TYPE, 0);
        final String year = args.getString(KEY_YEAR, "");
        final int month = args.getInt(KEY_MONTH, 0);
        final User currentUser = mUserRepo.getCurrentUser();
        if (TextUtils.isEmpty(year) || currentUser == null) {
            return null;
        }

        final Group currentGroup = currentUser.getCurrentIdentity().getGroup();
        final String groupId = currentGroup.getObjectId();
        switch (mStatsType) {
            case StatsType.SPENDING:
                return mStatsRepo.calcStatsSpending(groupId, year, month).toObservable();
            case StatsType.STORES:
                return mStatsRepo.calcStatsStores(groupId, year, month).toObservable();
            case StatsType.CURRENCIES:
                return mStatsRepo.calcStatsCurrencies(groupId, year, month).toObservable();
        }

        return null;
    }

    @Override
    protected void setStream(@NonNull Observable<Stats> observable) {
        mActivity.setStatsCalcStream(observable.toSingle(), mStatsType, WORKER_TAG);
    }

    @IntDef({StatsType.SPENDING, StatsType.STORES, StatsType.CURRENCIES})
    @Retention(RetentionPolicy.SOURCE)
    public @interface StatsType {
        int SPENDING = 1;
        int STORES = 2;
        int CURRENCIES = 3;
    }
}
