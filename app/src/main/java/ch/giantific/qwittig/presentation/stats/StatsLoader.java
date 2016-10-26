/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.stats;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Date;

import ch.giantific.qwittig.data.repositories.StatsRepository;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.data.rest.StatsResult;
import ch.giantific.qwittig.presentation.common.BaseRxLoader;
import rx.Observable;

/**
 * Handles the loading of statistical data and presents them as an {@link Observable}.
 * <p/>
 * Subclass of {@link BaseRxLoader}.
 */
public class StatsLoader extends BaseRxLoader<StatsResult> {

    private final UserRepository userRepo;
    private final StatsRepository statsRepo;
    private Date startDate;
    private Date endDAte;

    public StatsLoader(@NonNull Context context,
                       @NonNull UserRepository userRepo,
                       @NonNull StatsRepository statsRepo) {
        super(context);

        this.userRepo = userRepo;
        this.statsRepo = statsRepo;
    }

    public void setStartDate(@NonNull Date startDate) {
        this.startDate = startDate;
    }

    public void setEndDAte(@NonNull Date endDAte) {
        this.endDAte = endDAte;
    }

    @NonNull
    @Override
    protected Observable<StatsResult> getObservable() {
        return userRepo.getAuthToken()
                .flatMap(idToken -> statsRepo.calculateSpendingStats(idToken, startDate, endDAte))
                .toObservable();
    }
}
