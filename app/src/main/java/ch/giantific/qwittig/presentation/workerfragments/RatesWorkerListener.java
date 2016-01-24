/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.workerfragments;

import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import java.util.Map;

import ch.giantific.qwittig.domain.models.rates.CurrencyRates;
import rx.Observable;

/**
 * Defines the actions to take after the rates were fetched or after the fetch failed.
 */
public interface RatesWorkerListener extends BaseWorkerListener {
    /**
     * Sets the exchange rate fetch observable.
     *
     * @param observable the observable that fetches the currency exchange rates
     */
    void setRatesFetchStream(@NonNull rx.Observable<CurrencyRates> observable,
                             @NonNull String workerTag);
}
