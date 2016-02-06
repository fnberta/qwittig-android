/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.common.workers;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

/**
 * Handles the un-linking of the user's account from this Facebook or Google profile.
 */
public abstract class BaseGoogleApiLoginWorker extends Fragment implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    protected GoogleApiClient mGoogleApiClient;

    public BaseGoogleApiLoginWorker() {
        // empty default constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
    }

    protected final void setupGoogleApiClient() {
        final GoogleSignInOptions gso =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .build();

        mGoogleApiClient = new GoogleApiClient.Builder(getActivity().getApplicationContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        onGoogleClientConnected();
    }

    protected abstract void onGoogleClientConnected();

    @Override
    public void onConnectionSuspended(int i) {
        // do nothing
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        onGoogleClientConnectionFailed();
    }

    protected abstract void onGoogleClientConnectionFailed();

    protected final void unlinkGoogle() {
        Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                if (status.isSuccess()) {
                    onGoogleUnlinkSuccessful();
                } else {
                    onGoogleUnlinkFailed();
                }
            }
        });
    }

    protected abstract void onGoogleUnlinkSuccessful();

    protected abstract void onGoogleUnlinkFailed();

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
    }
}
