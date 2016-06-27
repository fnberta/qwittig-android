/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.tasks.addedit.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import ch.giantific.qwittig.domain.models.Task;
import ch.giantific.qwittig.domain.models.User;

/**
 * Represents a user available for selection in a {@link Task}. Includes the id of the
 * corresponding {@link User} object, whether it is involved or not and its position.
 * <p/>
 * Implements {@link Parcelable}.
 */
public class TaskUser implements Parcelable {

    public static final Creator<TaskUser> CREATOR = new Creator<TaskUser>() {
        @NonNull
        public TaskUser createFromParcel(@NonNull Parcel source) {
            return new TaskUser(source);
        }

        @NonNull
        public TaskUser[] newArray(int size) {
            return new TaskUser[size];
        }
    };
    private String mIdentityId;
    private boolean mInvolved;
    private int mPosition;

    public TaskUser(@NonNull String identityId, boolean involved) {
        mIdentityId = identityId;
        mInvolved = involved;
    }

    private TaskUser(@NonNull Parcel in) {
        mIdentityId = in.readString();
        mInvolved = in.readByte() != 0;
        mPosition = in.readInt();
    }

    public boolean isInvolved() {
        return mInvolved;
    }

    public void setIsInvolved(boolean involved) {
        mInvolved = involved;
    }

    public String getIdentityId() {
        return mIdentityId;
    }

    public void setIdentityId(@NonNull String identityId) {
        mIdentityId = identityId;
    }

    public int getPosition() {
        return mPosition;
    }

    public void setPosition(int position) {
        mPosition = position;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(mIdentityId);
        dest.writeByte(mInvolved ? (byte) 1 : (byte) 0);
        dest.writeInt(mPosition);
    }
}
