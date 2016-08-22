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

    private final Context context;

    public SystemServiceModule(@NonNull Context context) {
        this.context = context;
    }

    @Provides
    NotificationManager providesNotificationManager() {
        return (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }
}