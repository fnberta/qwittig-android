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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ch.berta.fabio.fabprogress.ProgressFinalAnimationListener;
import ch.giantific.qwittig.BR;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.bus.RxBus;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.models.User;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.viewmodels.ViewModelBaseImpl;
import ch.giantific.qwittig.presentation.settings.profile.UnlinkThirdPartyWorker.ProfileAction;
import ch.giantific.qwittig.utils.Utils;
import rx.Single;
import rx.SingleSubscriber;
import rx.Subscriber;
import rx.functions.Func1;

/**
 * Provides an implementation of the {@link SettingsProfileViewModel}.
 */
public class SettingsProfileViewModelImpl extends ViewModelBaseImpl<SettingsProfileViewModel.ViewListener>
        implements SettingsProfileViewModel {

    private static final String STATE_VALIDATE = "STATE_VALIDATE";
    private static final String STATE_IS_SAVING = "STATE_IS_SAVING";
    private static final String STATE_UNLINK_THIRD_PARTY = "STATE_UNLINK_THIRD_PARTY";
    private static final String STATE_AVATAR = "STATE_AVATAR";
    private static final String STATE_EMAIL = "STATE_EMAIL";
    private static final String STATE_NICKNAME = "STATE_NICKNAME";
    private static final String STATE_PASSWORD = "STATE_PASSWORD";
    private static final String STATE_PASSWORD_REPEAT = "STATE_PASSWORD_REPEAT";
    private final boolean mFacebookUser;
    private final boolean mGoogleUser;
    private final List<String> mGroupNicknames = new ArrayList<>();
    private final Navigator mNavigator;
    private boolean mValidate;
    private boolean mSaving;
    private boolean mAnimStop;
    private boolean mUnlinkThirdParty;
    private String mAvatar;
    private String mEmail;
    private String mNickname;
    private String mPassword;
    private String mPasswordRepeat;

    public SettingsProfileViewModelImpl(@Nullable Bundle savedState,
                                        @NonNull Navigator navigator,
                                        @NonNull RxBus<Object> eventBus,
                                        @NonNull UserRepository userRepository) {
        super(savedState, eventBus, userRepository);

        mNavigator = navigator;
        mFacebookUser = mCurrentUser.isFacebookUser();
        mGoogleUser = mCurrentUser.isGoogleUser();

        if (savedState != null) {
            mValidate = savedState.getBoolean(STATE_VALIDATE);
            mSaving = savedState.getBoolean(STATE_IS_SAVING);
            mUnlinkThirdParty = savedState.getBoolean(STATE_UNLINK_THIRD_PARTY);
            mEmail = savedState.getString(STATE_EMAIL);
            mPassword = savedState.getString(STATE_PASSWORD);
            mPasswordRepeat = savedState.getString(STATE_PASSWORD_REPEAT);
            mAvatar = savedState.getString(STATE_AVATAR);
            mNickname = savedState.getString(STATE_NICKNAME);
        } else {
            mEmail = mCurrentUser.getUsername();
            mPassword = "";
            mPasswordRepeat = "";
            mAvatar = mCurrentIdentity.getAvatarUrl();
            mNickname = mCurrentIdentity.getNickname();
        }
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);

        outState.putBoolean(STATE_VALIDATE, mValidate);
        outState.putBoolean(STATE_IS_SAVING, mSaving);
        outState.putBoolean(STATE_UNLINK_THIRD_PARTY, mUnlinkThirdParty);
        outState.putString(STATE_AVATAR, mAvatar);
        outState.putString(STATE_EMAIL, mEmail);
        outState.putString(STATE_NICKNAME, mNickname);
        outState.putString(STATE_PASSWORD, mPassword);
        outState.putString(STATE_PASSWORD_REPEAT, mPasswordRepeat);
    }

    @Override
    @Bindable
    public boolean isSaving() {
        return mSaving;
    }

    @Override
    @Bindable
    public boolean isAnimStop() {
        return mAnimStop;
    }

    @Override
    public void startSaving() {
        mSaving = true;
        notifyPropertyChanged(BR.saving);
    }

    @Override
    public void stopSaving(boolean anim) {
        mSaving = false;
        mAnimStop = anim;
        notifyPropertyChanged(BR.saving);
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
        return !mFacebookUser && !mGoogleUser || mUnlinkThirdParty;
    }

    @Override
    public boolean showUnlinkFacebook() {
        return mFacebookUser && !mUnlinkThirdParty;
    }

    @Override
    public boolean showUnlinkGoogle() {
        return mGoogleUser && !mUnlinkThirdParty;
    }

    @Override
    public void onViewVisible() {
        super.onViewVisible();

        getSubscriptions().add(mUserRepo.getIdentities(mCurrentIdentity.getGroup(), true)
                .filter(new Func1<Identity, Boolean>() {
                    @Override
                    public Boolean call(Identity identity) {
                        return !Objects.equals(mCurrentIdentity.getObjectId(), identity.getObjectId());
                    }
                })
                .map(new Func1<Identity, String>() {
                    @Override
                    public String call(Identity identity) {
                        return identity.getNickname();
                    }
                })
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {
                        // do nothing
                    }

                    @Override
                    public void onError(Throwable e) {
                        // do nothing
                    }

                    @Override
                    public void onNext(String nickname) {
                        mGroupNicknames.add(nickname);
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
        mUnlinkThirdParty = true;
        notifyPropertyChanged(BR.emailAndPasswordVisible);
        mView.reloadOptionsMenu();
        mView.showSetPasswordMessage(R.string.toast_unlink_password_required);
    }

    @Override
    public void onExitClick() {
        if (mSaving) {
            mView.showMessage(R.string.toast_saving_profile);
            return;
        }

        if (changesWereMade()) {
            mView.showDiscardChangesDialog();
        } else {
            mNavigator.finish(Activity.RESULT_CANCELED);
        }
    }

    private boolean changesWereMade() {
        return !Objects.equals(mEmail, mCurrentUser.getUsername())
                || !Objects.equals(mNickname, mCurrentIdentity.getNickname())
                || !Objects.equals(mAvatar, mCurrentIdentity.getAvatarUrl())
                || !TextUtils.isEmpty(mPassword);
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

        if (mUnlinkThirdParty) {
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
        if (mSaving || !validate()) {
            return;
        }

        if (mGroupNicknames.contains(mNickname)) {
            mView.showMessage(R.string.toast_profile_nickname_taken);
            return;
        }

        mCurrentUser.setUsername(mEmail);
        mCurrentUser.setPassword(mPassword);

        final List<Identity> identities = mCurrentUser.getIdentities();
        final boolean emptyAvatar = TextUtils.isEmpty(mAvatar);
        final boolean newAvatar = !emptyAvatar && !Objects.equals(mAvatar, mCurrentIdentity.getAvatarUrl());
        if (newAvatar) {
            getSubscriptions().add(mUserRepo.saveCurrentUserIdentitiesWithAvatar(mNickname, mAvatar)
                    .subscribe(new Subscriber<Identity>() {
                        @Override
                        public void onCompleted() {
                            if (!mUnlinkThirdParty) {
                                mNavigator.finish(Activity.RESULT_OK);
                            } else {
                                handleThirdParty();
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            mView.showMessage(R.string.toast_error_profile);
                        }

                        @Override
                        public void onNext(Identity identity) {
                            // do nothing
                        }
                    })
            );
        } else {
            for (Identity identity : identities) {
                identity.setNickname(mNickname);
                if (emptyAvatar) {
                    identity.removeAvatar();
                }
            }

            if (!mUnlinkThirdParty) {
                mCurrentUser.saveEventually();
                mNavigator.finish(Activity.RESULT_OK);
            } else {
                handleThirdParty();
            }
        }
    }

    @Override
    public ProgressFinalAnimationListener getProgressFinalAnimationListener() {
        return new ProgressFinalAnimationListener() {
            @Override
            public void onProgressFinalAnimationComplete() {
                mNavigator.finish(Activity.RESULT_OK);
            }
        };
    }

    private boolean validate() {
        setValidate(true);
        return isEmailValid() && isPasswordValid() && isPasswordEqual() && isNicknameComplete();
    }

    private void handleThirdParty() {
        if (mGoogleUser) {
            startSaving();
            mView.loadUnlinkThirdPartyWorker(ProfileAction.UNLINK_GOOGLE);
        } else if (mFacebookUser) {
            startSaving();
            mView.loadUnlinkThirdPartyWorker(ProfileAction.UNLINK_FACEBOOK);
        }
    }

    @Override
    public void setUnlinkActionStream(@NonNull Single<User> single, @NonNull final String workerTag,
                                      @ProfileAction int action) {
        getSubscriptions().add(single.subscribe(new SingleSubscriber<User>() {
                    @Override
                    public void onSuccess(User value) {
                        mView.removeWorker(workerTag);
                        stopSaving(true);
                    }

                    @Override
                    public void onError(Throwable error) {
                        mView.removeWorker(workerTag);
                        stopSaving(false);

                        mView.showMessage(R.string.toast_error_unlink_failed);
                    }
                })
        );
    }
}
