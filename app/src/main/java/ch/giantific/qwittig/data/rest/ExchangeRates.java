/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.data.rest;

import ch.giantific.qwittig.domain.models.rates.CurrencyRates;
import retrofit.http.GET;
import retrofit.http.Query;
import rx.Observable;

/**
 * Defines the call to the server to fetch the currency rates.
 */
public interface ExchangeRates {
    /**
     * Makes a GET call to the server to fetch the currency rates
     *
     * @param baseCurrency the base currency to base the rates on
     */
    @GET("/latest")
    Observable<CurrencyRates> getRates(@Query("base") String baseCurrency);
}
