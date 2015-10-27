/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.utils;

import android.annotation.TargetApi;
import android.os.Build;
import android.support.annotation.NonNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

/**
 * Provides useful static utility methods related to the handling of money.
 */
public class MoneyUtils {

    /**
     * Defines the maximum amount of digits an exchange rate can contain.
     */
    public static final int EXCHANGE_RATE_FRACTION_DIGITS = 6;

    private MoneyUtils() {
        // class cannot be instantiated
    }

    /**
     * Returns a string with a number properly formatted as money.
     *
     * @param money        the number to format
     * @param currencyCode the currency code to use for the formatting
     * @return the properly formatted money string
     */
    public static String formatMoney(Number money, String currencyCode) {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault());
        currencyFormat.setRoundingMode(RoundingMode.HALF_UP);
        currencyFormat.setCurrency(Currency.getInstance(currencyCode));

        return currencyFormat.format(money);
    }

    /**
     * Returns a string with a number properly formatted as money withou decimals.
     *
     * @param money        the number to format
     * @param currencyCode the currency code to use for the formatting
     * @return the properly formatted money string without decimals
     */
    public static String formatMoneyNoDecimals(Number money, String currencyCode) {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault());
        currencyFormat.setRoundingMode(RoundingMode.HALF_UP);
        currencyFormat.setCurrency(Currency.getInstance(currencyCode));
        currencyFormat.setMaximumFractionDigits(0);

        return currencyFormat.format(money);
    }

    /**
     * Returns a properly formatted string according to the devices' locale and the currency
     * set for the group.
     *
     * @param priceString the price to format
     * @return the properly formatted price
     */
    public static String formatPrice(@NonNull String priceString, String currencyCode) {
        BigDecimal price = parsePrice(priceString);
        return formatMoneyNoSymbol(price, currencyCode);
    }

    /**
     * Returns a {@link BigDecimal} parsed from the passed string. If the parsing fails, returns
     * {@link BigDecimal#ZERO}.
     *
     * @param priceString the string to parse
     * @return the parsed {@link BigDecimal} or {@link BigDecimal#ZERO} if parsing failed
     */
    public static BigDecimal parsePrice(@NonNull String priceString) {
        BigDecimal price = BigDecimal.ZERO;
        try {
            price = new BigDecimal(priceString);
        } catch (NumberFormatException e) {
            try {
                price = parseLocalizedStringToBigDecimal(priceString);
            } catch (ParseException e1) {
                return price;
            }
        }

        return price;
    }

    /**
     * Returns the maximum amount of fraction digits for the passed currency code.
     *
     * @param currencyCode the currency code for which to return the fraction digits
     * @return the maximum amount of fraction digits
     */
    public static int getMaximumFractionDigits(String currencyCode) {
        Currency groupCurrency = Currency.getInstance(currencyCode);
        return groupCurrency.getDefaultFractionDigits();
    }

    /**
     * Returns a {@link BigDecimal} parsed from a localized money string. Uses the system's default
     * locale.
     *
     * @param string the string to parse.
     * @return a {@link BigDecimal} parsed from a localized money string
     * @throws ParseException if the parsing failed
     */
    @NonNull
    public static BigDecimal parseLocalizedStringToBigDecimal(String string) throws ParseException {
        DecimalFormat decimalFormat = (DecimalFormat) NumberFormat.getInstance(Locale.getDefault());
        decimalFormat.setParseBigDecimal(true);
        return (BigDecimal) decimalFormat.parseObject(string);
    }

    /**
     * Returns a string with a number properly formatted as money but without the currency symbol.
     *
     * @param money        the number to format
     * @param currencyCode the currency code to use for the formatting
     * @return the properly formatted money string without currency symbol
     */
    public static String formatMoneyNoSymbol(Number money, String currencyCode) {
        return formatMoneyNoSymbol(money, getMaximumFractionDigits(currencyCode));
    }

    /**
     * Returns a string with a number properly formatted as money but without the currency symbol.
     *
     * @param money             the number to format
     * @param maxFractionDigits the max fraction digits to use
     * @return the properly formatted money string without currency symbol
     */
    public static String formatMoneyNoSymbol(Number money, int maxFractionDigits) {
        NumberFormat numberFormat = NumberFormat.getInstance(Locale.getDefault());
        numberFormat.setRoundingMode(RoundingMode.HALF_UP);
        numberFormat.setMinimumFractionDigits(maxFractionDigits);
        numberFormat.setMaximumFractionDigits(maxFractionDigits);

        return numberFormat.format(money);
    }

    /**
     * Returns a {@link BigDecimal} from the passed number and with a maximum amount of fraction
     * digits.
     *
     * @param numberOfFractionDigits the max amount of fraction digits to use
     * @param number                 the number to parse
     * @return a {@link BigDecimal} rounded to the max amount of fraction digits
     */
    public static BigDecimal roundToFractionDigits(int numberOfFractionDigits, double number) {
        return new BigDecimal(number).setScale(numberOfFractionDigits, RoundingMode.HALF_UP);
    }

    /**
     * Returns the display names for the currency codes.
     *
     * @param currencyCodes the currency codes to get the display names for
     * @return the display names for the currency codes
     */
    @NonNull
    public static List<String> getCurrencyDisplayNames(@NonNull List<String> currencyCodes) {
        List<String> displayNames = new ArrayList<>(currencyCodes.size());

        for (String currencyCode : currencyCodes) {
            displayNames.add(getCurrencyDisplayName(currencyCode));
        }

        return displayNames;
    }

    /**
     * Returns the display name for currency code.
     * <p/>
     * TODO: only works on api >19
     *
     * @param currencyCode the currency code to get the display name for
     * @return the display name for currency code
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static String getCurrencyDisplayName(String currencyCode) {
        Currency currency = Currency.getInstance(currencyCode);
        return currency.getDisplayName(Locale.getDefault());
    }
}
