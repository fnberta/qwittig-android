/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.assignments.list.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Calendar;
import java.util.Date;

import ch.giantific.qwittig.utils.DateUtils;

/**
 * Represents a task deadline.
 */
public class AssignmentDeadline implements Parcelable {

    public static final Creator<AssignmentDeadline> CREATOR = new Creator<AssignmentDeadline>() {
        @Override
        public AssignmentDeadline createFromParcel(Parcel in) {
            return new AssignmentDeadline(in);
        }

        @Override
        public AssignmentDeadline[] newArray(int size) {
            return new AssignmentDeadline[size];
        }
    };
    @StringRes
    private final int title;
    private final int type;
    private final Date date;

    private AssignmentDeadline(@StringRes int title, @DeadlineType int type, @NonNull Date date) {
        this.title = title;
        this.type = type;
        this.date = date;
    }

    private AssignmentDeadline(Parcel in) {
        title = in.readInt();
        type = in.readInt();
        date = new Date(in.readLong());
    }

    public static AssignmentDeadline newAllInstance(@StringRes int title) {
        return new AssignmentDeadline(title, DeadlineType.ALL, new Date(Long.MAX_VALUE));
    }

    public static AssignmentDeadline newTodayInstance(@StringRes int title) {
        final Calendar cal = DateUtils.getCalendarInstanceUTC();
        cal.add(Calendar.DAY_OF_YEAR, 1);
        DateUtils.resetToMidnight(cal);
        return new AssignmentDeadline(title, DeadlineType.TODAY, cal.getTime());
    }

    public static AssignmentDeadline newWeekInstance(@StringRes int title) {
        final Calendar cal = DateUtils.getCalendarInstanceUTC();
        int firstDayOfWeek = cal.getFirstDayOfWeek();
        cal.set(Calendar.DAY_OF_WEEK, firstDayOfWeek);
        cal.add(Calendar.WEEK_OF_YEAR, 1);
        DateUtils.resetToMidnight(cal);
        return new AssignmentDeadline(title, DeadlineType.WEEK, cal.getTime());
    }

    public static AssignmentDeadline newMonthInstance(@StringRes int title) {
        final Calendar cal = DateUtils.getCalendarInstanceUTC();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.add(Calendar.MONTH, 1);
        DateUtils.resetToMidnight(cal);
        return new AssignmentDeadline(title, DeadlineType.MONTH, cal.getTime());
    }

    public static AssignmentDeadline newYearInstance(@StringRes int title) {
        final Calendar cal = DateUtils.getCalendarInstanceUTC();
        cal.set(Calendar.DAY_OF_YEAR, 1);
        cal.add(Calendar.YEAR, 1);
        DateUtils.resetToMidnight(cal);
        return new AssignmentDeadline(title, DeadlineType.YEAR, cal.getTime());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(title);
        dest.writeInt(type);
        dest.writeLong(date.getTime());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @StringRes
    public int getTitle() {
        return title;
    }

    @DeadlineType
    public int getType() {
        return type;
    }

    public Date getDate() {
        return date;
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
