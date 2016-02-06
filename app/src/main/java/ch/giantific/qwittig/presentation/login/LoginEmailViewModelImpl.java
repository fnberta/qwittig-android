/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.login;

import android.app.Activity;
import android.databinding.Bindable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;

import ch.giantific.qwittig.BR;
import ch.giantific.qwittig.domain.models.parse.User;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.common.viewmodels.ViewModelBaseImpl;
import rx.Single;
import rx.SingleSubscriber;

/**
 * Created by fabio on 05.02.16.
 */
public class LoginEmailViewModelImpl extends ViewModelBaseImpl<LoginEmailViewModel.ViewListener>
        implements LoginEmailViewModel {

    private static final String STATE_LOADING = "STATE_LOADING";
    private boolean mLoading;
    private boolean mSignUp;
    private String mEmail;
    private String mPassword;
    private String mPasswordRepeat;
    private boolean mValidate;

    public LoginEmailViewModelImpl(@Nullable Bundle savedState,
                                   @NonNull LoginEmailViewModel.ViewListener view,
                                   @NonNull UserRepository userRepository) {
        super(savedState, view, userRepository);

        mLoading = savedState == null || (savedState.getBoolean(STATE_LOADING, false));
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);

        outState.putBoolean(STATE_LOADING, mLoading);
    }

    @Override
    @Bindable
    public boolean isLoading() {
        return mLoading;
    }

    @Override
    public void setLoading(boolean loading) {
        mLoading = loading;
        notifyPropertyChanged(BR.loading);
        if (loading) {
            mView.hideKeyboard();
        }
    }

    @Override
    @Bindable
    public boolean isValidate() {
        return mValidate;
    }

    @Override
    public void setValidate(boolean validate) {
        mValidate = validate;
        notifyPropertyChanged(BR.validate);
    }

    @Override
    public boolean isEmailComplete() {
        return !TextUtils.isEmpty(mEmail);
    }

    @Override
    public boolean isPasswordComplete() {
        return !TextUtils.isEmpty(mPassword);
    }

    @Override
    public boolean isPasswordsMatch() {
        return mPassword.equals(mPasswordRepeat);
    }

    @Override
    @Bindable
    public boolean isSignUp() {
        return mSignUp;
    }

    @Override
    public void setSignUp(boolean signUp) {
        mSignUp = signUp;
        notifyPropertyChanged(BR.signUp);
    }

    @Override
    public void setUserLoginStream(@NonNull Single<User> single, @NonNull final String workerTag,
                                   @LoginWorker.Type int type) {
        mSubscriptions.add(single
                .subscribe(new SingleSubscriber<User>() {
                    @Override
                    public void onSuccess(User value) {
                        mView.removeWorker(workerTag);
                        mView.finishScreen(Activity.RESULT_OK);
                    }

                    @Override
                    public void onError(Throwable error) {
                        mView.removeWorker(workerTag);
                        mView.showMessage(mUserRepo.getErrorMessage(error));
                    }
                })
        );
    }

    @Override
    public void onEmailChanged(CharSequence s, int start, int before, int count) {
        mEmail = s.toString();

        if (mValidate) {
            notifyPropertyChanged(BR.validate);
        }
    }

    @Override
    public void onPasswordChanged(CharSequence s, int start, int before, int count) {
        mPassword = s.toString();

        if (mValidate) {
            notifyPropertyChanged(BR.validate);
        }
    }

    @Override
    public void onPasswordRepeatChanged(CharSequence s, int start, int before, int count) {
        mPasswordRepeat = s.toString();

        if (mValidate) {
            notifyPropertyChanged(BR.validate);
        }
    }

    @Override
    public void onLoginClick(View view) {
        if (validate()) {
            mView.loadEmailLoginWorker(mEmail, mPassword);
        }
    }

    @Override
    public void onSignUpClick(View view) {
        if (validate()) {
            mView.loadEmailSignUpWorker(mEmail, mPassword);
        }
    }

    private boolean validate() {
        setValidate(true);
        return mSignUp
                ? isEmailComplete() && isPasswordComplete() && isPasswordsMatch()
                : isEmailComplete() && isPasswordComplete();
    }

    @Override
    public void onResetPasswordClick(View view) {
        mView.showResetPasswordDialog(mEmail);
    }

    @Override
    public void onValidEmailEntered(@NonNull String email) {
        mView.loadResetPasswordWorker(email);
    }

    @Override
    public void onNoEmailEntered() {
        // TODO: what do we do?
    }
}
