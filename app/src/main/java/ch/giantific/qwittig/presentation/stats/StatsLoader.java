/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.stats;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import ch.giantific.qwittig.domain.models.Group;
import ch.giantific.qwittig.domain.models.User;
import ch.giantific.qwittig.domain.repositories.StatsRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.common.BaseRxLoader;
import ch.giantific.qwittig.presentation.stats.StatsViewModel.StatsType;
import ch.giantific.qwittig.presentation.stats.models.Stats;
import rx.Observable;

/**
 * Created by fabio on 13.03.16.
 */
public class StatsLoader extends BaseRxLoader<Stats> {

    private UserRepository mUserRepo;
    private StatsRepository mStatsRepo;
    private int mStatsType;
    private String mYear;
    private int mMonthNumber;

    public StatsLoader(@NonNull Context context, @NonNull UserRepository userRepo,
                       @NonNull StatsRepository statsRepo, @StatsType int statsType,
                       @NonNull String year, int monthNumber) {
        super(context);

        mUserRepo = userRepo;
        mStatsRepo = statsRepo;
        mStatsType = statsType;
        mYear = year;
        mMonthNumber = monthNumber;
    }

    @Nullable
    @Override
    protected Observable<Stats> getObservable() {
        final User currentUser = mUserRepo.getCurrentUser();
        if (TextUtils.isEmpty(mYear) || currentUser == null) {
            return null;
        }

        final Group currentGroup = currentUser.getCurrentIdentity().getGroup();
        final String groupId = currentGroup.getObjectId();
        switch (mStatsType) {
            case StatsType.SPENDING:
                return mStatsRepo.calcStatsSpending(groupId, mYear, mMonthNumber).toObservable();
            case StatsType.STORES:
                return mStatsRepo.calcStatsStores(groupId, mYear, mMonthNumber).toObservable();
            case StatsType.CURRENCIES:
                return mStatsRepo.calcStatsCurrencies(groupId, mYear, mMonthNumber).toObservable();
        }

        return null;
    }
}
