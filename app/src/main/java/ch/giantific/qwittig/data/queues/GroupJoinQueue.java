package ch.giantific.qwittig.data.queues;

import android.support.annotation.NonNull;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by fabio on 28.07.16.
 */
@IgnoreExtraProperties
public class GroupJoinQueue {

    public static final String GROUP_ID = "groupId";
    public static final String IDENTITY_ID = "identityId";
    public static final String TYPE = "GROUP_JOINED";

    private String groupId;
    private String identityId;
    private String type;

    public GroupJoinQueue() {
        // required for firebase de-/serialization
    }

    public GroupJoinQueue(@NonNull String groupId, @NonNull String identityId) {
        this.groupId = groupId;
        this.identityId = identityId;
        type = TYPE;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getIdentityId() {
        return identityId;
    }

    public Map<String, Object> toMap() {
        final Map<String, Object> result = new HashMap<>();
        result.put(GROUP_ID, groupId);
        result.put(IDENTITY_ID, identityId);

        return result;
    }

    public String getType() {
        return type;
    }
}
