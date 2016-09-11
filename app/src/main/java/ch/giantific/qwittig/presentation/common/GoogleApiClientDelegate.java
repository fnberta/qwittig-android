package ch.giantific.qwittig.presentation.common;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;

import com.google.android.gms.appinvite.AppInvite;
import com.google.android.gms.appinvite.AppInviteReferral;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.Api.ApiOptions.NotRequiredOptions;
import com.google.android.gms.common.api.GoogleApiClient;

import timber.log.Timber;

/**
 * Created by fabio on 05.08.16.
 */
public class GoogleApiClientDelegate {

    private static final int INTENT_REQUEST_GOOGLE_SIGN_IN = 9001;
    private static final String GOOGLE_SERVER_ID = "1032365366003-7rsbsnqc5b0504mmdt6j1n61m39lgllp.apps.googleusercontent.com";

    @Nullable
    private final GoogleLoginCallback loginCallback;
    @Nullable
    private final GoogleInvitationCallback invitationCallback;
    private final FragmentActivity activity;
    private GoogleApiClient googleApiClient;

    public GoogleApiClientDelegate(@NonNull FragmentActivity activity,
                                   @Nullable GoogleLoginCallback loginCallback,
                                   @Nullable GoogleInvitationCallback invitationCallback) {
        this.activity = activity;
        this.loginCallback = loginCallback;
        this.invitationCallback = invitationCallback;
    }

    @SafeVarargs
    public final void createGoogleApiClient(@Nullable Api<? extends NotRequiredOptions>... extraApis) {
        final GoogleSignInOptions gso =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(GOOGLE_SERVER_ID)
                        .requestEmail()
                        .build();

        final GoogleApiClient.Builder builder = new GoogleApiClient.Builder(activity)
                .enableAutoManage(activity, connectionResult -> Timber.w("GoogleApiClient onConnectionFailed: %s", connectionResult))
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso);
        if (extraApis != null) {
            for (Api<? extends NotRequiredOptions> api : extraApis) {
                builder.addApi(api);
            }
        }

        googleApiClient = builder.build();
    }

    public void loginWithGoogle() {
        final Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        activity.startActivityForResult(signInIntent, INTENT_REQUEST_GOOGLE_SIGN_IN);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == INTENT_REQUEST_GOOGLE_SIGN_IN) {
            final GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleGoogleSignInResult(result);
        }
    }

    private void handleGoogleSignInResult(@NonNull GoogleSignInResult result) {
        if (loginCallback == null) {
            return;
        }

        if (!result.isSuccess()) {
            loginCallback.onGoogleLoginFailed();
            return;
        }

        final GoogleSignInAccount acct = result.getSignInAccount();
        if (acct == null) {
            loginCallback.onGoogleLoginFailed();
        } else {
            final String idToken = acct.getIdToken();
            if (!TextUtils.isEmpty(idToken)) {
                loginCallback.onGoogleLoginSuccessful(idToken);
            } else {
                loginCallback.onGoogleLoginFailed();
            }
        }
    }

    public void checkForInvitation() {
        AppInvite.AppInviteApi.getInvitation(googleApiClient, activity, false)
                .setResultCallback(result -> {
                    if (result.getStatus().isSuccess()) {
                        final Intent intent = result.getInvitationIntent();
                        final String deepLink = AppInviteReferral.getDeepLink(intent);
                        Timber.d("deepLink %s", deepLink);
                        final Uri uri = Uri.parse(deepLink);
                        if (invitationCallback != null) {
                            invitationCallback.onDeepLinkFound(uri);
                        }
                    } else {
                        Timber.i("getInvitation: no deep link found.");
                    }
                });
    }

    public interface GoogleLoginCallback {
        void onGoogleLoginSuccessful(@NonNull String idToken);

        void onGoogleLoginFailed();
    }

    public interface GoogleInvitationCallback {
        void onDeepLinkFound(@NonNull Uri deepLink);
    }
}
