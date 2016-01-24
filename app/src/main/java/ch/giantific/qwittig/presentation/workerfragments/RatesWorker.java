/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.presentation.workerfragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import javax.inject.Inject;

import ch.giantific.qwittig.data.rest.ExchangeRates;
import ch.giantific.qwittig.domain.models.rates.CurrencyRates;
import rx.Observable;

/**
 * Fetches the newest currency exchange rates online using {@link ExchangeRates}.
 * <p/>
 * Subclass of {@link BaseWorker}.
 */
public class RatesWorker extends BaseWorker<CurrencyRates, RatesWorkerListener> {

    public static final String WORKER_TAG = "RATES_WORKER";
    private static final String LOG_TAG = RatesWorker.class.getSimpleName();
    private static final String BUNDLE_BASE_CURRENCY = "BUNDLE_BASE_CURRENCY";
    @Inject
    ExchangeRates mExchangeRates;

    public RatesWorker() {
        // empty default constructor
    }

    /**
     * Returns a new instance of {@link RatesWorker} with a base currency code as an argument.
     *
     * @param baseCurrency the currency to use as a base for the foreign currencies
     * @return a new instance of {@link RatesWorker}
     */
    @NonNull
    public static RatesWorker newInstance(@NonNull String baseCurrency) {
        RatesWorker fragment = new RatesWorker();
        Bundle args = new Bundle();
        args.putString(BUNDLE_BASE_CURRENCY, baseCurrency);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    protected Observable<CurrencyRates> getObservable(@NonNull Bundle args) {
        final String baseCurrency = args.getString(BUNDLE_BASE_CURRENCY, "");
        if (!TextUtils.isEmpty(baseCurrency)) {
            return mExchangeRates.getRates(baseCurrency);
        }

        return null;
    }

    @Override
    protected void onError() {
        mActivity.onWorkerError(WORKER_TAG);
    }

    @Override
    protected void setStream(@NonNull Observable<CurrencyRates> observable) {
        mActivity.setRatesFetchStream(observable, WORKER_TAG);
    }
}
