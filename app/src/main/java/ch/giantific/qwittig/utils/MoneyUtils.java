package ch.giantific.qwittig.utils;

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
 * Created by fabio on 27.03.15.
 */
public class MoneyUtils {

    public static final int EXCHANGE_RATE_FRACTION_DIGITS = 6;

    private MoneyUtils() {
        // class cannot be instantiated
    }

    public static String formatMoney(Number money, String currencyCode) {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault());
        currencyFormat.setRoundingMode(RoundingMode.HALF_UP);
        currencyFormat.setCurrency(Currency.getInstance(currencyCode));

        return currencyFormat.format(money);
    }

    public static String formatMoneyNoDecimals(Number money, String currencyCode) {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault());
        currencyFormat.setRoundingMode(RoundingMode.HALF_UP);
        currencyFormat.setCurrency(Currency.getInstance(currencyCode));
        currencyFormat.setMaximumFractionDigits(0);

        return currencyFormat.format(money);
    }

    /**
     * Formats the price view according to the devices' locale and the currency set for the group.
     *
     * @param priceString
     * @return the formatted price as a String
     */
    public static String formatPrice(String priceString, String currencyCode) {
        BigDecimal price = parsePrice(priceString);
        return formatMoneyNoSymbol(price, currencyCode);
    }

    public static BigDecimal parsePrice(String priceString) {
        BigDecimal price = BigDecimal.ZERO;
        try {
            price = new BigDecimal(priceString);
        } catch (NumberFormatException e) {
            try {
                price = parseLocalizedStringToBigDecimal(priceString);
            } catch (ParseException e1) {
                e1.printStackTrace();
            }
        }

        return price;
    }

    public static int getMaximumFractionDigits(String currencyCode) {
        Currency groupCurrency = Currency.getInstance(currencyCode);
        return groupCurrency.getDefaultFractionDigits();
    }

    public static BigDecimal parseLocalizedStringToBigDecimal(String string) throws ParseException {
        DecimalFormat decimalFormat = (DecimalFormat) NumberFormat.getInstance(Locale.getDefault());
        decimalFormat.setParseBigDecimal(true);
        return (BigDecimal) decimalFormat.parseObject(string);
    }

    public static String formatMoneyNoSymbol(Number money, String currencyCode) {
        return formatMoneyNoSymbol(money, getMaximumFractionDigits(currencyCode));
    }

    public static String formatMoneyNoSymbol(Number money, int maxFractionDigits) {
        NumberFormat numberFormat = NumberFormat.getInstance(Locale.getDefault());
        numberFormat.setRoundingMode(RoundingMode.HALF_UP);
        numberFormat.setMinimumFractionDigits(maxFractionDigits);
        numberFormat.setMaximumFractionDigits(maxFractionDigits);

        return numberFormat.format(money);
    }

    public static BigDecimal roundToFractionDigits(int numberOfFractionDigits, double number) {
        return new BigDecimal(number).setScale(numberOfFractionDigits, RoundingMode.HALF_UP);
    }

    public static List<String> getCurrencyDisplayNames(List<String> currencyCodes) {
        List<String> displayNames = new ArrayList<>();

        for (String currencyCode : currencyCodes) {
            displayNames.add(getCurrencyDisplayName(currencyCode));
        }

        return displayNames;
    }

    public static String getCurrencyDisplayName(String currencyCode) {
        Currency currency = Currency.getInstance(currencyCode);
        return currency.getDisplayName(Locale.getDefault());
    }
}
