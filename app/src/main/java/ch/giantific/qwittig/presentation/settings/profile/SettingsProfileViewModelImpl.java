/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.profile;

import android.app.Activity;
import android.databinding.Bindable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.SaveCallback;

import ch.giantific.qwittig.BR;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.domain.models.parse.User;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.common.viewmodels.ViewModelBaseImpl;
import ch.giantific.qwittig.presentation.settings.profile.UnlinkThirdPartyWorker.UnlinkAction;
import ch.giantific.qwittig.utils.Utils;
import rx.Single;
import rx.SingleSubscriber;
import rx.functions.Func1;

/**
 * Created by fabio on 10.02.16.
 */
public class SettingsProfileViewModelImpl extends ViewModelBaseImpl<SettingsProfileViewModel.ViewListener>
        implements SettingsProfileViewModel {

    private static final String STATE_IS_SAVING = "STATE_IS_SAVING";
    private static final String STATE_UNLINK_THIRD_PARTY = "STATE_UNLINK_THIRD_PARTY";
    private boolean mSaving;
    private boolean mUnlinkThirdParty;

    private boolean mFacebookUser;
    private boolean mGoogleUser;

    private String mAvatar;
    private String mEmail;
    private String mNickname;
    private String mPassword;
    private String mPasswordRepeat;

    private Uri mAvatarNew;

    public SettingsProfileViewModelImpl(@Nullable Bundle savedState,
                                        @NonNull SettingsProfileViewModel.ViewListener view,
                                        @NonNull UserRepository userRepository) {
        super(savedState, view, userRepository);

        mEmail = mCurrentUser.getEmail();
        mFacebookUser = mCurrentUser.isFacebookUser();
        mGoogleUser = mCurrentUser.isGoogleUser();
        mNickname = mCurrentIdentity.getNickname();
        mAvatar = mCurrentIdentity.getAvatarUrl();

        if (savedState != null) {
            mSaving = savedState.getBoolean(STATE_IS_SAVING);
            mUnlinkThirdParty = savedState.getBoolean(STATE_UNLINK_THIRD_PARTY);
        }
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);

        outState.putBoolean(STATE_UNLINK_THIRD_PARTY, mUnlinkThirdParty);
        outState.putBoolean(STATE_IS_SAVING, mSaving);
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
        return Utils.isPasswordValid(mPassword);
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

    @Override
    public void onPickAvatarMenuClick() {
        mView.showAvatarPicker();
    }

    @Override
    public void onNewAvatarTaken(@NonNull Uri avatar) {
        mAvatarNew = avatar;
        setAvatar(avatar.toString());
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
        // TODO: check for avatar changes, maybe create ParseFile after avatar is taken
        // TODO: fix null checks
        return mEmail != null && !mEmail.equals(mCurrentUser.getEmail())
                || mNickname != null && !mNickname.equals(mCurrentIdentity.getNickname())
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
    }

    @Override
    public void onNicknameChanged(CharSequence s, int start, int before, int count) {
        mNickname = s.toString();
    }

    @Override
    public void onPasswordChanged(CharSequence s, int start, int before, int count) {
        mPassword = s.toString();

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
    }

    @Override
    public void onFabSaveChangesClick() {
        if (mSaving) {
            return;
        }

        boolean valuesChanged = false;
        if (isEmailValid()) {
            mCurrentUser.setUsername(mEmail);
            valuesChanged = true;
        }
        if (isNicknameComplete()) {
            // TODO: set for all identities
            mCurrentIdentity.setNickname(mNickname);
            valuesChanged = true;
        }
        if (isPasswordValid() && isPasswordEqual()) {
            mCurrentUser.setPassword(mPassword);
            valuesChanged = true;
        }
        if (TextUtils.isEmpty(mAvatar)) {
            mCurrentIdentity.removeAvatar();
            valuesChanged = true;
        }

        if (valuesChanged) {
            if (mUnlinkThirdParty) {
                if (mGoogleUser) {
                    mSaving = true;
                    mView.startSaveAnim();
                    mView.loadUnlinkThirdPartyWorker(UnlinkAction.UNLINK_GOOGLE);
                } else if (mFacebookUser) {
                    mSaving = true;
                    mView.startSaveAnim();
                    mView.loadUnlinkThirdPartyWorker(UnlinkAction.UNLINK_FACEBOOK);
                }

                return;
            }

            if (mAvatarNew != null) {
                mSubscriptions.add(mView.encodeAvatar(mAvatarNew)
                        .flatMap(new Func1<byte[], Single<ParseFile>>() {
                            @Override
                            public Single<ParseFile> call(byte[] bytes) {
                                final ParseFile avatar = new ParseFile(bytes);
                                return Single.create(new Single.OnSubscribe<ParseFile>() {
                                    @Override
                                    public void call(final SingleSubscriber<? super ParseFile> singleSubscriber) {
                                        avatar.saveInBackground(new SaveCallback() {
                                            @Override
                                            public void done(ParseException e) {
                                                if (singleSubscriber.isUnsubscribed()) {
                                                    return;
                                                }

                                                if (e!= null) {
                                                    singleSubscriber.onError(e);
                                                } else {
                                                    singleSubscriber.onSuccess(avatar);
                                                }
                                            }
                                        });
                                    }
                                });
                            }
                        })
                        .subscribe(new SingleSubscriber<ParseFile>() {
                            @Override
                            public void onSuccess(ParseFile avatar) {
                                mCurrentIdentity.setAvatar(avatar);

                                mCurrentIdentity.saveEventually();
                                mCurrentUser.saveEventually();
                                mView.finishScreen(Activity.RESULT_OK);
                            }

                            @Override
                            public void onError(Throwable error) {
                                // TODO: handle error
                            }
                        })
                );
            } else {
                mCurrentIdentity.saveEventually();
                mCurrentUser.saveEventually();
                mView.finishScreen(Activity.RESULT_OK);
            }
        }
    }

    @Override
    public void setUnlinkStream(@NonNull Single<User> single, @NonNull final String workerTag) {
        mSubscriptions.add(single.subscribe(new SingleSubscriber<User>() {
                    @Override
                    public void onSuccess(User value) {
                        mView.removeWorker(workerTag);

                        mSaving = false;
                        mView.showSaveFinishedAnim();
                    }

                    @Override
                    public void onError(Throwable error) {
                        mView.removeWorker(workerTag);

                        mSaving = false;
                        mView.stopSaveAnim();
                        mView.showMessage(mUserRepo.getErrorMessage(error));
                    }
                })
        );
    }
}
