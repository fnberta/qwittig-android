/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.di.components;

import ch.giantific.qwittig.di.modules.HomePurchasesViewModelModule;
import ch.giantific.qwittig.di.modules.RepositoriesModule;
import ch.giantific.qwittig.di.scopes.PerFragment;
import ch.giantific.qwittig.presentation.home.purchases.list.PurchasesFragment;
import ch.giantific.qwittig.data.loaders.PurchasesLoader;
import dagger.Component;

/**
 * Created by fabio on 12.01.16.
 */
@PerFragment
@Component(dependencies = {ApplicationComponent.class},
        modules = {HomePurchasesViewModelModule.class, RepositoriesModule.class})
public interface PurchasesListComponent {

    void inject(PurchasesFragment purchasesFragment);

    PurchasesLoader getPurchasesLoader();
}
