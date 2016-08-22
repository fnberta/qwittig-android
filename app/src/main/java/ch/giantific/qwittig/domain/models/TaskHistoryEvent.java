/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.domain.models;

import android.support.annotation.NonNull;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.ServerValue;

import java.util.Date;
import java.util.Map;

/**
 * Represents an event when a user finished a task.
 */
@IgnoreExtraProperties
public class TaskHistoryEvent implements FirebaseModel {

    public static final String BASE_PATH = "taskHistoryEvents";

    public static final String PATH_TASK = "task";
    public static final String PATH_IDENTITY = "identity";
    public static final String PATH_DATE = "date";

    private String id;
    private long createdAt;
    private String task;
    private String identity;
    private Date date;

    public TaskHistoryEvent() {
        // required for firebase de-/serialization
    }

    public TaskHistoryEvent(@NonNull String task, @NonNull String identity, @NonNull Date date) {
        this.task = task;
        this.identity = identity;
        this.date = date;
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

    public String getTask() {
        return task;
    }

    public String getIdentity() {
        return identity;
    }

    public Date getDate() {
        return date;
    }
}
