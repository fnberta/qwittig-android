package ch.giantific.qwittig.presentation.stats.formatters;

import android.support.annotation.NonNull;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.util.Calendar;
import java.util.Date;

import ch.giantific.qwittig.utils.DateUtils;

/**
 * Created by fabio on 16.08.16.
 */
public class DateAxisFormatter implements IAxisValueFormatter {

    private final String unit;
    private final Date date;
    private final Calendar calendar;

    public DateAxisFormatter(@NonNull String unit) {
        this.unit = unit;
        this.date = new Date();
        this.calendar = Calendar.getInstance();
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        date.setTime((long) value);
        calendar.setTime(date);
        switch (unit) {
            case "days":
                return String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
            case "months":
                return DateUtils.getMonthNameShort(calendar.get(Calendar.MONTH));
            case "years":
                return String.valueOf(calendar.get(Calendar.YEAR));
            default:
                return String.valueOf(value);
        }
    }

    @Override
    public int getDecimalDigits() {
        return 0;
    }
}
