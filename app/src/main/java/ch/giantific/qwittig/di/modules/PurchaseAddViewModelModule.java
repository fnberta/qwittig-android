/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.di.modules;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.di.scopes.PerFragment;
import ch.giantific.qwittig.domain.repositories.GroupRepository;
import ch.giantific.qwittig.domain.repositories.PurchaseRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.viewmodels.PurchaseAddEditViewModel;
import ch.giantific.qwittig.presentation.viewmodels.PurchaseAddEditViewModelAddAutoImpl;
import ch.giantific.qwittig.presentation.viewmodels.PurchaseAddEditViewModelAddImpl;
import dagger.Module;
import dagger.Provides;

/**
 * Created by fabio on 12.01.16.
 */
@Module
public class PurchaseAddViewModelModule extends BaseViewModelModule {

    public PurchaseAddViewModelModule(@Nullable Bundle savedState) {
        super(savedState);
    }

    @PerFragment
    @Provides
    PurchaseAddEditViewModel providesPurchaseAddViewModel(@NonNull GroupRepository groupRepository,
                                                          @NonNull UserRepository userRepository,
                                                          @NonNull SharedPreferences sharedPreferences,
                                                          @NonNull PurchaseRepository purchaseRepository) {
        return new PurchaseAddEditViewModelAddImpl(mSavedState, groupRepository, userRepository,
                sharedPreferences, purchaseRepository);
    }
}
