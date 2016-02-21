/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.utils.googleapi;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import rx.Single;
import rx.SingleSubscriber;

/**
 * Created by fabio on 10.02.16.
 */
public class GoogleApiClientUnlink extends BaseGoogleApiClientSingle<Void> {

    private GoogleApiClientUnlink(@NonNull Context context) {
        super(context);
    }

    public static Single<Void> create(@NonNull Context context) {
        return Single.create(new GoogleApiClientUnlink(context));
    }

    @Override
    protected void onGoogleApiClientReady(@NonNull GoogleApiClient apiClient,
                                          @NonNull final SingleSubscriber<? super Void> subscriber) {
        Auth.GoogleSignInApi.revokeAccess(apiClient)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        if (status.isSuccess()) {
                            subscriber.onSuccess(null);
                        } else {
                            subscriber.onError(new GoogleAuthStatusException(status));
                        }
                    }
                });
    }
}
