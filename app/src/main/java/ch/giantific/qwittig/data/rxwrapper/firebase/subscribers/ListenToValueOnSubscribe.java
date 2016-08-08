package ch.giantific.qwittig.data.rxwrapper.firebase.subscribers;

import android.support.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import ch.giantific.qwittig.domain.models.FirebaseModel;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;
import rx.subscriptions.BooleanSubscription;

/**
 * Created by fabio on 10.07.16.
 */
public class ListenToValueOnSubscribe<T extends FirebaseModel> implements Observable.OnSubscribe<T> {

    private final Query mQuery;
    private final Class<T> mType;

    public ListenToValueOnSubscribe(@NonNull Query query, @NonNull Class<T> type) {
        mQuery = query;
        mType = type;
    }

    @Override
    public void call(Subscriber<? super T> subscriber) {
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

        private final Subscriber<? super T> mSubscriber;
        private final Class<T> mType;

        public RxValueEventListener(@NonNull Subscriber<? super T> subscriber,
                                    @NonNull Class<T> type) {
            mSubscriber = subscriber;
            mType = type;
        }

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if (dataSnapshot.hasChildren() && !mSubscriber.isUnsubscribed()) {
                final T value = dataSnapshot.getValue(mType);
                if (value == null) {
                    mSubscriber.onError(new Throwable("unable to cast firebase data response to " + mType.getSimpleName()));
                    return;
                }

                value.setId(dataSnapshot.getKey());
                mSubscriber.onNext(value);
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
