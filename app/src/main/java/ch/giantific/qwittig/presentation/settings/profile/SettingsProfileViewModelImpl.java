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

import java.util.List;

import ch.giantific.qwittig.BR;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.models.User;
import ch.giantific.qwittig.domain.repositories.IdentityRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.common.viewmodels.ViewModelBaseImpl;
import ch.giantific.qwittig.presentation.settings.profile.SettingsProfileWorker.ProfileAction;
import ch.giantific.qwittig.utils.Utils;
import rx.Single;
import rx.SingleSubscriber;

/**
 * Created by fabio on 10.02.16.
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
    private IdentityRepository mIdentityRepo;
    private boolean mValidate;
    private boolean mSaving;
    private boolean mUnlinkThirdParty;
    private boolean mFacebookUser;
    private boolean mGoogleUser;
    private String mAvatar;
    private String mEmail;
    private String mNickname;
    private String mPassword;
    private String mPasswordRepeat;

    public SettingsProfileViewModelImpl(@Nullable Bundle savedState,
                                        @NonNull SettingsProfileViewModel.ViewListener view,
                                        @NonNull UserRepository userRepository,
                                        @NonNull IdentityRepository identityRepository) {
        super(savedState, view, userRepository);

        mIdentityRepo = identityRepository;
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
        return mPassword.equals(mPasswordRepeat);
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

    public void setSaving(boolean saving) {
        mSaving = saving;
        if (saving) {
            mView.startSaveAnim();
        } else {
            mView.stopSaveAnim();
        }
    }

    @Override
    public void onPickAvatarMenuClick() {
        mView.showAvatarPicker();
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
    public void onUpOrBackClick() {
        if (mSaving) {
            mView.showMessage(R.string.toast_saving_profile);
            return;
        }

        if (changesWereMade()) {
            mView.showDiscardChangesDialog();
        } else {
            mView.finishScreen(Activity.RESULT_CANCELED);
        }
    }

    private boolean changesWereMade() {
        // TODO: check for avatar changes
        return !mEmail.equals(mCurrentUser.getUsername())
                || !mNickname.equals(mCurrentIdentity.getNickname())
                || !TextUtils.isEmpty(mPassword);
    }

    @Override
    public void onDiscardChangesSelected() {
        mView.finishScreen(Result.CHANGES_DISCARDED);
    }

    @Override
    public void onSaveAnimFinished() {
        mView.finishScreen(Activity.RESULT_OK);
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
    public void onFabSaveChangesClick() {
        if (mSaving || !validate()) {
            return;
        }

        mCurrentUser.setUsername(mEmail);
        mCurrentUser.setPassword(mPassword);

        final List<Identity> identities = mCurrentUser.getIdentities();
        final boolean emptyAvatar = TextUtils.isEmpty(mAvatar);
        final boolean newAvatar = !emptyAvatar && !mAvatar.equals(mCurrentIdentity.getAvatarUrl());
        if (newAvatar) {
            setSaving(true);
            getSubscriptions().add(mView.encodeAvatar(mAvatar)
                    .subscribe(new SingleSubscriber<byte[]>() {
                        @Override
                        public void onSuccess(byte[] value) {
                            mView.loadSaveAvatarWorker(mNickname, value);
                        }

                        @Override
                        public void onError(Throwable error) {
                            // TODO: handle error
                        }
                    })
            );
        } else {
            for (Identity identity : identities) {
                identity.setNickname(mNickname);
                if (emptyAvatar) {
                    identity.removeAvatar();
                }
                identity.saveEventually();
            }
        }
    }

    private boolean validate() {
        setValidate(true);
        return isEmailValid() && isPasswordValid() && isPasswordEqual() && isNicknameComplete();
    }

    private void handleThirdParty() {
        if (mGoogleUser) {
            setSaving(true);
            mView.loadUnlinkThirdPartyWorker(ProfileAction.UNLINK_GOOGLE);
        } else if (mFacebookUser) {
            setSaving(true);
            mView.loadUnlinkThirdPartyWorker(ProfileAction.UNLINK_FACEBOOK);
        }
    }

    @Override
    public void setProfileActionStream(@NonNull Single<User> single, @NonNull final String workerTag,
                                       @ProfileAction int action) {
        switch (action) {
            case ProfileAction.SAVE_AVATAR:
                getSubscriptions().add(single.subscribe(new SingleSubscriber<User>() {
                            @Override
                            public void onSuccess(User user) {
                                mView.removeWorker(workerTag);

                                if (!mUnlinkThirdParty) {
                                    mSaving = false;
                                    mView.showSaveFinishedAnim();
                                } else {
                                    handleThirdParty();
                                }
                            }

                            @Override
                            public void onError(Throwable error) {
                                mView.removeWorker(workerTag);
                                mView.showMessage(R.string.toast_error_profile);
                                // TODO: handle error
                            }
                        })
                );
                break;
            case ProfileAction.UNLINK_FACEBOOK:
                // fall through
            case ProfileAction.UNLINK_GOOGLE:
                getSubscriptions().add(single.subscribe(new SingleSubscriber<User>() {
                            @Override
                            public void onSuccess(User value) {
                                mView.removeWorker(workerTag);

                                mSaving = false;
                                mView.showSaveFinishedAnim();
                            }

                            @Override
                            public void onError(Throwable error) {
                                mView.removeWorker(workerTag);

                                setSaving(false);
                                mView.showMessage(mUserRepo.getErrorMessage(error));
                            }
                        })
                );
                break;
        }
    }
}
