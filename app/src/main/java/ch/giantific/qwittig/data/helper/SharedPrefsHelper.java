package ch.giantific.qwittig.data.helper;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import java.math.BigDecimal;
import java.util.Map;

import javax.inject.Inject;

import ch.giantific.qwittig.utils.MoneyUtils;

/**
 * Created by fabio on 04.07.16.
 */
public class SharedPrefsHelper {

    private static final String EXCHANGE_RATE_LAST_FETCHED_TIME = "EXCHANGE_RATE_LAST_FETCHED_TIME";
    private static final long EXCHANGE_RATE_REFRESH_INTERVAL = 24 * 60 * 60 * 1000;
    private SharedPreferences mSharedPrefs;

    @Inject
    public SharedPrefsHelper(@NonNull SharedPreferences sharedPrefs) {
        mSharedPrefs = sharedPrefs;
    }

    public boolean isExchangeRatesFetchNeeded() {
        long lastFetched = mSharedPrefs.getLong(EXCHANGE_RATE_LAST_FETCHED_TIME, 0);
        long currentTime = System.currentTimeMillis();
        return (currentTime - lastFetched) > EXCHANGE_RATE_REFRESH_INTERVAL;
    }

    public float getExchangeRate(@NonNull String currencyCode) {
        return mSharedPrefs.getFloat(currencyCode, 1);
    }

    public void saveExchangeRates(@NonNull Map<String, Float> exchangeRates) {
        final SharedPreferences.Editor editor = mSharedPrefs.edit();
        for (Map.Entry<String, Float> exchangeRate : exchangeRates.entrySet()) {
            final BigDecimal roundedExchangeRate =
                    MoneyUtils.roundExchangeRate(1 / exchangeRate.getValue());
            editor.putFloat(exchangeRate.getKey(), roundedExchangeRate.floatValue());
        }
        final long currentTime = System.currentTimeMillis();
        editor.putLong(EXCHANGE_RATE_LAST_FETCHED_TIME, currentTime);
        editor.apply();
    }
}
