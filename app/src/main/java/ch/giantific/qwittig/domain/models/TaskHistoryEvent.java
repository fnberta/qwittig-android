/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.domain.models;

import android.support.annotation.NonNull;

import com.parse.ParseACL;
import com.parse.ParseClassName;
import com.parse.ParseObject;

import java.util.Date;

import ch.giantific.qwittig.utils.parse.ParseUtils;

/**
 * Represents an event when a user finished a task.
 * <p/>
 * Subclass of {@link ParseObject}.
 */
@ParseClassName("TaskHistoryEvent")
public class TaskHistoryEvent extends ParseObject {

    public static final String CLASS = "TaskHistoryEvent";
    public static final String TASK = "task";
    public static final String IDENTITY = "identity";
    public static final String DATE = "date";
    public static final String PIN_LABEL = "taskHistoryEventPinLabel";

    public TaskHistoryEvent() {
        // A default constructor is required.
    }

    public TaskHistoryEvent(@NonNull Task task, @NonNull Identity identity, @NonNull Date date) {
        setTask(task);
        setIdentity(identity);
        setDate(date);
        setAccessRights(task.getGroup());
    }

    private void setAccessRights(@NonNull Group group) {
        final ParseACL acl = ParseUtils.getDefaultAcl(group, false);
        setACL(acl);
    }

    public Task getTask() {
        return (Task) getParseObject(TASK);
    }

    public void setTask(@NonNull Task task) {
        put(TASK, task);
    }

    public Identity getIdentity() {
        return (Identity) getParseObject(IDENTITY);
    }

    public void setIdentity(@NonNull Identity identity) {
        put(IDENTITY, identity);
    }

    public Date getDate() {
        return getDate(DATE);
    }

    public void setDate(@NonNull Date date) {
        put(DATE, date);
    }
}
