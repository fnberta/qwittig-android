/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.general;

import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import ch.giantific.qwittig.presentation.common.delegates.GoogleApiClientDelegate;
import ch.giantific.qwittig.presentation.common.dialogs.EmailReAuthenticateDialogFragment;
import ch.giantific.qwittig.presentation.common.presenters.BasePresenter;
import ch.giantific.qwittig.presentation.common.presenters.BaseViewListener;
import ch.giantific.qwittig.presentation.common.workers.EmailUserWorkerListener;
import ch.giantific.qwittig.presentation.common.workers.FacebookUserWorkerListener;
import ch.giantific.qwittig.presentation.common.workers.GoogleUserWorkerListener;

/**
 * Defines an observable view model for the main settings screen.
 */
public interface SettingsContract {

    interface Presenter extends BasePresenter<ViewListener>,
            LeaveGroupDialogFragment.DialogInteractionListener,
            EmailUserWorkerListener,
            GoogleUserWorkerListener,
            FacebookUserWorkerListener,
            DeleteAccountDialogFragment.DialogInteractionListener,
            EmailReAuthenticateDialogFragment.DialogInteractionListener,
            GoogleApiClientDelegate.GoogleLoginCallback {

        void onPreferencesLoaded();

        void onGroupSelected(@NonNull String identityId);

        void onGroupNameChanged(@NonNull String newName);

        void onProfileClick();

        void onAddGroupClick();

        void onUsersClick();

        void onLeaveGroupClick();

        void onLogoutMenuClick();

        void onDeleteAccountMenuClick();

        void onFacebookSignedIn(@NonNull String token);

        void onFacebookLoginFailed();
    }

    interface ViewListener extends BaseViewListener {

        void startEnterTransition();

        void setupGroupSelection(@NonNull CharSequence[] entries, @NonNull CharSequence[] values,
                                 @NonNull String selectedValue);

        void setCurrentGroupTitle(@NonNull String title);

        void setChangeGroupNameText(@NonNull String text);

        void setLeaveGroupTitle(@StringRes int message, @NonNull String groupName);

        void loadGoogleUserWorker();

        void loadDeleteGoogleUserWorker(@NonNull String idToken);

        void loadDeleteFacebookUserWorker(@NonNull String token);

        void loadDeleteEmailUserWorker(@NonNull String currentEmail,
                                       @NonNull String currentPassword);

        void showLeaveGroupDialog(@StringRes int message);

        void showDeleteAccountDialog();

        void showProgressDialog(@StringRes int message);

        void hideProgressDialog();

        void reAuthenticateGoogle();

        void showEmailReAuthenticateDialog(@Nullable String email);

        void reAuthenticateFacebook();
    }

    @IntDef({Result.LOGOUT, Result.GROUP_SELECTED, Result.GROUPS_CHANGED})
    @Retention(RetentionPolicy.SOURCE)
    @interface Result {
        int LOGOUT = 2;
        int GROUP_SELECTED = 3;
        int GROUPS_CHANGED = 4;
    }
}
