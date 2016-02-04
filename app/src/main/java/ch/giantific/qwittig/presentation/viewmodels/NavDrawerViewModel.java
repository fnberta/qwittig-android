/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.viewmodels;

import android.databinding.Bindable;
import android.databinding.Observable;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.view.View;
import android.widget.AdapterView;

import com.parse.ParseObject;

import java.util.List;

import ch.giantific.qwittig.domain.models.parse.Group;

/**
 * Created by fabio on 12.01.16.
 */
public interface NavDrawerViewModel extends Observable, ViewInteraction<NavDrawerViewModel.ViewListener> {

    void unsubscribe();

    @Bindable
    String getUserNickname();

    @Bindable
    byte[] getUserAvatar();

    @Bindable
    int getSelectedGroup();

    void notifySelectedGroupChanged();

    boolean isUserLoggedIn();

    void onNavDrawerReady();

    void onLoginSuccessful();

    void onLogout();

    void onGroupChanged();

    void onProfileUpdated();

    void onGroupSelected(@NonNull AdapterView<?> parent, View view, int position, long id);

    void onAvatarClick(View view);

    interface ViewListener {
        void bindHeaderView();

        void showMessage(@StringRes int resId);

        void setupHeaderGroupSelection(@NonNull List<Group> groups);

        void notifyHeaderGroupListChanged();

        void startProfileSettingsActivity();

        void startHomeActivityAndFinish();

        void onNewGroupSet();
    }
}
