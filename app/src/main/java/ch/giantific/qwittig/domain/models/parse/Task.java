/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.domain.models.parse;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;

import com.parse.ParseACL;
import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

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
import ch.giantific.qwittig.utils.ParseUtils;

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
    public static final String USERS_INVOLVED = "usersInvolved";
    public static final String HISTORY = "history";
    public static final String PIN_LABEL = "tasksPinLabel";

    @StringDef({TIME_FRAME_ONE_TIME, TIME_FRAME_DAILY, TIME_FRAME_WEEKLY, TIME_FRAME_MONTHLY,
            TIME_FRAME_YEARLY, TIME_FRAME_AS_NEEDED})
    @Retention(RetentionPolicy.SOURCE)
    public @interface TimeFrame {}
    public static final String TIME_FRAME_ONE_TIME = "oneTime";
    public static final String TIME_FRAME_DAILY = "daily";
    public static final String TIME_FRAME_WEEKLY = "weekly";
    public static final String TIME_FRAME_MONTHLY = "monthly";
    public static final String TIME_FRAME_YEARLY = "yearly";
    public static final String TIME_FRAME_AS_NEEDED = "asNeeded";
    private boolean mIsLoading;
    public Task() {
        // A default constructor is required.
    }

    public Task(@NonNull ParseUser initiator, @NonNull String title, @NonNull ParseObject group,
                @NonNull @TimeFrame String timeFrame, @Nullable Date deadline,
                @NonNull List<ParseUser> usersInvolved) {
        setInitiator(initiator);
        setTitle(title);
        setGroup(group);
        setTimeFrame(timeFrame);
        if (deadline != null) {
            setDeadline(deadline);
        }
        setUsersInvolved(usersInvolved);
        setAccessRights(group);
    }

    private void setAccessRights(@NonNull ParseObject group) {
        ParseACL acl = ParseUtils.getDefaultAcl(group);
        setACL(acl);
    }

    public User getInitiator() {
        return (User) getParseUser(INITIATOR);
    }

    public void setInitiator(@NonNull ParseUser initiator) {
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
    public List<ParseUser> getUsersInvolved() {
        List<ParseUser> usersInvolved = getList(USERS_INVOLVED);
        if (usersInvolved == null) {
            return Collections.emptyList();
        }

        return usersInvolved;
    }

    public void setUsersInvolved(@NonNull List<ParseUser> usersInvolved) {
        put(USERS_INVOLVED, usersInvolved);
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
        return mIsLoading;
    }

    public void setLoading(boolean isLoading) {
        mIsLoading = isLoading;
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
        if (timeFrame.equals(Task.TIME_FRAME_AS_NEEDED)) {
            // as needed tasks have no deadline
            return;
        }

        Calendar deadline = DateUtils.getCalendarInstanceUTC();
        deadline.setTime(new Date());
        switch (timeFrame) {
            case Task.TIME_FRAME_DAILY:
                deadline.add(Calendar.DAY_OF_YEAR, 1);
                break;
            case Task.TIME_FRAME_WEEKLY:
                deadline.add(Calendar.WEEK_OF_YEAR, 1);
                break;
            case Task.TIME_FRAME_MONTHLY:
                deadline.add(Calendar.MONTH, 1);
                break;
            case Task.TIME_FRAME_YEARLY:
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
    @Nullable
    public User getUserResponsible() {
        List<ParseUser> users = getUsersInvolved();
        if (!users.isEmpty()) {
            return (User) users.get(0);
        }

        return null;
    }

    /**
     * Adds a history event to the task.
     * <p/>
     * A history event simply contains the object id of the user who completed the task and the
     * date on which he/she did.
     *
     * @param currentUser the current user who just completed the task
     * @return the new user responsible
     */
    @Nullable
    public User addHistoryEvent(@Nullable ParseUser currentUser) {
        if (currentUser == null) {
            return null;
        }

        List<ParseUser> usersInvolved = getUsersInvolved();
        Collections.rotate(usersInvolved, -1);

        final String currentUserId = currentUser.getObjectId();
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
        historyNew.put(currentUser.getObjectId(), entries);
        put(HISTORY, historyNew);

        return (User) usersInvolved.get(0);
    }
}
