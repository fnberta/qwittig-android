package ch.giantific.qwittig.data.rxwrapper.firebase.subscribers;

import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import rx.Single;
import rx.SingleSubscriber;

/**
 * Created by fabio on 10.07.16.
 */
public class ListenToTaskOnCompleteOnSubscribe<T> implements Single.OnSubscribe<T> {

    private final Task<T> mTask;

    public ListenToTaskOnCompleteOnSubscribe(@NonNull Task<T> task) {
        mTask = task;
    }

    @Override
    public void call(SingleSubscriber<? super T> singleSubscriber) {
        mTask.addOnCompleteListener(new RxTaskOnCompleteListener<>(singleSubscriber));
    }

    private static class RxTaskOnCompleteListener<T> implements OnCompleteListener<T> {

        private final SingleSubscriber<? super T> mSubscriber;

        public RxTaskOnCompleteListener(@NonNull SingleSubscriber<? super T> subscriber) {
            mSubscriber = subscriber;
        }

        @Override
        public void onComplete(@NonNull Task<T> task) {
            if (mSubscriber.isUnsubscribed()) {
                return;
            }

            if (task.isSuccessful()) {
                mSubscriber.onSuccess(task.getResult());
            } else {
                mSubscriber.onError(task.getException());
            }
        }
    }
}
