package ch.giantific.qwittig.data.rxwrapper.firebase.subscribers;

import android.support.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import ch.giantific.qwittig.domain.models.FirebaseModel;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;
import rx.subscriptions.BooleanSubscription;

/**
 * Created by fabio on 10.07.16.
 */
public class ListenToValuesOnSubscribe<T extends FirebaseModel> implements Observable.OnSubscribe<List<T>> {

    private final Query mQuery;
    private final Class<T> mType;

    public ListenToValuesOnSubscribe(@NonNull Query query, @NonNull Class<T> type) {
        mQuery = query;
        mType = type;
    }

    @Override
    public void call(Subscriber<? super List<T>> subscriber) {
        final ValueEventListener eventListener =
                mQuery.addValueEventListener(new RxValueEventListener<>(subscriber, mType));
        subscriber.add(BooleanSubscription.create(new Action0() {
                    @Override
                    public void call() {
                        mQuery.removeEventListener(eventListener);
                    }
                })
        );
    }

    private static class RxValueEventListener<T extends FirebaseModel> implements ValueEventListener {

        private final Subscriber<? super List<T>> mSubscriber;
        private final Class<T> mType;

        public RxValueEventListener(@NonNull Subscriber<? super List<T>> subscriber,
                                    @NonNull Class<T> type) {
            mSubscriber = subscriber;
            mType = type;
        }

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            final List<T> items = new ArrayList<>();
            if (dataSnapshot.hasChildren()) {
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    final T value = childSnapshot.getValue(mType);
                    if (value != null) {
                        value.setId(childSnapshot.getKey());
                        items.add(value);
                    } else if (!mSubscriber.isUnsubscribed()) {
                        mSubscriber.onError(new Throwable("unable to cast firebase data response to " + mType.getSimpleName()));
                    }
                }
            }

            if (!mSubscriber.isUnsubscribed()) {
                mSubscriber.onNext(items);
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            if (!mSubscriber.isUnsubscribed()) {
                mSubscriber.onError(databaseError.toException());
            }
        }
    }
}
