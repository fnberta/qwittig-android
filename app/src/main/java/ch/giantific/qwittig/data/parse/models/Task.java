package ch.giantific.qwittig.data.parse.models;

import android.support.annotation.NonNull;
import android.support.annotation.StringDef;
import android.util.Log;

import com.parse.ParseACL;
import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import ch.giantific.qwittig.utils.DateUtils;
import ch.giantific.qwittig.utils.ParseUtils;

/**
 * Created by fabio on 12.10.14.
 */
@ParseClassName("Task")
public class Task extends ParseObject {

    public static final String CLASS = "Task";
    public static final String TITLE = "title";
    public static final String GROUP = "group";
    public static final String TIME_FRAME = "timeFrame";
    public static final String DEADLINE = "deadline";
    public static final String USER_RESPONSIBLE = "userResponsible";
    public static final String USERS_INVOLVED = "usersInvolved";
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


    public String getTitle() {
        return getString(TITLE);
    }

    public void setTitle(String name) {
        put(TITLE, name);
    }

    public Group getGroup() {
        return (Group) getParseObject(GROUP);
    }

    public void setGroup(ParseObject group) {
        put(GROUP, group);
    }

    public String getTimeFrame() {
        return getString(TIME_FRAME);
    }

    public void setTimeFrame(@TimeFrame String timeFrame) {
        put(TIME_FRAME, timeFrame);
    }

    public Date getDeadline() {
        return getDate(DEADLINE);
    }
    
    public void setDeadline(@NonNull Date deadline) {
        Calendar cal = DateUtils.getCalendarInstanceUTC();
        cal.setTime(deadline);
        cal = DateUtils.resetToMidnight(cal);

        put(DEADLINE, cal.getTime());
    }

    public User getUserResponsible() {
        return (User) getParseUser(USER_RESPONSIBLE);
    }

    public void setUserResponsible(ParseUser userResponsible) {
        put(USER_RESPONSIBLE, userResponsible);
    }

    public List<ParseUser> getUsersInvolved() {
        List<ParseUser> usersInvolved = getList(USERS_INVOLVED);
        if (usersInvolved == null) {
            return Collections.emptyList();
        }

        return usersInvolved;
    }

    public void setUsersInvolved(List<ParseUser> usersInvolved) {
        put(USERS_INVOLVED, usersInvolved);
    }

    public Task() {
        // A default constructor is required.
    }

    public Task(String title, ParseObject group, ParseUser userResponsible,
                List<ParseUser> usersInvolved) {
        this(title, group, Task.TIME_FRAME_AS_NEEDED, null, userResponsible,
                usersInvolved);
    }

    public Task(String title, ParseObject group, @TimeFrame String timeFrame, Date deadline,
                ParseUser userResponsible, List<ParseUser> usersInvolved) {
        setTitle(title);
        setGroup(group);
        setTimeFrame(timeFrame);
        if (deadline != null) {
            setDeadline(deadline);
        }
        setUserResponsible(userResponsible);
        setUsersInvolved(usersInvolved);
        setAccessRights(group);
    }

    private void setAccessRights(ParseObject group) {
        ParseACL acl = ParseUtils.getDefaultAcl(group);
        setACL(acl);
    }
}
