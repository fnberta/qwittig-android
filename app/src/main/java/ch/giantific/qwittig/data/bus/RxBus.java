package ch.giantific.qwittig.data.bus;

import android.support.annotation.NonNull;

import javax.inject.Inject;
import javax.inject.Singleton;

import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

/**
 * Created by fabio on 15.06.16.
 */
@Singleton
public class RxBus<T> {

    private final Subject<T, T> subject = PublishSubject.create();

    @Inject
    public RxBus() {
    }

    public <E extends T> void post(@NonNull E event) {
        subject.onNext(event);
    }

    public Observable<T> observe() {
        return subject.asObservable();
    }

    public <E extends T> Observable<E> observeEvents(@NonNull Class<E> eventClass) {
        return subject.asObservable().ofType(eventClass);
    }

}
