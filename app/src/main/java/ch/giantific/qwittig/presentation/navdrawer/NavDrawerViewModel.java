/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.navdrawer;

import android.databinding.Bindable;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.AdapterView;

import java.util.List;

import ch.giantific.qwittig.domain.models.parse.Group;
import ch.giantific.qwittig.domain.models.parse.Identity;
import ch.giantific.qwittig.presentation.common.viewmodels.ViewModel;

/**
 * Created by fabio on 12.01.16.
 */
public interface NavDrawerViewModel extends ViewModel {

    @Bindable
    String getIdentityNickname();

    @Bindable
    byte[] getIdentityAvatar();

    @Bindable
    int getSelectedIdentity();

    void notifySelectedGroupChanged();

    boolean isUserLoggedIn();

    void onNavDrawerReady();

    void onLoginSuccessful();

    void onLogout();

    void onIdentityChanged();

    void onProfileUpdated();

    void onIdentitySelected(@NonNull AdapterView<?> parent, View view, int position, long id);

    void onAvatarClick(View view);

    interface ViewListener extends ViewModel.ViewListener {
        void bindHeaderView();

        void setupHeaderIdentitySelection(@NonNull List<Identity> identities);

        void notifyHeaderIdentitiesChanged();

        void startProfileSettingsActivity();

        void startHomeActivityAndFinish();

        void onIdentitySelected();
    }
}
