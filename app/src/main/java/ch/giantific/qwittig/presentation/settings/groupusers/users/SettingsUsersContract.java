/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.groupusers.users;

import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.view.View;

import ch.giantific.qwittig.presentation.common.presenters.BasePresenter;
import ch.giantific.qwittig.presentation.common.presenters.SortedListPresenter;
import ch.giantific.qwittig.presentation.common.views.BaseView;
import ch.giantific.qwittig.presentation.common.views.SortedListView;
import ch.giantific.qwittig.presentation.settings.groupusers.users.viewmodels.items.SettingsUsersItemViewModel;

/**
 * Defines an observable view model for the manage users settings screen.
 */
public interface SettingsUsersContract {

    interface Presenter extends BasePresenter<ViewListener>,
            SortedListPresenter<SettingsUsersItemViewModel>,
            SettingsUsersRecyclerAdapter.AdapterInteractionListener,
            NicknamePromptDialogFragment.DialogInteractionListener,
            InvitationLinkWorkerListener {

        void onNewAvatarTaken(@NonNull String avatarPath);

        void onAddUserClick(View view);
    }

    interface ViewListener extends BaseView,
            SortedListView<SettingsUsersItemViewModel> {

        void startEnterTransition();

        void loadLinkShareOptions(@NonNull String link);

        void showProgressDialog(@StringRes int message);

        void hideProgressDialog();

        void showChangeNicknameDialog(@NonNull String nickname, int position);

        void loadInvitationLinkWorker(@NonNull final String identityId,
                                      @NonNull String groupName,
                                      @NonNull String inviterNickname);
    }
}
