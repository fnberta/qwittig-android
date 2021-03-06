/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.list.di;

import ch.giantific.qwittig.di.scopes.PerActivity;
import ch.giantific.qwittig.presentation.common.di.GoogleApiClientDelegateModule;
import ch.giantific.qwittig.presentation.common.di.PersistentViewModelsModule;
import ch.giantific.qwittig.presentation.common.di.SimplePresentersModule;
import ch.giantific.qwittig.presentation.purchases.list.HomeActivity;
import ch.giantific.qwittig.presentation.purchases.list.drafts.DraftsFragment;
import ch.giantific.qwittig.presentation.purchases.list.purchases.PurchasesFragment;
import dagger.Subcomponent;

/**
 * Provides the dependencies for the home screen.
 */
@PerActivity
@Subcomponent(modules = {SimplePresentersModule.class, PersistentViewModelsModule.class,
        GoogleApiClientDelegateModule.class})
public interface HomeSubcomponent {

    void inject(HomeActivity homeActivity);

    void inject(PurchasesFragment purchasesFragment);

    void inject(DraftsFragment draftsFragment);
}
