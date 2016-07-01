/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.domain.repositories;

import java.util.List;

import ch.giantific.qwittig.presentation.settings.groupusers.addgroup.Currency;

/**
 * Provides the methods to get the remote config options
 */
public interface RemoteConfigRepository extends BaseRepository {

    String SHOW_OCR_RATING = "show_ocr_rating";
    String SUPPORTED_CURRENCIES = "supported_currencies";

    void fetchAndActivate();

    boolean isShowOcrRating();

    String[] getSupportedCurrencyCodes();

    /**
     * Returns the currently supported currencies as
     * {@link Currency} objects with a name and currency code.
     *
     * @return the currently supported currencies
     */
    List<Currency> getSupportedCurrencies();
}
