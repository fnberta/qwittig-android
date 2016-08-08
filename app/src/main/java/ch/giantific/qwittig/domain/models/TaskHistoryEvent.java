/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.domain.models;

import android.support.annotation.NonNull;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.PropertyName;
import com.google.firebase.database.ServerValue;

import java.util.Date;
import java.util.Map;

/**
 * Represents an event when a user finished a task.
 */
@IgnoreExtraProperties
public class TaskHistoryEvent implements FirebaseModel {

    public static final String PATH = "taskHistoryEvents";
    public static final String PATH_TASK = "task";
    public static final String PATH_IDENTITY = "identity";
    public static final String PATH_DATE = "date";
    private String mId;
    @PropertyName(PATH_CREATED_AT)
    private long mCreatedAt;
    @PropertyName(PATH_TASK)
    private String mTask;
    @PropertyName(PATH_IDENTITY)
    private String mIdentity;
    @PropertyName(PATH_DATE)
    private Date mDate;

    public TaskHistoryEvent() {
        // required for firebase de-/serialization
    }

    public TaskHistoryEvent(@NonNull String task, @NonNull String identity, @NonNull Date date) {
        mTask = task;
        mIdentity = identity;
        mDate = date;
    }

    @Exclude
    public String getId() {
        return mId;
    }

    @Override
    public void setId(@NonNull String id) {
        mId = id;
    }

    @Override
    public Map<String, String> getCreatedAt() {
        return ServerValue.TIMESTAMP;
    }

    @Exclude
    public Date getCreatedAtDate() {
        return new Date(mCreatedAt);
    }

    public String getTask() {
        return mTask;
    }

    public String getIdentity() {
        return mIdentity;
    }

    public Date getDate() {
        return mDate;
    }
}
