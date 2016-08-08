package ch.giantific.qwittig.data.rxwrapper.firebase.subscribers;

import android.support.annotation.NonNull;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;

import ch.giantific.qwittig.data.rxwrapper.firebase.RxChildEvent;
import ch.giantific.qwittig.data.rxwrapper.firebase.RxChildEvent.EventType;
import ch.giantific.qwittig.domain.models.FirebaseModel;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;
import rx.subscriptions.BooleanSubscription;

/**
 * Created by fabio on 10.07.16.
 */
public class ListenToChildEventsOnSubscribe<T extends FirebaseModel> implements Observable.OnSubscribe<RxChildEvent<T>> {

    private final Query mQuery;
    private final Class<T> mType;

    public ListenToChildEventsOnSubscribe(@NonNull Query query,
                                          @NonNull Class<T> type) {
        mQuery = query;
        mType = type;
    }

    @Override
    public void call(Subscriber<? super RxChildEvent<T>> subscriber) {
        final ChildEventListener childEventListener =
                mQuery.addChildEventListener(new RxChildEventListener<>(subscriber, mType));
        subscriber.add(BooleanSubscription.create(new Action0() {
                    @Override
                    public void call() {
                        mQuery.removeEventListener(childEventListener);
                    }
                })
        );
    }

    private static class RxChildEventListener<T extends FirebaseModel> implements ChildEventListener {

        private final Subscriber<? super RxChildEvent<T>> mSubscriber;
        private final Class<T> mType;

        public RxChildEventListener(@NonNull Subscriber<? super RxChildEvent<T>> subscriber,
                                    @NonNull Class<T> type) {
            mSubscriber = subscriber;
            mType = type;
        }

        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String previousChildKey) {
            if (!mSubscriber.isUnsubscribed()) {
                final T value = dataSnapshot.getValue(mType);
                if (value == null) {
                    mSubscriber.onError(new Throwable("unable to cast firebase data response to " + mType.getSimpleName()));
                    return;
                }

                value.setId(dataSnapshot.getKey());
                mSubscriber.onNext(new RxChildEvent<>(EventType.ADDED, value, previousChildKey));
            }
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String previousChildKey) {
            if (!mSubscriber.isUnsubscribed()) {
                final T value = dataSnapshot.getValue(mType);
                if (value == null) {
                    mSubscriber.onError(new Throwable("unable to cast firebase data response to " + mType.getSimpleName()));
                    return;
                }

                value.setId(dataSnapshot.getKey());
                mSubscriber.onNext(new RxChildEvent<>(EventType.CHANGED, value, previousChildKey));
            }
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            if (!mSubscriber.isUnsubscribed()) {
                final T value = dataSnapshot.getValue(mType);
                if (value == null) {
                    mSubscriber.onError(new Throwable("unable to cast firebase data response to " + mType.getSimpleName()));
                    return;
                }

                value.setId(dataSnapshot.getKey());
                mSubscriber.onNext(new RxChildEvent<>(EventType.REMOVED, value, null));
            }
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String previousChildKey) {
            if (!mSubscriber.isUnsubscribed()) {
                final T value = dataSnapshot.getValue(mType);
                if (value == null) {
                    mSubscriber.onError(new Throwable("unable to cast firebase data response to " + mType.getSimpleName()));
                    return;
                }

                value.setId(dataSnapshot.getKey());
                mSubscriber.onNext(new RxChildEvent<>(EventType.MOVED, value, previousChildKey));
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
