package ch.giantific.qwittig.data.rxwrapper.firebase.subscribers;

import android.support.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import rx.Single;
import rx.SingleSubscriber;

/**
 * Created by fabio on 10.07.16.
 */
public class ListenToHasChildOnSubscribe implements Single.OnSubscribe<Boolean> {

    private final Query query;
    private final String childPath;

    public ListenToHasChildOnSubscribe(@NonNull Query query,
                                       @NonNull String childPath) {
        this.query = query;
        this.childPath = childPath;
    }

    @Override
    public void call(SingleSubscriber<? super Boolean> singleSubscriber) {
        query.addListenerForSingleValueEvent(new RxCheckChildListener(singleSubscriber, childPath));
    }

    private static class RxCheckChildListener implements ValueEventListener {

        private final SingleSubscriber<? super Boolean> subscriber;
        private final String childPath;

        RxCheckChildListener(@NonNull SingleSubscriber<? super Boolean> subscriber,
                             @NonNull String childPath) {
            this.subscriber = subscriber;
            this.childPath = childPath;
        }

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if (subscriber.isUnsubscribed()) {
                return;
            }

            subscriber.onSuccess(dataSnapshot.hasChild(childPath));
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            if (!subscriber.isUnsubscribed()) {
                subscriber.onError(databaseError.toException());
            }
        }
    }
}
