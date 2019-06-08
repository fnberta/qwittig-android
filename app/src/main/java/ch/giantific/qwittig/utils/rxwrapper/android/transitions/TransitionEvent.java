package ch.giantific.qwittig.utils.rxwrapper.android.transitions;

import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.transition.Transition;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by fabio on 02.10.16.
 */

public class TransitionEvent {

    @EventType
    private int eventType;
    @Nullable
    private Transition transition;

    public TransitionEvent(@EventType int eventType, @Nullable Transition transition) {
        this.eventType = eventType;
        this.transition = transition;
    }

    public static TransitionEvent createEmptyEnd() {
        return new TransitionEvent(EventType.END, null);
    }

    @EventType
    public int getEventType() {
        return eventType;
    }

    @Nullable
    public Transition getTransition() {
        return transition;
    }

    @IntDef({EventType.START, EventType.END, EventType.CANCEL, EventType.PAUSE, EventType.RESUME})
    @Retention(RetentionPolicy.SOURCE)
    public @interface EventType {
        int START = 1;
        int END = 2;
        int CANCEL = 3;
        int PAUSE = 4;
        int RESUME = 5;
    }
}
