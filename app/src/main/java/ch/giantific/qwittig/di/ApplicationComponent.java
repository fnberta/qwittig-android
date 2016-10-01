/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.di;

import android.app.Application;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationManagerCompat;

import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.storage.FirebaseStorage;

import javax.inject.Singleton;

import ch.giantific.qwittig.Qwittig;
import ch.giantific.qwittig.data.rest.DeleteUserData;
import ch.giantific.qwittig.data.rest.ExchangeRates;
import ch.giantific.qwittig.data.rest.Stats;
import dagger.Component;

/**
 * Provides application wide dependencies as singletons.
 *
 * @see {@link Qwittig}
 */
@Singleton
@Component(modules = {ApplicationModule.class, FirebaseModule.class, RestServiceModule.class})
public interface ApplicationComponent {

    Application getApplication();

    SharedPreferences getSharedPreferences();

    NotificationManagerCompat getNotificationManagerCompat();

    ExchangeRates getExchangeRates();

    DeleteUserData getDeleteUserData();

    Stats providesStats();

    FirebaseAuth getFirebaseAuth();

    FirebaseDatabase getFirebaseDatabase();

    FirebaseStorage getFirebaseStorage();

    FirebaseMessaging getFirebaseMessaging();

    FirebaseRemoteConfig getFirebaseRemoteConfig();

    FirebaseJobDispatcher providesJobDispatcher();
}
