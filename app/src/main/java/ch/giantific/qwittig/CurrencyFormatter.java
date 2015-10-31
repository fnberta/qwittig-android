/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig;

import android.support.annotation.NonNull;

import com.github.mikephil.charting.utils.ValueFormatter;

import ch.giantific.qwittig.utils.MoneyUtils;

/**
 * Formats stats value types as currencies without decimals.
 * <p/>
 * Implements {@link ValueFormatter}.
 */
public class CurrencyFormatter implements ValueFormatter {

    private String mCurrencyCode;

    /**
     * Constructs new {@link CurrencyFormatter} with the currency code specified.
     *
     * @param currencyCode the currency code to use for the formatting
     */
    public CurrencyFormatter(@NonNull String currencyCode) {
        mCurrencyCode = currencyCode;
    }

    @NonNull
    @Override
    public String getFormattedValue(float value) {
        return MoneyUtils.formatMoneyNoDecimals(value, mCurrencyCode);
    }
}
