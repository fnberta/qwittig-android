package ch.giantific.qwittig.data.rxwrapper.firebase;

import android.support.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import ch.giantific.qwittig.data.rxwrapper.firebase.subscribers.ListenToChildEventsOnSubscribe;
import ch.giantific.qwittig.data.rxwrapper.firebase.subscribers.ListenToValueOnSubscribe;
import ch.giantific.qwittig.data.rxwrapper.firebase.subscribers.ListenToValueOnceOnSubscribe;
import ch.giantific.qwittig.data.rxwrapper.firebase.subscribers.ListenToValuesOnSubscribe;
import ch.giantific.qwittig.data.rxwrapper.firebase.subscribers.ListenToValuesOnceOnSubscribe;
import ch.giantific.qwittig.domain.models.FirebaseModel;
import rx.Observable;
import rx.Single;
import rx.SingleSubscriber;

/**
 * Created by Nick Moskalenko on 15/05/2016.
 */
public class RxFirebaseDatabase {

    @NonNull
    public static <T extends FirebaseModel> Single<T> observeValueOnce(@NonNull final Query query,
                                                                       @NonNull final Class<T> clazz) {
        return Single.create(new ListenToValueOnceOnSubscribe<>(query, clazz));
    }

    @NonNull
    public static <T extends FirebaseModel> Observable<T> observeValuesOnce(@NonNull final Query query,
                                                                            @NonNull final Class<T> clazz) {
        return Observable.create(new ListenToValuesOnceOnSubscribe<>(query, clazz));
    }

    @NonNull
    public static <T extends FirebaseModel> Observable<T> observeValue(@NonNull final Query query,
                                                                       @NonNull final Class<T> clazz) {
        return Observable.create(new ListenToValueOnSubscribe<>(query, clazz));
    }

    @NonNull
    public static <T extends FirebaseModel> Observable<List<T>> observeValues(@NonNull final Query query,
                                                                              @NonNull final Class<T> clazz) {
        return Observable.create(new ListenToValuesOnSubscribe<>(query, clazz));
    }

    @NonNull
    public static <T extends FirebaseModel> Observable<RxChildEvent<T>> observeChildren(@NonNull final Query query,
                                                                                        @NonNull final Class<T> clazz) {
        return Observable.create(new ListenToChildEventsOnSubscribe<>(query, clazz));
    }

    @NonNull
    public static Single<Boolean> checkForChild(@NonNull final Query query,
                                                @NonNull final String childId) {
        return Single.create(new Single.OnSubscribe<Boolean>() {
            @Override
            public void call(final SingleSubscriber<? super Boolean> singleSubscriber) {
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (singleSubscriber.isUnsubscribed()) {
                            return;
                        }

                        singleSubscriber.onSuccess(dataSnapshot.hasChild(childId));
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        if (!singleSubscriber.isUnsubscribed()) {
                            singleSubscriber.onError(databaseError.toException());
                        }
                    }
                });
            }
        });
    }
}
