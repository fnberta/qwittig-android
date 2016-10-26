/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.ocrrating.di;

import android.support.annotation.NonNull;

import ch.giantific.qwittig.data.repositories.PurchaseRepository;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.di.scopes.PerActivity;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.purchases.ocrrating.OcrRatingContract;
import ch.giantific.qwittig.presentation.purchases.ocrrating.OcrRatingPresenter;
import ch.giantific.qwittig.presentation.purchases.ocrrating.OcrRatingViewModel;
import dagger.Module;
import dagger.Provides;

/**
 * Defines which implementation to use for the purchase addItemAtPosition screen and how to instantiate it.
 */
@Module
public class OcrRatingPresenterModule {

    private final String ocrDataId;

    public OcrRatingPresenterModule(@NonNull String ocrDataId) {
        this.ocrDataId = ocrDataId;
    }

    @PerActivity
    @Provides
    OcrRatingContract.Presenter providesOcrRatingPresenter(@NonNull Navigator navigator,
                                                           @NonNull OcrRatingViewModel viewModel,
                                                           @NonNull UserRepository userRepo,
                                                           @NonNull PurchaseRepository purchaseRepo) {
        return new OcrRatingPresenter(navigator, viewModel, userRepo, purchaseRepo, ocrDataId);
    }
}
