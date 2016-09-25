package ch.giantific.qwittig.data.rxwrapper.firebase;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.firebase.database.DataSnapshot;

import ch.giantific.qwittig.data.rxwrapper.firebase.RxChildEvent.EventType;

/**
 * Created by fabio on 19.07.16.
 */
public class RxChildSnapshotEvent {

    private final int eventType;
    private final DataSnapshot snapshot;
    private final String previousChildKey;

    public RxChildSnapshotEvent(@EventType int eventType,
                                @NonNull DataSnapshot snapshot,
                                @Nullable String previousChildKey) {
        this.eventType = eventType;
        this.snapshot = snapshot;
        this.previousChildKey = previousChildKey;
    }

    @EventType
    public int getEventType() {
        return eventType;
    }

    @NonNull
    public DataSnapshot getSnapshot() {
        return snapshot;
    }

    @Nullable
    public String getPreviousChildKey() {
        return previousChildKey;
    }
}
