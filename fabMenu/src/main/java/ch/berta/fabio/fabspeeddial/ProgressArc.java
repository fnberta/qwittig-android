package ch.berta.fabio.fabspeeddial;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

/**
 * Created by fabio on 06.06.16.
 */
public class ProgressArc extends View {

    private static final Interpolator DECELERATE_INTERPOLATOR = new DecelerateInterpolator();
    private static final Interpolator LINEAR_INTERPOLATOR = new LinearInterpolator();

    private static final int MINIMUM_SWEEP_ANGLE = 20;
    private static final int MAXIMUM_SWEEP_ANGLE = 300;
    private static final int ROTATE_ANIMATOR_DURATION = 2000;
    private static final int SWEEP_ANIM_DURATION = 1000;
    private static final int COMPLETE_ANIM_DURATION = SWEEP_ANIM_DURATION * 2;
    private static final int COMPLETE_ROTATE_DURATION = COMPLETE_ANIM_DURATION * 6;

    private final RectF arcBounds = new RectF();
    private final Paint paint = new Paint();
    private final Rect shadowPadding = new Rect();

    private int fabSize;
    private boolean complete;

    private ValueAnimator rotateAnim;
    private ValueAnimator growAnim;
    private ValueAnimator shrinkAnim;
    private ValueAnimator completeAnim;
    private float currentSweepAngle;
    private float currentRotationAngleOffset;
    private float currentRotationAngle;
    private boolean animationPlaying;
    private boolean growing;
    private boolean showCompleteAnimOnNextCycle;
    @ColorInt
    private int arcColor;
    private int arcWidth;
    private Listener listener;

    public ProgressArc(Context context) {
        this(context, null);
    }

