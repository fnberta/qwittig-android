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

import ch.giantific.qwittig.presentation.common.GoogleApiClientDelegate;
import ch.giantific.qwittig.presentation.common.viewmodels.ViewModel;
import ch.giantific.qwittig.presentation.common.workers.EmailUserWorkerListener;
import ch.giantific.qwittig.presentation.common.workers.GoogleUserWorkerListener;
import ch.giantific.qwittig.presentation.common.fragments.dialogs.EmailReAuthenticateDialogFragment;

/**
 * Defines an observable view model for the main settings screen.
 */
public interface SettingsViewModel extends ViewModel<SettingsViewModel.ViewListener>,
        LeaveGroupDialogFragment.DialogInteractionListener,
        EmailUserWorkerListener, GoogleUserWorkerListener,
        DeleteAccountDialogFragment.DialogInteractionListener,
        EmailReAuthenticateDialogFragment.DialogInteractionListener,
        GoogleApiClientDelegate.GoogleLoginCallback {

    void onPreferencesLoaded();

    void onGroupSelected(@NonNull String identityId);

    void onGroupNameChanged(@NonNull String newName);

    void onLeaveGroupClick();

    void onLogoutMenuClick();

    void onDeleteAccountMenuClick();

    @IntDef({Result.LOGOUT, Result.GROUP_SELECTED, Result.GROUPS_CHANGED})
    @Retention(RetentionPolicy.SOURCE)
    @interface Result {
        int LOGOUT = 2;
        int GROUP_SELECTED = 3;
        int GROUPS_CHANGED = 4;
    }

    /**
     * Defines the interaction with the attached view.
     */
    interface ViewListener extends ViewModel.ViewListener {

        void setupGroupSelection(@NonNull CharSequence[] entries, @NonNull CharSequence[] values,
                                 @NonNull String selectedValue);

        void setCurrentGroupTitle(@NonNull String title);

        void setChangeGroupNameText(@NonNull String text);

        void setLeaveGroupTitle(@StringRes int message, @NonNull String groupName);

        void loadGoogleUserWorker();

        void loadDeleteGoogleUserWorker(@NonNull String idToken);

        void loadDeleteEmailUserWorker(@NonNull String currentEmail,
                                       @NonNull String currentPassword);

        void showLeaveGroupDialog(@StringRes int message);

        void showDeleteAccountDialog();

        void showProgressDialog(@StringRes int message);

        void hideProgressDialog();

        void reAuthenticateGoogle();

        void showEmailReAuthenticateDialog(@Nullable String email);
    }
}
