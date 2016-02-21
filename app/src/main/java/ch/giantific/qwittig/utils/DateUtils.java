/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.utils;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Provides useful static utility methods for the parsing and formatting of dates.
 */
public class DateUtils {

    private DateUtils() {
        // class cannot be instantiated
    }

    /**
     * Returns a {@link Date} parsed from year, month and day.
     *
     * @param year  the year to use
     * @param month the month to use
     * @param day   the day to use
     * @return a {@link Date} parsed from year, month and day
     */
    @NonNull
    public static Date parseDateFromPicker(int year, int month, int day) {
        String dateString = String.valueOf(year) + "-" + (month + 1) + "-" + day;
        return parseStringToDate(dateString);
    }

    /**
     * Returns a {@link Date} parsed from a string with the format yyyy-MM-dd.
     *
     * @param date the string to parse
     * @return the parsed {@link Date}
     */
    @NonNull
    public static Date parseStringToDate(@Nullable String date) {
        final SimpleDateFormat dateParser = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

        Date parsedDate;
        try {
            parsedDate = dateParser.parse(date);
        } catch (ParseException e) {
            parsedDate = new Date();
        }
        return parsedDate;
    }

    /**
     * Returns the date as milliseconds since January 1st, 1970 at UTC.
     *
     * @param date the date to convert
     * @return the date converted to Unix Epoch
     */
    public static long parseDateToLong(@NonNull Date date) {
        return date.getTime();
    }

    /**
     * Returns a {@link Date} parsed from milliseconds since January 1st, 1970 at UTC.
     *
     * @param date the milliseconds to parse
     * @return the parsed {@link Date}
     */
    @NonNull
    public static Date parseLongToDate(long date) {
        return new Date(date);
    }

    /**
     * Returns a string with the short version of the date.
     *
     * @param date the date to format
     * @return a string with the short version of the date
     */
    public static String formatDateShort(@NonNull Date date) {
        final DateFormat dateFormatter = DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault());

        return dateFormatter.format(date);
    }

    /**
     * Returns a string with the long version of the date.
     *
     * @param date the date to format
     * @return a string with the long version of the date
     */
    public static String formatDateLong(long date) {
        return formatDateLong(new Date(date));
    }

    /**
     * Returns a string with the long version of the date.
     *
     * @param date the date to format
     * @return a string with the long version of the date
     */
    public static String formatDateLong(@NonNull Date date) {
        final DateFormat dateFormatter = DateFormat.getDateInstance(DateFormat.LONG, Locale.getDefault());
        return dateFormatter.format(date);
    }

    /**
     * Returns the date as a string, formatted with 3 letter version of the month on one line and
     * below the day.
     *
     * @param date the date to format
     * @return the formatted date string
     */
    @NonNull
    public static String formatMonthDayLineSeparated(@NonNull Date date) {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        final String month = calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.US); // TODO: use Locale.getDefault() (but make sure only 3 letters)
        final String day = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));

        return month + System.getProperty("line.separator") + day;
    }

    /**
     * Returns the short name of a month number.
     *
     * @param month the month number to get the name for
     * @return the short name of the month
     */
    public static String getMonthNameShort(@IntRange(from = 1, to = 12) int month) {
        return getMonthNames()[month - 1];
    }

    /**
     * Returns the short names of all months in the year.
     *
     * @return the short names of all months in the year
     */
    public static String[] getMonthNames() {
        return new DateFormatSymbols(Locale.getDefault()).getShortMonths();
    }

    /**
     * Returns a new {@link Calendar} instance configure to UTC timezone.
     *
     * @return a new {@link Calendar} instance configure to UTC timezone
     */
    public static Calendar getCalendarInstanceUTC() {
        return Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    }

    /**
     * Resets the hour, minutes, seconds and millis of {@link Calendar} instance to 0, meaning
     * midnight.
     *
     * @param cal the {@link Calendar} instance to reset
     * @return a {@link Calendar} instance reset to midnight
     */
    @NonNull
    public static Calendar resetToMidnight(@NonNull Calendar cal) {
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return cal;
    }
}
