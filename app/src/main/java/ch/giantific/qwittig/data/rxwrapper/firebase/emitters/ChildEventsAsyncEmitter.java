package ch.giantific.qwittig.data.rxwrapper.firebase.emitters;

import android.support.annotation.NonNull;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;

import ch.giantific.qwittig.data.rxwrapper.firebase.RxChildEvent;
import ch.giantific.qwittig.domain.models.FirebaseModel;
import rx.AsyncEmitter;
import rx.functions.Action1;

/**
 * Created by fabio on 20.08.16.
 */

public class ChildEventsAsyncEmitter<T extends FirebaseModel> implements Action1<AsyncEmitter<RxChildEvent<T>>> {

    private final Query query;
    private final Class<T> type;

    public ChildEventsAsyncEmitter(@NonNull Query query, @NonNull Class<T> type) {
        this.type = type;
        this.query = query;
    }

    @Override
    public void call(AsyncEmitter<RxChildEvent<T>> asyncEmitter) {
        final ChildEventListener childEventListener =
                query.addChildEventListener(new RxChildEventListener<T>(asyncEmitter, type));
        asyncEmitter.setCancellation(new AsyncEmitter.Cancellable() {
            @Override
            public void cancel() throws Exception {
                query.removeEventListener(childEventListener);
            }
        });
    }

    private static class RxChildEventListener<T extends FirebaseModel> implements ChildEventListener {

        private final AsyncEmitter<RxChildEvent<T>> asyncEmitter;
        private final Class<T> type;

        RxChildEventListener(@NonNull AsyncEmitter<RxChildEvent<T>> asyncEmitter,
                             @NonNull Class<T> type) {
            this.asyncEmitter = asyncEmitter;
            this.type = type;
        }

        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String previousChildKey) {
            final T value = dataSnapshot.getValue(type);
            if (value == null) {
                asyncEmitter.onError(new Throwable("unable to cast firebase data response to " + type.getSimpleName()));
                return;
            }

            value.setId(dataSnapshot.getKey());
            asyncEmitter.onNext(new RxChildEvent<>(RxChildEvent.EventType.ADDED, value, previousChildKey));
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String previousChildKey) {
            final T value = dataSnapshot.getValue(type);
            if (value == null) {
                asyncEmitter.onError(new Throwable("unable to cast firebase data response to " + type.getSimpleName()));
                return;
            }

            value.setId(dataSnapshot.getKey());
            asyncEmitter.onNext(new RxChildEvent<>(RxChildEvent.EventType.CHANGED, value, previousChildKey));
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            final T value = dataSnapshot.getValue(type);
            if (value == null) {
                asyncEmitter.onError(new Throwable("unable to cast firebase data response to " + type.getSimpleName()));
                return;
            }

            value.setId(dataSnapshot.getKey());
            asyncEmitter.onNext(new RxChildEvent<>(RxChildEvent.EventType.REMOVED, value, null));
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String previousChildKey) {
            final T value = dataSnapshot.getValue(type);
            if (value == null) {
                asyncEmitter.onError(new Throwable("unable to cast firebase data response to " + type.getSimpleName()));
                return;
            }

            value.setId(dataSnapshot.getKey());
            asyncEmitter.onNext(new RxChildEvent<>(RxChildEvent.EventType.MOVED, value, previousChildKey));
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            asyncEmitter.onError(databaseError.toException());
        }
    }
}
