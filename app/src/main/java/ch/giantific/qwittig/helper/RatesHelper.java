package ch.giantific.qwittig.helper;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.text.TextUtils;

import java.util.Map;

import ch.giantific.qwittig.data.rates.RestClient;
import ch.giantific.qwittig.data.rates.models.CurrencyRates;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by fabio on 10.12.14.
 */
public class RatesHelper extends Fragment {

    private static final String LOG_TAG = RatesHelper.class.getSimpleName();
    private static final String BUNDLE_BASE_CURRENCY = "base_currency";
    private HelperInteractionListener mListener;

    public RatesHelper() {
        // empty default constructor
    }

    public static RatesHelper newInstance(String baseCurrency) {
        RatesHelper fragment = new RatesHelper();
        Bundle args = new Bundle();
        args.putString(BUNDLE_BASE_CURRENCY, baseCurrency);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (HelperInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement FragmentInteractionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retain this fragment across configuration changes.
        setRetainInstance(true);

        String baseCurrency = "";
        Bundle args = getArguments();
        if (args != null) {
            baseCurrency = args.getString(BUNDLE_BASE_CURRENCY);
        }

        if (!TextUtils.isEmpty(baseCurrency)) {
            getRates(baseCurrency);
        }
    }

    private void getRates(String baseCurrency) {
        RestClient.getService().getRates(baseCurrency, new Callback<CurrencyRates>() {
            @Override
            public void success(CurrencyRates currencyRates, Response response) {
                Map<String, Double> exchangeRates = currencyRates.getRates();
                if (mListener != null) {
                    mListener.onRatesFetchSuccessful(exchangeRates);
                }
            }

            @Override
            public void failure(RetrofitError error) {
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

    public interface HelperInteractionListener {
        void onRatesFetchSuccessful(Map<String, Double> exchangeRates);
        void onRatesFetchFailed(String errorMessage);
    }
}
