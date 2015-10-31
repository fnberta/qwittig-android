/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.utils;

import android.animation.Animator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.inputmethod.InputMethodManager;

/**
 * Provides useful static methods for animations.
 */
public class ViewUtils {

    /**
     * Delay before a FAB gets revealed.
     */
    public static final long FAB_CIRCULAR_REVEAL_DELAY = 50;
    /**
     * Default alpha of a disabled view.
     */
    public static final float DISABLED_ALPHA = 0.26f;
    /**
     * Default alpha of a disabled view in RGB.
     */
    public static final int DISABLED_ALPHA_RGB = 66;

    private ViewUtils() {
        // class cannot be instantiated
    }

    /**
     * Returns an {@link Animator} that reveals a view in a circular fashion.
     * @param view the view to reveal
     * @return a circular reveal {@link Animator}
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static Animator getCircularRevealAnimator(@NonNull View view) {
        // get the center for the clipping circle
        int cx = view.getWidth() / 2;
        int cy = view.getHeight() / 2;

        // get the final radius for the clipping circle
        int finalRadius = Math.max(view.getWidth(), view.getHeight()) / 2;

        // create the animator for this view (the start radius is zero)
        Animator animator = ViewAnimationUtils.createCircularReveal(view, cx, cy, 0, finalRadius);
        animator.setStartDelay(FAB_CIRCULAR_REVEAL_DELAY);

        return animator;
    }

    /**
     * Returns an {@link Animator} that hides a view in a circular fashion.
     * @param view the view to hide
     * @return a circular hide {@link Animator}
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static Animator getCircularHideAnimator(@NonNull View view) {
        // get the center for the clipping circle
        int cx = view.getWidth() / 2;
        int cy = view.getHeight() / 2;

        // get the initial radius for the clipping circle
        int initialRadius = view.getWidth();

        // create the animator for this view (the start radius is zero) and return it
        return ViewAnimationUtils.createCircularReveal(view, cx, cy, initialRadius, 0);
    }

    /**
     * Hides the software keyboard.
     *
     * @param activity the activity to get the focused view
     */
    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager)
                activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        // Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();

        // If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }

        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
