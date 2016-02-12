/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.data.rest;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

/**
 * Represents a collection of currency rates stored in a {@link Map}.
 */
public class CurrencyRates {

    @SerializedName("base")
    private String mBase;
    @SerializedName("date")
    private String mDate;
    @SerializedName("rates")
    private Map<String, Float> mRates;

    public String getBase() {
        return mBase;
    }

    public void setBase(String base) {
        mBase = base;
    }

    public String getDate() {
        return mDate;
    }

    public void setDate(String date) {
        mDate = date;
    }

    public Map<String, Float> getRates() {
        return mRates;
    }

    public void setRates(Map<String, Float> rates) {
        mRates = rates;
    }
}
