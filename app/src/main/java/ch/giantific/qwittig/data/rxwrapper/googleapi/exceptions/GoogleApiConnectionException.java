/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.data.rxwrapper.googleapi.exceptions;

import android.support.annotation.NonNull;

import com.google.android.gms.common.ConnectionResult;

/**
 * Defines an exception thrown when the connection to Google's servers failed.
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
