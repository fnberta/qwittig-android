/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.presentation.common.widgets;

import android.content.Context;
import android.util.AttributeSet;

import com.github.mikephil.charting.components.Legend;

/**
 * Provides a pie char with sensible default settings already set.
 * <p/>
 * Subclass of {@link com.github.mikephil.charting.charts.PieChart}
 */
public class PieChart extends com.github.mikephil.charting.charts.PieChart {

    public static final int ANIMATION_Y_TIME = 1000;

    public PieChart(Context context) {
        super(context);

        setDefaultValues();
    }

    public PieChart(Context context, AttributeSet attrs) {
        super(context, attrs);

        setDefaultValues();
    }

    public PieChart(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        setDefaultValues();
    }

    private void setDefaultValues() {
        setNoDataText("");
        setHoleRadius(35f);
        setTransparentCircleRadius(40f);
        setDescription("");
        setCenterTextSize(20f);
        setCenterTextWordWrapEnabled(true);

        Legend legend = getLegend();
        legend.setPosition(Legend.LegendPosition.RIGHT_OF_CHART);
    }
}
