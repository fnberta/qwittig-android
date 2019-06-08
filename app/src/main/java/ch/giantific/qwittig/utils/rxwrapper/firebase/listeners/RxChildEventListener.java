package ch.giantific.qwittig.utils.rxwrapper.firebase.listeners;

import android.support.annotation.NonNull;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import ch.giantific.qwittig.utils.rxwrapper.firebase.RxChildEvent.EventType;
import ch.giantific.qwittig.utils.rxwrapper.firebase.RxChildSnapshotEvent;
import rx.AsyncEmitter;

/**
 * Created by fabio on 24.09.16.
 */
public class RxChildEventListener implements ChildEventListener {

    private final AsyncEmitter<RxChildSnapshotEvent> asyncEmitter;

    public RxChildEventListener(@NonNull AsyncEmitter<RxChildSnapshotEvent> asyncEmitter) {
        this.asyncEmitter = asyncEmitter;
    }

    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String previousChildKey) {
        asyncEmitter.onNext(new RxChildSnapshotEvent(EventType.ADDED, dataSnapshot, previousChildKey));
    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String previousChildKey) {
        asyncEmitter.onNext(new RxChildSnapshotEvent(EventType.CHANGED, dataSnapshot, previousChildKey));
    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {
        asyncEmitter.onNext(new RxChildSnapshotEvent(EventType.REMOVED, dataSnapshot, null));
    }

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String previousChildKey) {
        asyncEmitter.onNext(new RxChildSnapshotEvent(EventType.MOVED, dataSnapshot, previousChildKey));
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
        asyncEmitter.onError(databaseError.toException());
    }
}
