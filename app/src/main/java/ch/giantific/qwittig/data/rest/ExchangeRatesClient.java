/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.data.rest;

import retrofit.GsonConverterFactory;
import retrofit.Retrofit;
import retrofit.RxJavaCallAdapterFactory;

/**
 * Provides a client to connect to the REST api of a web service to fetch currency rates
 */
public class ExchangeRatesClient {

    private static final String BASE_URL = "http://api.fixer.io";
    private static final Retrofit REST_ADAPTER = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
            .build();
    private static final ExchangeRates EXCHANGE_RATES_SERVICE = REST_ADAPTER.create(ExchangeRates.class);

    private ExchangeRatesClient() {
        // class cannot be instantiated
    }

    /**
     * Returns the static singleton reference to the {@link ExchangeRates} instance.
     *
     * @return the static singleton instance of the {@link ExchangeRates}
     */
    public static ExchangeRates getService() {
        return EXCHANGE_RATES_SERVICE;
    }

}
