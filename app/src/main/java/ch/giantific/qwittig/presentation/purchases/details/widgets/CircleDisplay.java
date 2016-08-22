/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.details.widgets;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.support.annotation.ColorInt;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import ch.giantific.qwittig.R;

import static ch.giantific.qwittig.utils.ViewUtils.DISABLED_ALPHA_RGB;

/**
 * Simple custom-view for displaying values (with and without animation) and
 * selecting values onTouch().
 *
 * @author Philipp Jahoda
 */
public class CircleDisplay extends View {

    /**
     * start angle of the view
     */
    private float startAngle = 270f;
    /**
     * angle that represents the displayed value
     */
    private float angle = 0f;
    /**
     * current state of the animation
     */
    private float phase = 0f;
    /**
     * the currently displayed value, can be percent or actual value
     */
    private float value = 0f;
    /**
     * represents the alpha value used for the remainder bar
     */
    private int dimAlpha = DISABLED_ALPHA_RGB;
    /**
     * rect object that represents the bounds of the view, needed for drawing
     * the circle
     */
    private RectF circleBox = new RectF();
    private Paint arcPaint;
    /**
     * object animator for doing the drawing animations
     */
    private ObjectAnimator drawAnimator;
    /**
     * boolean flag that indicates if the box has been setup
     */
    private boolean boxSetup = false;

    public CircleDisplay(Context context) {
        super(context);

        init(context);
    }

    public CircleDisplay(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context);
    }

    public CircleDisplay(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(context);
    }

    private void init(Context context) {
        boxSetup = false;

        arcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        arcPaint.setStyle(Style.FILL);
        arcPaint.setColor(getColor(context));

        drawAnimator = ObjectAnimator.ofFloat(this, "phase", phase, 1.0f).setDuration(1000);
        drawAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
    }

    @ColorInt
    private int getColor(Context context) {
        final TypedValue typedValue = new TypedValue();
        final TypedArray a = context.obtainStyledAttributes(typedValue.data, new int[]{R.attr.colorPrimary});
        final int color = a.getColor(0, 0);
        a.recycle();

        return color;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (!boxSetup) {
            boxSetup = true;
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
        arcPaint.setAlpha(dimAlpha);

        float r = getRadius();

        c.drawCircle(getWidth() / 2, getHeight() / 2, r, arcPaint);
    }

    /**
     * draws the actual value slice/arc
     *
     * @param c
     */
    private void drawValue(Canvas c) {
        arcPaint.setAlpha(255);

        float angle = this.angle * phase;

        c.drawArc(circleBox, startAngle, angle, true, arcPaint);
    }

    /**
     * sets up the bounds of the view
     */
    private void setupBox() {

        int width = getWidth();
        int height = getHeight();

        float diameter = getDiameter();

        circleBox = new RectF(width / 2 - diameter / 2, height / 2 - diameter / 2, width / 2
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
        angle = calcAngle(toShow / total * 100f);
        value = toShow;

        if (animated)
            startAnim();
        else {
            phase = 1f;
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
        return value;
    }

    public void startAnim() {
        phase = 0f;
        drawAnimator.start();
    }

    /**
     * set the duration of the drawing animation in milliseconds
     *
     * @param durationmillis
     */
    public void setAnimDuration(int durationmillis) {
        drawAnimator.setDuration(durationmillis);
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
        startAngle = angle;
    }

    /**
     * set the color of the arc
     *
     * @param color
     */
    public void setColor(int color) {
        arcPaint.setColor(color);
    }

    /**
     * set the aplha value to be used for the remainder of the arc, default 80
     * (use value between 0 and 255)
     *
     * @param alpha
     */
    public void setDimAlpha(int alpha) {
        dimAlpha = alpha;
    }
}
