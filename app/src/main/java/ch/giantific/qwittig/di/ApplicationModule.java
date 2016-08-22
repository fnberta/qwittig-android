/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.di;

import android.app.Application;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.preference.PreferenceManager;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Defines how the application wide dependencies are instantiated.
 */
@Module
public class ApplicationModule {

    private final Application application;

    public ApplicationModule(@NonNull Application application) {
        this.application = application;
    }

    @Provides
    @Singleton
    Application providesApplication() {
        return application;
    }

    @Provides
    @Singleton
    SharedPreferences providesSharedPreferences(Application application) {
        return PreferenceManager.getDefaultSharedPreferences(application);
    }

    @Provides
    @Singleton
    LocalBroadcastManager providesLocalBroadcastManager(Application application) {
        return LocalBroadcastManager.getInstance(application);
    }
}
