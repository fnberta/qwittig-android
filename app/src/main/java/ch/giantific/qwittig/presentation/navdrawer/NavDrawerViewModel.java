/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.navdrawer;

import android.databinding.Bindable;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.AdapterView;

import java.util.List;

import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.presentation.common.SpinnerInteraction;
import ch.giantific.qwittig.presentation.common.viewmodels.ViewModel;

/**
 * Defines an observable view model for the navigation drawer.
 */
public interface NavDrawerViewModel extends ViewModel<NavDrawerViewModel.ViewListener> {

    void setSpinnerInteraction(@NonNull SpinnerInteraction spinnerInteraction);

    @Bindable
    String getNickname();

    void setNickname(@NonNull String nickname);

    @Bindable
    String getAvatar();

    void setAvatar(@NonNull String avatar);

    @Bindable
    int getSelectedIdentity();

    List<Identity> getIdentities();

    boolean isUserLoggedIn();

    void afterLogout();

    void onIdentitySelected(@NonNull AdapterView<?> parent, View view, int position, long id);

    void onAvatarClick(View view);

    /**
     * Defines the interaction with the attached view.
     */
    interface ViewListener extends ViewModel.ViewListener {

    }
}
