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
 * Provides a reactive GoogleApiClient for signing out users from their google account for the app.
 */
public class GoogleApiClientSignOut extends BaseGoogleApiClientSingle<Void> {

    private GoogleApiClientSignOut(@NonNull Context context) {
        super(context);
    }

    @NonNull
    public static Single<Void> create(@NonNull Context context) {
        return Single.create(new GoogleApiClientSignOut(context));
    }

    @Override
    protected void onGoogleApiClientReady(@NonNull GoogleApiClient apiClient,
                                          @NonNull final SingleSubscriber<? super Void> subscriber) {
        Auth.GoogleSignInApi.signOut(apiClient)
                .setResultCallback(status -> {
                    if (status.isSuccess()) {
                        subscriber.onSuccess(null);
                    } else {
                        subscriber.onError(new GoogleAuthStatusException(status));
                    }
                });
    }
}
