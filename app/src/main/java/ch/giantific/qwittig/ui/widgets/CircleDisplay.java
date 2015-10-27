/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.ui.widgets;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

/**
 * Simple custom-view for displaying values (with and without animation) and
 * selecting values onTouch().
 *
 * @author Philipp Jahoda
 */
public class CircleDisplay extends View {

    private static final String LOG_TAG = CircleDisplay.class.getSimpleName();
    /**
     * startangle of the view
     */
    private float mStartAngle = 270f;
    /**
     * angle that represents the displayed value
     */
    private float mAngle = 0f;
    /**
     * current state of the animation
     */
    private float mPhase = 0f;
    /**
     * the currently displayed value, can be percent or actual value
     */
    private float mValue = 0f;
    /**
     * represents the alpha value used for the remainder bar
     */
    private int mDimAlpha = 80;
    /**
     * rect object that represents the bounds of the view, needed for drawing
     * the circle
     */
    private RectF mCircleBox = new RectF();
    private Paint mArcPaint;
    /**
     * object animator for doing the drawing animations
     */
    private ObjectAnimator mDrawAnimator;
    /**
     * boolean flag that indicates if the box has been setup
     */
    private boolean mBoxSetup = false;

    public CircleDisplay(Context context) {
        super(context);
        init();
    }

    public CircleDisplay(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CircleDisplay(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mBoxSetup = false;

        mArcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mArcPaint.setStyle(Style.FILL);
        mArcPaint.setColor(Color.rgb(192, 255, 140));

        mDrawAnimator = ObjectAnimator.ofFloat(this, "phase", mPhase, 1.0f).setDuration(3000);
        mDrawAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (!mBoxSetup) {
            mBoxSetup = true;
            setupBox();
        }

        drawWholeCircle(canvas);
        drawValue(canvas);
    }

    /**
     * draws the background circle with less alpha
     *
     * @param c
     */
    private void drawWholeCircle(Canvas c) {
        mArcPaint.setAlpha(mDimAlpha);

        float r = getRadius();

        c.drawCircle(getWidth() / 2, getHeight() / 2, r, mArcPaint);
    }

    /**
     * draws the actual value slice/arc
     *
     * @param c
     */
    private void drawValue(Canvas c) {
        mArcPaint.setAlpha(255);

        float angle = mAngle * mPhase;

        c.drawArc(mCircleBox, mStartAngle, angle, true, mArcPaint);
    }

    /**
     * sets up the bounds of the view
     */
    private void setupBox() {

        int width = getWidth();
        int height = getHeight();

        float diameter = getDiameter();

        mCircleBox = new RectF(width / 2 - diameter / 2, height / 2 - diameter / 2, width / 2
                + diameter / 2, height / 2 + diameter / 2);
    }

    /**
     * shows the given value in the circle view
     *
     * @param toShow
     * @param total
     * @param animated
     */
    public void showValue(float toShow, float total, boolean animated) {
        mAngle = calcAngle(toShow / total * 100f);
        mValue = toShow;

        if (animated)
            startAnim();
        else {
            mPhase = 1f;
            invalidate();
        }
    }

    /**
     * Returns the currently displayed value from the view. Depending on the
     * used method to show the value, this value can be percent or actual value.
     *
     * @return
     */
    public float getValue() {
        return mValue;
    }

    public void startAnim() {
        mPhase = 0f;
        mDrawAnimator.start();
    }

    /**
     * set the duration of the drawing animation in milliseconds
     *
     * @param durationmillis
     */
    public void setAnimDuration(int durationmillis) {
        mDrawAnimator.setDuration(durationmillis);
    }

    /**
     * returns the diameter of the drawn circle/arc
     *
     * @return
     */
    public float getDiameter() {
        return Math.min(getWidth(), getHeight());
    }

    /**
     * returns the radius of the drawn circle
     *
     * @return
     */
    public float getRadius() {
        return getDiameter() / 2f;
    }

    /**
     * calculates the needed angle for a given value
     *
     * @param percent
     * @return
     */
    private float calcAngle(float percent) {
        return percent / 100f * 360f;
    }

    /**
     * set the starting angle for the view
     *
     * @param angle
     */
    public void setStartAngle(float angle) {
        mStartAngle = angle;
    }

    /**
     * set the color of the arc
     *
     * @param color
     */
    public void setColor(int color) {
        mArcPaint.setColor(color);
    }

    /**
     * set the aplha value to be used for the remainder of the arc, default 80
     * (use value between 0 and 255)
     *
     * @param alpha
     */
    public void setDimAlpha(int alpha) {
        mDimAlpha = alpha;
    }
}
