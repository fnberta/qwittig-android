/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.di;

import android.support.v4.content.LocalBroadcastManager;

import ch.giantific.qwittig.data.bus.LocalBroadcast;
import ch.giantific.qwittig.data.bus.LocalBroadcastImpl;
import dagger.Module;
import dagger.Provides;

/**
 * Defines which implementation to use for the {@link LocalBroadcast} interface and how to
 * instantiate it.
 */
@Module
public class LocalBroadcastModule {

    @Provides
    LocalBroadcast providesLocalBroadcast(LocalBroadcastManager broadcastManager) {
        return new LocalBroadcastImpl(broadcastManager);
    }
}
