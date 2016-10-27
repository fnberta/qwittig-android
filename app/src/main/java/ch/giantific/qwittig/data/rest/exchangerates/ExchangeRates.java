/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.data.rest.exchangerates;

import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Single;

/**
 * Defines the call to the server to fetch the currency rates.
 */
public interface ExchangeRates {
    /**
     * Makes a GET call to the server to fetch the currency rates.
     *
     * @param baseCurrency the base currency to base the rates on
     * @return the result as an {@link Single}
     */
    @GET("latest")
    Single<ExchangeRatesResult> getRates(@Query("base") String baseCurrency);
}
