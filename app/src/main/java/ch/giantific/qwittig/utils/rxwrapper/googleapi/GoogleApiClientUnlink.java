/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.utils.rxwrapper.googleapi;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.api.GoogleApiClient;

import ch.giantific.qwittig.utils.rxwrapper.googleapi.exceptions.GoogleAuthStatusException;
import rx.Single;
import rx.SingleSubscriber;

/**
 * Provides a reactive GoogleApiClient for un-linking a user's Google account from the app.
 */
public class GoogleApiClientUnlink extends BaseGoogleApiClientSingle<Void> {

    private GoogleApiClientUnlink(@NonNull Context context) {
        super(context);
    }

    @NonNull
    public static Single<Void> create(@NonNull Context context) {
        return Single.create(new GoogleApiClientUnlink(context));
    }

    @Override
    protected void onGoogleApiClientReady(@NonNull GoogleApiClient apiClient,
                                          @NonNull final SingleSubscriber<? super Void> subscriber) {
        Auth.GoogleSignInApi.revokeAccess(apiClient)
                .setResultCallback(status -> {
                    if (status.isSuccess()) {
                        subscriber.onSuccess(null);
                    } else {
                        subscriber.onError(new GoogleAuthStatusException(status));
                    }
                });
    }
}
