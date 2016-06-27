package ch.giantific.qwittig.data.bus;

import android.support.annotation.NonNull;

import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

/**
 * Created by fabio on 15.06.16.
 */
public class RxBus<T> {

    private final Subject<T, T> mSubject = PublishSubject.create();

    public RxBus() {
    }

    public <E extends T> void post(@NonNull E event) {
        mSubject.onNext(event);
    }

    public Observable<T> observe() {
        return mSubject.asObservable();
    }

    public <E extends T> Observable<E> observeEvents(@NonNull Class<E> eventClass) {
        return mSubject.asObservable().ofType(eventClass);
    }

}
