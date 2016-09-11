package ch.giantific.qwittig.data.rxwrapper.firebase.emitters;

import android.support.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import ch.giantific.qwittig.domain.models.FirebaseModel;
import rx.AsyncEmitter;
import rx.functions.Action1;

/**
 * Created by fabio on 20.08.16.
 */

public class ValuesAsyncEmitter<T extends FirebaseModel> implements Action1<AsyncEmitter<List<T>>> {

    private final Query query;
    private final Class<T> type;

    public ValuesAsyncEmitter(@NonNull Query query, @NonNull Class<T> type) {
        this.type = type;
        this.query = query;
    }

    @Override
    public void call(AsyncEmitter<List<T>> asyncEmitter) {
        final ValueEventListener valueEventListener =
                query.addValueEventListener(new RxValuesEventListener<>(asyncEmitter, type));
        asyncEmitter.setCancellation(() -> query.removeEventListener(valueEventListener));
    }

    private static class RxValuesEventListener<T extends FirebaseModel> implements ValueEventListener {

        private final AsyncEmitter<List<T>> asyncEmitter;
        private final Class<T> type;

        RxValuesEventListener(@NonNull AsyncEmitter<List<T>> asyncEmitter,
                              @NonNull Class<T> type) {
            this.asyncEmitter = asyncEmitter;
            this.type = type;
        }

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            final List<T> items = new ArrayList<>();
            if (dataSnapshot.hasChildren()) {
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    final T value = childSnapshot.getValue(type);
                    if (value != null) {
                        value.setId(childSnapshot.getKey());
                        items.add(value);
                    } else {
                        asyncEmitter.onError(new Throwable("unable to cast firebase data response to " + type.getSimpleName()));
                    }
                }
            }

            asyncEmitter.onNext(items);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            asyncEmitter.onError(databaseError.toException());
        }
    }
}
