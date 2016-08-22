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
import ch.giantific.qwittig.data.helper.RemoteConfigHelper;
import ch.giantific.qwittig.data.repositories.GroupRepository;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.viewmodels.ViewModelBaseImpl;
import ch.giantific.qwittig.presentation.login.LoginWorker.LoginType;
import rx.Single;
import rx.SingleSubscriber;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Provides an implementation of the {@link LoginEmailViewModel}.
 */
public class LoginEmailViewModelImpl extends ViewModelBaseImpl<LoginEmailViewModel.ViewListener>
        implements LoginEmailViewModel {

    private static final String STATE_SIGN_UP = "STATE_SIGN_UP";

    private final RemoteConfigHelper configHelper;
    private final GroupRepository groupRepo;
    private String identityId;
    private boolean signUp;
    private String email;
    private String password;
    private String passwordRepeat;
    private boolean validate;

    public LoginEmailViewModelImpl(@Nullable Bundle savedState,
                                   @NonNull Navigator navigator,
                                   @NonNull RxBus<Object> eventBus,
                                   @NonNull RemoteConfigHelper configHelper,
                                   @NonNull UserRepository userRepository,
                                   @NonNull GroupRepository groupRepo) {
        super(savedState, navigator, eventBus, userRepository);
        this.configHelper = configHelper;

        this.groupRepo = groupRepo;

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

    @Override
    public void setIdentityId(@NonNull String identityId) {
        this.identityId = identityId;
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
    public void setUserLoginStream(@NonNull Single<FirebaseUser> single,
                                   @NonNull final String workerTag,
                                   @LoginType final int type) {
        getSubscriptions().add(single
                .flatMap(new Func1<FirebaseUser, Single<Boolean>>() {
                    @Override
                    public Single<Boolean> call(final FirebaseUser firebaseUser) {
                        final String userId = firebaseUser.getUid();
                        if (!TextUtils.isEmpty(identityId)) {
                            return userRepo.getIdentity(identityId)
                                    .doOnSuccess(new Action1<Identity>() {
                                        @Override
                                        public void call(Identity identity) {
                                            groupRepo.joinGroup(userId, identityId, identity.getGroup());
                                        }
                                    })
                                    .map(new Func1<Identity, Boolean>() {
                                        @Override
                                        public Boolean call(Identity identity) {
                                            return type == LoginType.SIGN_UP_EMAIL;
                                        }
                                    });
                        }

                        if (type == LoginType.SIGN_UP_EMAIL) {
                            return Single.just(true)
                                    .doOnSuccess(new Action1<Boolean>() {
                                        @Override
                                        public void call(Boolean isUserNew) {
                                            final String email = firebaseUser.getEmail();
                                            final String defaultNickname = !TextUtils.isEmpty(email)
                                                    ? email.substring(0, email.indexOf("@"))
                                                    : "";
                                            groupRepo.createGroup(userId,
                                                    configHelper.getDefaultGroupName(),
                                                    configHelper.getDefaultGroupCurrency(),
                                                    defaultNickname, null);
                                        }
                                    });
                        }

                        return Single.just(false);
                    }
                })
                .subscribe(new SingleSubscriber<Boolean>() {
                    @Override
                    public void onSuccess(Boolean isUserNew) {
                        view.removeWorker(workerTag);

                        if (isUserNew) {
                            view.showProfileScreen(!TextUtils.isEmpty(identityId));
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
