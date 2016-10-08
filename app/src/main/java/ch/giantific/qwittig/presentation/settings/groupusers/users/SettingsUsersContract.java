/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.groupusers.users;

import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.view.View;

import ch.giantific.qwittig.presentation.common.listadapters.interactions.ListInteraction;
import ch.giantific.qwittig.presentation.common.presenters.BasePresenter;
import ch.giantific.qwittig.presentation.common.presenters.BaseViewListener;
import ch.giantific.qwittig.presentation.common.presenters.ListPresenter;
import ch.giantific.qwittig.presentation.settings.groupusers.users.viewmodels.SettingsUsersViewModel;
import ch.giantific.qwittig.presentation.settings.groupusers.users.viewmodels.items.SettingsUsersItemViewModel;

/**
 * Defines an observable view model for the manage users settings screen.
 */
public interface SettingsUsersContract {

    interface Presenter extends BasePresenter<ViewListener>,
            ListPresenter<SettingsUsersItemViewModel>,
            SettingsUsersRecyclerAdapter.AdapterInteractionListener,
            NicknamePromptDialogFragment.DialogInteractionListener {

        SettingsUsersViewModel getViewModel();

        void setListInteraction(@NonNull ListInteraction listInteraction);

        void onNewAvatarTaken(@NonNull String avatarPath);

        void onAddUserClick(View view);
    }

    interface ViewListener extends BaseViewListener {

        void startEnterTransition();

        void loadLinkShareOptions(@NonNull String link);

        void showProgressDialog(@StringRes int message);

        void hideProgressDialog();

        void showChangeNicknameDialog(@NonNull String nickname, int position);

        String getGoogleApiKey();
    }
}
