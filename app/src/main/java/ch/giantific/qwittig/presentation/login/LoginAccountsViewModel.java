/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.login;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import ch.giantific.qwittig.presentation.common.GoogleApiClientDelegate;
import ch.giantific.qwittig.presentation.common.viewmodels.LoadingViewModel;
import ch.giantific.qwittig.presentation.common.viewmodels.ViewModel;

/**
 * Defines an observable view model for the login accounts screen.
 */
public interface LoginAccountsViewModel extends ViewModel<LoginAccountsViewModel.ViewListener>,
        LoadingViewModel, LoginWorkerListener, GoogleApiClientDelegate.GoogleLoginCallback {

    void setInvitationIdentityId(@NonNull String identityId);

    void onFacebookSignedIn(@NonNull String idToken);

    void onFacebookLoginFailed();

    View.OnClickListener getLoginGoogleClickListener();

    void onUseEmailClick(View view);

    /**
     * Defines the interaction with the attached view.
     */
    interface ViewListener extends ViewModel.ViewListener {

        void loadFacebookLoginWorker(@NonNull String idToken);

        void loginWithGoogle();

        void loadGoogleLoginWorker(@Nullable String tokenId);

        void showEmailFragment(@NonNull String identityId);

        void showProfileFragment(boolean withInvitation);
    }
}
