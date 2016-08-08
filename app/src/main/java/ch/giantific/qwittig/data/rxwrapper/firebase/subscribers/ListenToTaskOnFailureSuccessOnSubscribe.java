package ch.giantific.qwittig.data.rxwrapper.firebase.subscribers;

import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import rx.Single;
import rx.SingleSubscriber;
import rx.functions.Action0;
import rx.subscriptions.BooleanSubscription;

/**
 * Created by fabio on 10.07.16.
 */
public class ListenToTaskOnFailureSuccessOnSubscribe<T> implements Single.OnSubscribe<T> {

    private final Task<T> mTask;

    public ListenToTaskOnFailureSuccessOnSubscribe(@NonNull Task<T> task) {
        mTask = task;
    }

    @Override
    public void call(SingleSubscriber<? super T> singleSubscriber) {
        final RxTaskOnFailureSuccessListener<T> listener = new RxTaskOnFailureSuccessListener<>(singleSubscriber);
        mTask.addOnFailureListener(listener).addOnSuccessListener(listener);
    }

    private static class RxTaskOnFailureSuccessListener<T> implements OnSuccessListener<T>, OnFailureListener {

        private final SingleSubscriber<? super T> mSubscriber;

        public RxTaskOnFailureSuccessListener(@NonNull SingleSubscriber<? super T> subscriber) {
            mSubscriber = subscriber;
        }

        @Override
        public void onFailure(@NonNull Exception e) {
            if (!mSubscriber.isUnsubscribed()) {
                mSubscriber.onError(e);
            }
        }

        @Override
        public void onSuccess(T t) {
            if (!mSubscriber.isUnsubscribed()) {
                mSubscriber.onSuccess(t);
            }
        }
    }
}
