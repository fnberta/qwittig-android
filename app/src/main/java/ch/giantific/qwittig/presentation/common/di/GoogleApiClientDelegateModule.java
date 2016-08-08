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

    private final FragmentActivity mActivity;
    private final GoogleLoginCallback mLoginCallback;
    private final GoogleInvitationCallback mInvitationCallback;

    public GoogleApiClientDelegateModule(@NonNull FragmentActivity activity,
                                         @NonNull GoogleLoginCallback loginCallback,
                                         @Nullable GoogleInvitationCallback invitationCallback) {
        mActivity = activity;
        mLoginCallback = loginCallback;
        mInvitationCallback = invitationCallback;
    }

    @PerActivity
    @Provides
    GoogleApiClientDelegate providesGoogleApiClientDelegate() {
        return new GoogleApiClientDelegate(mActivity, mLoginCallback, mInvitationCallback);
    }
}
