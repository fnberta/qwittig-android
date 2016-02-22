/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home.purchases.list.di;

import ch.giantific.qwittig.di.ApplicationComponent;
import ch.giantific.qwittig.di.RepositoriesModule;
import ch.giantific.qwittig.di.scopes.PerFragment;
import ch.giantific.qwittig.presentation.home.purchases.list.PurchasesFragment;
import ch.giantific.qwittig.data.loaders.PurchasesLoader;
import dagger.Component;

/**
 * Created by fabio on 12.01.16.
 */
@PerFragment
@Component(dependencies = {ApplicationComponent.class},
        modules = {PurchasesListViewModelModule.class, RepositoriesModule.class})
public interface PurchasesListComponent {

    void inject(PurchasesFragment purchasesFragment);

    PurchasesLoader getPurchasesLoader();
}
