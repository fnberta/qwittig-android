package ch.giantific.qwittig.domain.models;

import android.support.annotation.NonNull;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.PropertyName;
import com.google.firebase.database.ServerValue;

import java.util.Date;
import java.util.Map;

/**
 * Created by fabio on 02.06.16.
 */
@IgnoreExtraProperties
public class OcrData implements FirebaseModel {

    public static final String PATH = "ocrData";
    public static final String PATH_USER = "user";
    public static final String PATH_DATA = "data";
    public static final String PATH_RECEIPT = "receipt";
    private String mId;
    @PropertyName(PATH_CREATED_AT)
    private long mCreatedAt;
    @PropertyName(PATH_USER)
    private String mUser;
    @PropertyName(PATH_DATA)
    private Map<String, Object> mData;
    @PropertyName(PATH_RECEIPT)
    private String mReceipt;

    public OcrData() {
        // required for firebase de-/serialization
    }

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

    public String getUser() {
        return mUser;
    }

    public Map<String, Object> getData() {
        return mData;
    }

    public String getReceipt() {
        return mReceipt;
    }
}
