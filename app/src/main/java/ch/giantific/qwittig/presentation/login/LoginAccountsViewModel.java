/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.login;

import android.net.Uri;
import android.support.annotation.Nullable;
import android.view.View;

import ch.giantific.qwittig.presentation.common.viewmodels.LoadingViewModel;
import ch.giantific.qwittig.presentation.common.viewmodels.ViewModel;

/**
 * Created by fabio on 05.02.16.
 */
public interface LoginAccountsViewModel extends ViewModel, LoadingViewModel, LoginWorkerListener {

    void onGoogleSignedIn(@Nullable String tokenId, @Nullable String displayName,
                          @Nullable Uri photoUrl);

    void onGoogleLoginFailed();

    void onLoginFacebookClick(View view);

    void onLoginGoogleClick(View view);

    void onUseEmailClick(View view);

    interface ViewListener extends ViewModel.ViewListener {

        void loadFacebookLoginWorker();

        void loginWithGoogle();

        void loadGoogleTokenVerifyWorker(@Nullable String tokenId, @Nullable String displayName,
                                         @Nullable Uri photoUrl);

        void showEmailFragment();

        void finishScreen(int result);
    }
}
