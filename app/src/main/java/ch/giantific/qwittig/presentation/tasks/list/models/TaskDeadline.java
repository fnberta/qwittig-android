/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.tasks.list.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Calendar;
import java.util.Date;

import ch.giantific.qwittig.utils.DateUtils;

/**
 * Represents a task deadline.
 */
public class TaskDeadline implements Parcelable {

    public static final Parcelable.Creator<TaskDeadline> CREATOR = new Parcelable.Creator<TaskDeadline>() {
        @Override
        public TaskDeadline createFromParcel(Parcel source) {
            return new TaskDeadline(source);
        }

        @Override
        public TaskDeadline[] newArray(int size) {
            return new TaskDeadline[size];
        }
    };
    private final String title;
    private final int type;
    private final Date date;

    private TaskDeadline(@NonNull String title, @DeadlineType int type, @NonNull Date date) {
        this.title = title;
        this.type = type;
        this.date = date;
    }

    private TaskDeadline(Parcel in) {
        title = in.readString();
        type = in.readInt();
        long tmpMDate = in.readLong();
        date = tmpMDate == -1 ? null : new Date(tmpMDate);
    }

    public static TaskDeadline newAllInstance(@NonNull String title) {
        return new TaskDeadline(title, DeadlineType.ALL, new Date(Long.MAX_VALUE));
    }

    public static TaskDeadline newTodayInstance(@NonNull String title) {
        final Calendar cal = DateUtils.getCalendarInstanceUTC();
        cal.add(Calendar.DAY_OF_YEAR, 1);
        DateUtils.resetToMidnight(cal);
        return new TaskDeadline(title, DeadlineType.TODAY, cal.getTime());
    }

    public static TaskDeadline newWeekInstance(@NonNull String title) {
        final Calendar cal = DateUtils.getCalendarInstanceUTC();
        int firstDayOfWeek = cal.getFirstDayOfWeek();
        cal.set(Calendar.DAY_OF_WEEK, firstDayOfWeek);
        cal.add(Calendar.WEEK_OF_YEAR, 1);
        DateUtils.resetToMidnight(cal);
        return new TaskDeadline(title, DeadlineType.WEEK, cal.getTime());
    }

    public static TaskDeadline newMonthInstance(@NonNull String title) {
        final Calendar cal = DateUtils.getCalendarInstanceUTC();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.add(Calendar.MONTH, 1);
        DateUtils.resetToMidnight(cal);
        return new TaskDeadline(title, DeadlineType.MONTH, cal.getTime());
    }

    public static TaskDeadline newYearInstance(@NonNull String title) {
        final Calendar cal = DateUtils.getCalendarInstanceUTC();
        cal.set(Calendar.DAY_OF_YEAR, 1);
        cal.add(Calendar.YEAR, 1);
        DateUtils.resetToMidnight(cal);
        return new TaskDeadline(title, DeadlineType.YEAR, cal.getTime());
    }

    @DeadlineType
    public int getType() {
        return type;
    }

    public Date getDate() {
        return date;
    }

    @Override
    public String toString() {
        return title;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final TaskDeadline that = (TaskDeadline) o;
        return type == that.getType();
    }

    @Override
    public int hashCode() {
        return type;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeInt(type);
        dest.writeLong(date != null ? date.getTime() : -1);
    }

    @IntDef({DeadlineType.ALL, DeadlineType.TODAY, DeadlineType.WEEK, DeadlineType.MONTH,
            DeadlineType.YEAR})
    @Retention(RetentionPolicy.SOURCE)
    public @interface DeadlineType {
        int ALL = 1;
        int TODAY = 2;
        int WEEK = 3;
        int MONTH = 4;
        int YEAR = 5;
    }
}
