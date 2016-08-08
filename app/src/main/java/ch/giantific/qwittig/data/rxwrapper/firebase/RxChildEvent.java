package ch.giantific.qwittig.data.rxwrapper.firebase;

import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import ch.giantific.qwittig.data.rxwrapper.firebase.subscribers.ListenToChildEventsOnSubscribe;
import ch.giantific.qwittig.domain.models.FirebaseModel;

/**
 * Created by fabio on 19.07.16.
 */
public class RxChildEvent<T extends FirebaseModel> {

    private final int mEventType;
    private final T mValue;
    private final String mPreviousChildKey;

    public RxChildEvent(@EventType int eventType,
                        @NonNull T value,
                        @Nullable String previousChildKey) {
        mEventType = eventType;
        mValue = value;
        mPreviousChildKey = previousChildKey;
    }

    @EventType
    public int getEventType() {
        return mEventType;
    }

    @NonNull
    public T getValue() {
        return mValue;
    }

    @Nullable
    public String getPreviousChildKey() {
        return mPreviousChildKey;
    }

    @IntDef({EventType.ADDED, EventType.CHANGED, EventType.REMOVED, EventType.MOVED})
    @Retention(RetentionPolicy.SOURCE)
    public @interface EventType {
        int ADDED = 0;
        int CHANGED = 1;
        int REMOVED = 2;
        int MOVED = 3;
    }
}
