package ch.giantific.qwittig.presentation.stats.formatters;

import android.support.annotation.NonNull;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import ch.giantific.qwittig.data.rest.stats.StatsResult.UnitType;
import ch.giantific.qwittig.utils.DateUtils;

/**
 * Created by fabio on 16.08.16.
 */
public class DateAxisFormatter implements IAxisValueFormatter {

    private final String unit;

    public DateAxisFormatter(@NonNull String unit) {
        this.unit = unit;
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        final int rounded = ((int) value);
        switch (unit) {
            case UnitType.DAYS:
                return String.valueOf(rounded);
            case UnitType.MONTHS:
                return DateUtils.getMonthNameShort(rounded);
            case UnitType.YEARS:
                return String.valueOf(rounded);
            default:
                return String.valueOf(value);
        }
    }

    @Override
    public int getDecimalDigits() {
        return 0;
    }
}
