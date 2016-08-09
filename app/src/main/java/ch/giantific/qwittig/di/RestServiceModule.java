/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.di;

import javax.inject.Singleton;

import ch.giantific.qwittig.data.rest.DeleteUserData;
import ch.giantific.qwittig.data.rest.ExchangeRates;
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

    //    private static final String BASE_URL_QWITTIG = "https://qwittig.com/api/";
    private static final String BASE_URL_QWITTIG = "http://192.168.0.111:3000/api/";
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
}
