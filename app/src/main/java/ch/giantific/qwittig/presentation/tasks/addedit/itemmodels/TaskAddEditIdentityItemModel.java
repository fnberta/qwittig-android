/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.tasks.addedit.itemmodels;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;

import ch.giantific.qwittig.domain.models.Identity;

import static ch.giantific.qwittig.utils.ViewUtils.DISABLED_ALPHA;

/**
 * Defines a view model for a {@link RecyclerView} row that represents an identity available for
 * selection for a task.
 */
public class TaskAddEditIdentityItemModel extends BaseObservable implements Parcelable {

    public static final Creator<TaskAddEditIdentityItemModel> CREATOR = new Creator<TaskAddEditIdentityItemModel>() {
        @Override
        public TaskAddEditIdentityItemModel createFromParcel(Parcel source) {
            return new TaskAddEditIdentityItemModel(source);
        }

        @Override
        public TaskAddEditIdentityItemModel[] newArray(int size) {
            return new TaskAddEditIdentityItemModel[size];
        }
    };
    private final String identityId;
    private final String nickname;
    private final String avatar;
    private boolean involved;

    public TaskAddEditIdentityItemModel(@NonNull Identity identity) {
        identityId = identity.getId();
        nickname = identity.getNickname();
        avatar = identity.getAvatar();
    }

    protected TaskAddEditIdentityItemModel(Parcel in) {
        identityId = in.readString();
        nickname = in.readString();
        avatar = in.readString();
        involved = in.readByte() != 0;
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
        return involved ? 1f : DISABLED_ALPHA;
    }

    public boolean isInvolved() {
        return involved;
    }

    public void setInvolved(boolean involved) {
        this.involved = involved;
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
        dest.writeByte(involved ? (byte) 1 : (byte) 0);
    }
}
