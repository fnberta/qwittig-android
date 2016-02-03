/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.workerfragments;

import android.support.annotation.NonNull;

import rx.Single;

/**
 * Defines the actions to take after the rates were fetched or after the fetch failed.
 */
public interface RatesWorkerListener extends BaseWorkerListener {
    /**
     * Sets the exchange rate fetch observable.
     *
     * @param single the observable that fetches the currency exchange rates
     */
    void setRateFetchStream(@NonNull Single<Float> single,
                            @NonNull String workerTag);
}
