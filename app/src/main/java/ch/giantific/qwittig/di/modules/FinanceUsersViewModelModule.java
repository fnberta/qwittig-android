/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.di.modules;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.di.scopes.PerFragment;
import ch.giantific.qwittig.domain.repositories.IdentityRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.finance.identities.IdentitiesViewModel;
import ch.giantific.qwittig.presentation.finance.identities.IdentitiesViewModelImpl;
import dagger.Module;
import dagger.Provides;

/**
 * Created by fabio on 12.01.16.
 */
@Module
public class FinanceUsersViewModelModule extends BaseViewModelModule<IdentitiesViewModel.ViewListener> {

    public FinanceUsersViewModelModule(@Nullable Bundle savedState,
                                       @NonNull IdentitiesViewModel.ViewListener view) {
        super(savedState, view);
    }

    @PerFragment
    @Provides
    IdentitiesViewModel providesFinanceUsersViewModel(@NonNull IdentityRepository identityRepository,
                                                      @NonNull UserRepository userRepository) {
        return new IdentitiesViewModelImpl(mSavedState, mView, identityRepository, userRepository);
    }
}
