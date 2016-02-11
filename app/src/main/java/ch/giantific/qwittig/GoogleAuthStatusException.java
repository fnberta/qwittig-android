/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig;

import android.support.annotation.NonNull;

import com.google.android.gms.common.api.Status;

/**
 * Created by fabio on 10.02.16.
 */
public class GoogleAuthStatusException extends RuntimeException {

    private final Status mStatus;

    public GoogleAuthStatusException(@NonNull Status status) {
        mStatus = status;
    }

    public Status getStatus() {
        return mStatus;
    }
}
