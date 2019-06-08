package ch.giantific.qwittig.presentation.stats.formatters;

import android.support.annotation.NonNull;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.text.NumberFormat;

/**
 * Created by fabio on 16.08.16.
 */
public class ChartCurrencyFormatter implements IValueFormatter, IAxisValueFormatter {

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
