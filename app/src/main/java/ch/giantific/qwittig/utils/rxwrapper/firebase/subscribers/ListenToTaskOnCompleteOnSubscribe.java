package ch.giantific.qwittig.utils.rxwrapper.firebase.subscribers;

import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import rx.Single;
import rx.SingleSubscriber;

/**
 * Created by fabio on 10.07.16.
 */
public class ListenToTaskOnCompleteOnSubscribe<T> implements Single.OnSubscribe<T> {

    private final Task<T> task;

    public ListenToTaskOnCompleteOnSubscribe(@NonNull Task<T> task) {
        this.task = task;
    }

    @Override
    public void call(SingleSubscriber<? super T> singleSubscriber) {
        task.addOnCompleteListener(new RxTaskOnCompleteListener<>(singleSubscriber));
    }

    private static class RxTaskOnCompleteListener<T> implements OnCompleteListener<T> {

        private final SingleSubscriber<? super T> subscriber;

        RxTaskOnCompleteListener(@NonNull SingleSubscriber<? super T> subscriber) {
            this.subscriber = subscriber;
        }

        @Override
        public void onComplete(@NonNull Task<T> task) {
            if (subscriber.isUnsubscribed()) {
                return;
            }

            if (task.isSuccessful()) {
                subscriber.onSuccess(task.getResult());
            } else {
                subscriber.onError(task.getException());
            }
        }
    }
}
