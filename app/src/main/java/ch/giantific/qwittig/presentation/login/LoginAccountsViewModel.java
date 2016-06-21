/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.login;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import ch.giantific.qwittig.presentation.common.viewmodels.LoadingViewModel;
import ch.giantific.qwittig.presentation.common.viewmodels.ViewModel;

/**
 * Defines an observable view model for the login accounts screen.
 */
public interface LoginAccountsViewModel extends ViewModel<LoginAccountsViewModel.ViewListener>, LoadingViewModel, LoginWorkerListener {

    void setInvitationIdentityId(@NonNull String identityId);

    void onGoogleSignedIn(@Nullable String tokenId, @Nullable String displayName,
                          @Nullable Uri photoUrl);

    void onGoogleLoginFailed();

    void onLoginFacebookClick(View view);

    View.OnClickListener getLoginGoogleClickListener();

    void onUseEmailClick(View view);

    /**
     * Defines the interaction with the attached view.
     */
    interface ViewListener extends ViewModel.ViewListener {

        void loadFacebookLoginWorker(@NonNull String identityId);

        void loginWithGoogle();

        void loadGoogleTokenVerifyWorker(@Nullable String tokenId, @Nullable String displayName,
                                         @Nullable Uri photoUrl, @NonNull String identityId);

        void showEmailFragment(@NonNull String identityId);

        void showProfileFragment(boolean withInvitation);
    }
}
