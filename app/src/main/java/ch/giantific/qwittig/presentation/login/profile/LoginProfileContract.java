/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.login.profile;

import android.support.annotation.NonNull;
import android.view.View;

import ch.giantific.qwittig.presentation.common.presenters.BasePresenter;
import ch.giantific.qwittig.presentation.common.presenters.BaseViewListener;

/**
 * Defines an observable view model for the login/sign-up with email screen.
 */
public interface LoginProfileContract {

    interface Presenter extends BasePresenter<ViewListener> {

        LoginProfileViewModel getViewModel();

        void setWithInvitation(boolean withInvitation);

        void onNewAvatarTaken(@NonNull String avatarPath);

        void onAvatarClick(View view);

        void onNicknameChanged(CharSequence s, int start, int before, int count);

        void onDoneClick(View view);
    }

    interface ViewListener extends BaseViewListener {

        void showFirstGroupScreen();
    }
}
