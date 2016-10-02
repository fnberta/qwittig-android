package ch.giantific.qwittig.utils.rxwrapper.android;

import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.transition.Transition;

import ch.giantific.qwittig.utils.rxwrapper.android.transitions.TransitionEmitter;
import ch.giantific.qwittig.utils.rxwrapper.android.transitions.TransitionEvent;
import ch.giantific.qwittig.utils.rxwrapper.android.visibility.FabVisibilityOnSubscribe;
import rx.AsyncEmitter;
import rx.Observable;
import rx.Single;

/**
 * Created by fabio on 02.10.16.
 */

public class RxAndroidViews {

    public static Observable<TransitionEvent> observeTransition(@NonNull Transition transition) {
        return Observable.fromEmitter(new TransitionEmitter(transition), AsyncEmitter.BackpressureMode.LATEST);
    }

    public static Single<FloatingActionButton> getFabVisibilityChange(@NonNull FloatingActionButton fab) {
        return Single.create(new FabVisibilityOnSubscribe(fab));
    }
}
