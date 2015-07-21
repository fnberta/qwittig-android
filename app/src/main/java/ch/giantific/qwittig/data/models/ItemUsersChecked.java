package ch.giantific.qwittig.data.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by fabio on 15.01.15.
 */
public class ItemUsersChecked implements Parcelable {

    private boolean[] mUsersChecked;

    public boolean[] getUsersChecked() {
        return mUsersChecked;
    }

    public void setUsersChecked(boolean[] usersChecked) {
        mUsersChecked = usersChecked;
    }

    public ItemUsersChecked(boolean[] usersChecked) {
        mUsersChecked = usersChecked;
    }

    public void checkAll() {
        for (int i = 0, mUsersCheckedLength = mUsersChecked.length; i < mUsersCheckedLength; i++) {
            mUsersChecked[i] = true;
        }
    }

    public void checkAllExceptBuyer(int buyerPosition) {
        for (int i = 0, mUsersCheckedLength = mUsersChecked.length; i < mUsersCheckedLength; i++) {
            mUsersChecked[i] = i == buyerPosition;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeBooleanArray(this.mUsersChecked);
    }

    private ItemUsersChecked(Parcel in) {
        this.mUsersChecked = in.createBooleanArray();
    }

    public static final Parcelable.Creator<ItemUsersChecked> CREATOR = new Parcelable.Creator<ItemUsersChecked>() {
        public ItemUsersChecked createFromParcel(Parcel source) {
            return new ItemUsersChecked(source);
        }

        public ItemUsersChecked[] newArray(int size) {
            return new ItemUsersChecked[size];
        }
    };
}
