/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.login.invitation;

import android.view.View;

import ch.giantific.qwittig.presentation.common.presenters.BasePresenter;
import ch.giantific.qwittig.presentation.common.presenters.BaseViewListener;

/**
 * Defines an observable view model for the login/sign-up with email screen.
 */
public interface LoginInvitationContract {

    interface Presenter extends BasePresenter<ViewListener> {

        LoginInvitationViewModel getViewModel();

        void onAcceptClick(View view);

        void onDeclineClick(View view);
    }

    interface ViewListener extends BaseViewListener {
        void showAccountsLogin(boolean accept);
    }
}
