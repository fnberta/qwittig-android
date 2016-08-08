package ch.giantific.qwittig.domain.models;

import android.support.annotation.NonNull;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.PropertyName;
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

    public static final String PATH = "users";
    public static final String PATH_CURRENT_IDENTITY = "currentIdentity";
    public static final String PATH_IDENTITIES = "identities";
    public static final String PATH_ARCHIVED_IDENTITIES = "archivedIdentities";
    public static final String PATH_TOKENS = "tokens";
    private String mId;
    @PropertyName(PATH_CREATED_AT)
    private long mCreatedAt;
    @PropertyName(PATH_CURRENT_IDENTITY)
    private String mCurrentIdentity;
    @PropertyName(PATH_IDENTITIES)
    private Map<String, Boolean> mIdentities = new HashMap<>();
    @PropertyName(PATH_ARCHIVED_IDENTITIES)
    private Map<String, Boolean> mArchivedIdentities = new HashMap<>();
    @PropertyName(PATH_TOKENS)
    private Map<String, Boolean> mTokens;

    public User() {
        // required for firebase de-/serialization
    }

    public User(@NonNull String currentIdentity, @NonNull List<String> identities,
                @NonNull List<String> archivedIdentities) {
        this(currentIdentity, identities);

        for (String archivedId : archivedIdentities) {
            mArchivedIdentities.put(archivedId, true);
        }
    }

    public User(@NonNull String currentIdentity,
                @NonNull List<String> identities) {
        mCurrentIdentity = currentIdentity;
        for (String id : identities) {
            mIdentities.put(id, true);
        }
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

    public String getCurrentIdentity() {
        return mCurrentIdentity;
    }

    public Map<String, Boolean> getIdentities() {
        return mIdentities;
    }

    @Exclude
    public Set<String> getIdentitiesIds() {
        return mIdentities.keySet();
    }

    public Map<String, Boolean> getArchivedIdentities() {
        return mArchivedIdentities;
    }

    @Exclude
    public Set<String> getArchivedIdentitiesIds() {
        return mArchivedIdentities.keySet();
    }

    public Map<String, Boolean> getTokens() {
        return mTokens;
    }

    @Exclude
    public Set<String> getTokenIds() {
        return mTokens.keySet();
    }
}
