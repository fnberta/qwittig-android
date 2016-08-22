/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.data.rest;

import java.util.Map;

/**
 * Represents a collection of currency rates stored in a {@link Map}.
 */
public class CurrencyRates {

    private String base;
    private String date;
    private Map<String, Float> rates;

    public String getBase() {
        return base;
    }

    public void setBase(String base) {
        this.base = base;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Map<String, Float> getRates() {
        return rates;
    }

    public void setRates(Map<String, Float> rates) {
        this.rates = rates;
    }
}
