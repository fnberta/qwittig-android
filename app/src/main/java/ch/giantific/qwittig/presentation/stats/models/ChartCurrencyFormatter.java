package ch.giantific.qwittig.presentation.stats.models;

import android.support.annotation.NonNull;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.AxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.text.NumberFormat;

/**
 * Created by fabio on 16.08.16.
 */
public class ChartCurrencyFormatter implements ValueFormatter, AxisValueFormatter {

    private final NumberFormat currencyFormatter;

    public ChartCurrencyFormatter(@NonNull NumberFormat currencyFormatter) {
        this.currencyFormatter = currencyFormatter;
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        return currencyFormatter.format(value);
    }

    @Override
    public int getDecimalDigits() {
        return 0;
    }

    @Override
    public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
        return currencyFormatter.format(value);
    }
}
