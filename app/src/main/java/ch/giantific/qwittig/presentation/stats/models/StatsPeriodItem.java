/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.stats.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import ch.giantific.qwittig.presentation.stats.StatsContract.StatsPeriod;


/**
 * Represents a stats page with a title and the type number.
 */
public class StatsPeriodItem implements Parcelable {

    public static final Parcelable.Creator<StatsPeriodItem> CREATOR = new Parcelable.Creator<StatsPeriodItem>() {
        @Override
        public StatsPeriodItem createFromParcel(Parcel source) {
            return new StatsPeriodItem(source);
        }

        @Override
        public StatsPeriodItem[] newArray(int size) {
            return new StatsPeriodItem[size];
        }
    };
    private final String periodName;
    @StatsPeriod
    private final int type;

    public StatsPeriodItem(@NonNull String periodName, @StatsPeriod int type) {
        this.periodName = periodName;
        this.type = type;
    }

    @SuppressWarnings("WrongConstant")
    protected StatsPeriodItem(Parcel in) {
        periodName = in.readString();
        type = in.readInt();
    }

    public String getPeriodName() {
        return periodName;
    }

    @StatsPeriod
    public int getType() {
        return type;
    }

    @Override
    public String toString() {
        return periodName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final StatsPeriodItem that = (StatsPeriodItem) o;

        if (type != that.getType()) return false;
        return periodName.equals(that.getPeriodName());
    }

    @Override
    public int hashCode() {
        int result = periodName.hashCode();
        result = 31 * result + type;
        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(periodName);
        dest.writeInt(type);
    }
}
