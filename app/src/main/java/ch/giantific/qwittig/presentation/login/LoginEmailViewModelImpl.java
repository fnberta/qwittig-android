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

import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

import ch.giantific.qwittig.BR;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.bus.RxBus;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.domain.usecases.AfterLoginUseCase;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.viewmodels.ViewModelBaseImpl;
import ch.giantific.qwittig.presentation.login.LoginWorker.LoginType;
import rx.Single;
import rx.SingleSubscriber;

/**
 * Provides an implementation of the {@link LoginEmailViewModel}.
 */
public class LoginEmailViewModelImpl extends ViewModelBaseImpl<LoginEmailViewModel.ViewListener>
        implements LoginEmailViewModel {

    private static final String STATE_SIGN_UP = "STATE_SIGN_UP";

    private final AfterLoginUseCase afterLoginUseCase;
    private String joinIdentityId;
    private boolean signUp;
    private String email;
    private String password;
    private String passwordRepeat;
    private boolean validate;

    public LoginEmailViewModelImpl(@Nullable Bundle savedState,
                                   @NonNull Navigator navigator,
                                   @NonNull RxBus<Object> eventBus,
                                   @NonNull UserRepository userRepository,
                                   @NonNull AfterLoginUseCase afterLoginUseCase) {
        super(savedState, navigator, eventBus, userRepository);

        this.afterLoginUseCase = afterLoginUseCase;

        if (savedState != null) {
            signUp = savedState.getBoolean(STATE_SIGN_UP);
        } else {
            signUp = false;
        }
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);

        outState.putBoolean(STATE_SIGN_UP, signUp);
    }

    public void setJoinIdentityId(@NonNull String joinIdentityId) {
        this.joinIdentityId = joinIdentityId;
    }

    @Override
    public void setLoading(boolean loading) {
        super.setLoading(loading);

        if (loading) {
            view.hideKeyboard();
        }
    }

    @Override
    @Bindable
    public boolean isValidate() {
        return validate;
    }

    @Override
    public void setValidate(boolean validate) {
        this.validate = validate;
        notifyPropertyChanged(BR.validate);
    }

    @Override
    public boolean isEmailComplete() {
        return !TextUtils.isEmpty(email);
    }

    @Override
    public boolean isPasswordComplete() {
        return !TextUtils.isEmpty(password);
    }

    @Override
    public boolean isPasswordsMatch() {
        return Objects.equals(password, passwordRepeat);
    }

    @Override
    @Bindable
    public boolean isSignUp() {
        return signUp;
    }

    @Override
    public void setSignUp(boolean signUp) {
        this.signUp = signUp;
        notifyPropertyChanged(BR.signUp);
    }

    @Override
    public void setUserLoginStream(@NonNull Single<FirebaseUser> loginResult,
                                   @NonNull final String workerTag,
                                   @LoginType final int type) {
        afterLoginUseCase.setLoginResult(loginResult);
        afterLoginUseCase.setJoinIdentityId(joinIdentityId);
        getSubscriptions().add(afterLoginUseCase.execute()
                .subscribe(new SingleSubscriber<Boolean>() {
                    @Override
                    public void onSuccess(Boolean isUserNew) {
                        view.removeWorker(workerTag);

                        if (isUserNew) {
                            view.showProfileScreen(!TextUtils.isEmpty(joinIdentityId));
                        } else {
                            navigator.finish(Activity.RESULT_OK);
                        }
                    }

                    @Override
                    public void onError(Throwable error) {
                        view.removeWorker(workerTag);
                        setLoading(false);

                        view.showMessage(R.string.toast_error_login);
                    }
                })
        );
    }

    @Override
    public void setEmailUserStream(@NonNull Single<Void> single, @NonNull final String workerTag) {
        getSubscriptions().add(single.subscribe(new SingleSubscriber<Void>() {
                    @Override
                    public void onSuccess(Void value) {
                        view.removeWorker(workerTag);
                        setLoading(false);

                        view.showMessage(R.string.toast_password_reset);
                    }

                    @Override
                    public void onError(Throwable error) {
                        view.removeWorker(workerTag);
                        setLoading(false);

                        view.showMessage(R.string.toast_error_login);
                    }
                })
        );
    }

    @Override
    public void onEmailChanged(CharSequence s, int start, int before, int count) {
        email = s.toString();

        if (validate) {
            notifyPropertyChanged(BR.validate);
        }
    }

    @Override
    public void onPasswordChanged(CharSequence s, int start, int before, int count) {
        password = s.toString();

        if (validate) {
            notifyPropertyChanged(BR.validate);
        }
    }

    @Override
    public void onPasswordRepeatChanged(CharSequence s, int start, int before, int count) {
        passwordRepeat = s.toString();

        if (validate) {
            notifyPropertyChanged(BR.validate);
        }
    }

    @Override
    public void onLoginClick(View view) {
        if (validate()) {
            setLoading(true);
            this.view.loadEmailLoginWorker(email, password);
        }
    }

    @Override
    public void onSignUpClick(View view) {
        if (!signUp) {
            setSignUp(true);
            return;
        }

        if (validate()) {
            setLoading(true);
            this.view.loadEmailSignUpWorker(email, password);
        }
    }

    private boolean validate() {
        setValidate(true);
        return signUp
                ? isEmailComplete() && isPasswordComplete() && isPasswordsMatch()
                : isEmailComplete() && isPasswordComplete();
    }

    @Override
    public void onResetPasswordClick(View view) {
        this.view.showResetPasswordDialog(email);
    }

    @Override
    public void onValidEmailEntered(@NonNull String email) {
        view.loadResetPasswordWorker(email);
    }
}
