/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.utils.googleapi;

/**
 * Defines an exception thrown when the connection to Google's servers was suspended.
 */
public class GoogleApiConnectionSuspendedException extends RuntimeException {

    private final int mCause;

    public GoogleApiConnectionSuspendedException(int cause) {
        mCause = cause;
    }

    public int getErrorCause() {
        return mCause;
    }
}
