/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home.purchases.addedit.di;

import ch.giantific.qwittig.di.ApplicationComponent;
import ch.giantific.qwittig.di.RepositoriesModule;
import ch.giantific.qwittig.di.scopes.PerFragment;
import ch.giantific.qwittig.presentation.home.purchases.addedit.PurchaseAddFragment;
import dagger.Component;

/**
 * Provides the dependencies for the purchase add screen.
 */
@PerFragment
@Component(dependencies = {ApplicationComponent.class},
        modules = {PurchaseAddViewModelModule.class, RepositoriesModule.class})
public interface PurchaseAddComponent {

    void inject(PurchaseAddFragment purchaseAddFragment);
}
