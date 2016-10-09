/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.profile;

import android.app.Activity;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.view.View;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import ch.giantific.qwittig.presentation.common.delegates.GoogleApiClientDelegate;
import ch.giantific.qwittig.presentation.common.dialogs.DiscardChangesDialogFragment;
import ch.giantific.qwittig.presentation.common.dialogs.EmailReAuthenticateDialogFragment;
import ch.giantific.qwittig.presentation.common.presenters.BasePresenter;
import ch.giantific.qwittig.presentation.common.presenters.BaseViewListener;
import ch.giantific.qwittig.presentation.common.workers.EmailUserWorkerListener;
import ch.giantific.qwittig.presentation.common.workers.FacebookUserWorkerListener;
import ch.giantific.qwittig.presentation.common.workers.GoogleUserWorkerListener;

/**
 * Defines a observable view model for profile settings screen.
 */
public interface SettingsProfileContract {

    interface Presenter extends BasePresenter<ViewListener>,
            GoogleUserWorkerListener,
            EmailUserWorkerListener,
            FacebookUserWorkerListener,
            AvatarLoadListener,
            DiscardChangesDialogFragment.DialogInteractionListener,
            EmailReAuthenticateDialogFragment.DialogInteractionListener,
            GoogleApiClientDelegate.GoogleLoginCallback {

        boolean showDeleteAvatar();

        boolean showUnlinkFacebook();

        boolean showUnlinkGoogle();

        void onPickAvatarMenuClick();

        void onNewAvatarTaken(@NonNull String avatar);

        void onDeleteAvatarMenuClick();

        void onUnlinkThirdPartyLoginMenuClick();

        void onPasswordChanged(CharSequence s, int start, int before, int count);

        void onSaveProfileClick(View view);

        void onFacebookSignedIn(@NonNull String token);

        void onFacebookLoginFailed();

        void onExitClick();
    }

    interface ViewListener extends BaseViewListener {

        void startPostponedEnterTransition();

        void loadUnlinkGoogleWorker(@NonNull String email, @NonNull String password,
                                    @NonNull String idToken);

        void loadUnlinkFacebookWorker(@NonNull String email, @NonNull String password,
                                      @NonNull String idToken);

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

        void reAuthenticateFacebook();
    }

    @IntDef({Activity.RESULT_OK, Activity.RESULT_CANCELED, Result.CHANGES_DISCARDED})
    @Retention(RetentionPolicy.SOURCE)
    @interface Result {
        int CHANGES_DISCARDED = 2;
    }
}
