/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.navdrawer;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import ch.giantific.qwittig.BR;

/**
 * Provides an implementation of the {@link NavDrawerContract}.
 */
public class NavDrawerViewModel extends BaseObservable {

    private String nickname;
    private String avatar;
    private int selectedIdentity;

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
