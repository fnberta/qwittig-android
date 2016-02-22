/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.profile;

import android.app.Activity;
import android.databinding.Bindable;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import ch.giantific.qwittig.presentation.common.fragments.DiscardChangesDialogFragment;
import ch.giantific.qwittig.presentation.common.viewmodels.ViewModel;
import ch.giantific.qwittig.presentation.settings.profile.SettingsProfileWorker.ProfileAction;
import rx.Single;

/**
 * Defines a observable view model for profile settings screen.
 */
public interface SettingsProfileViewModel extends ViewModel, SettingsProfileWorkerListener,
        AvatarLoadListener,
        DiscardChangesDialogFragment.DialogInteractionListener {

    @Bindable
    boolean isValidate();

    void setValidate(boolean validate);

    @Bindable
    String getAvatar();

    void setAvatar(@NonNull String avatarUrl);

    @Bindable
    String getNickname();

    void setNickname(@NonNull String nickname);

    @Bindable
    String getEmail();

    void setEmail(@NonNull String email);

    @Bindable
    boolean isNicknameComplete();

    @Bindable
    boolean isEmailValid();

    @Bindable
    boolean isPasswordValid();

    @Bindable
    boolean isPasswordEqual();

    @Bindable
    boolean isEmailAndPasswordVisible();

    boolean showUnlinkFacebook();

    boolean showUnlinkGoogle();

    void onPickAvatarMenuClick();

    void onNewAvatarTaken(@NonNull String avatar);

    void onDeleteAvatarMenuClick();

    void onUnlinkThirdPartyLoginMenuClick();

    void onUpOrBackClick();

    void onSaveAnimFinished();

    void onEmailChanged(CharSequence s, int start, int before, int count);

    void onNicknameChanged(CharSequence s, int start, int before, int count);

    void onPasswordChanged(CharSequence s, int start, int before, int count);

    void onPasswordRepeatChanged(CharSequence s, int start, int before, int count);

    void onFabSaveChangesClick();

    @IntDef({Activity.RESULT_OK, Activity.RESULT_CANCELED, Result.CHANGES_DISCARDED})
    @Retention(RetentionPolicy.SOURCE)
    @interface Result {
        int CHANGES_DISCARDED = 2;
    }

    /**
     * Defines the interaction with the attached view.
     */
    interface ViewListener extends ViewModel.ViewListener {

        void startPostponedEnterTransition();

        void loadSaveAvatarWorker(@NonNull String nickname, @NonNull byte[] avatar);

        void loadUnlinkThirdPartyWorker(@ProfileAction int unlinkAction);

        void showDiscardChangesDialog();

        void showAvatarPicker();

        void showSetPasswordMessage(@StringRes int message);

        void dismissSetPasswordMessage();

        Single<byte[]> encodeAvatar(@NonNull String avatar);

        void reloadOptionsMenu();

        void startSaveAnim();

        void stopSaveAnim();

        void showSaveFinishedAnim();

        void finishScreen(@Result int result);
    }
}
