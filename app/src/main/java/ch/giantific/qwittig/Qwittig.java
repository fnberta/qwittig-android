/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig;

import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.multidex.MultiDex;

import com.facebook.FacebookSdk;

import ch.giantific.qwittig.di.ApplicationComponent;
import ch.giantific.qwittig.di.ApplicationModule;
import ch.giantific.qwittig.di.DaggerApplicationComponent;
import ch.giantific.qwittig.di.RestServiceModule;
import timber.log.Timber;

/**
 * Handles application wide initialisations.
 * <p/>
 * Subclass of {@link android.app.Application}.
 */
public class Qwittig extends Application {

    private ApplicationComponent appComponent;

    public static ApplicationComponent getAppComponent(@NonNull Context context) {
        return ((Qwittig) context.getApplicationContext()).getAppComponent();
    }

    private ApplicationComponent getAppComponent() {
        return appComponent;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
//            LeakCanary.install(this);
        }

        buildAppComponent();
        FacebookSdk.sdkInitialize(this);
    }

    private void buildAppComponent() {
        appComponent = DaggerApplicationComponent.builder()
                .applicationModule(new ApplicationModule(this))
                .restServiceModule(new RestServiceModule())
                .build();
    }
}