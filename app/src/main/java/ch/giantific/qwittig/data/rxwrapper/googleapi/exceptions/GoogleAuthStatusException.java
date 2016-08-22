/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.data.rxwrapper.googleapi.exceptions;

import android.support.annotation.NonNull;

import com.google.android.gms.common.api.Status;

/**
 * Defines an exception thrown when the authentication with Google's servers failed.
 */
public class GoogleAuthStatusException extends RuntimeException {

    private final Status status;

    public GoogleAuthStatusException(@NonNull Status status) {
        this.status = status;
    }

    public Status getStatus() {
        return status;
    }
}
