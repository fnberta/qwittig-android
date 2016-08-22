package ch.giantific.qwittig.data.repositories;

import android.support.annotation.NonNull;

import java.util.Date;

import javax.inject.Inject;

import ch.giantific.qwittig.data.rest.Stats;
import ch.giantific.qwittig.data.rest.StatsRequest;
import ch.giantific.qwittig.data.rest.StatsResult;
import rx.Single;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by fabio on 19.07.16.
 */
public class StatsRepository {

    private final Stats stats;

    @Inject
    public StatsRepository(@NonNull Stats stats) {
        this.stats = stats;
    }

    public Single<StatsResult> calculateSpendingStats(@NonNull String idToken,
                                                      @NonNull Date startDate,
                                                      @NonNull Date endDate) {
        return stats.calculateStats(new StatsRequest(idToken, startDate, endDate))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }
}
