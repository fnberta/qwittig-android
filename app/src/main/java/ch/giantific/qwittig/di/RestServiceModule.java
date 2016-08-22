/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.di;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.inject.Singleton;

import ch.giantific.qwittig.data.rest.DeleteUserData;
import ch.giantific.qwittig.data.rest.ExchangeRates;
import ch.giantific.qwittig.data.rest.Stats;
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

    @Provides
    @Singleton
    ExchangeRates providesExchangeRateService() {
        final Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL_EXCHANGE_RATES)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();

        return retrofit.create(ExchangeRates.class);
    }

    @Provides
    @Singleton
    DeleteUserData providesDeleteUserDataService() {
        final Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL_QWITTIG)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();

        return retrofit.create(DeleteUserData.class);
    }

    @Provides
    @Singleton
    Stats providesStatsService() {
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
}
