/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.stats.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import ch.giantific.qwittig.presentation.stats.StatsViewModel.StatsType;

/**
 * Represents a stats page with a title and the type number.
 */
public class StatsTypeItem implements Parcelable {

    public static final Parcelable.Creator<StatsTypeItem> CREATOR = new Parcelable.Creator<StatsTypeItem>() {
        @Override
        public StatsTypeItem createFromParcel(Parcel source) {
            return new StatsTypeItem(source);
        }

        @Override
        public StatsTypeItem[] newArray(int size) {
            return new StatsTypeItem[size];
        }
    };
    private final String title;
    @StatsType
    private final int type;

    public StatsTypeItem(@NonNull String title, @StatsType int type) {
        this.title = title;
        this.type = type;
    }

    @SuppressWarnings("WrongConstant")
    protected StatsTypeItem(Parcel in) {
        title = in.readString();
        type = in.readInt();
    }

    public String getTitle() {
        return title;
    }

    @StatsType
    public int getType() {
        return type;
    }

    @Override
    public String toString() {
        return title;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final StatsTypeItem that = (StatsTypeItem) o;

        if (type != that.getType()) return false;
        return title.equals(that.getTitle());

    }

    @Override
    public int hashCode() {
        int result = title.hashCode();
        result = 31 * result + type;
        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeInt(type);
    }
}
