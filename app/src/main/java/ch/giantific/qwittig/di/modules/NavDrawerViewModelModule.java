/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.di.modules;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.di.scopes.PerActivity;
import ch.giantific.qwittig.domain.repositories.IdentityRepository;
import ch.giantific.qwittig.domain.repositories.PurchaseRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.navdrawer.NavDrawerViewModel;
import ch.giantific.qwittig.presentation.navdrawer.NavDrawerViewModelImpl;
import dagger.Module;
import dagger.Provides;

/**
 * Created by fabio on 12.01.16.
 */
@Module
public class NavDrawerViewModelModule extends BaseViewModelModule<NavDrawerViewModel.ViewListener> {

    public NavDrawerViewModelModule(@Nullable Bundle savedState,
                                    @NonNull NavDrawerViewModel.ViewListener view) {
        super(savedState, view);
    }

    @PerActivity
    @Provides
    NavDrawerViewModel providesNavDrawerViewModel(@NonNull UserRepository userRepository,
                                                  @NonNull IdentityRepository identityRepository) {
        return new NavDrawerViewModelImpl(mSavedState, mView, userRepository, identityRepository);
    }

}
