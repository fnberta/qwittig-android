/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.ocrrating.di;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.data.bus.RxBus;
import ch.giantific.qwittig.data.repositories.PurchaseRepository;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.di.scopes.PerActivity;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.di.BaseViewModelModule;
import ch.giantific.qwittig.presentation.purchases.ocrrating.OcrRatingViewModel;
import ch.giantific.qwittig.presentation.purchases.ocrrating.OcrRatingViewModelImpl;
import dagger.Module;
import dagger.Provides;

/**
 * Defines which implementation to use for the purchase add screen and how to instantiate it.
 */
@Module
public class OcrRatingViewModelModule extends BaseViewModelModule {

    private final String ocrDataId;

    public OcrRatingViewModelModule(@Nullable Bundle savedState, @NonNull String ocrDataId) {
        super(savedState);

        this.ocrDataId = ocrDataId;
    }

    @PerActivity
    @Provides
    OcrRatingViewModel providesOcrRatingViewModel(@NonNull Navigator navigator,
                                                  @NonNull RxBus<Object> eventBus,
                                                  @NonNull UserRepository userRepository,
                                                  @NonNull PurchaseRepository purchaseRepository) {
        return new OcrRatingViewModelImpl(savedState, navigator, eventBus, userRepository,
                purchaseRepository, ocrDataId);
    }
}
