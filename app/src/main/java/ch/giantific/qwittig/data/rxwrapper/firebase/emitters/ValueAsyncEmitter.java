package ch.giantific.qwittig.data.rxwrapper.firebase.emitters;

import android.support.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import ch.giantific.qwittig.domain.models.FirebaseModel;
import rx.AsyncEmitter;
import rx.functions.Action1;

/**
 * Created by fabio on 20.08.16.
 */

public class ValueAsyncEmitter<T extends FirebaseModel> implements Action1<AsyncEmitter<T>> {

    private final Query query;
    private final Class<T> type;

    public ValueAsyncEmitter(@NonNull Query query, @NonNull Class<T> type) {
        this.type = type;
        this.query = query;
    }

    @Override
    public void call(AsyncEmitter<T> asyncEmitter) {
        final ValueEventListener valueEventListener =
                query.addValueEventListener(new RxValueEventListener<>(asyncEmitter, type));
        asyncEmitter.setCancellation(new AsyncEmitter.Cancellable() {
            @Override
            public void cancel() throws Exception {
                query.removeEventListener(valueEventListener);
            }
        });
    }

    private static class RxValueEventListener<T extends FirebaseModel> implements ValueEventListener {

        private final AsyncEmitter<T> asyncEmitter;
        private final Class<T> type;

        RxValueEventListener(@NonNull AsyncEmitter<T> asyncEmitter,
                             @NonNull Class<T> type) {
            this.asyncEmitter = asyncEmitter;
            this.type = type;
        }

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if (dataSnapshot.hasChildren()) {
                final T value = dataSnapshot.getValue(type);
                if (value == null) {
                    asyncEmitter.onError(new Throwable("unable to cast firebase data response to " + type.getSimpleName()));
                    return;
                }

                value.setId(dataSnapshot.getKey());
                asyncEmitter.onNext(value);
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            asyncEmitter.onError(databaseError.toException());
        }
    }
}
