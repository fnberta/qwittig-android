/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.addusers;

import android.support.annotation.NonNull;

import ch.giantific.qwittig.presentation.common.viewmodels.ListViewModel;
import ch.giantific.qwittig.presentation.settings.addusers.items.AddUsersItem;
import ch.giantific.qwittig.presentation.settings.addusers.items.NicknameItem;
import ch.giantific.qwittig.presentation.settings.addusers.items.UserItem;

/**
 * Created by fabio on 07.02.16.
 */
public interface SettingsAddUsersViewModel extends ListViewModel<AddUsersItem>, AddUserWorkerListener,
        NicknameItem.AddListener, UserItem.ShareListener {

    interface ViewListener extends ListViewModel.ViewListener {

        void loadAddUserWorker(@NonNull String nickname, @NonNull String groupId,
                               @NonNull String groupName);

        void loadLinkShareOptions(@NonNull String link);

        void toggleProgressDialog(boolean show);
    }
}
