/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.data.helpers;

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
 * Subclass of {@link BaseHelper}.
 */
public class RatesHelper extends BaseHelper {

    private static final String LOG_TAG = RatesHelper.class.getSimpleName();
    private static final String BUNDLE_BASE_CURRENCY = "BUNDLE_BASE_CURRENCY";
    @Nullable
    private HelperInteractionListener mListener;

    public RatesHelper() {
        // empty default constructor
    }

    /**
     * Returns a new instance of {@link RatesHelper} with a base currency code as an argument.
     *
     * @param baseCurrency the currency to use as a base for the foreign currencies
     * @return a new instance of {@link RatesHelper}
     */
    @NonNull
    public static RatesHelper newInstance(@NonNull String baseCurrency) {
        RatesHelper fragment = new RatesHelper();
        Bundle args = new Bundle();
        args.putString(BUNDLE_BASE_CURRENCY, baseCurrency);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (HelperInteractionListener) activity;
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
            baseCurrency = args.getString(BUNDLE_BASE_CURRENCY);
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
    public interface HelperInteractionListener {
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
