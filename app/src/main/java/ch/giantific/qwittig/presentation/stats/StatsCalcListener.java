/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.stats;

import android.support.annotation.NonNull;

import ch.giantific.qwittig.presentation.common.workers.BaseWorkerListener;
import ch.giantific.qwittig.presentation.stats.models.Stats;
import rx.Single;

/**
 * Defines the actions to take after statistics were calculated or after the calculation failed.
 */
public interface StatsCalcListener extends BaseWorkerListener {
    /**
     * Sets the {@link Single} that emits the stats calculation.
     *
     * @param single the single emitting the stats calculation
     * @param workerTag  the tag of the worker fragment
     */
    void setStatsCalcStream(@NonNull Single<Stats> single, int statsType,
                            @NonNull String workerTag);
}
