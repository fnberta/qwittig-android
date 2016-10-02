package ch.giantific.qwittig.utils.rxwrapper.firebase;

import android.support.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.Query;

import java.util.List;

import ch.giantific.qwittig.utils.rxwrapper.firebase.emitters.ChildEventEmitter;
import ch.giantific.qwittig.utils.rxwrapper.firebase.emitters.SingleValueEmitter;
import ch.giantific.qwittig.utils.rxwrapper.firebase.emitters.ValueEventEmitter;
import ch.giantific.qwittig.domain.models.FirebaseModel;
import rx.AsyncEmitter.BackpressureMode;
import rx.Observable;
import rx.Single;
import rx.functions.Func1;

/**
 * Created by Nick Moskalenko on 15/05/2016.
 */
public class RxFirebaseDatabase {

    @NonNull
    public static <T extends FirebaseModel> Single<T> observeValueOnce(@NonNull final Query query,
                                                                       @NonNull final Class<T> type) {
        return Observable.fromEmitter(new SingleValueEmitter(query), BackpressureMode.LATEST)
                .map(DataMapper.mapWithId(type))
                .toSingle();
    }

    @NonNull
    public static <T extends FirebaseModel> Observable<T> observeValueListOnce(@NonNull final Query query,
                                                                               @NonNull final Class<T> type) {
        return Observable.fromEmitter(new SingleValueEmitter(query), BackpressureMode.LATEST)
                .map(DataMapper.mapListWithId(type))
                .concatMapIterable(l -> l);
    }

    @NonNull
    public static <T extends FirebaseModel> Observable<T> observeValue(@NonNull final Query query,
                                                                       @NonNull final Class<T> type) {
        return Observable.fromEmitter(new ValueEventEmitter(query), BackpressureMode.LATEST)
                .map(DataMapper.mapWithId(type));
    }

    @NonNull
    public static <T> Observable<T> observeValue(@NonNull final Query query,
                                                 @NonNull Func1<DataSnapshot, T> dataMapper) {
        return Observable.fromEmitter(new ValueEventEmitter(query), BackpressureMode.LATEST)
                .map(dataMapper);
    }

    @NonNull
    public static <T extends FirebaseModel> Observable<List<T>> observeValueList(@NonNull final Query query,
                                                                                 @NonNull final Class<T> type) {
        return Observable.fromEmitter(new ValueEventEmitter(query), BackpressureMode.LATEST)
                .map(DataMapper.mapListWithId(type));
    }

    @NonNull
    public static <T extends FirebaseModel> Observable<RxChildEvent<T>> observeChildren(@NonNull final Query query,
                                                                                        @NonNull final Class<T> type) {
        return Observable.fromEmitter(new ChildEventEmitter(query), BackpressureMode.LATEST)
                .map(DataMapper.mapChildWithId(type));
    }
}
