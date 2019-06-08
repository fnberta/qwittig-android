/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.login.profile;

import android.support.annotation.NonNull;
import android.view.View;

import ch.giantific.qwittig.presentation.common.presenters.BasePresenter;
import ch.giantific.qwittig.presentation.common.views.BaseView;

/**
 * Defines an observable view model for the login/sign-up with email screen.
 */
public interface LoginProfileContract {

    interface Presenter extends BasePresenter<ViewListener> {

        void setWithInvitation(boolean withInvitation);

        void onNewAvatarTaken(@NonNull String avatarPath);

        void onAvatarClick(View view);

        void onDoneClick(View view);
    }

    interface ViewListener extends BaseView {

        void showFirstGroupAdjust();
    }
}
