package ch.giantific.qwittig.data.rates;

import com.squareup.okhttp.OkHttpClient;

import java.util.concurrent.TimeUnit;

import ch.giantific.qwittig.data.rates.models.CurrencyRates;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.client.OkClient;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Created by fabio on 01.02.15.
 */
public class RestClient {

    private static final String BASE_URL = "http://api.fixer.io";
    private static final long READ_TIMEOUT = 300000;
    private static final RestAdapter REST_ADAPTER = new RestAdapter.Builder()
            .setEndpoint(BASE_URL)
            .setClient(new OkClient(generateOkHttp()))
            .build();
    private static final ExchangeRates EXCHANGE_RATES_SERVICE = REST_ADAPTER.create(ExchangeRates.class);

    private RestClient() {
    }

    public static ExchangeRates getService() {
        return EXCHANGE_RATES_SERVICE;
    }

    private static OkHttpClient generateOkHttp() {
        OkHttpClient client = new OkHttpClient();
        client.setReadTimeout(READ_TIMEOUT, TimeUnit.MILLISECONDS);
        return client;
    }

    public interface ExchangeRates {
        @GET("/latest")
        void getRates(@Query("base") String baseCurrency, Callback<CurrencyRates> callback);
    }
}
