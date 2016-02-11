/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.profile;

import android.app.Activity;
import android.databinding.Bindable;
import android.net.Uri;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import ch.giantific.qwittig.presentation.common.fragments.DiscardChangesDialogFragment;
import ch.giantific.qwittig.presentation.common.viewmodels.ViewModel;
import ch.giantific.qwittig.presentation.settings.profile.UnlinkThirdPartyWorker.UnlinkAction;
import rx.Single;

/**
 * Created by fabio on 10.02.16.
 */
public interface SettingsProfileViewModel extends ViewModel, UnlinkThirdPartyWorkerListener,
        DiscardChangesDialogFragment.DialogInteractionListener {

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

    void onNewAvatarTaken(@NonNull Uri avatar);

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

    interface ViewListener extends ViewModel.ViewListener {
        void loadUnlinkThirdPartyWorker(@UnlinkAction int unlinkAction);

        void showDiscardChangesDialog();

        void showAvatarPicker();

        void showSetPasswordMessage(@StringRes int message);

        void dismissSetPasswordMessage();

        Single<byte[]> encodeAvatar(@NonNull Uri avatarUri);

        void reloadOptionsMenu();

        void startSaveAnim();

        void stopSaveAnim();

        void showSaveFinishedAnim();

        void finishScreen(@Result int result);
    }
}
