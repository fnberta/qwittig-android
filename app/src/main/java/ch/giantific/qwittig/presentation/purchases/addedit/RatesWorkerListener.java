/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.addedit;

import android.support.annotation.NonNull;

import ch.giantific.qwittig.presentation.common.workers.BaseWorkerListener;
import rx.Single;

/**
 * Defines the actions to take after the rates were fetched or after the fetch failed.
 */
public interface RatesWorkerListener extends BaseWorkerListener {
    /**
     * Sets the exchange rate fetch result stream.
     *
     * @param single    the {@link Single} that emits the results
     * @param workerTag the tag of the worker fragment
     */
    void setRateFetchStream(@NonNull Single<Float> single,
                            @NonNull String workerTag);
}
