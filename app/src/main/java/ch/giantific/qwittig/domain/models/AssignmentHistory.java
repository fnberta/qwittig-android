/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.domain.models;

import android.support.annotation.NonNull;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.ServerValue;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents an event when a user finished a assignment.
 */
@IgnoreExtraProperties
public class AssignmentHistory implements FirebaseModel {

    public static final String BASE_PATH = "assignmentHistory";

    public static final String PATH_ASSIGNMENT = "assignment";
    public static final String PATH_IDENTITY = "identity";
    public static final String PATH_DATE = "date";

    private String id;
    private long createdAt;
    private String assignment;
    private String identity;
    private long date;

    public AssignmentHistory() {
        // required for firebase de-/serialization
    }

    public AssignmentHistory(@NonNull String assignment,
                             @NonNull String identity,
                             @NonNull Date date) {
        this.assignment = assignment;
        this.identity = identity;
        this.date = date.getTime();
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

    public String getAssignment() {
        return assignment;
    }

    public String getIdentity() {
        return identity;
    }

    public long getDate() {
        return date;
    }

    @Exclude
    public Date getDateDate() {
        return new Date(date);
    }

    public Map<String, Object> toMap() {
        final Map<String, Object> result = new HashMap<>();
        result.put(PATH_CREATED_AT, ServerValue.TIMESTAMP);
        result.put(PATH_ASSIGNMENT, assignment);
        result.put(PATH_IDENTITY, identity);
        result.put(PATH_DATE, date);

        return result;
    }
}
