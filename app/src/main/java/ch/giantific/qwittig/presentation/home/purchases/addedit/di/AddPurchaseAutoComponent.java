/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home.purchases.addedit.di;

import ch.giantific.qwittig.di.ApplicationComponent;
import ch.giantific.qwittig.di.RepositoriesModule;
import ch.giantific.qwittig.di.scopes.PerFragment;
import ch.giantific.qwittig.presentation.home.purchases.addedit.AddPurchaseFragment;
import dagger.Component;

/**
 * Provides the dependencies for the purchase add auto screen.
 */
@PerFragment
@Component(dependencies = {ApplicationComponent.class},
        modules = {AddPurchaseAutoViewModelModule.class, RepositoriesModule.class})
public interface AddPurchaseAutoComponent {

    void inject(AddPurchaseFragment addPurchaseFragment);
}
