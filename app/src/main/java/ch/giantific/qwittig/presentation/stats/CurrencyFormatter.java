/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.stats;

import android.support.annotation.NonNull;

import com.github.mikephil.charting.utils.ValueFormatter;

import java.text.NumberFormat;

import ch.giantific.qwittig.utils.MoneyUtils;

/**
 * Formats stats value types as currencies without decimals.
 * <p/>
 * Implements {@link ValueFormatter}.
 */
public class CurrencyFormatter implements ValueFormatter {

    private NumberFormat mMoneyFormatter;

    /**
     * Constructs new {@link CurrencyFormatter} with the currency code specified.
     *
     * @param currencyCode the currency code to use for the formatting
     */
    public CurrencyFormatter(@NonNull String currencyCode) {
        mMoneyFormatter = MoneyUtils.getMoneyFormatter(currencyCode, true, false);
    }

    @NonNull
    @Override
    public String getFormattedValue(float value) {
        return mMoneyFormatter.format(value);
    }
}
