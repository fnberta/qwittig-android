/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.addedit.viewmodels.items;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import static ch.giantific.qwittig.utils.ViewUtils.DISABLED_ALPHA;

/**
 * Represents a user with an object id, a nickname, an avatar and whether it is selected or not.
 */
public class PurchaseAddEditArticleIdentityItemViewModel extends BaseObservable implements Parcelable {

    public static final Creator<PurchaseAddEditArticleIdentityItemViewModel> CREATOR = new Creator<PurchaseAddEditArticleIdentityItemViewModel>() {
        @Override
        public PurchaseAddEditArticleIdentityItemViewModel createFromParcel(Parcel source) {
            return new PurchaseAddEditArticleIdentityItemViewModel(source);
        }

        @Override
        public PurchaseAddEditArticleIdentityItemViewModel[] newArray(int size) {
            return new PurchaseAddEditArticleIdentityItemViewModel[size];
        }
    };
    private final String identityId;
    private final String nickname;
    private final String avatar;
    private boolean selected;

    public PurchaseAddEditArticleIdentityItemViewModel(@NonNull String identityId, @NonNull String nickname,
                                                       @Nullable String avatar, boolean selected) {
        this.identityId = identityId;
        this.nickname = nickname;
        this.avatar = avatar;
        this.selected = selected;
    }

    private PurchaseAddEditArticleIdentityItemViewModel(Parcel in) {
        identityId = in.readString();
        nickname = in.readString();
        avatar = in.readString();
        selected = in.readByte() != 0;
    }

    public String getIdentityId() {
        return identityId;
    }

    @Bindable
    public String getNickname() {
        return nickname;
    }

    @Bindable
    public String getAvatar() {
        return avatar;
    }

    @Bindable
    public float getAlpha() {
        return selected ? 1f : DISABLED_ALPHA;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(identityId);
        dest.writeString(nickname);
        dest.writeString(avatar);
        dest.writeByte(selected ? (byte) 1 : (byte) 0);
    }
}
