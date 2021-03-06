/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.login.invitation;

import android.view.View;

import ch.giantific.qwittig.presentation.common.presenters.BasePresenter;
import ch.giantific.qwittig.presentation.common.views.BaseView;

/**
 * Defines an observable view model for the login/sign-up with email screen.
 */
public interface LoginInvitationContract {

    interface Presenter extends BasePresenter<ViewListener> {

        void onAcceptClick(View view);

        void onDeclineClick(View view);
    }

    interface ViewListener extends BaseView {
        void showAccountsLogin(boolean accept);
    }
}
