package ch.giantific.qwittig.data.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by fabio on 16.09.15.
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

    public void setUserId(String userId) {
        mUserId = userId;
    }

    public int getPosition() {
        return mPosition;
    }

    public void setPosition(int position) {
        mPosition = position;
    }

    public TaskUser(String userId, boolean isInvolved, int position) {
        mUserId = userId;
        mIsInvolved = isInvolved;
        mPosition = position;
    }

    public TaskUser(String userId, boolean isInvolved) {
        mUserId = userId;
        mIsInvolved = isInvolved;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mUserId);
        dest.writeByte(mIsInvolved ? (byte) 1 : (byte) 0);
        dest.writeInt(this.mPosition);
    }

    protected TaskUser(Parcel in) {
        this.mUserId = in.readString();
        this.mIsInvolved = in.readByte() != 0;
        this.mPosition = in.readInt();
    }

    public static final Creator<TaskUser> CREATOR = new Creator<TaskUser>() {
        public TaskUser createFromParcel(Parcel source) {
            return new TaskUser(source);
        }

        public TaskUser[] newArray(int size) {
            return new TaskUser[size];
        }
    };
}
