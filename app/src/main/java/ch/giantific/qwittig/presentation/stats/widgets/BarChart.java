/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.stats.widgets;

import android.content.Context;
import android.util.AttributeSet;

import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;

/**
 * Provides a bar char with sensible default settings already set.
 * <p/>
 * Subclass of {@link com.github.mikephil.charting.charts.BarChart}
 */
public class BarChart extends com.github.mikephil.charting.charts.BarChart {

    public static final int ANIMATION_Y_TIME = 1000;

    public BarChart(Context context) {
        this(context, null);
    }

    public BarChart(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BarChart(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        setDefaultValues();
    }

    private void setDefaultValues() {
        setNoDataText("");
        final Description desc = new Description();
        desc.setText("");
        setDescription(desc);
        setFitBars(true);

        final XAxis xAxis = getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);

        final YAxis yAxisRight = getAxisRight();
        yAxisRight.setAxisMinimum(1f);
        yAxisRight.setGranularity(1f);
        yAxisRight.setDrawLabels(false);
        yAxisRight.setDrawGridLines(false);

        final YAxis yAxisLeft = getAxisLeft();
        yAxisLeft.setAxisMinimum(1f);
        yAxisLeft.setGranularity(1f);
    }

}
