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
    private final String mIdentityId;
    private final String mNickname;
    private final String mAvatar;
    private boolean mInvolved;

    public TaskAddEditIdentityItemModel(@NonNull Identity identity) {
        mIdentityId = identity.getId();
        mNickname = identity.getNickname();
        mAvatar = identity.getAvatar();
    }

    protected TaskAddEditIdentityItemModel(Parcel in) {
        mIdentityId = in.readString();
        mNickname = in.readString();
        mAvatar = in.readString();
        mInvolved = in.readByte() != 0;
    }

    public String getIdentityId() {
        return mIdentityId;
    }

    @Bindable
    public String getNickname() {
        return mNickname;
    }

    @Bindable
    public String getAvatar() {
        return mAvatar;
    }

    @Bindable
    public float getAlpha() {
        return mInvolved ? 1f : DISABLED_ALPHA;
    }

    public boolean isInvolved() {
        return mInvolved;
    }

    public void setInvolved(boolean involved) {
        mInvolved = involved;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mIdentityId);
        dest.writeString(mNickname);
        dest.writeString(mAvatar);
        dest.writeByte(mInvolved ? (byte) 1 : (byte) 0);
    }
}
