package ch.giantific.qwittig.data.helper;

import android.support.annotation.NonNull;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import ch.giantific.qwittig.presentation.settings.groupusers.addgroup.Currency;

/**
 * Created by fabio on 03.07.16.
 */
public class RemoteConfigHelper {

    private static final String SHOW_OCR_RATING = "show_ocr_rating";
    private static final String SUPPORTED_CURRENCIES = "supported_currencies";
    private static final String DEFAULT_GROUP_NAME = "default_group_name";
    private static final String DEFAULT_GROUP_CURRENCY = "default_group_currency";

    private final FirebaseRemoteConfig remoteConfig;

    @Inject
    public RemoteConfigHelper(@NonNull FirebaseRemoteConfig remoteConfig) {
        this.remoteConfig = remoteConfig;
    }

    public void fetchAndActivate() {
        remoteConfig.fetch();
        remoteConfig.activateFetched();
    }

    public boolean isShowOcrRating() {
        return remoteConfig.getBoolean(SHOW_OCR_RATING);
    }

    public String[] getSupportedCurrencyCodes() {
        return remoteConfig.getString(SUPPORTED_CURRENCIES).split(",");
    }

    /**
     * Returns the currently supported currencies as
     * {@link Currency} objects with a name and currency code.
     *
     * @return the currently supported currencies
     */
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

    public String getDefaultGroupName() {
        return remoteConfig.getString(DEFAULT_GROUP_NAME);
    }

    public String getDefaultGroupCurrency() {
        return remoteConfig.getString(DEFAULT_GROUP_CURRENCY);
    }
}
