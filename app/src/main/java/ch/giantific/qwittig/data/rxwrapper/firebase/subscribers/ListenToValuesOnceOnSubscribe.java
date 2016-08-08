package ch.giantific.qwittig.data.rxwrapper.firebase.subscribers;

import android.support.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import ch.giantific.qwittig.domain.models.FirebaseModel;
import rx.Observable;
import rx.Subscriber;

/**
 * Created by fabio on 10.07.16.
 */
public class ListenToValuesOnceOnSubscribe<T extends FirebaseModel> implements Observable.OnSubscribe<T> {

    private final Query mQuery;
    private final Class<T> mType;

    public ListenToValuesOnceOnSubscribe(@NonNull Query query, @NonNull Class<T> type) {
        mQuery = query;
        mType = type;
    }

    @Override
    public void call(Subscriber<? super T> subscriber) {
        mQuery.addListenerForSingleValueEvent(new RxSingleValuesEventListener<>(subscriber, mType));
    }

    private static class RxSingleValuesEventListener<T extends FirebaseModel> implements ValueEventListener {

        private final Subscriber<? super T> mSubscriber;
        private final Class<T> mType;

        public RxSingleValuesEventListener(@NonNull Subscriber<? super T> subscriber,
                                           @NonNull Class<T> type) {
            mSubscriber = subscriber;
            mType = type;
        }

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if (dataSnapshot.hasChildren()) {
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    final T value = childSnapshot.getValue(mType);
                    if (!mSubscriber.isUnsubscribed()) {
                        if (value != null) {
                            value.setId(childSnapshot.getKey());
                            mSubscriber.onNext(value);
                        } else {
                            mSubscriber.onError(new Throwable("unable to cast firebase data response to " + mType.getSimpleName()));
                        }
                    }

                }
            }

            if (!mSubscriber.isUnsubscribed()) {
                mSubscriber.onCompleted();
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
