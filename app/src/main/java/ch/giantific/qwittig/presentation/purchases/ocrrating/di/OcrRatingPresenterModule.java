/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.ocrrating.di;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.data.repositories.PurchaseRepository;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.di.scopes.PerActivity;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.di.BasePresenterModule;
import ch.giantific.qwittig.presentation.purchases.ocrrating.OcrRatingContract;
import ch.giantific.qwittig.presentation.purchases.ocrrating.OcrRatingPresenter;
import dagger.Module;
import dagger.Provides;

/**
 * Defines which implementation to use for the purchase add screen and how to instantiate it.
 */
@Module
public class OcrRatingPresenterModule extends BasePresenterModule {

    private final String ocrDataId;

    public OcrRatingPresenterModule(@Nullable Bundle savedState, @NonNull String ocrDataId) {
        super(savedState);

        this.ocrDataId = ocrDataId;
    }

    @PerActivity
    @Provides
    OcrRatingContract.Presenter providesOcrRatingPresenter(@NonNull Navigator navigator,
                                                           @NonNull UserRepository userRepo,
                                                           @NonNull PurchaseRepository purchaseRepo) {
        return new OcrRatingPresenter(savedState, navigator, userRepo, purchaseRepo, ocrDataId);
    }
}
