/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.di;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.inject.Singleton;

import ch.giantific.qwittig.data.rest.ExchangeRates;
import ch.giantific.qwittig.data.rest.Stats;
import ch.giantific.qwittig.data.rest.UrlShortener;
import ch.giantific.qwittig.data.rest.UserDataDeletion;
import dagger.Module;
import dagger.Provides;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Defines how the application wide rest services are instantiated.
 */
@Module
public class RestServiceModule {

    private static final String BASE_URL_QWITTIG = "https://qwittig.com/api2/";
    //    private static final String BASE_URL_QWITTIG = "http://192.168.0.111:4000/api2/";
//    private static final String BASE_URL_QWITTIG = "http://10.0.2.2:4000/api2/";
    private static final String BASE_URL_EXCHANGE_RATES = "http://api.fixer.io/";
    private static final String BASE_URL_URL_SHORTENER = "https://www.googleapis.com/urlshortener/v1/";

    @Provides
    @Singleton
    ExchangeRates providesExchangeRates() {
        final Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL_EXCHANGE_RATES)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();

        return retrofit.create(ExchangeRates.class);
    }

    @Provides
    @Singleton
    UserDataDeletion providesUserDataDeletion() {
        final Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL_QWITTIG)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();

        return retrofit.create(UserDataDeletion.class);
    }

    @Provides
    @Singleton
    Stats providesStats() {
        final Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                .create();

        final Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL_QWITTIG)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();

        return retrofit.create(Stats.class);
    }

    @Provides
    @Singleton
    UrlShortener providesUrlShortener() {
        final Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL_URL_SHORTENER)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();

        return retrofit.create(UrlShortener.class);
    }
}
