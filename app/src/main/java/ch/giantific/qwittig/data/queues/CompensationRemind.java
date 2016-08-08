package ch.giantific.qwittig.data.queues;

import android.support.annotation.NonNull;

import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.PropertyName;

/**
 * Created by fabio on 28.07.16.
 */
@IgnoreExtraProperties
public class CompensationRemind {

    private static final String TYPE = "REMIND_DEBTOR";
    @PropertyName("compensationId")
    private String mCompensationId;
    @PropertyName("type")
    private String mType;

    public CompensationRemind() {
        // required for firebase de-/serialization
    }

    public CompensationRemind(@NonNull String compensationId) {
        mCompensationId = compensationId;
        mType = TYPE;
    }

    public String getCompensationId() {
        return mCompensationId;
    }

    public String getType() {
        return mType;
    }
}
