/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.profile;

import android.app.Activity;
import android.databinding.Bindable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;

import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ch.giantific.qwittig.BR;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.bus.RxBus;
import ch.giantific.qwittig.data.repositories.GroupRepository;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.models.User;
import ch.giantific.qwittig.presentation.common.IndefiniteSubscriber;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.viewmodels.ViewModelBaseImpl;
import ch.giantific.qwittig.utils.Utils;
import rx.Observable;
import rx.Single;
import rx.SingleSubscriber;
import rx.functions.Action1;
import rx.functions.Func1;
import timber.log.Timber;

/**
 * Provides an implementation of the {@link SettingsProfileViewModel}.
 */
public class SettingsProfileViewModelImpl extends ViewModelBaseImpl<SettingsProfileViewModel.ViewListener>
        implements SettingsProfileViewModel {

    private static final String STATE_VALIDATE = "STATE_VALIDATE";
    private static final String STATE_UNLINK_THIRD_PARTY = "STATE_UNLINK_THIRD_PARTY";
    private static final String STATE_AVATAR = "STATE_AVATAR";
    private static final String STATE_EMAIL = "STATE_EMAIL";
    private static final String STATE_NICKNAME = "STATE_NICKNAME";
    private static final String STATE_PASSWORD = "STATE_PASSWORD";
    private static final String STATE_PASSWORD_REPEAT = "STATE_PASSWORD_REPEAT";
    private final List<String> mGroupNicknames;
    private final GroupRepository mGroupRepo;
    private boolean mFacebookUser;
    private boolean mGoogleUser;
    private boolean mValidate;
    private boolean mUnlinkSocialLogin;
    private String mAvatar;
    private String mAvatarOrig;
    private String mEmail;
    private String mEmailOrig;
    private String mNickname;
    private String mNicknameOrig;
    private String mPassword;
    private String mPasswordRepeat;

    public SettingsProfileViewModelImpl(@Nullable Bundle savedState,
                                        @NonNull Navigator navigator,
                                        @NonNull RxBus<Object> eventBus,
                                        @NonNull UserRepository userRepository,
                                        @NonNull GroupRepository groupRepository) {
        super(savedState, navigator, eventBus, userRepository);

        mGroupRepo = groupRepository;
        mGroupNicknames = new ArrayList<>();

        if (savedState != null) {
            mValidate = savedState.getBoolean(STATE_VALIDATE);
            mUnlinkSocialLogin = savedState.getBoolean(STATE_UNLINK_THIRD_PARTY);
            mEmail = savedState.getString(STATE_EMAIL);
            mPassword = savedState.getString(STATE_PASSWORD);
            mPasswordRepeat = savedState.getString(STATE_PASSWORD_REPEAT);
            mAvatar = savedState.getString(STATE_AVATAR);
            mNickname = savedState.getString(STATE_NICKNAME);
        } else {
            mPassword = "";
            mPasswordRepeat = "";
        }
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);

        outState.putBoolean(STATE_VALIDATE, mValidate);
        outState.putBoolean(STATE_UNLINK_THIRD_PARTY, mUnlinkSocialLogin);
        outState.putString(STATE_AVATAR, mAvatar);
        outState.putString(STATE_EMAIL, mEmail);
        outState.putString(STATE_NICKNAME, mNickname);
        outState.putString(STATE_PASSWORD, mPassword);
        outState.putString(STATE_PASSWORD_REPEAT, mPasswordRepeat);
    }

    @Override
    public boolean isValidate() {
        return mValidate;
    }

    @Override
    public void setValidate(boolean validate) {
        mValidate = validate;
        notifyPropertyChanged(BR.validate);
    }

    @Override
    @Bindable
    public String getAvatar() {
        return mAvatar;
    }

    @Override
    public void setAvatar(@NonNull String avatarUrl) {
        mAvatar = avatarUrl;
        notifyPropertyChanged(BR.avatar);
    }

    @Override
    public void onAvatarLoaded() {
        mView.startPostponedEnterTransition();
    }

    @Override
    @Bindable
    public String getNickname() {
        return mNickname;
    }

    @Override
    public void setNickname(@NonNull String nickname) {
        mNickname = nickname;
        notifyPropertyChanged(BR.nickname);
    }

    @Override
    @Bindable
    public String getEmail() {
        return mEmail;
    }

    @Override
    public void setEmail(@NonNull String email) {
        mEmail = email;
        notifyPropertyChanged(BR.email);
    }

    @Override
    @Bindable
    public boolean isNicknameComplete() {
        return !TextUtils.isEmpty(mNickname);
    }

    @Override
    @Bindable
    public boolean isEmailValid() {
        return Utils.isEmailValid(mEmail);
    }

    @Override
    @Bindable
    public boolean isPasswordValid() {
        return TextUtils.isEmpty(mPassword) || Utils.isPasswordValid(mPassword);
    }

    @Override
    @Bindable
    public boolean isPasswordEqual() {
        return Objects.equals(mPassword, mPasswordRepeat);
    }

    @Override
    @Bindable
    public boolean isEmailAndPasswordVisible() {
        return !mFacebookUser && !mGoogleUser || mUnlinkSocialLogin;
    }

    @Override
    public boolean showUnlinkFacebook() {
        return mFacebookUser && !mUnlinkSocialLogin;
    }

    @Override
    public boolean showUnlinkGoogle() {
        return mGoogleUser && !mUnlinkSocialLogin;
    }

    @Override
    protected void onUserLoggedIn(@NonNull FirebaseUser currentUser) {
        super.onUserLoggedIn(currentUser);

        mGoogleUser = mUserRepo.isGoogleUser(currentUser);
        mFacebookUser = mUserRepo.isFacebookUser(currentUser);
        notifyPropertyChanged(BR.emailAndPasswordVisible);
        mView.reloadOptionsMenu();

        mEmailOrig = currentUser.getEmail();
        if (TextUtils.isEmpty(mEmail) && !TextUtils.isEmpty(mEmailOrig)) {
            setEmail(mEmailOrig);
        }

        getSubscriptions().add(mUserRepo.getUser(currentUser.getUid())
                .flatMap(new Func1<User, Single<Identity>>() {
                    @Override
                    public Single<Identity> call(User user) {
                        return mUserRepo.getIdentity(user.getCurrentIdentity());
                    }
                })
                .doOnSuccess(new Action1<Identity>() {
                    @Override
                    public void call(Identity identity) {
                        final String nickname = identity.getNickname();
                        mNicknameOrig = nickname;
                        if (TextUtils.isEmpty(mNickname)) {
                            setNickname(nickname);
                        }

                        final String avatar = identity.getAvatar();
                        mAvatarOrig = avatar;
                        if (TextUtils.isEmpty(mAvatar)) {
                            setAvatar(avatar);
                        }
                    }
                })
                .flatMapObservable(new Func1<Identity, Observable<Identity>>() {
                    @Override
                    public Observable<Identity> call(Identity identity) {
                        return mGroupRepo.getGroupIdentities(identity.getGroup(), true);
                    }
                })
                .subscribe(new IndefiniteSubscriber<Identity>() {
                    @Override
                    public void onNext(Identity identity) {
                        mGroupNicknames.add(identity.getNickname());
                    }
                })
        );
    }

    @Override
    public void onPickAvatarMenuClick() {
        mNavigator.startImagePicker();
    }

    @Override
    public void onNewAvatarTaken(@NonNull String avatar) {
        setAvatar(avatar);
        mView.reloadOptionsMenu();
    }

    @Override
    public void onDeleteAvatarMenuClick() {
        setAvatar("");
        mView.reloadOptionsMenu();
    }

    @Override
    public void onUnlinkThirdPartyLoginMenuClick() {
        mUnlinkSocialLogin = true;
        notifyPropertyChanged(BR.emailAndPasswordVisible);
        mView.reloadOptionsMenu();
        mView.showSetPasswordMessage(R.string.toast_unlink_password_required);
    }

    @Override
    public void onExitClick() {
        if (changesWereMade()) {
            mView.showDiscardChangesDialog();
        } else {
            mNavigator.finish(Activity.RESULT_CANCELED);
        }
    }

    private boolean changesWereMade() {
        return !Objects.equals(mEmail, mEmailOrig)
                || !Objects.equals(mNickname, mNicknameOrig)
                || isAvatarChanged()
                || !TextUtils.isEmpty(mPassword);
    }

    private boolean isAvatarChanged() {
        return !Objects.equals(mAvatar, mAvatarOrig);
    }

    @Override
    public void onDiscardChangesSelected() {
        mNavigator.finish(Result.CHANGES_DISCARDED);
    }

    @Override
    public void onEmailChanged(CharSequence s, int start, int before, int count) {
        mEmail = s.toString();
        if (mValidate) {
            notifyPropertyChanged(BR.validate);
        }
    }

    @Override
    public void onNicknameChanged(CharSequence s, int start, int before, int count) {
        mNickname = s.toString();
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

        if (mUnlinkSocialLogin) {
            if (TextUtils.isEmpty(mPassword)) {
                mView.showSetPasswordMessage(R.string.toast_unlink_password_required);
            } else {
                mView.dismissSetPasswordMessage();
            }
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
    public void onFabSaveProfileClick(View view) {
        if (!validate()) {
            return;
        }

        if (!Objects.equals(mNickname, mNicknameOrig) && mGroupNicknames.contains(mNickname)) {
            mView.showMessage(R.string.toast_profile_nickname_taken);
            return;
        }

        if (mUnlinkSocialLogin) {
            unlinkSocialLogin();
            return;
        }

        final String email = mGoogleUser || mFacebookUser || Objects.equals(mEmail, mEmailOrig) ? null : mEmail;
        final String password = mGoogleUser || mFacebookUser ? null : mPassword;
        if (!TextUtils.isEmpty(email) || !TextUtils.isEmpty(password)) {
            mView.showReAuthenticateDialog(mEmailOrig);
            return;
        }

        // only nickname and/or avatar changed, proceed straight to saving profile
        saveProfile();
    }

    private boolean validate() {
        setValidate(true);
        return isEmailValid() && isPasswordValid() && isPasswordEqual() && isNicknameComplete();
    }

    private void unlinkSocialLogin() {
        if (mGoogleUser) {
            mView.reAuthenticateGoogle();
        } else if (mFacebookUser) {
            mView.reAuthenticateFacebook();
        }
    }

    @Override
    public void onGoogleLoginSuccessful(@NonNull String idToken) {
        mView.showProgressDialog(R.string.progress_profile_unlink);
        mView.loadUnlinkGoogleWorker(mEmail, mPassword, idToken);
    }

    @Override
    public void onGoogleLoginFailed() {
        mView.showMessage(R.string.toast_error_login_google);
    }

    @Override
    public void setGoogleUserStream(@NonNull Single<Void> single, @NonNull final String workerTag) {
        getSubscriptions().add(single
                .flatMap(new Func1<Void, Single<User>>() {
                    @Override
                    public Single<User> call(Void aVoid) {
                        return mUserRepo.updateProfile(mNickname, mAvatar, isAvatarChanged());
                    }
                })
                .subscribe(profileSubscriber(workerTag))
        );
    }

    @Override
    public void onFacebookSignedIn(@NonNull String token) {
        mView.showProgressDialog(R.string.progress_profile_unlink);
        mView.loadUnlinkFacebookWorker(mEmail, mPassword, token);
    }

    @Override
    public void onFacebookLoginFailed() {
        mView.showMessage(R.string.toast_error_login_facebook);
    }

    @Override
    public void setFacebookUserStream(@NonNull Single<Void> single, @NonNull String workerTag) {
        getSubscriptions().add(single
                .flatMap(new Func1<Void, Single<User>>() {
                    @Override
                    public Single<User> call(Void aVoid) {
                        return mUserRepo.updateProfile(mNickname, mAvatar, isAvatarChanged());
                    }
                })
                .subscribe(profileSubscriber(workerTag))
        );
    }

    @Override
    public void onValidEmailAndPasswordEntered(@NonNull String email, @NonNull String password) {
        mView.showProgressDialog(R.string.progress_profile_change_email_pw);
        mView.loadChangeEmailPasswordWorker(email, password, mEmail, mPassword);
    }

    @Override
    public void setEmailUserStream(@NonNull Single<Void> single, @NonNull final String workerTag) {
        getSubscriptions().add(single
                .flatMap(new Func1<Void, Single<User>>() {
                    @Override
                    public Single<User> call(Void aVoid) {
                        return mUserRepo.updateProfile(mNickname, mAvatar, isAvatarChanged());
                    }
                })
                .subscribe(profileSubscriber(workerTag))
        );
    }

    @NonNull
    private SingleSubscriber<User> profileSubscriber(@NonNull final String workerTag) {
        return new SingleSubscriber<User>() {
            @Override
            public void onSuccess(User user) {
                mView.removeWorker(workerTag);
                mView.hideProgressDialog();

                mNavigator.finish(Activity.RESULT_OK);
            }

            @Override
            public void onError(Throwable error) {
                mView.removeWorker(workerTag);
                mView.hideProgressDialog();

                Timber.e(error, "failed to unlink or change email/pw and save profile with error:");
                mView.showMessage(R.string.toast_error_profile);
            }
        };
    }

    private void saveProfile() {
        getSubscriptions().add(mUserRepo.updateProfile(mNickname, mAvatar, isAvatarChanged())
                .subscribe(new SingleSubscriber<User>() {
                    @Override
                    public void onSuccess(User user) {
                        mNavigator.finish(Activity.RESULT_OK);
                    }

                    @Override
                    public void onError(Throwable error) {
                        Timber.e(error, "failed to save profile with error:");
                        mView.showMessage(R.string.toast_error_profile);
                    }
                })
        );
    }
}