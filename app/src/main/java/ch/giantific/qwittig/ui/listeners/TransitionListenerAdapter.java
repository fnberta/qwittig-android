/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.ui.listeners;

/**
 * Provides empty default implementations for
 * {@link android.transition.Transition.TransitionListener} to allow subclasses to only implement
 * the specific method needed.
 * <p/>
 * Implements {@link android.transition.Transition.TransitionListener}.
 */

import android.annotation.TargetApi;
import android.os.Build;
import android.transition.Transition;

@TargetApi(Build.VERSION_CODES.KITKAT)
public class TransitionListenerAdapter implements Transition.TransitionListener {
    @Override
    public void onTransitionStart(Transition transition) {
    }

    @Override
    public void onTransitionEnd(Transition transition) {
    }

    @Override
    public void onTransitionCancel(Transition transition) {
    }

    @Override
    public void onTransitionPause(Transition transition) {
    }

    @Override
    public void onTransitionResume(Transition transition) {
    }
}