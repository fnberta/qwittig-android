package ch.berta.fabio.fabspeeddial;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

/**
 * Created by fabio on 06.06.16.
 */
public class CompleteView extends FrameLayout {

    private static final Interpolator LINEAR_INTERPOLATOR = new LinearInterpolator();
    private static final Interpolator ACCELERATE_DECELERATE_INTERPOLATOR = new AccelerateDecelerateInterpolator();
    private ImageView imageView;
    private Listener listener;

    public CompleteView(Context context) {
        this(context, null, 0);
    }

    public CompleteView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);

    }

    public CompleteView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(context);
    }

    private void init(@NonNull Context context) {
        LayoutInflater.from(context).inflate(R.layout.complete_view, this, true);
        imageView = (ImageView) findViewById(R.id.iv_complete);
    }

    public void setListener(@NonNull Listener listener) {
        this.listener = listener;
    }

    public void animateIn(@NonNull AnimatorSet progressArcAnimator) {
        final ObjectAnimator completeFabAnim = ObjectAnimator.ofFloat(this, "alpha", 1);
        completeFabAnim.setDuration(300).setInterpolator(ACCELERATE_DECELERATE_INTERPOLATOR);

        final ObjectAnimator iconScaleAnimX = ObjectAnimator.ofFloat(imageView, "scaleX", 0, 1);
        iconScaleAnimX.setDuration(250).setInterpolator(LINEAR_INTERPOLATOR);
        final ObjectAnimator iconScaleAnimY = ObjectAnimator.ofFloat(imageView, "scaleY", 0, 1);
        iconScaleAnimY.setDuration(250).setInterpolator(LINEAR_INTERPOLATOR);

        final AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(completeFabAnim, progressArcAnimator, iconScaleAnimX, iconScaleAnimY);
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);

                setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                if (listener != null) {
                    listener.onCompleteViewShown();
                }
            }
        });
        animatorSet.start();
    }

    public void show() {
        setVisibility(View.VISIBLE);
        setAlpha(1);
    }

    public void hide() {
        setVisibility(View.GONE);
        setAlpha(0);
    }

    public interface Listener {
        void onCompleteViewShown();
    }
}
