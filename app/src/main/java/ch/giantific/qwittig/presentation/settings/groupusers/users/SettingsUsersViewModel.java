/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.groupusers.users;

import android.databinding.Bindable;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.view.View;

import ch.giantific.qwittig.presentation.common.viewmodels.ListViewModel;
import ch.giantific.qwittig.presentation.settings.groupusers.users.models.SettingsUsersUserRowViewModel;

/**
 * Defines an observable view model for the manage users settings screen.
 */
public interface SettingsUsersViewModel extends ListViewModel<SettingsUsersUserRowViewModel, SettingsUsersViewModel.ViewListener>, AddUserWorkerListener,
        SettingsUsersRecyclerAdapter.AdapterInteractionListener, NicknamePromptDialogFragment.DialogInteractionListener {

    @Bindable
    String getGroupName();

    @Bindable
    String getNickname();

    void setNickname(@NonNull String nickname);

    @Bindable
    boolean isNicknameComplete();

    @Bindable
    boolean isValidate();

    void setValidate(boolean validate);

    @Bindable
    boolean isGroupEmpty();

    void onNewAvatarTaken(@NonNull String avatarPath);

    void onNicknameChanged(CharSequence s, int start, int before, int count);

    void onAddUserClick(View view);

    /**
     * Defines the interaction with the attached view.
     */
    interface ViewListener extends ListViewModel.ViewListener {

        void loadAddUserWorker(@NonNull String nickname, @NonNull String groupId,
                               @NonNull String groupName);

        void loadLinkShareOptions(@NonNull String link);

        void showProgressDialog(@StringRes int message);

        void hideProgressDialog();

        void showChangeNicknameDialog(@NonNull String nickname, int position);
    }
}
