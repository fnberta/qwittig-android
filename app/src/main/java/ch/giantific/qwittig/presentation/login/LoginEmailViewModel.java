/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.login;

import android.databinding.Bindable;
import android.support.annotation.NonNull;
import android.view.View;

import ch.giantific.qwittig.presentation.common.viewmodels.LoadingViewModel;
import ch.giantific.qwittig.presentation.common.viewmodels.ViewModel;
import ch.giantific.qwittig.presentation.common.workers.EmailUserWorkerListener;

/**
 * Defines an observable view model for the login/sign-up with email screen.
 */
public interface LoginEmailViewModel extends ViewModel<LoginEmailViewModel.ViewListener>, LoadingViewModel,
        LoginWorkerListener, EmailUserWorkerListener, EmailPromptDialogFragment.DialogInteractionListener {

    void setIdentityId(@NonNull String identityId);

    @Bindable
    boolean isSignUp();

    void setSignUp(boolean signUp);

    @Bindable
    boolean isValidate();

    void setValidate(boolean validate);

    @Bindable
    boolean isEmailComplete();

    @Bindable
    boolean isPasswordComplete();

    @Bindable
    boolean isPasswordsMatch();

    void onEmailChanged(CharSequence s, int start, int before, int count);

    void onPasswordChanged(CharSequence s, int start, int before, int count);

    void onPasswordRepeatChanged(CharSequence s, int start, int before, int count);

    void onLoginClick(View view);

    void onSignUpClick(View view);

    void onResetPasswordClick(View view);

    /**
     * Defines the interaction with the attached view.
     */
    interface ViewListener extends ViewModel.ViewListener {

        void loadEmailLoginWorker(@NonNull final String email, @NonNull String password);

        void loadEmailSignUpWorker(@NonNull final String email, @NonNull String password);

        void loadResetPasswordWorker(@NonNull String email);

        void showResetPasswordDialog(@NonNull String email);

        void hideKeyboard();

        void showProfileScreen(boolean accepted);
    }
}
