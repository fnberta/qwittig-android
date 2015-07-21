package ch.giantific.qwittig.ui.widgets;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.internal.widget.TintTypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;

import ch.giantific.qwittig.R;

import static ch.giantific.qwittig.constants.AppConstants.ANIMATION_ACC_DEC_SPEED;

/**
 * Created by fabio on 07.06.15.
 */
public class FabSpeedDial extends FloatingActionButton {

    private static final String LOG_TAG = FabSpeedDial.class.getSimpleName();
    private int mAnimationTime;
    private int mShowTranslationY;

    public FabSpeedDial(Context context, int showTranslationY) {
        super(context);

        mAnimationTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
        mShowTranslationY = showTranslationY;
    }

    public FabSpeedDial(Context context, AttributeSet attrs) {
        super(context, attrs);

        mAnimationTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
        final TintTypedArray typedArray = TintTypedArray.obtainStyledAttributes(getContext(), attrs,
                R.styleable.FabSpeedDial);
        mShowTranslationY = typedArray.getDimensionPixelOffset(R.styleable.FabSpeedDial_showTranslationY, 0);

        typedArray.recycle();
    }

    public void hideSpeedDial() {
        Interpolator interpolator = new DecelerateInterpolator(ANIMATION_ACC_DEC_SPEED);
        //AnimationUtils.loadInterpolator(getActivity(), android.R.interpolator.fast_out_slow_in)
        animate()
                .translationY(0)
                .setDuration(mAnimationTime)
                .setInterpolator(interpolator)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        setVisibility(View.GONE);
                    }
                });
    }

    public void showSpeedDial() {
        Interpolator interpolator = new OvershootInterpolator();
        //AnimationUtils.loadInterpolator(getActivity(), android.R.interpolator.fast_out_slow_in)
        animate()
                .translationY(mShowTranslationY)
                .setDuration(mAnimationTime)
                .setInterpolator(interpolator)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        super.onAnimationStart(animation);
                        setVisibility(View.VISIBLE);
                    }
                });
    }
}
