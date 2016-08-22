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
public class Group implements FirebaseModel {

    public static final String BASE_PATH = "groups";

    public static final String PATH_NAME = "name";
    public static final String PATH_CURRENCY = "currency";
    public static final String PATH_IDENTITIES = "identities";

    private String id;
    private long createdAt;
    private String name;
    private String currency;
    private Map<String, Boolean> identities;

    public Group() {
        // required for firebase de-/serialization
    }

    public Group(@NonNull String name, @NonNull String currency,
                 @NonNull List<String> identities) {
        this(name, currency);

        this.identities = new HashMap<>();
        for (String id : identities) {
            this.identities.put(id, true);
        }
    }

    public Group(@NonNull String name, @NonNull String currency) {
        this.name = name;
        this.currency = currency;
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
        return null;
    }

    @Exclude
    public Date getCreatedAtDate() {
        return new Date(createdAt);
    }

    public String getName() {
        return name;
    }

    public String getCurrency() {
        return currency;
    }

    public Map<String, Boolean> getIdentities() {
        return identities;
    }

    @Exclude
    public Set<String> getIdentitiesIds() {
        return identities.keySet();
    }

    @Exclude
    public Map<String, Object> toMap() {
        final Map<String, Object> result = new HashMap<>();
        result.put(PATH_CREATED_AT, ServerValue.TIMESTAMP);
        result.put(PATH_NAME, name);
        result.put(PATH_CURRENCY, currency);
        result.put(PATH_IDENTITIES, identities);

        return result;
    }
}

