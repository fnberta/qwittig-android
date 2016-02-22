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
import ch.giantific.qwittig.presentation.common.viewmodels.ViewModel;

/**
 * Created by fabio on 12.01.16.
 */
public interface NavDrawerViewModel extends ViewModel {

    @Bindable
    String getIdentityNickname();

    @Bindable
    String getIdentityAvatar();

    @Bindable
    int getSelectedIdentity();

    List<Identity> getIdentities();

    boolean isUserLoggedIn();

    void onLoginSuccessful();

    void onLogout();

    void onProfileUpdated();

    /**
     * Updates the group quick switch with the most recent values.
     */
    void onIdentityChanged();

    /**
     * Sets the group quick switch to the appropriate new selection. Called when the user changes
     * his group in the settings screen.
     */
    void onSettingsIdentitySelected();

    void onIdentitySelected(@NonNull AdapterView<?> parent, View view, int position, long id);

    void onAvatarClick(View view);

    interface ViewListener extends ViewModel.ViewListener {

        void notifyHeaderIdentitiesChanged();

        void startProfileSettingsActivity();

        void startHomeActivityAndFinish();

        void onIdentitySelected();

        void startLoginActivity();
    }
}
