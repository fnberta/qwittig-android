/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home.purchases.addedit.di;

import ch.giantific.qwittig.di.ApplicationComponent;
import ch.giantific.qwittig.di.RepositoriesModule;
import ch.giantific.qwittig.di.scopes.PerScreen;
import ch.giantific.qwittig.presentation.home.purchases.addedit.AddPurchaseFragment;
import dagger.Component;

/**
 * Provides the dependencies for the purchase add screen.
 */
@PerScreen
@Component(dependencies = {ApplicationComponent.class},
        modules = {AddPurchaseViewModelModule.class, RepositoriesModule.class})
public interface AddPurchaseComponent {

    void inject(AddPurchaseFragment addPurchaseFragment);
}
