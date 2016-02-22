/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.di;

import android.app.NotificationManager;
import android.content.Context;
import android.support.annotation.NonNull;

import dagger.Module;
import dagger.Provides;

/**
 * Defines the instantiation of Android system services.
 */
@Module
public class SystemServiceModule {

    private final Context mContext;

    public SystemServiceModule(@NonNull Context context) {
        mContext = context;
    }

    @Provides
    NotificationManager providesNotificationManager() {
        return (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
    }
}