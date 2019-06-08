package ch.giantific.qwittig.utils.rxwrapper.firebase.listeners;

import android.support.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import rx.AsyncEmitter;

/**
 * Created by fabio on 24.09.16.
 */
public class RxValueEventListener implements ValueEventListener {

    private final AsyncEmitter<DataSnapshot> asyncEmitter;
    private final boolean singleEvent;

    public RxValueEventListener(@NonNull AsyncEmitter<DataSnapshot> asyncEmitter,
                                boolean singleEvent) {
        this.asyncEmitter = asyncEmitter;
        this.singleEvent = singleEvent;
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        asyncEmitter.onNext(dataSnapshot);
        if (singleEvent) {
            asyncEmitter.onCompleted();
        }
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
        asyncEmitter.onError(databaseError.toException());
    }
}
