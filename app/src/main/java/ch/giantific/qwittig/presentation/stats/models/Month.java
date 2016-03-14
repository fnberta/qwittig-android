/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.stats.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

import ch.giantific.qwittig.utils.DateUtils;

/**
 * Represents a standard month with a short name and a number (1-12).
 */
public class Month implements Parcelable {

    public static final Parcelable.Creator<Month> CREATOR = new Parcelable.Creator<Month>() {
        @Override
        public Month createFromParcel(Parcel source) {
            return new Month(source);
        }

        @Override
        public Month[] newArray(int size) {
            return new Month[size];
        }
    };
    private final String mNameShort;
    private final int mNumber;

    public Month(@NonNull String shortName) {
        mNumber = 0;
        mNameShort = shortName;
    }

    public Month(@IntRange(from = 1, to = 12) int number) {
        mNumber = number;
        mNameShort = DateUtils.getMonthNameShort(number);
    }

    protected Month(Parcel in) {
        mNameShort = in.readString();
        mNumber = in.readInt();
    }

    public int getNumber() {
        return mNumber;
    }

    public String getNameShort() {
        return mNameShort;
    }

    @Override
    public String toString() {
        return mNameShort;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mNameShort);
        dest.writeInt(mNumber);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final Month month = (Month) o;
        return mNameShort.equals(month.getNameShort());

    }

    @Override
    public int hashCode() {
        return mNameShort.hashCode();
    }
}
