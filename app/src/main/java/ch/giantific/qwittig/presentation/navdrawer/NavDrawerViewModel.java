/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.navdrawer;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.support.annotation.NonNull;

import javax.inject.Inject;

import ch.giantific.qwittig.BR;
import ch.giantific.qwittig.di.scopes.PerActivity;

/**
 * Provides an implementation of the {@link NavDrawerContract}.
 */
@PerActivity
public class NavDrawerViewModel extends BaseObservable {

    private String nickname;
    private String avatar;
    private int selectedIdentity;

    @Inject
    public NavDrawerViewModel() {
    }

    @Bindable
    public String getNickname() {
        return nickname;
    }

    public void setNickname(@NonNull String nickname) {
        this.nickname = nickname;
        notifyPropertyChanged(BR.nickname);
    }

    @Bindable
    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(@NonNull String avatar) {
        this.avatar = avatar;
        notifyPropertyChanged(BR.avatar);
    }

    @Bindable
    public int getSelectedIdentity() {
        return selectedIdentity;
    }

    public void setSelectedIdentity(int selectedIdentity) {
        this.selectedIdentity = selectedIdentity;
        notifyPropertyChanged(BR.selectedIdentity);
    }
}
