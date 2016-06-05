/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.login;

import android.databinding.Bindable;
import android.support.annotation.NonNull;
import android.view.View;

import ch.giantific.qwittig.presentation.common.viewmodels.LoadingViewModel;
import ch.giantific.qwittig.presentation.common.viewmodels.ViewModel;
import rx.Single;

/**
 * Defines an observable view model for the login/sign-up with email screen.
 */
public interface LoginProfileViewModel extends ViewModel {

    @Bindable
    boolean isValidate();

    void setValidate(boolean validate);

    @Bindable
    String getAvatar();

    void setAvatar(@NonNull String avatar);

    @Bindable
    String getNickname();

    @Bindable
    boolean isNicknameComplete();

    void onNewAvatarTaken(@NonNull String avatarPath);

    void onAvatarClick(View view);

    void onNicknameChanged(CharSequence s, int start, int before, int count);

    void onFabDoneClick(View view);

    /**
     * Defines the interaction with the attached view.
     */
    interface ViewListener extends ViewModel.ViewListener {
        void finishScreen(int result);

        void showFirstGroupFragment();

        void showAvatarPicker();
    }
}
