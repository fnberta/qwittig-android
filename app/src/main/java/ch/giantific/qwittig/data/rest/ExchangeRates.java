/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.data.rest;

import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Single;

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
    Single<CurrencyRates> getRates(@Query("base") String baseCurrency);
}
