/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.addusers;

import android.support.annotation.NonNull;

import ch.giantific.qwittig.presentation.common.viewmodels.ListViewModel;
import ch.giantific.qwittig.presentation.settings.addusers.listitems.ListItem;
import ch.giantific.qwittig.presentation.settings.addusers.listitems.NicknameItem;
import ch.giantific.qwittig.presentation.settings.addusers.listitems.UserItem;

/**
 * Created by fabio on 07.02.16.
 */
public interface SettingsAddUsersViewModel extends ListViewModel<ListItem>, AddUserWorkerListener,
        NicknameItem.AddListener, UserItem.ShareListener {

    interface ViewListener extends ListViewModel.ViewListener {

        void loadAddUserWorker(@NonNull String nickname, @NonNull String groupId,
                               @NonNull String groupName);

        void loadLinkShareOptions(@NonNull String link);

        void toggleProgressDialog(boolean show);
    }
}
