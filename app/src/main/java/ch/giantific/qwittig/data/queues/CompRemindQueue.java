package ch.giantific.qwittig.data.queues;

import android.support.annotation.NonNull;
import android.support.annotation.StringDef;

import com.google.firebase.database.IgnoreExtraProperties;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by fabio on 28.07.16.
 */
@IgnoreExtraProperties
public class CompRemindQueue {

    private String compensationId;
    private String type;

    public CompRemindQueue() {
        // required for firebase de-/serialization
    }

    public CompRemindQueue(@NonNull String compensationId, @NonNull @RemindType String type) {
        this.compensationId = compensationId;
        this.type = type;
    }

    public String getCompensationId() {
        return compensationId;
    }

    @RemindType
    public String getType() {
        return type;
    }

    @StringDef({RemindType.REMIND_DEBTOR})
    @Retention(RetentionPolicy.SOURCE)
    public @interface RemindType {
        String REMIND_DEBTOR = "COMPENSATION_REMIND_DEBTOR";
    }
}
