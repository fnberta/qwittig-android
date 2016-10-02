package ch.giantific.qwittig.utils.rxwrapper.firebase;

import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import ch.giantific.qwittig.domain.models.FirebaseModel;

/**
 * Created by fabio on 19.07.16.
 */
public class RxChildEvent<T extends FirebaseModel> {

    private final int eventType;
    private final T value;
    private final String previousChildKey;

    public RxChildEvent(@EventType int eventType,
                        @NonNull T value,
                        @Nullable String previousChildKey) {
        this.eventType = eventType;
        this.value = value;
        this.previousChildKey = previousChildKey;
    }

    @EventType
    public int getEventType() {
        return eventType;
    }

    @NonNull
    public T getValue() {
        return value;
    }

    @Nullable
    public String getPreviousChildKey() {
        return previousChildKey;
    }

    @IntDef({EventType.NONE, EventType.ADDED, EventType.CHANGED, EventType.REMOVED, EventType.MOVED})
    @Retention(RetentionPolicy.SOURCE)
    public @interface EventType {
        int NONE = -1;
        int ADDED = 0;
        int CHANGED = 1;
        int REMOVED = 2;
        int MOVED = 3;
    }
}
