/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.domain.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import ch.giantific.qwittig.domain.models.parse.Task;
import ch.giantific.qwittig.domain.models.parse.User;

/**
 * Represents a user available for selection in a {@link Task}. Includes the id of the
 * corresponding {@link User} object, whether it is involved or not and its position.
 *
 * Implements {@link Parcelable}.
 */
public class TaskUser implements Parcelable {

    private String mUserId;
    private boolean mIsInvolved;
    private int mPosition;

    public boolean isInvolved() {
        return mIsInvolved;
    }

    public void setIsInvolved(boolean isInvolved) {
        mIsInvolved = isInvolved;
    }

    public String getUserId() {
        return mUserId;
    }

    public void setUserId(@NonNull String userId) {
        mUserId = userId;
    }

    public int getPosition() {
        return mPosition;
    }

    public void setPosition(int position) {
        mPosition = position;
    }

    public TaskUser(@NonNull String userId, boolean isInvolved) {
        mUserId = userId;
        mIsInvolved = isInvolved;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(this.mUserId);
        dest.writeByte(mIsInvolved ? (byte) 1 : (byte) 0);
        dest.writeInt(this.mPosition);
    }

    protected TaskUser(@NonNull Parcel in) {
        this.mUserId = in.readString();
        this.mIsInvolved = in.readByte() != 0;
        this.mPosition = in.readInt();
    }

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
}
