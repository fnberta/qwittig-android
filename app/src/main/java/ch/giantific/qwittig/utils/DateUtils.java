package ch.giantific.qwittig.utils;

import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by fabio on 27.03.15.
 */
public class DateUtils {

    private DateUtils() {
        // class cannot be instantiated
    }

    public static Date parseDateFromPicker(int year, int month, int day) {
        String dateString = String.valueOf(year) + "-" + (month + 1) + "-" + day;
        return parseStringToDate(dateString);
    }

    public static Date parseStringToDate(String date) {
        final SimpleDateFormat dateParser = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

        Date parsedDate;
        try {
            parsedDate = dateParser.parse(date);
        } catch (ParseException e) {
            parsedDate = new Date();
        }
        return parsedDate;
    }

    public static long parseDateToLong(Date date) {
        return date.getTime();
    }

    public static Date parseLongToDate(long date) {
        return new Date(date);
    }

    public static String formatDateShort(Date date) {
        final DateFormat dateFormatter = DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault());

        return dateFormatter.format(date);
    }

    public static String formatDateLong(Date date) {
        final DateFormat dateFormatter = DateFormat.getDateInstance(DateFormat.LONG, Locale.getDefault());

        return dateFormatter.format(date);
    }

    public static String formatMonthDayLineSeparated(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        String month = calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.US); // TODO: use Locale.getDefault() (but make sure only 3 letters)
        String day = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));

        return month + System.getProperty("line.separator") + day;
    }

    public static String[] getMonthNames() {
        return new DateFormatSymbols(Locale.getDefault()).getShortMonths();
    }

    public static String getMonthNameShort(int month) {
        return getMonthNames()[month - 1];
    }

    public static Calendar getCalendarInstanceUTC() {
        return Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    }

    /**
     * Resets hour, minutes, seconds and millis to 0, meaning midnight.
     * @param cal
     * @return
     */
    public static Calendar resetToMidnight(Calendar cal) {
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return cal;
    }
}
