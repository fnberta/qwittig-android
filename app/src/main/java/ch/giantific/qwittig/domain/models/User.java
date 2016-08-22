package ch.giantific.qwittig.domain.models;

import android.support.annotation.NonNull;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.ServerValue;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by fabio on 02.07.16.
 */
@IgnoreExtraProperties
public class User implements FirebaseModel {

    public static final String BASE_PATH = "users";

    public static final String PATH_CURRENT_IDENTITY = "currentIdentity";
    public static final String PATH_IDENTITIES = "identities";
    public static final String PATH_ARCHIVED_IDENTITIES = "archivedIdentities";
    public static final String PATH_TOKENS = "tokens";

    private String id;
    private long createdAt;
    private String currentIdentity;
    private Map<String, Boolean> identities = new HashMap<>();
    private Map<String, Boolean> archivedIdentities = new HashMap<>();
    private Map<String, Boolean> tokens;

    public User() {
        // required for firebase de-/serialization
    }

    public User(@NonNull String currentIdentity, @NonNull List<String> identities,
                @NonNull List<String> archivedIdentities) {
        this(currentIdentity, identities);

        for (String archivedId : archivedIdentities) {
            this.archivedIdentities.put(archivedId, true);
        }
    }

    public User(@NonNull String currentIdentity,
                @NonNull List<String> identities) {
        this.currentIdentity = currentIdentity;
        for (String id : identities) {
            this.identities.put(id, true);
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

    public String getCurrentIdentity() {
        return currentIdentity;
    }

    public Map<String, Boolean> getIdentities() {
        return identities;
    }

    @Exclude
    public Set<String> getIdentitiesIds() {
        return identities.keySet();
    }

    public Map<String, Boolean> getArchivedIdentities() {
        return archivedIdentities;
    }

    @Exclude
    public Set<String> getArchivedIdentitiesIds() {
        return archivedIdentities.keySet();
    }

    public Map<String, Boolean> getTokens() {
        return tokens;
    }

    @Exclude
    public Set<String> getTokenIds() {
        return tokens.keySet();
    }
}
