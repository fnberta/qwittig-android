/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.data.rest;

import android.support.annotation.NonNull;

import com.squareup.okhttp.OkHttpClient;

import java.util.concurrent.TimeUnit;

import ch.giantific.qwittig.domain.models.rates.CurrencyRates;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.client.OkClient;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Provides a client to connect to the REST api of a web service to fetch currency rates
 */
public class ExchangeRatesClient {

    private static final String BASE_URL = "http://api.fixer.io";
    private static final long READ_TIMEOUT = 300000;
    private static final RestAdapter REST_ADAPTER = new RestAdapter.Builder()
            .setEndpoint(BASE_URL)
            .setClient(new OkClient(generateOkHttp()))
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

    @NonNull
    private static OkHttpClient generateOkHttp() {
        OkHttpClient client = new OkHttpClient();
        client.setReadTimeout(READ_TIMEOUT, TimeUnit.MILLISECONDS);
        return client;
    }

    /**
     * Defines the call to the server to fetch the currency rates.
     */
    public interface ExchangeRates {
        /**
         * Makes a GET call to the server to fetch the currency rates
         * @param baseCurrency the base currency to base the rates on
         * @param callback the callback that gets called with server's response
         */
        @GET("/latest")
        void getRates(@Query("base") String baseCurrency, Callback<CurrencyRates> callback);
    }
}