    public ProgressArc(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ProgressArc(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(context);
    }

    public void setListener(@NonNull Listener listener) {
        this.listener = listener;
    }

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    public boolean isCompleteOrCompleting() {
        return complete || completeAnim.isRunning();
    }

    public boolean isAnimationPlaying() {
        return animationPlaying;
    }

    public void setShadowPadding(int startEnd, int topBottom) {
        shadowPadding.set(startEnd, topBottom, startEnd, topBottom);
    }

    private void init(Context context) {
        final Resources res = getResources();
        fabSize = res.getDimensionPixelSize(R.dimen.fab_menu_fab_size_normal);
        arcWidth = res.getDimensionPixelSize(R.dimen.fp_progress_arc_stroke_width);
        arcColor = ContextCompat.getColor(context, R.color.green_500);

        setupPaint();
        setupAnimations();
    }

    private void setupPaint() {
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(arcWidth);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setColor(arcColor);
    }

    private void setupAnimations() {
        setupRotateAnimation();
        setupGrowAnimation();
        setupShrinkAnimation();
        setupCompleteAnimation();
    }

    private void setupRotateAnimation() {
        rotateAnim = ValueAnimator.ofFloat(0f, 360f);
        rotateAnim.setInterpolator(LINEAR_INTERPOLATOR);
        rotateAnim.setDuration(ROTATE_ANIMATOR_DURATION);
        rotateAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float angle = getAnimatedFraction(animation) * 360f;
                updateCurrentRotationAngle(angle);
            }
        });
        rotateAnim.setRepeatCount(ValueAnimator.INFINITE);
        rotateAnim.setRepeatMode(ValueAnimator.RESTART);
    }

    private void updateCurrentRotationAngle(float currentRotationAngle) {
        this.currentRotationAngle = currentRotationAngle;
        invalidate();
    }

    private void setupGrowAnimation() {
        growAnim = ValueAnimator.ofFloat(MINIMUM_SWEEP_ANGLE, MAXIMUM_SWEEP_ANGLE);
        growAnim.setInterpolator(DECELERATE_INTERPOLATOR);
        growAnim.setDuration(SWEEP_ANIM_DURATION);
        growAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float animatedFraction = getAnimatedFraction(animation);
                float angle = MINIMUM_SWEEP_ANGLE + animatedFraction * (MAXIMUM_SWEEP_ANGLE - MINIMUM_SWEEP_ANGLE);
                updateCurrentSweepAngle(angle);
            }
        });
        growAnim.addListener(new Animator.AnimatorListener() {
            boolean cancelled;

            @Override
            public void onAnimationStart(Animator animation) {
                cancelled = false;
                growing = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (!cancelled) {
                    setShrinking();
                    shrinkAnim.start();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                cancelled = true;
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    private void setShrinking() {
        growing = false;
        currentRotationAngleOffset = currentRotationAngleOffset + (360 - MAXIMUM_SWEEP_ANGLE);
    }

    private void setupShrinkAnimation() {
        shrinkAnim = ValueAnimator.ofFloat(MAXIMUM_SWEEP_ANGLE, MINIMUM_SWEEP_ANGLE);
        shrinkAnim.setInterpolator(DECELERATE_INTERPOLATOR);
        shrinkAnim.setDuration(SWEEP_ANIM_DURATION);
        shrinkAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float animatedFraction = getAnimatedFraction(animation);
                updateCurrentSweepAngle(MAXIMUM_SWEEP_ANGLE -
                        animatedFraction * (MAXIMUM_SWEEP_ANGLE - MINIMUM_SWEEP_ANGLE));
            }
        });
        shrinkAnim.addListener(new Animator.AnimatorListener() {
            boolean cancelled;

            @Override
            public void onAnimationStart(Animator animation) {
                cancelled = false;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (!cancelled) {
                    setGrowing();
                    if (showCompleteAnimOnNextCycle) {
                        showCompleteAnimOnNextCycle = false;
                        completeAnim.start();
                    } else {
                        growAnim.start();
                    }
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                cancelled = true;
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    private void setGrowing() {
        growing = true;
        currentRotationAngleOffset += MINIMUM_SWEEP_ANGLE;
    }

    private void setupCompleteAnimation() {
        completeAnim = ValueAnimator.ofFloat(MAXIMUM_SWEEP_ANGLE, MINIMUM_SWEEP_ANGLE);
        completeAnim.setInterpolator(DECELERATE_INTERPOLATOR);
        completeAnim.setDuration(COMPLETE_ANIM_DURATION);
        completeAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float animatedFraction = getAnimatedFraction(animation);
                float angle = MINIMUM_SWEEP_ANGLE + animatedFraction * 360;
                updateCurrentSweepAngle(angle);
            }
        });
        completeAnim.addListener(new Animator.AnimatorListener() {
            boolean cancelled;

            @Override
            public void onAnimationStart(Animator animation) {
                cancelled = false;
                growing = true;
                rotateAnim.setInterpolator(DECELERATE_INTERPOLATOR);
                rotateAnim.setDuration(COMPLETE_ROTATE_DURATION);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (!cancelled) {
                    stopProgress();
                }

                completeAnim.removeListener(this);
                complete = true;
                listener.onArcFinalAnimComplete();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                cancelled = true;
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
    }

    private float getAnimatedFraction(@NonNull ValueAnimator animator) {
        float fraction = ((float) animator.getCurrentPlayTime()) / animator.getDuration();
        fraction = Math.min(fraction, 1f);
        fraction = animator.getInterpolator().getInterpolation(fraction);
        return fraction;
    }

    private void updateCurrentSweepAngle(float currentSweepAngle) {
        this.currentSweepAngle = currentSweepAngle;
        invalidate();
    }

    private void resetArcProperties() {
        currentSweepAngle = 0;
        currentRotationAngle = 0;
        currentRotationAngleOffset = 0;
    }

    private void stopAnimators() {
        rotateAnim.cancel();
        growAnim.cancel();
        shrinkAnim.cancel();
        completeAnim.cancel();
    }

    public AnimatorSet getScaleDownAnimator() {
        final float scalePercent = (float) getWidth() / (getWidth() + arcWidth + 5);
        final ObjectAnimator arcScaleX = ObjectAnimator.ofFloat(this, "scaleX", scalePercent);
        final ObjectAnimator arcScaleY = ObjectAnimator.ofFloat(this, "scaleY", scalePercent);

        final AnimatorSet set = new AnimatorSet();
        set.setDuration(150).setInterpolator(new DecelerateInterpolator());
        set.playTogether(arcScaleX, arcScaleY);
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                setAlpha(0);
            }
        });

        return set;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        if (Utils.isRunningLollipopAndHigher()) {
            arcBounds.left = 0;
            arcBounds.top = 0;
            arcBounds.right = w;
            arcBounds.bottom = h;
        } else {
            arcBounds.left = shadowPadding.left;
            arcBounds.top = shadowPadding.top;
            arcBounds.right = shadowPadding.left + fabSize;
            arcBounds.bottom = shadowPadding.bottom + fabSize;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float startAngle = currentRotationAngle - currentRotationAngleOffset;
        float sweepAngle = currentSweepAngle;
        if (!growing) {
            startAngle = startAngle + (360 - sweepAngle);
        }

        canvas.drawArc(arcBounds, startAngle, sweepAngle, false, paint);
    }

    /**
     * Starts the indeterminate spinning progress circle.
     */
    public void startProgress() {
        animationPlaying = true;

        setAlpha(1);
        resetArcProperties();
        rotateAnim.start();
        growAnim.start();
        invalidate();
    }

    ////
    // Public api methods
    ////

    /**
     * Stops the indeterminate spinning progress circle.
     */
    public void stopProgress() {
        animationPlaying = false;

        stopAnimators();
        resetArcProperties();
        invalidate();

        final ObjectAnimator fadeOutAnim = ObjectAnimator.ofFloat(this, "alpha", 1, 0);
        fadeOutAnim.setDuration(100).start();
    }

    /**
     * Resets the {@link FloatingActionButton} to its original icon and background and makes it
     * clickable again.
     */
    public void resetProgress() {
        complete = false;

        stopProgress();
        setupAnimations();

        setScaleX(1);
        setScaleY(1);
    }

    /**
     * Starts the final animation, i.e. makes the spinning progress circle determinate.
     */
    public void startProgressFinalAnimation() {
        if (!animationPlaying || completeAnim.isRunning()) {
            return;
        }

        showCompleteAnimOnNextCycle = true;
    }

    public interface Listener {
        void onArcFinalAnimComplete();
    }
}
