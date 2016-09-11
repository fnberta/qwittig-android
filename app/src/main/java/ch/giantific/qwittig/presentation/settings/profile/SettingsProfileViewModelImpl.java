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
import rx.Single;
import rx.SingleSubscriber;
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
    private final List<String> groupNicknames;
    private final GroupRepository groupRepo;
    private boolean facebookUser;
    private boolean googleUser;
    private boolean validate;
    private boolean unlinkSocialLogin;
    private String avatar;
    private String avatarOrig;
    private String email;
    private String emailOrig;
    private String nickname;
    private String nicknameOrig;
    private String password;
    private String passwordRepeat;

    public SettingsProfileViewModelImpl(@Nullable Bundle savedState,
                                        @NonNull Navigator navigator,
                                        @NonNull RxBus<Object> eventBus,
                                        @NonNull UserRepository userRepo,
                                        @NonNull GroupRepository groupRepo) {
        super(savedState, navigator, eventBus, userRepo);

        this.groupRepo = groupRepo;
        groupNicknames = new ArrayList<>();

        if (savedState != null) {
            validate = savedState.getBoolean(STATE_VALIDATE);
            unlinkSocialLogin = savedState.getBoolean(STATE_UNLINK_THIRD_PARTY);
            email = savedState.getString(STATE_EMAIL);
            password = savedState.getString(STATE_PASSWORD);
            passwordRepeat = savedState.getString(STATE_PASSWORD_REPEAT);
            avatar = savedState.getString(STATE_AVATAR);
            nickname = savedState.getString(STATE_NICKNAME);
        } else {
            password = "";
            passwordRepeat = "";
        }
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);

        outState.putBoolean(STATE_VALIDATE, validate);
        outState.putBoolean(STATE_UNLINK_THIRD_PARTY, unlinkSocialLogin);
        outState.putString(STATE_AVATAR, avatar);
        outState.putString(STATE_EMAIL, email);
        outState.putString(STATE_NICKNAME, nickname);
        outState.putString(STATE_PASSWORD, password);
        outState.putString(STATE_PASSWORD_REPEAT, passwordRepeat);
    }

    @Override
    public boolean isValidate() {
        return validate;
    }

    @Override
    public void setValidate(boolean validate) {
        this.validate = validate;
        notifyPropertyChanged(BR.validate);
    }

    @Override
    @Bindable
    public String getAvatar() {
        return avatar;
    }

    @Override
    public void setAvatar(@NonNull String avatarUrl) {
        avatar = avatarUrl;
        notifyPropertyChanged(BR.avatar);
    }

    @Override
    public void onAvatarLoaded() {
        view.startPostponedEnterTransition();
    }

    @Override
    @Bindable
    public String getNickname() {
        return nickname;
    }

    @Override
    public void setNickname(@NonNull String nickname) {
        this.nickname = nickname;
        notifyPropertyChanged(BR.nickname);
    }

    @Override
    @Bindable
    public String getEmail() {
        return email;
    }

    @Override
    public void setEmail(@NonNull String email) {
        this.email = email;
        notifyPropertyChanged(BR.email);
    }

    @Override
    @Bindable
    public boolean isNicknameComplete() {
        return !TextUtils.isEmpty(nickname);
    }

    @Override
    @Bindable
    public boolean isEmailValid() {
        return Utils.isEmailValid(email);
    }

    @Override
    @Bindable
    public boolean isPasswordValid() {
        return TextUtils.isEmpty(password) || Utils.isPasswordValid(password);
    }

    @Override
    @Bindable
    public boolean isPasswordEqual() {
        return Objects.equals(password, passwordRepeat);
    }

    @Override
    @Bindable
    public boolean isEmailAndPasswordVisible() {
        return !facebookUser && !googleUser || unlinkSocialLogin;
    }

    @Override
    public boolean showUnlinkFacebook() {
        return facebookUser && !unlinkSocialLogin;
    }

    @Override
    public boolean showUnlinkGoogle() {
        return googleUser && !unlinkSocialLogin;
    }

    @Override
    protected void onUserLoggedIn(@NonNull FirebaseUser currentUser) {
        super.onUserLoggedIn(currentUser);

        googleUser = userRepo.isGoogleUser(currentUser);
        facebookUser = userRepo.isFacebookUser(currentUser);
        notifyPropertyChanged(BR.emailAndPasswordVisible);
        view.reloadOptionsMenu();

        emailOrig = currentUser.getEmail();
        if (TextUtils.isEmpty(email) && !TextUtils.isEmpty(emailOrig)) {
            setEmail(emailOrig);
        }

        getSubscriptions().add(userRepo.getUser(currentUser.getUid())
                .flatMap(user -> userRepo.getIdentity(user.getCurrentIdentity()))
                .doOnSuccess(identity -> {
                    final String nickname1 = identity.getNickname();
                    nicknameOrig = nickname1;
                    if (TextUtils.isEmpty(SettingsProfileViewModelImpl.this.nickname)) {
                        setNickname(nickname1);
                    }

                    final String avatar1 = identity.getAvatar();
                    avatarOrig = avatar1;
                    if (TextUtils.isEmpty(SettingsProfileViewModelImpl.this.avatar)) {
                        setAvatar(avatar1);
                    }
                })
                .flatMapObservable(identity -> groupRepo.getGroupIdentities(identity.getGroup(), true))
                .subscribe(new IndefiniteSubscriber<Identity>() {
                    @Override
                    public void onNext(Identity identity) {
                        groupNicknames.add(identity.getNickname());
                    }
                })
        );
    }

    @Override
    public void onPickAvatarMenuClick() {
        navigator.startImagePicker();
    }

    @Override
    public void onNewAvatarTaken(@NonNull String avatar) {
        setAvatar(avatar);
        view.reloadOptionsMenu();
    }

    @Override
    public void onDeleteAvatarMenuClick() {
        setAvatar("");
        view.reloadOptionsMenu();
    }

    @Override
    public void onUnlinkThirdPartyLoginMenuClick() {
        unlinkSocialLogin = true;
        notifyPropertyChanged(BR.emailAndPasswordVisible);
        view.reloadOptionsMenu();
        view.showSetPasswordMessage(R.string.toast_unlink_password_required);
    }

    @Override
    public void onExitClick() {
        if (changesWereMade()) {
            view.showDiscardChangesDialog();
        } else {
            navigator.finish(Activity.RESULT_CANCELED);
        }
    }

    private boolean changesWereMade() {
        return !Objects.equals(email, emailOrig)
                || !Objects.equals(nickname, nicknameOrig)
                || isAvatarChanged()
                || !TextUtils.isEmpty(password);
    }

    private boolean isAvatarChanged() {
        return !Objects.equals(avatar, avatarOrig);
    }

    @Override
    public void onDiscardChangesSelected() {
        navigator.finish(Result.CHANGES_DISCARDED);
    }

    @Override
    public void onEmailChanged(CharSequence s, int start, int before, int count) {
        email = s.toString();
        if (validate) {
            notifyPropertyChanged(BR.validate);
        }
    }

    @Override
    public void onNicknameChanged(CharSequence s, int start, int before, int count) {
        nickname = s.toString();
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

        if (unlinkSocialLogin) {
            if (TextUtils.isEmpty(password)) {
                view.showSetPasswordMessage(R.string.toast_unlink_password_required);
            } else {
                view.dismissSetPasswordMessage();
            }
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
    public void onFabSaveProfileClick(View view) {
        if (!validate()) {
            return;
        }

        if (!Objects.equals(nickname, nicknameOrig) && groupNicknames.contains(nickname)) {
            this.view.showMessage(R.string.toast_profile_nickname_taken);
            return;
        }

        if (unlinkSocialLogin) {
            unlinkSocialLogin();
            return;
        }

        final String email = googleUser || facebookUser || Objects.equals(this.email, emailOrig) ? null : this.email;
        final String password = googleUser || facebookUser ? null : this.password;
        if (!TextUtils.isEmpty(email) || !TextUtils.isEmpty(password)) {
            this.view.showReAuthenticateDialog(emailOrig);
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
        if (googleUser) {
            view.reAuthenticateGoogle();
        } else if (facebookUser) {
            view.reAuthenticateFacebook();
        }
    }

    @Override
    public void onGoogleLoginSuccessful(@NonNull String idToken) {
        view.showProgressDialog(R.string.progress_profile_unlink);
        view.loadUnlinkGoogleWorker(email, password, idToken);
    }

    @Override
    public void onGoogleLoginFailed() {
        view.showMessage(R.string.toast_error_login_google);
    }

    @Override
    public void setGoogleUserStream(@NonNull Single<Void> single, @NonNull final String workerTag) {
        getSubscriptions().add(single
                .flatMap(aVoid -> userRepo.updateProfile(nickname, avatar, isAvatarChanged()))
                .subscribe(profileSubscriber(workerTag))
        );
    }

    @Override
    public void onFacebookSignedIn(@NonNull String token) {
        view.showProgressDialog(R.string.progress_profile_unlink);
        view.loadUnlinkFacebookWorker(email, password, token);
    }

    @Override
    public void onFacebookLoginFailed() {
        view.showMessage(R.string.toast_error_login_facebook);
    }

    @Override
    public void setFacebookUserStream(@NonNull Single<Void> single, @NonNull String workerTag) {
        getSubscriptions().add(single
                .flatMap(aVoid -> userRepo.updateProfile(nickname, avatar, isAvatarChanged()))
                .subscribe(profileSubscriber(workerTag))
        );
    }

    @Override
    public void onValidEmailAndPasswordEntered(@NonNull String email, @NonNull String password) {
        view.showProgressDialog(R.string.progress_profile_change_email_pw);
        view.loadChangeEmailPasswordWorker(email, password, this.email, this.password);
    }

    @Override
    public void setEmailUserStream(@NonNull Single<Void> single, @NonNull final String workerTag) {
        getSubscriptions().add(single
                .flatMap(aVoid -> userRepo.updateProfile(nickname, avatar, isAvatarChanged()))
                .subscribe(profileSubscriber(workerTag))
        );
    }

    @NonNull
    private SingleSubscriber<User> profileSubscriber(@NonNull final String workerTag) {
        return new SingleSubscriber<User>() {
            @Override
            public void onSuccess(User user) {
                view.removeWorker(workerTag);
                view.hideProgressDialog();

                navigator.finish(Activity.RESULT_OK);
            }

            @Override
            public void onError(Throwable error) {
                view.removeWorker(workerTag);
                view.hideProgressDialog();

                Timber.e(error, "failed to unlink or change email/pw and save profile with error:");
                view.showMessage(R.string.toast_error_profile);
            }
        };
    }

    private void saveProfile() {
        getSubscriptions().add(userRepo.updateProfile(nickname, avatar, isAvatarChanged())
                .subscribe(new SingleSubscriber<User>() {
                    @Override
                    public void onSuccess(User user) {
                        navigator.finish(Activity.RESULT_OK);
                    }

                    @Override
                    public void onError(Throwable error) {
                        Timber.e(error, "failed to save profile with error:");
                        view.showMessage(R.string.toast_error_profile);
                    }
                })
        );
    }
}