/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.di;

import android.content.Context;
import android.support.annotation.NonNull;

import ch.giantific.qwittig.LocalBroadcast;
import ch.giantific.qwittig.LocalBroadcastImpl;
import dagger.Module;
import dagger.Provides;

/**
 * Created by fabio on 12.01.16.
 */
@Module
public class LocalBroadcastModule {

    private final Context mContext;

    public LocalBroadcastModule(@NonNull Context context) {
        mContext = context;
    }

    @Provides
    LocalBroadcast providesLocalBroadcast() {
        return new LocalBroadcastImpl(mContext);
    }
}
