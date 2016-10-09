/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.login.email;

import android.support.annotation.NonNull;
import android.view.View;

import ch.giantific.qwittig.presentation.common.presenters.BasePresenter;
import ch.giantific.qwittig.presentation.common.presenters.BaseViewListener;
import ch.giantific.qwittig.presentation.common.workers.EmailUserWorkerListener;
import ch.giantific.qwittig.presentation.login.LoginWorkerListener;

/**
 * Defines an observable view model for the login/sign-up with email screen.
 */
public interface LoginEmailContract {

    interface Presenter extends BasePresenter<ViewListener>,
            LoginWorkerListener,
            EmailUserWorkerListener,
            EmailPromptDialogFragment.DialogInteractionListener {

        void onLoginClick(View view);

        void onSignUpClick(View view);

        void onResetPasswordClick(View view);
    }

    interface ViewListener extends BaseViewListener {

        void loadEmailLoginWorker(@NonNull final String email, @NonNull String password);

        void loadEmailSignUpWorker(@NonNull final String email, @NonNull String password);

        void loadResetPasswordWorker(@NonNull String email);

        void showResetPasswordDialog(@NonNull String email);

        void hideKeyboard();

        void showProfileAdjust(boolean accepted);
    }
}
