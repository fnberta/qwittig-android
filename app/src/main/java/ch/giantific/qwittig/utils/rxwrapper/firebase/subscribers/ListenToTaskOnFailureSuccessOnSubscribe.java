package ch.giantific.qwittig.utils.rxwrapper.firebase.subscribers;

import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import rx.Single;
import rx.SingleSubscriber;

/**
 * Created by fabio on 10.07.16.
 */
public class ListenToTaskOnFailureSuccessOnSubscribe<T> implements Single.OnSubscribe<T> {

    private final Task<T> task;

    public ListenToTaskOnFailureSuccessOnSubscribe(@NonNull Task<T> task) {
        this.task = task;
    }

    @Override
    public void call(SingleSubscriber<? super T> singleSubscriber) {
        final RxTaskOnFailureSuccessListener<T> listener = new RxTaskOnFailureSuccessListener<>(singleSubscriber);
        task.addOnFailureListener(listener).addOnSuccessListener(listener);
    }

    private static class RxTaskOnFailureSuccessListener<T> implements OnSuccessListener<T>, OnFailureListener {

        private final SingleSubscriber<? super T> subscriber;

        RxTaskOnFailureSuccessListener(@NonNull SingleSubscriber<? super T> subscriber) {
            this.subscriber = subscriber;
        }

        @Override
        public void onFailure(@NonNull Exception e) {
            if (!subscriber.isUnsubscribed()) {
                subscriber.onError(e);
            }
        }

        @Override
        public void onSuccess(T t) {
            if (!subscriber.isUnsubscribed()) {
                subscriber.onSuccess(t);
            }
        }
    }
}
