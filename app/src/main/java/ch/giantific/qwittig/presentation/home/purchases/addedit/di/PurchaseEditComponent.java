/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home.purchases.addedit.di;

import ch.giantific.qwittig.di.ApplicationComponent;
import ch.giantific.qwittig.di.RepositoriesModule;
import ch.giantific.qwittig.di.scopes.PerFragment;
import ch.giantific.qwittig.presentation.home.purchases.addedit.PurchaseEditDraftFragment;
import ch.giantific.qwittig.presentation.home.purchases.addedit.PurchaseEditFragment;
import dagger.Component;

/**
 * Provides the dependencies for the edit purchase screen.
 */
@PerFragment
@Component(dependencies = {ApplicationComponent.class},
        modules = {PurchaseEditViewModelModule.class, RepositoriesModule.class})
public interface PurchaseEditComponent {

    void inject(PurchaseEditFragment purchaseEditFragment);

    void inject(PurchaseEditDraftFragment purchaseEditDraftFragment);
}
