/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.users;

import android.support.annotation.NonNull;

import ch.giantific.qwittig.presentation.common.viewmodels.ListViewModel;
import ch.giantific.qwittig.presentation.settings.users.items.SettingsUsersUserItem;
import ch.giantific.qwittig.presentation.settings.users.items.SettingsUsersNicknameItem;
import ch.giantific.qwittig.presentation.settings.users.items.SettingsUsersBaseItem;

/**
 * Defines an observable view model for the manage users settings screen.
 */
public interface SettingsUsersViewModel extends ListViewModel<SettingsUsersBaseItem>, AddUserWorkerListener,
        SettingsUsersNicknameItem.AddListener, SettingsUsersUserItem.ShareListener {

    boolean isItemDismissable(int position);

    void onItemDismiss(int position);

    /**
     * Defines the interaction with the attached view.
     */
    interface ViewListener extends ListViewModel.ViewListener {

        void loadAddUserWorker(@NonNull String nickname, @NonNull String groupId,
                               @NonNull String groupName);

        void loadLinkShareOptions(@NonNull String link);

        void toggleProgressDialog(boolean show);
    }
}
