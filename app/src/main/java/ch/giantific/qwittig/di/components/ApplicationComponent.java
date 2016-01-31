/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.di.components;

import android.app.Application;
import android.content.SharedPreferences;

import javax.inject.Singleton;

import ch.giantific.qwittig.Qwittig;
import ch.giantific.qwittig.data.rest.ExchangeRates;
import ch.giantific.qwittig.data.rest.OcrClient;
import ch.giantific.qwittig.data.rest.ReceiptOcr;
import ch.giantific.qwittig.di.modules.ApplicationModule;
import ch.giantific.qwittig.di.modules.RestServiceModule;
import ch.giantific.qwittig.presentation.workerfragments.OcrWorker;
import ch.giantific.qwittig.presentation.workerfragments.RatesWorker;
import dagger.Component;

/**
 * Created by fabio on 12.01.16.
 */
@Singleton
@Component(modules = {ApplicationModule.class, RestServiceModule.class})
public interface ApplicationComponent {

    Application getApplication();

    SharedPreferences getSharedPreferences();

    ReceiptOcr getReceiptOcr();

    ExchangeRates getExchangeRates();
}
