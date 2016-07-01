package ch.giantific.qwittig.data.repositories;

import android.support.annotation.NonNull;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ch.giantific.qwittig.domain.repositories.RemoteConfigRepository;
import ch.giantific.qwittig.presentation.settings.groupusers.addgroup.Currency;

/**
 * Created by fabio on 30.06.16.
 */
public class FirebaseRemoteConfigRepository implements RemoteConfigRepository {

    private FirebaseRemoteConfig mRemoteConfig;

    public FirebaseRemoteConfigRepository(@NonNull FirebaseRemoteConfig remoteConfig) {
        mRemoteConfig = remoteConfig;
    }

    @Override
    public int getErrorMessage(@NonNull Throwable e) {
        return 0;
    }

    @Override
    public boolean isAlreadySavedLocal(@NonNull String objectId) {
        return false;
    }

    @Override
    public void fetchAndActivate() {
        mRemoteConfig.fetch();
        mRemoteConfig.activateFetched();
    }

    @Override
    public boolean isShowOcrRating() {
        return mRemoteConfig.getBoolean(SHOW_OCR_RATING);
    }

    @Override
    public String[] getSupportedCurrencyCodes() {
        return mRemoteConfig.getString(SUPPORTED_CURRENCIES).split(",");
    }

    @Override
    public List<Currency> getSupportedCurrencies() {
        final String[] currencyCodes = getSupportedCurrencyCodes();
        final List<String> displayNames = new ArrayList<>(currencyCodes.length);
        for (String currencyCode : currencyCodes) {
            displayNames.add(getCurrencyDisplayName(currencyCode));
        }

        final List<Currency> currencies = new ArrayList<>(currencyCodes.length);
        for (int i = 0; i < currencyCodes.length; i++) {
            currencies.add(new Currency(displayNames.get(i), currencyCodes[i]));
        }

        return currencies;
    }

    private String getCurrencyDisplayName(@NonNull String currencyCode) {
        final java.util.Currency currency = java.util.Currency.getInstance(currencyCode);
        return currency.getDisplayName(Locale.getDefault());
    }
}
