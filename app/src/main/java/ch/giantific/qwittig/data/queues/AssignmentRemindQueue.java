package ch.giantific.qwittig.data.queues;

import android.support.annotation.NonNull;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by fabio on 28.07.16.
 */
@IgnoreExtraProperties
public class AssignmentRemindQueue {

    private static final String TYPE = "ASSIGNMENT_REMIND";
    private String assignmentId;
    private String type;

    public AssignmentRemindQueue() {
        // required for firebase de-/serialization
    }

    public AssignmentRemindQueue(@NonNull String assignmentId) {
        this.assignmentId = assignmentId;
        this.type = TYPE;
    }

    public String getAssignmentId() {
        return assignmentId;
    }

    public String getType() {
        return type;
    }
}
