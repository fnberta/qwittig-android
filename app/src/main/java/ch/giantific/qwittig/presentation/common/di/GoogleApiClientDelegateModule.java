package ch.giantific.qwittig.presentation.common.di;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;

import ch.giantific.qwittig.di.scopes.PerActivity;
import ch.giantific.qwittig.presentation.common.GoogleApiClientDelegate;
import ch.giantific.qwittig.presentation.common.GoogleApiClientDelegate.GoogleInvitationCallback;
import ch.giantific.qwittig.presentation.common.GoogleApiClientDelegate.GoogleLoginCallback;
import dagger.Module;
import dagger.Provides;

/**
 * Created by fabio on 17.06.16.
 */
@Module
public class GoogleApiClientDelegateModule {

    private final FragmentActivity activity;
    private final GoogleLoginCallback loginCallback;
    private final GoogleInvitationCallback invitationCallback;

    public GoogleApiClientDelegateModule(@NonNull FragmentActivity activity,
                                         @Nullable GoogleLoginCallback loginCallback,
                                         @Nullable GoogleInvitationCallback invitationCallback) {
        this.activity = activity;
        this.loginCallback = loginCallback;
        this.invitationCallback = invitationCallback;
    }

    @PerActivity
    @Provides
    GoogleApiClientDelegate providesGoogleApiClientDelegate() {
        return new GoogleApiClientDelegate(activity, loginCallback, invitationCallback);
    }
}
