/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.data.rest;

import java.util.Map;

/**
 * Represents a collection of currency rates stored in a {@link Map}.
 */
public class ExchangeRatesResult {

    private String base;
    private String date;
    private Map<String, Float> rates;

    public ExchangeRatesResult(String base, String date, Map<String, Float> rates) {
        this.base = base;
        this.date = date;
        this.rates = rates;
    }

    public String getBase() {
        return base;
    }

    public String getDate() {
        return date;
    }

    public Map<String, Float> getRates() {
        return rates;
    }
}
