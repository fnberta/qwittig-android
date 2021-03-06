package ch.giantific.qwittig.domain.models;

import android.support.annotation.NonNull;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.ServerValue;

import java.util.Date;
import java.util.Map;

/**
 * Created by fabio on 02.06.16.
 */
@IgnoreExtraProperties
public class OcrData implements FirebaseModel {

    public static final String BASE_PATH = "ocrData";

    public static final String PATH_PURCHASE = "purchase";
    public static final String PATH_USER = "user";
    public static final String PATH_DATA = "data";
    public static final String PATH_RECEIPT = "receipt";
    public static final String PATH_PROCESSED = "processed";

    private String id;
    private long createdAt;
    private String purchase;
    private String user;
    private Map<String, Object> data;
    private String receipt;
    private boolean processed;

    public OcrData() {
        // required for firebase de-/serialization
    }

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

    public String getPurchase() {
        return purchase;
    }

    public String getUser() {
        return user;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public String getReceipt() {
        return receipt;
    }

    public boolean isProcessed() {
        return processed;
    }
}
