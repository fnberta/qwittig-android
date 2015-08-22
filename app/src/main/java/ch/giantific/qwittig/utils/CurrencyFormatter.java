package ch.giantific.qwittig.utils;

import com.github.mikephil.charting.utils.ValueFormatter;

import ch.giantific.qwittig.utils.MoneyUtils;

/**
 * Created by fabio on 20.07.15.
 */
public class CurrencyFormatter implements ValueFormatter {

    private String mCurrencyCode;

    public CurrencyFormatter(String currencyCode) {
        mCurrencyCode = currencyCode;
    }

    @Override
    public String getFormattedValue(float value) {
        return MoneyUtils.formatMoneyNoDecimals(value, mCurrencyCode);
    }
}
