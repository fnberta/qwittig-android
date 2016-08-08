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
public class Group implements FirebaseModel {

    public static final String PATH = "groups";
    public static final String PATH_NAME = "name";
    public static final String PATH_CURRENCY = "currency";
    public static final String PATH_IDENTITIES = "identities";
    private String mId;
    @PropertyName(PATH_CREATED_AT)
    private long mCreatedAt;
    @PropertyName(PATH_NAME)
    private String mName;
    @PropertyName(PATH_CURRENCY)
    private String mCurrency;
    @PropertyName(PATH_IDENTITIES)
    private Map<String, Boolean> mIdentities;

    public Group() {
        // required for firebase de-/serialization
    }

    public Group(@NonNull String name, @NonNull String currency,
                 @NonNull List<String> identities) {
        this(name, currency);

        mIdentities = new HashMap<>();
        for (String id : identities) {
            mIdentities.put(id, true);
        }
    }

    public Group(@NonNull String name, @NonNull String currency) {
        mName = name;
        mCurrency = currency;
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
        return null;
    }

    @Exclude
    public Date getCreatedAtDate() {
        return new Date(mCreatedAt);
    }

    public String getName() {
        return mName;
    }

    public String getCurrency() {
        return mCurrency;
    }

    public Map<String, Boolean> getIdentities() {
        return mIdentities;
    }

    @Exclude
    public Set<String> getIdentitiesIds() {
        return mIdentities.keySet();
    }

    @Exclude
    public Map<String, Object> toMap() {
        final Map<String, Object> result = new HashMap<>();
        result.put(PATH_CREATED_AT, ServerValue.TIMESTAMP);
        result.put(PATH_NAME, mName);
        result.put(PATH_CURRENCY, mCurrency);
        result.put(PATH_IDENTITIES, mIdentities);

        return result;
    }
}

