package ch.giantific.qwittig.data.rxwrapper.firebase;

import android.support.annotation.NonNull;

import com.google.firebase.database.Query;

import java.util.List;

import ch.giantific.qwittig.data.rxwrapper.firebase.emitters.ChildEventsAsyncEmitter;
import ch.giantific.qwittig.data.rxwrapper.firebase.emitters.ValueAsyncEmitter;
import ch.giantific.qwittig.data.rxwrapper.firebase.emitters.ValuesAsyncEmitter;
import ch.giantific.qwittig.data.rxwrapper.firebase.emitters.ValuesOnceAsyncEmitter;
import ch.giantific.qwittig.data.rxwrapper.firebase.subscribers.ListenToHasChildOnSubscribe;
import ch.giantific.qwittig.data.rxwrapper.firebase.subscribers.ListenToValueOnceOnSubscribe;
import ch.giantific.qwittig.domain.models.FirebaseModel;
import rx.AsyncEmitter.BackpressureMode;
import rx.Observable;
import rx.Single;

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
        return Observable.fromAsync(new ValuesOnceAsyncEmitter<>(query, clazz), BackpressureMode.LATEST);
    }

    @NonNull
    public static <T extends FirebaseModel> Observable<T> observeValue(@NonNull final Query query,
                                                                       @NonNull final Class<T> clazz) {
        return Observable.fromAsync(new ValueAsyncEmitter<>(query, clazz), BackpressureMode.LATEST);
    }

    @NonNull
    public static <T extends FirebaseModel> Observable<List<T>> observeValues(@NonNull final Query query,
                                                                              @NonNull final Class<T> clazz) {
        return Observable.fromAsync(new ValuesAsyncEmitter<>(query, clazz), BackpressureMode.LATEST);
    }

    @NonNull
    public static <T extends FirebaseModel> Observable<RxChildEvent<T>> observeChildren(@NonNull final Query query,
                                                                                        @NonNull final Class<T> clazz) {
        return Observable.fromAsync(new ChildEventsAsyncEmitter<>(query, clazz), BackpressureMode.LATEST);
    }

    @NonNull
    public static Single<Boolean> checkForChild(@NonNull final Query query,
                                                @NonNull final String childPath) {
        return Single.create(new ListenToHasChildOnSubscribe(query, childPath));
    }
}
