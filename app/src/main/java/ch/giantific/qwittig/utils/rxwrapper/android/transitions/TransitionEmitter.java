package ch.giantific.qwittig.utils.rxwrapper.android.transitions;

import android.support.annotation.NonNull;
import android.transition.Transition;

import rx.AsyncEmitter;
import rx.functions.Action1;

/**
 * Created by fabio on 02.10.16.
 */

public class TransitionEmitter implements Action1<AsyncEmitter<TransitionEvent>> {

    private Transition transition;

    public TransitionEmitter(@NonNull Transition transition) {
        this.transition = transition;
    }

    @Override
    public void call(AsyncEmitter<TransitionEvent> asyncEmitter) {
        final Transition.TransitionListener listener = new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(Transition transition) {
                asyncEmitter.onNext(new TransitionEvent(TransitionEvent.EventType.START, transition));
            }

            @Override
            public void onTransitionEnd(Transition transition) {
                asyncEmitter.onNext(new TransitionEvent(TransitionEvent.EventType.END, transition));
                asyncEmitter.onCompleted();
            }

            @Override
            public void onTransitionCancel(Transition transition) {
                asyncEmitter.onNext(new TransitionEvent(TransitionEvent.EventType.CANCEL, transition));
                asyncEmitter.onCompleted();
            }

            @Override
            public void onTransitionPause(Transition transition) {
                asyncEmitter.onNext(new TransitionEvent(TransitionEvent.EventType.PAUSE, transition));
            }

            @Override
            public void onTransitionResume(Transition transition) {
                asyncEmitter.onNext(new TransitionEvent(TransitionEvent.EventType.RESUME, transition));
            }
        };
        transition.addListener(listener);
        asyncEmitter.setCancellation(() -> transition.removeListener(listener));
    }
}
