/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.di;

import android.app.Application;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;

import javax.inject.Singleton;

import ch.giantific.qwittig.Qwittig;
import ch.giantific.qwittig.data.rest.ExchangeRates;
import ch.giantific.qwittig.data.rest.ReceiptOcr;
import dagger.Component;

/**
 * Provides application wide dependencies as singletons.
 *
 * @see {@link Qwittig}
 */
@Singleton
@Component(modules = {ApplicationModule.class, RestServiceModule.class})
public interface ApplicationComponent {

    Application getApplication();

    SharedPreferences getSharedPreferences();

    LocalBroadcastManager getLocalBroadcastManager();

    ReceiptOcr getReceiptOcr();

    ExchangeRates getExchangeRates();
}
