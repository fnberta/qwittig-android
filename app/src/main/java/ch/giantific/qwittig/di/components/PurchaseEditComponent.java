/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.di.components;

import ch.giantific.qwittig.di.modules.PurchaseEditViewModelModule;
import ch.giantific.qwittig.di.modules.RepositoriesModule;
import ch.giantific.qwittig.di.scopes.PerFragment;
import ch.giantific.qwittig.presentation.home.purchases.addedit.PurchaseEditDraftFragment;
import ch.giantific.qwittig.presentation.home.purchases.addedit.PurchaseEditFragment;
import dagger.Component;

/**
 * Created by fabio on 12.01.16.
 */
@PerFragment
@Component(dependencies = {ApplicationComponent.class},
        modules = {PurchaseEditViewModelModule.class, RepositoriesModule.class})
public interface PurchaseEditComponent {

    void inject(PurchaseEditFragment purchaseEditFragment);

    void inject(PurchaseEditDraftFragment purchaseEditDraftFragment);
}
