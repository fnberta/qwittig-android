/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.profile;

import android.app.Activity;
import android.databinding.Bindable;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.view.View;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import ch.giantific.qwittig.presentation.common.GoogleApiClientDelegate;
import ch.giantific.qwittig.presentation.common.fragments.dialogs.DiscardChangesDialogFragment;
import ch.giantific.qwittig.presentation.common.fragments.dialogs.EmailReAuthenticateDialogFragment;
import ch.giantific.qwittig.presentation.common.viewmodels.ViewModel;
import ch.giantific.qwittig.presentation.common.workers.EmailUserWorkerListener;
import ch.giantific.qwittig.presentation.common.workers.GoogleUserWorkerListener;

/**
 * Defines a observable view model for profile settings screen.
 */
public interface SettingsProfileViewModel extends ViewModel<SettingsProfileViewModel.ViewListener>,
        GoogleUserWorkerListener,
        EmailUserWorkerListener,
        AvatarLoadListener,
        DiscardChangesDialogFragment.DialogInteractionListener,
        EmailReAuthenticateDialogFragment.DialogInteractionListener,
        GoogleApiClientDelegate.GoogleLoginCallback {

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

    void onExitClick();

    void onEmailChanged(CharSequence s, int start, int before, int count);

    void onNicknameChanged(CharSequence s, int start, int before, int count);

    void onPasswordChanged(CharSequence s, int start, int before, int count);

    void onPasswordRepeatChanged(CharSequence s, int start, int before, int count);

    void onFabSaveProfileClick(View view);

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

        void loadUnlinkGoogleWorker(@NonNull String email, @NonNull String password,
                                    @NonNull String idToken);

        void loadUnlinkFacebookWorker();

        void showDiscardChangesDialog();

        void showSetPasswordMessage(@StringRes int message);

        void dismissSetPasswordMessage();

        void reloadOptionsMenu();

        void showProgressDialog(@StringRes int message);

        void hideProgressDialog();

        void showReAuthenticateDialog(@NonNull String currentEmail);

        void loadChangeEmailPasswordWorker(@NonNull String currentEmail,
                                           @NonNull String currentPassword,
                                           @Nullable String newEmail,
                                           @Nullable String newPassword);

        void reAuthenticateGoogle();
    }
}
