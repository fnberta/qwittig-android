/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.ui.widgets;

import android.content.Context;
import android.util.AttributeSet;

import com.github.mikephil.charting.components.YAxis;

/**
 * Provides a bar char with sensible default settings already set.
 * <p/>
 * Subclass of {@link com.github.mikephil.charting.charts.BarChart}
 */
public class BarChart extends com.github.mikephil.charting.charts.BarChart {

    public static final int ANIMATION_Y_TIME = 1000;

    public BarChart(Context context) {
        super(context);

        setDefaultValues();
    }

    public BarChart(Context context, AttributeSet attrs) {
        super(context, attrs);

        setDefaultValues();
    }

    public BarChart(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        setDefaultValues();
    }

    private void setDefaultValues() {
        setNoDataText("");
        setDescription("");

        YAxis yAxisRight = getAxisRight();
        yAxisRight.setDrawLabels(false);
    }

}
