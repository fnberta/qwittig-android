package ch.giantific.qwittig.utils;

import android.animation.Animator;
import android.annotation.TargetApi;
import android.os.Build;
import android.view.View;
import android.view.ViewAnimationUtils;

/**
 * Created by fabio on 17.10.15.
 */
public class AnimUtils {

    public static final long FAB_CIRCULAR_REVEAL_DELAY = 50;
    public static final float DISABLED_ALPHA = 0.26f;
    public static final int DISABLED_ALPHA_RGB = 66;

    private AnimUtils() {
        // class cannot be instantiated
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static Animator getCircularRevealAnimator(View view) {
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

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static Animator getCircularHideAnimator(View view) {
        // get the center for the clipping circle
        int cx = view.getWidth() / 2;
        int cy = view.getHeight() / 2;

        // get the initial radius for the clipping circle
        int initialRadius = view.getWidth();

        // create the animator for this view (the start radius is zero) and return it
        return ViewAnimationUtils.createCircularReveal(view, cx, cy, initialRadius, 0);
    }
}
