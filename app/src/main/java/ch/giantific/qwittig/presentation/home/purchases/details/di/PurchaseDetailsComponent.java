/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home.purchases.details.di;

import ch.giantific.qwittig.di.ApplicationComponent;
import ch.giantific.qwittig.di.RepositoriesModule;
import ch.giantific.qwittig.di.scopes.PerFragment;
import ch.giantific.qwittig.presentation.home.purchases.details.PurchaseDetailsFragment;
import dagger.Component;

/**
 * Provides the dependencies for the purchase details screen.
 */
@PerFragment
@Component(dependencies = {ApplicationComponent.class},
        modules = {PurchaseDetailsViewModelModule.class, RepositoriesModule.class})
public interface PurchaseDetailsComponent {

    void inject(PurchaseDetailsFragment purchaseDetailsFragment);
}
