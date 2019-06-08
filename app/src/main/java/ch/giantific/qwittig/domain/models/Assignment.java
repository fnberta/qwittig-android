/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.domain.models;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.ServerValue;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.giantific.qwittig.utils.DateUtils;

/**
 * Represents a task that is defined within a group and assigned to specific users.
 */
@IgnoreExtraProperties
public class Assignment implements FirebaseModel {

    public static final String BASE_PATH = "assignments";

    public static final String PATH_TITLE = "title";
    public static final String PATH_GROUP = "group";
    public static final String PATH_TIME_FRAME = "timeFrame";
    public static final String PATH_DEADLINE = "deadline";
    public static final String PATH_IDENTITIES = "identities";

    private String id;
    private long createdAt;
    private String title;
    private String group;
    @TimeFrame
    private String timeFrame;
    private long deadline;
    private Map<String, Integer> identities;

    public Assignment() {
        // required for firebase de-/serialization
    }

    public Assignment(@NonNull String title, @NonNull String group,
                      @NonNull @TimeFrame String timeFrame, @Nullable Date deadline,
                      @NonNull List<String> identities) {
        this.title = title;
        this.group = group;
        this.timeFrame = timeFrame;
        if (deadline != null) {
            this.deadline = deadline.getTime();
        }
        this.identities = new LinkedHashMap<>();
        for (int i = 0, size = identities.size(); i < size; i++) {
            this.identities.put(identities.get(i), i);
        }
    }

    @Exclude
    public String getId() {
        return id;
    }

    @Override
    public void setId(@NonNull String id) {
        this.id = id;
    }

    @Override
    public Map<String, String> getCreatedAt() {
        return ServerValue.TIMESTAMP;
    }

    @Exclude
    public Date getCreatedAtDate() {
        return new Date(createdAt);
    }

    public String getTitle() {
        return title;
    }

    public String getGroup() {
        return group;
    }

    @TimeFrame
    public String getTimeFrame() {
        return timeFrame;
    }

    public long getDeadline() {
        return deadline;
    }

    @Exclude
    public Date getDeadlineDate() {
        return new Date(deadline);
    }

    public Map<String, Integer> getIdentities() {
        return identities;
    }

    @Exclude
    public Set<String> getIdentityIds() {
        return identities.keySet();
    }

    @Exclude
    public String[] getIdentityIdsSorted() {
        final Map<String, Integer> identities = getIdentities();
        final String[] sorted = new String[identities.size()];
        for (Map.Entry<String, Integer> entry : identities.entrySet()) {
            sorted[entry.getValue()] = entry.getKey();
        }

        return sorted;
    }

    @Exclude
    public String getIdentityIdResponsible() {
        return getIdentityIdsSorted()[0];
    }

    /**
     * Calculates the days it takes from today until the deadline of the task is reached.
     *
     * @return the number of days until the deadline is reached
     */
    @Exclude
    public int getDaysToDeadline() {
        final Calendar todayCal = DateUtils.getCalendarInstanceUTC();
        final Calendar deadlineCal = DateUtils.getCalendarInstanceUTC();
        deadlineCal.setTime(new Date(deadline));

        if (todayCal.get(Calendar.YEAR) == deadlineCal.get(Calendar.YEAR)) {
            return deadlineCal.get(Calendar.DAY_OF_YEAR) - todayCal.get(Calendar.DAY_OF_YEAR);
        }

        int extraDays = 0;
        if (deadlineCal.get(Calendar.YEAR) > todayCal.get(Calendar.YEAR)) {
            while (deadlineCal.get(Calendar.YEAR) > todayCal.get(Calendar.YEAR)) {
                deadlineCal.add(Calendar.YEAR, -1);
                // getActualMaximum() important for leap years
                extraDays += deadlineCal.getActualMaximum(Calendar.DAY_OF_YEAR);
            }

            return extraDays - todayCal.get(Calendar.DAY_OF_YEAR) + deadlineCal.get(Calendar.DAY_OF_YEAR);
        }
        if (deadlineCal.get(Calendar.YEAR) < todayCal.get(Calendar.YEAR)) {
            while (deadlineCal.get(Calendar.YEAR) < todayCal.get(Calendar.YEAR)) {
                deadlineCal.add(Calendar.YEAR, 1);
                // getActualMaximum() important for leap years
                extraDays += deadlineCal.getActualMaximum(Calendar.DAY_OF_YEAR);
            }

            return (extraDays - deadlineCal.get(Calendar.DAY_OF_YEAR) + todayCal.get(Calendar.DAY_OF_YEAR)) * -1;
        }

        return 0;
    }

    @StringDef({TimeFrame.ONE_TIME, TimeFrame.DAILY, TimeFrame.WEEKLY, TimeFrame.MONTHLY,
            TimeFrame.YEARLY, TimeFrame.AS_NEEDED})
    @Retention(RetentionPolicy.SOURCE)
    public @interface TimeFrame {
        String ONE_TIME = "oneTime";
        String DAILY = "daily";
        String WEEKLY = "weekly";
        String MONTHLY = "monthly";
        String YEARLY = "yearly";
        String AS_NEEDED = "asNeeded";
    }
}
