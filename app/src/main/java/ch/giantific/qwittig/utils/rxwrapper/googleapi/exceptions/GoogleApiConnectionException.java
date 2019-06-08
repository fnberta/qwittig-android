/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.utils.rxwrapper.googleapi.exceptions;

import android.support.annotation.NonNull;

import com.google.android.gms.common.ConnectionResult;

/**
 * Defines an exception thrown when the connection to Google's servers failed.
 */
public class GoogleApiConnectionException extends RuntimeException {

    private final ConnectionResult connectionResult;

    public GoogleApiConnectionException(@NonNull String detailMessage,
                                        @NonNull ConnectionResult connectionResult) {
        super(detailMessage);

        this.connectionResult = connectionResult;
    }

    public ConnectionResult getConnectionResult() {
        return connectionResult;
    }
}
