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
    private final RemoteConfigHelper mConfigHelper;
    private final GroupRepository mGroupRepo;
    private String mIdentityId;
    private boolean mSignUp;
    private String mEmail;
    private String mPassword;
    private String mPasswordRepeat;
    private boolean mValidate;

    public LoginEmailViewModelImpl(@Nullable Bundle savedState,
                                   @NonNull Navigator navigator,
                                   @NonNull RxBus<Object> eventBus,
                                   @NonNull RemoteConfigHelper configHelper,
                                   @NonNull UserRepository userRepository,
                                   @NonNull GroupRepository groupRepository) {
        super(savedState, navigator, eventBus, userRepository);
        mConfigHelper = configHelper;

        mGroupRepo = groupRepository;

        if (savedState != null) {
            mSignUp = savedState.getBoolean(STATE_SIGN_UP);
        } else {
            mSignUp = false;
        }
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);

        outState.putBoolean(STATE_SIGN_UP, mSignUp);
    }

    @Override
    public void setIdentityId(@NonNull String identityId) {
        mIdentityId = identityId;
    }

    @Override
    public void setLoading(boolean loading) {
        super.setLoading(loading);

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
    public void setUserLoginStream(@NonNull Single<FirebaseUser> single,
                                   @NonNull final String workerTag,
                                   @LoginWorker.Type final int type) {
        getSubscriptions().add(single
                .flatMap(new Func1<FirebaseUser, Single<Boolean>>() {
                    @Override
                    public Single<Boolean> call(final FirebaseUser firebaseUser) {
                        final String userId = firebaseUser.getUid();
                        if (!TextUtils.isEmpty(mIdentityId)) {
                            return mUserRepo.getIdentity(mIdentityId)
                                    .doOnSuccess(new Action1<Identity>() {
                                        @Override
                                        public void call(Identity identity) {
                                            mGroupRepo.joinGroup(userId, mIdentityId, identity.getGroup());
                                        }
                                    })
                                    .map(new Func1<Identity, Boolean>() {
                                        @Override
                                        public Boolean call(Identity identity) {
                                            return type == LoginWorker.Type.SIGN_UP_EMAIL;
                                        }
                                    });
                        }

                        if (type == LoginWorker.Type.SIGN_UP_EMAIL) {
                            return Single.just(true)
                                    .doOnSuccess(new Action1<Boolean>() {
                                        @Override
                                        public void call(Boolean isUserNew) {
                                            final String email = firebaseUser.getEmail();
                                            final String defaultNickname = !TextUtils.isEmpty(email)
                                                    ? email.substring(0, email.indexOf("@"))
                                                    : "";
                                            mGroupRepo.createGroup(userId,
                                                    mConfigHelper.getDefaultGroupName(),
                                                    mConfigHelper.getDefaultGroupCurrency(),
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
                        mView.removeWorker(workerTag);

                        if (isUserNew) {
                            mView.showProfileScreen(!TextUtils.isEmpty(mIdentityId));
                        } else {
                            mNavigator.finish(Activity.RESULT_OK);
                        }
                    }

                    @Override
                    public void onError(Throwable error) {
                        mView.removeWorker(workerTag);
                        setLoading(false);

                        mView.showMessage(R.string.toast_error_login);
                    }
                })
        );
    }

    @Override
    public void setEmailUserStream(@NonNull Single<Void> single, @NonNull final String workerTag) {
        getSubscriptions().add(single.subscribe(new SingleSubscriber<Void>() {
                    @Override
                    public void onSuccess(Void value) {
                        mView.removeWorker(workerTag);
                        setLoading(false);

                        mView.showMessage(R.string.toast_password_reset);
                    }

                    @Override
                    public void onError(Throwable error) {
                        mView.removeWorker(workerTag);
                        setLoading(false);

                        mView.showMessage(R.string.toast_error_login);
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
            mView.loadEmailLoginWorker(mEmail, mPassword);
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
}
