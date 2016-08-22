package ch.giantific.qwittig.data.rxwrapper.firebase.subscribers;

import android.support.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import ch.giantific.qwittig.domain.models.FirebaseModel;
import rx.Single;
import rx.SingleSubscriber;

/**
 * Created by fabio on 10.07.16.
 */
public class ListenToValueOnceOnSubscribe<T extends FirebaseModel> implements Single.OnSubscribe<T> {

    private final Query query;
    private final Class<T> type;

    public ListenToValueOnceOnSubscribe(@NonNull Query query,
                                        @NonNull Class<T> type) {
        this.query = query;
        this.type = type;
    }

    @Override
    public void call(SingleSubscriber<? super T> singleSubscriber) {
        query.addListenerForSingleValueEvent(new RxSingleValueEventListener<>(singleSubscriber, type));
    }

    private static class RxSingleValueEventListener<T extends FirebaseModel> implements ValueEventListener {

        private final SingleSubscriber<? super T> subscriber;
        private final Class<T> type;

        RxSingleValueEventListener(@NonNull SingleSubscriber<? super T> subscriber,
                                   @NonNull Class<T> type) {
            this.subscriber = subscriber;
            this.type = type;
        }

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if (dataSnapshot.hasChildren() && !subscriber.isUnsubscribed()) {
                final T value = dataSnapshot.getValue(type);
                if (value == null) {
                    subscriber.onError(new Throwable("unable to cast firebase data response to " + type.getSimpleName()));
                    return;
                }

                value.setId(dataSnapshot.getKey());
                subscriber.onSuccess(value);
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            if (!subscriber.isUnsubscribed()) {
                subscriber.onError(databaseError.toException());
            }
        }
    }
}
