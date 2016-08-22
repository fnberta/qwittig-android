/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.list.di;

import ch.giantific.qwittig.di.scopes.PerActivity;
import ch.giantific.qwittig.presentation.common.di.GoogleApiClientDelegateModule;
import ch.giantific.qwittig.presentation.purchases.list.HomeActivity;
import ch.giantific.qwittig.presentation.purchases.list.drafts.DraftsFragment;
import ch.giantific.qwittig.presentation.purchases.list.drafts.DraftsViewModel;
import ch.giantific.qwittig.presentation.purchases.list.purchases.PurchasesFragment;
import ch.giantific.qwittig.presentation.purchases.list.purchases.PurchasesViewModel;
import dagger.Subcomponent;

/**
 * Provides the dependencies for the home screen.
 */
@PerActivity
@Subcomponent(modules = {HomeViewModelModule.class, GoogleApiClientDelegateModule.class,
        PurchasesListViewModelModule.class, DraftsListViewModelModule.class})
public interface HomeSubcomponent {

    void inject(HomeActivity homeActivity);

    void inject(PurchasesFragment purchasesFragment);

    void inject(DraftsFragment draftsFragment);

    PurchasesViewModel getPurchasesViewModel();

    DraftsViewModel getDraftsViewModel();
}
