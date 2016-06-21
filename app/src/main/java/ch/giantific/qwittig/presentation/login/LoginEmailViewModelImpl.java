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

import java.util.Objects;

import ch.giantific.qwittig.BR;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.bus.RxBus;
import ch.giantific.qwittig.domain.models.User;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.viewmodels.ViewModelBaseImpl;
import rx.Single;
import rx.SingleSubscriber;

/**
 * Provides an implementation of the {@link LoginEmailViewModel}.
 */
public class LoginEmailViewModelImpl extends ViewModelBaseImpl<LoginEmailViewModel.ViewListener>
        implements LoginEmailViewModel {

    private static final String STATE_LOADING = "STATE_LOADING";
    private static final String STATE_SIGN_UP = "STATE_SIGN_UP";
    private final Navigator mNavigator;
    private String mIdentityId;
    private boolean mLoading;
    private boolean mSignUp;
    private String mEmail;
    private String mPassword;
    private String mPasswordRepeat;
    private boolean mValidate;

    public LoginEmailViewModelImpl(@Nullable Bundle savedState,
                                   @NonNull Navigator navigator,
                                   @NonNull RxBus<Object> eventBus,
                                   @NonNull UserRepository userRepository) {
        super(savedState, eventBus, userRepository);

        mNavigator = navigator;

        if (savedState != null) {
            mLoading = savedState.getBoolean(STATE_LOADING);
            mSignUp = savedState.getBoolean(STATE_SIGN_UP);
        } else {
            mLoading = false;
            mSignUp = false;
        }
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);

        outState.putBoolean(STATE_LOADING, mLoading);
        outState.putBoolean(STATE_SIGN_UP, mSignUp);
    }

    @Override
    public void setIdentityId(@NonNull String identityId) {
        mIdentityId = identityId;
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
        return Objects.equals(mPassword, mPasswordRepeat);
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
                                   @LoginWorker.Type final int type) {
        getSubscriptions().add(single
                .subscribe(new SingleSubscriber<User>() {
                    @Override
                    public void onSuccess(User user) {
                        mView.removeWorker(workerTag);

                        if (type == LoginWorker.Type.RESET_PASSWORD) {
                            mView.showMessage(R.string.toast_password_reset);
                        } else if (user.isNew()) {
                            mView.showProfileScreen(!TextUtils.isEmpty(mIdentityId));
                        } else {
                            mNavigator.finish(Activity.RESULT_OK);
                        }
                    }

                    @Override
                    public void onError(Throwable error) {
                        mView.removeWorker(workerTag);
                        setLoading(false);

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
            setLoading(true);
            mView.loadEmailLoginWorker(mEmail, mPassword, mIdentityId);
        }
    }

    @Override
    public void onSignUpClick(View view) {
        if (!mSignUp) {
            setSignUp(true);
            return;
        }

        if (validate()) {
            setLoading(true);
            mView.loadEmailSignUpWorker(mEmail, mPassword, mIdentityId);
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
}
