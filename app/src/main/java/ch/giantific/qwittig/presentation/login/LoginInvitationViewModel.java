/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.login;

import android.databinding.Bindable;
import android.databinding.BindingAdapter;
import android.support.annotation.NonNull;
import android.view.View;

import ch.giantific.qwittig.presentation.common.viewmodels.ViewModel;

/**
 * Defines an observable view model for the login/sign-up with email screen.
 */
public interface LoginInvitationViewModel extends ViewModel<LoginInvitationViewModel.ViewListener> {

    @Bindable
    String getGroupName();

    void setGroupName(@NonNull String groupName);

    @Bindable
    String getInviterNickname();

    void setInviterNickname(@NonNull String inviterNickname);

    void onAcceptClick(View view);

    void onDeclineClick(View view);

    /**
     * Defines the interaction with the attached view.
     */
    interface ViewListener extends ViewModel.ViewListener {
        void showAccountsScreen(boolean accept);
    }
}
