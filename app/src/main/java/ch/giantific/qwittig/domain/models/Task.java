/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.domain.models;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;

import com.parse.ParseACL;
import com.parse.ParseClassName;
import com.parse.ParseObject;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.giantific.qwittig.utils.DateUtils;
import ch.giantific.qwittig.utils.parse.ParseUtils;

/**
 * Represents a task that is defined within a {@link Group} and assigned to specific users
 * <p/>
 * Subclass of {@link ParseObject}.
 */
@ParseClassName("Task")
public class Task extends ParseObject {

    public static final String CLASS = "Task";
    public static final String INITIATOR = "initiator";
    public static final String TITLE = "title";
    public static final String GROUP = "group";
    public static final String TIME_FRAME = "timeFrame";
    public static final String DEADLINE = "deadline";
    public static final String IDENTITIES = "identities";
    public static final String HISTORY = "history";
    public static final String PIN_LABEL = "tasksPinLabel";
    private boolean mLoading;

    public Task() {
        // A default constructor is required.
    }

    public Task(@NonNull Identity initiator, @NonNull String title, @NonNull Group group,
                @NonNull @TimeFrame String timeFrame, @Nullable Date deadline,
                @NonNull List<Identity> identities) {
        setInitiator(initiator);
        setTitle(title);
        setGroup(group);
        setTimeFrame(timeFrame);
        if (deadline != null) {
            setDeadline(deadline);
        }
        setIdentities(identities);
        setAccessRights(group);
    }

    private void setAccessRights(@NonNull Group group) {
        ParseACL acl = ParseUtils.getDefaultAcl(group);
        setACL(acl);
    }

    public Identity getInitiator() {
        return (Identity) getParseObject(INITIATOR);
    }

    public void setInitiator(@NonNull Identity initiator) {
        put(INITIATOR, initiator);
    }

    public String getTitle() {
        return getString(TITLE);
    }

    public void setTitle(@NonNull String name) {
        put(TITLE, name);
    }

    public Group getGroup() {
        return (Group) getParseObject(GROUP);
    }

    public void setGroup(@NonNull ParseObject group) {
        put(GROUP, group);
    }

    @TimeFrame
    public String getTimeFrame() {
        @TimeFrame String timeFrame = getString(TIME_FRAME);
        return timeFrame;
    }

    public void setTimeFrame(@NonNull @TimeFrame String timeFrame) {
        put(TIME_FRAME, timeFrame);
    }

    public Date getDeadline() {
        return getDate(DEADLINE);
    }

    public void setDeadline(@NonNull Date deadline) {
        put(DEADLINE, deadline);
    }

    @NonNull
    public List<Identity> getIdentities() {
        List<Identity> identities = getList(IDENTITIES);
        if (identities == null) {
            return Collections.emptyList();
        }

        return identities;
    }

    public void setIdentities(@NonNull List<Identity> identities) {
        put(IDENTITIES, identities);
    }

    @NonNull
    public Map<String, List<Date>> getHistory() {
        Map<String, List<Date>> history = getMap(HISTORY);
        if (history == null) {
            return Collections.emptyMap();
        }

        return history;
    }

    public void setHistory(@NonNull Map<String, List<Date>> history) {
        put(HISTORY, history);
    }

    public boolean isLoading() {
        return mLoading;
    }

    public void setLoading(boolean isLoading) {
        mLoading = isLoading;
    }

    /**
     * Resets the specified deadline to midnight and sets it or removes it if it is null.
     *
     * @param deadline the deadline to reset and set
     */
    public void setDeadlineResetMidnight(@Nullable Date deadline) {
        if (deadline == null) {
            remove(DEADLINE);
        } else {
            Calendar cal = DateUtils.getCalendarInstanceUTC();
            cal.setTime(deadline);
            cal = DateUtils.resetToMidnight(cal);

            setDeadline(cal.getTime());
        }
    }

    /**
     * Updates the task's deadline to the next time the task needs to be finished. Adds the time
     * frame to the date the task was finished and not to the date when it was supposed to be
     * finished.
     */
    public void updateDeadline() {
        String timeFrame = getTimeFrame();
        if (timeFrame.equals(TimeFrame.AS_NEEDED)) {
            // as needed tasks have no deadline
            return;
        }

        Calendar deadline = DateUtils.getCalendarInstanceUTC();
        deadline.setTime(new Date());
        switch (timeFrame) {
            case TimeFrame.DAILY:
                deadline.add(Calendar.DAY_OF_YEAR, 1);
                break;
            case TimeFrame.WEEKLY:
                deadline.add(Calendar.WEEK_OF_YEAR, 1);
                break;
            case TimeFrame.MONTHLY:
                deadline.add(Calendar.MONTH, 1);
                break;
            case TimeFrame.YEARLY:
                deadline.add(Calendar.YEAR, 1);
                break;
        }

        deadline = DateUtils.resetToMidnight(deadline);
        setDeadline(deadline.getTime());
    }

    /**
     * Returns the user that is currently responsible for finishing the task.
     *
     * @return the user currently responsible for finishing the task
     */
    public Identity getUserResponsible() {
        return getIdentities().get(0);
    }

    /**
     * Adds a history event to the task.
     * <p/>
     * A history event simply contains the object id of the user who completed the task and the
     * date on which he/she did.
     *
     * @param currentIdentity the current user who just completed the task
     * @return the new user responsible
     */
    @Nullable
    public Identity addHistoryEvent(@Nullable Identity currentIdentity) {
        if (currentIdentity == null) {
            return null;
        }

        List<Identity> identities = getIdentities();
        Collections.rotate(identities, -1);

        final String currentUserId = currentIdentity.getObjectId();
        Map<String, List<Date>> historyNew = new HashMap<>();

        Map<String, List<Date>> historyOld = getHistory();
        if (!historyOld.isEmpty()) {
            historyNew.putAll(historyOld);
        }

        List<Date> entries = historyNew.get(currentUserId);
        if (entries == null) {
            entries = new ArrayList<>();
        }
        entries.add(new Date());
        historyNew.put(currentUserId, entries);
        put(HISTORY, historyNew);

        return identities.get(0);
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
