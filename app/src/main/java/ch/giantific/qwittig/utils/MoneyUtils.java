/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.utils;

import android.support.annotation.NonNull;

import com.parse.ParseConfig;

import java.math.BigDecimal;
import java.math.RoundingMode;
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

    // TODO: replace with currency dependent value
    public static final double MIN_DIFF = 0.01;
    private static final int EXCHANGE_RATE_FRACTION_DIGITS = 6;
    private static final int CONVERTED_PRICE_FRACTION_DIGITS = 4;

    private MoneyUtils() {
        // class cannot be instantiated
    }

    /**
     * Returns a {@link NumberFormat} instance that formats digits like money in the given currency.
     *
     * @param currencyCode the currency in which to format
     * @param withSymbol   whether to display the currency symbol
     * @param withDecimals whether to display decimals
     * @return {@link NumberFormat} instance that formats digits like money
     */
    public static NumberFormat getMoneyFormatter(@NonNull String currencyCode, boolean withSymbol,
                                                 boolean withDecimals) {
        final NumberFormat moneyFormatter;
        if (withSymbol) {
            moneyFormatter = NumberFormat.getCurrencyInstance();
            moneyFormatter.setRoundingMode(RoundingMode.HALF_UP);
            moneyFormatter.setCurrency(Currency.getInstance(currencyCode));
            if (!withDecimals) {
                moneyFormatter.setMaximumFractionDigits(0);
            }
        } else {
            moneyFormatter = NumberFormat.getInstance();
            moneyFormatter.setRoundingMode(RoundingMode.HALF_UP);
            final int fractionDigits = withDecimals
                    ? Currency.getInstance(currencyCode).getDefaultFractionDigits()
                    : 0;
            moneyFormatter.setMinimumFractionDigits(fractionDigits);
            moneyFormatter.setMaximumFractionDigits(fractionDigits);
        }

        return moneyFormatter;
    }

    /**
     * Returns a {@link NumberFormat} instance that formats the exchange rate with the default
     * number of decimal digits.
     *
     * @return a {@link NumberFormat} instance that formats an exchange rate
     */
    public static NumberFormat getExchangeRateFormatter() {
        final NumberFormat moneyFormatter = NumberFormat.getInstance();
        moneyFormatter.setMinimumFractionDigits(EXCHANGE_RATE_FRACTION_DIGITS);
        moneyFormatter.setMaximumFractionDigits(EXCHANGE_RATE_FRACTION_DIGITS);

        return moneyFormatter;
    }

    /**
     * Returns a parsed double using a standard {@link NumberFormat} instance for the system's
     * default locale.
     *
     * @param price the price string to parse
     * @return a parsed double
     */
    public static double parsePrice(@NonNull String price) {
        final NumberFormat parser = NumberFormat.getInstance();
        try {
            return Double.parseDouble(price);
        } catch (NumberFormatException e) {
            try {
                return parser.parse(price).doubleValue();
            } catch (ParseException e1) {
                return 0;
            }
        }
    }

    /**
     * Returns the default amount of fraction digits for the passed currency code.
     *
     * @param currencyCode the currency code for which to return the fraction digits
     * @return the maximum amount of fraction digits
     */
    public static int getFractionDigits(@NonNull String currencyCode) {
        final Currency groupCurrency = Currency.getInstance(currencyCode);
        return groupCurrency.getDefaultFractionDigits();
    }

    /**
     * Returns a properly rounded {@link BigDecimal}.
     *
     * @param exchangeRate the rate to round
     * @return the rounded exchange rate
     */
    public static BigDecimal roundExchangeRate(double exchangeRate) {
        return roundToFractionDigits(exchangeRate, EXCHANGE_RATE_FRACTION_DIGITS);
    }

    /**
     * Returns a properly rounded {@link BigDecimal}.
     *
     * @param price the price to round
     * @return the rounded price
     */
    public static BigDecimal roundConvertedPrice(double price) {
        return roundToFractionDigits(price, CONVERTED_PRICE_FRACTION_DIGITS);
    }

    /**
     * Returns a {@link BigDecimal} from the passed number and with a maximum amount of fraction
     * digits.
     *
     * @param number         the number to parse
     * @param fractionDigits the max amount of fraction digits to use
     * @return a {@link BigDecimal} rounded to the max amount of fraction digits
     */
    private static BigDecimal roundToFractionDigits(double number, int fractionDigits) {
        return new BigDecimal(number).setScale(fractionDigits, RoundingMode.HALF_UP);
    }
}
