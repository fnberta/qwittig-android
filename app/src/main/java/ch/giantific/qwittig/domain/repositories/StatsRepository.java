/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.domain.repositories;

import android.support.annotation.NonNull;

import ch.giantific.qwittig.domain.models.stats.Stats;
import rx.Single;

/**
 * Provides the methods to calculate stats.
 */
public interface StatsRepository extends Repository {

    /**
     * Calculates the stats that show how much each users of the group spent in a month/year.
     *
     * @param groupId the object id of the group to calculate stats for
     * @param year    the year for which to calculate stats for
     * @param month   the month for which to calculate stats for, 0 means the whole year
     */
    Single<Stats> calcStatsSpending(@NonNull String groupId, @NonNull String year, int month);

    /**
     * Calculates the stats that shows the percentages of stores used in purchases.
     *
     * @param groupId the object id of the group to calculate stats for
     * @param year    the year for which to calculate stats for
     * @param month   the month for which to calculate stats for, 0 means the whole year
     */
    Single<Stats> calcStatsStores(@NonNull String groupId, @NonNull String year, int month);

    /**
     * Calculates the stats that the percentages of currencies used in purchases.
     *
     * @param groupId the object id of the group to calculate stats for
     * @param year    the year for which to calculate stats for
     * @param month   the month for which to calculate stats for, 0 means the whole year
     */
    Single<Stats> calcStatsCurrencies(@NonNull String groupId, @NonNull String year, int month);
}
