/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.data.rxwrapper.googleapi;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import ch.giantific.qwittig.data.rxwrapper.googleapi.exceptions.GoogleApiConnectionException;
import ch.giantific.qwittig.data.rxwrapper.googleapi.exceptions.GoogleApiConnectionSuspendedException;
import rx.Single;
import rx.SingleSubscriber;
import rx.functions.Action0;
import rx.subscriptions.Subscriptions;

/**
 * Provides an abstract base class for a reactive GoogleApiClient emitting an {@link Single}.
 */
public abstract class BaseGoogleApiClientSingle<T> implements Single.OnSubscribe<T> {

    private final Context context;

    public BaseGoogleApiClientSingle(@NonNull Context context) {
        this.context = context;
    }

    @Override
    public void call(SingleSubscriber<? super T> singleSubscriber) {
        final GoogleApiClient apiClient = createApiClient(singleSubscriber);
        try {
            apiClient.connect();
        } catch (Throwable e) {
            singleSubscriber.onError(e);
        }

        singleSubscriber.add(Subscriptions.create(new Action0() {
                    @Override
                    public void call() {
                        if (apiClient.isConnected() || apiClient.isConnecting()) {
                            apiClient.disconnect();
                        }
                    }
                })
        );
    }

    private GoogleApiClient createApiClient(@NonNull SingleSubscriber<? super T> subscriber) {
        final GoogleApiClient.Builder apiClientBuilder = new GoogleApiClient.Builder(context);
        final GoogleSignInOptions gso =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .build();
        apiClientBuilder.addApi(Auth.GOOGLE_SIGN_IN_API, gso);

        final ApiClientCallbacks apiClientCallbacks = new ApiClientCallbacks(subscriber);
        apiClientBuilder.addConnectionCallbacks(apiClientCallbacks);
        apiClientBuilder.addOnConnectionFailedListener(apiClientCallbacks);

        final GoogleApiClient apiClient = apiClientBuilder.build();
        apiClientCallbacks.setGoogleApiClient(apiClient);

        return apiClient;
    }

    protected abstract void onGoogleApiClientReady(@NonNull GoogleApiClient apiClient,
                                                   @NonNull SingleSubscriber<? super T> subscriber);

    private class ApiClientCallbacks implements GoogleApiClient.ConnectionCallbacks,
            GoogleApiClient.OnConnectionFailedListener {

        private final SingleSubscriber<? super T> mSubscriber;
        private GoogleApiClient mGoogleApiClient;

        public ApiClientCallbacks(@NonNull SingleSubscriber<? super T> subscriber) {
            mSubscriber = subscriber;
        }

        public void setGoogleApiClient(GoogleApiClient googleApiClient) {
            mGoogleApiClient = googleApiClient;
        }

        @Override
        public void onConnected(@Nullable Bundle bundle) {
            onGoogleApiClientReady(mGoogleApiClient, mSubscriber);
        }

        @Override
        public void onConnectionSuspended(int i) {
            mSubscriber.onError(new GoogleApiConnectionSuspendedException(i));
        }

        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
            mSubscriber.onError(new GoogleApiConnectionException("Error connecting to GoogleApiClient.", connectionResult));
        }
    }
}
