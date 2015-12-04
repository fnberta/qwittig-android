/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.workerfragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.Map;

import ch.giantific.qwittig.data.rest.ExchangeRatesClient;
import ch.giantific.qwittig.domain.models.rates.CurrencyRates;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Fetches the newest currency exchange rates online using {@link ExchangeRatesClient.ExchangeRates}.
 * <p/>
 * Subclass of {@link BaseWorker}.
 */
public class RatesWorker extends BaseWorker {

    private static final String LOG_TAG = RatesWorker.class.getSimpleName();
    private static final String BUNDLE_BASE_CURRENCY = "BUNDLE_BASE_CURRENCY";
    @Nullable
    private WorkerInteractionListener mListener;

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

    @Override
    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (WorkerInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement DialogInteractionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String baseCurrency = "";
        Bundle args = getArguments();
        if (args != null) {
            baseCurrency = args.getString(BUNDLE_BASE_CURRENCY, "");
        }

        if (TextUtils.isEmpty(baseCurrency)) {
            if (mListener != null) {
                mListener.onRatesFetchFailed("");
            }

            return;
        }

        getRates(baseCurrency);
    }

    private void getRates(@NonNull String baseCurrency) {
        ExchangeRatesClient.getService().getRates(baseCurrency, new Callback<CurrencyRates>() {
            @Override
            public void success(@NonNull CurrencyRates currencyRates, Response response) {
                Map<String, Float> exchangeRates = currencyRates.getRates();
                if (mListener != null) {
                    mListener.onRatesFetched(exchangeRates);
                }
            }

            @Override
            public void failure(@NonNull RetrofitError error) {
                if (mListener != null) {
                    mListener.onRatesFetchFailed(error.getLocalizedMessage());
                }
            }
        });
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * Defines the actions to take after the rates were fetched or after the fetch failed.
     */
    public interface WorkerInteractionListener {
        /**
         * Handles the successful fetch of current currency exchange rates.
         *
         * @param exchangeRates the fetched currency exchange rates
         */
        void onRatesFetched(@NonNull Map<String, Float> exchangeRates);

        /**
         * Handles the failed fetch of current currency exchange rates.
         *
         * @param errorMessage the error message received from the server
         */
        void onRatesFetchFailed(@NonNull String errorMessage);
    }
}
