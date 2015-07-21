package ch.giantific.qwittig.ui.widgets;

import android.content.Context;
import android.util.AttributeSet;

import com.github.mikephil.charting.components.Legend;

/**
 * Created by fabio on 17.07.15.
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
        setCenterTextSize(24f);

        Legend legend = getLegend();
        legend.setPosition(Legend.LegendPosition.RIGHT_OF_CHART);
    }
}
