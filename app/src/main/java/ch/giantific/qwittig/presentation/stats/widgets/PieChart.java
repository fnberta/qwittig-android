/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.stats.widgets;

import android.content.Context;
import android.util.AttributeSet;

/**
 * Provides a pie char with sensible default settings already set.
 * <p/>
 * Subclass of {@link com.github.mikephil.charting.charts.PieChart}
 */
public class PieChart extends com.github.mikephil.charting.charts.PieChart {

    public static final int ANIMATION_Y_TIME = 1000;

    public PieChart(Context context) {
        this(context, null);
    }

    public PieChart(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PieChart(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        setDefaultValues();
    }

    private void setDefaultValues() {
        setNoDataText("");
        setDrawHoleEnabled(false);
        setDrawCenterText(false);
//        setDescription("");

        getLegend().setEnabled(false);
    }
}
