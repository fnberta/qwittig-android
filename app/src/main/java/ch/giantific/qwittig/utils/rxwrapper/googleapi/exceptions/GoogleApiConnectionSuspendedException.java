/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.utils.rxwrapper.googleapi.exceptions;

/**
 * Defines an exception thrown when the connection to Google's servers was suspended.
 */
public class GoogleApiConnectionSuspendedException extends RuntimeException {

    private final int cause;

    public GoogleApiConnectionSuspendedException(int cause) {
        this.cause = cause;
    }

    public int getErrorCause() {
        return cause;
    }
}
