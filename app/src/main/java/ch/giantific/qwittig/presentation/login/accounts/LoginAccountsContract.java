/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.login.accounts;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import ch.giantific.qwittig.presentation.common.delegates.GoogleApiClientDelegate;
import ch.giantific.qwittig.presentation.common.presenters.BasePresenter;
import ch.giantific.qwittig.presentation.common.views.BaseView;
import ch.giantific.qwittig.presentation.login.LoginWorkerListener;

/**
 * Defines an observable view model for the login accounts screen.
 */
public interface LoginAccountsContract {

    interface Presenter extends BasePresenter<ViewListener>,
            LoginWorkerListener,
            GoogleApiClientDelegate.GoogleLoginCallback {

        void onFacebookSignedIn(@NonNull String idToken);

        void onFacebookLoginFailed();

        View.OnClickListener getLoginGoogleClickListener();

        void onUseEmailClick(View view);
    }

    interface ViewListener extends BaseView {

        void loadFacebookLoginWorker(@NonNull String idToken);

        void loginWithGoogle();

        void loadGoogleLoginWorker(@Nullable String tokenId);

        void showEmailLogin(@NonNull String identityId);

        void showProfileAdjust(boolean withInvitation);
    }
}
