/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig;

import android.support.annotation.NonNull;

import com.google.android.gms.common.ConnectionResult;

/**
 * Created by fabio on 10.02.16.
 */
public class GoogleApiConnectionException extends RuntimeException {

    private final ConnectionResult mConnectionResult;

    public GoogleApiConnectionException(@NonNull String detailMessage,
                                        @NonNull ConnectionResult connectionResult) {
        super(detailMessage);

        mConnectionResult = connectionResult;
    }

    public ConnectionResult getConnectionResult() {
        return mConnectionResult;
    }
}
