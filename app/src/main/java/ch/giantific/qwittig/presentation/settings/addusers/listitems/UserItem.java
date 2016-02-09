/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.addusers.listitems;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.support.annotation.NonNull;
import android.view.View;

import ch.giantific.qwittig.BR;

/**
 * Created by fabio on 07.02.16.
 */
public class UserItem extends BaseObservable implements ListItem {

    private ShareListener mShareListener;
    private String mNickname;
    private String mShareLink;

    public UserItem(@NonNull String nickname, @NonNull String shareLink) {
        mNickname = nickname;
        mShareLink = shareLink;
    }

    public void setShareListener(@NonNull ShareListener shareListener) {
        mShareListener = shareListener;
    }

    @Bindable
    public String getNickname() {
        return mNickname;
    }

    public void setNickname(@NonNull String nickname) {
        mNickname = nickname;
        notifyPropertyChanged(BR.nickname);
    }

    public String getShareLink() {
        return mShareLink;
    }

    public void setShareLink(@NonNull String shareLink) {
        mShareLink = shareLink;
    }

    public void onShareClick(View view) {
        mShareListener.onShareClick(mShareLink);
    }

    @Override
    public int getType() {
        return Type.USER;
    }

    public interface ShareListener {
        void onShareClick(@NonNull String shareLink);
    }
}
