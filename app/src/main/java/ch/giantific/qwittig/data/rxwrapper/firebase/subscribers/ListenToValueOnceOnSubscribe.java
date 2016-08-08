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

    private final Query mQuery;
    private final Class<T> mType;

    public ListenToValueOnceOnSubscribe(@NonNull Query query,
                                        @NonNull Class<T> type) {
        mQuery = query;
        mType = type;
    }

    @Override
    public void call(SingleSubscriber<? super T> singleSubscriber) {
        mQuery.addListenerForSingleValueEvent(new RxSingleValueEventListener<>(singleSubscriber, mType));
    }

    private static class RxSingleValueEventListener<T extends FirebaseModel> implements ValueEventListener {

        private final SingleSubscriber<? super T> mSubscriber;
        private final Class<T> mType;

        public RxSingleValueEventListener(@NonNull SingleSubscriber<? super T> subscriber,
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
                mSubscriber.onSuccess(value);
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
