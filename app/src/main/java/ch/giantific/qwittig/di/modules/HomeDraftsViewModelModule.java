/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.di.modules;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.di.scopes.PerFragment;
import ch.giantific.qwittig.domain.repositories.GroupRepository;
import ch.giantific.qwittig.domain.repositories.PurchaseRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.home.purchases.list.HomeDraftsViewModel;
import ch.giantific.qwittig.presentation.home.purchases.list.HomeDraftsViewModelImpl;
import dagger.Module;
import dagger.Provides;

/**
 * Created by fabio on 12.01.16.
 */
@Module
public class HomeDraftsViewModelModule extends BaseViewModelModule<HomeDraftsViewModel.ViewListener> {

    public HomeDraftsViewModelModule(@Nullable Bundle savedState,
                                     @NonNull HomeDraftsViewModel.ViewListener view) {
        super(savedState, view);
    }

    @PerFragment
    @Provides
    HomeDraftsViewModel providesHomeDraftsViewModel(@NonNull GroupRepository groupRepository,
                                                    @NonNull UserRepository userRepository,
                                                    @NonNull PurchaseRepository purchaseRepository) {
        return new HomeDraftsViewModelImpl(mSavedState, mView, groupRepository, userRepository,
                purchaseRepository);
    }
}
