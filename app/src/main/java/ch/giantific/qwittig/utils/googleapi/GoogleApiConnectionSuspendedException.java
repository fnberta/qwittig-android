/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.utils.googleapi;

/**
 * Created by fabio on 10.02.16.
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
