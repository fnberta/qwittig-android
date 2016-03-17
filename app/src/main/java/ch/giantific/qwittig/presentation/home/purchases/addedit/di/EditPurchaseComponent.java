/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home.purchases.addedit.di;

import ch.giantific.qwittig.di.ApplicationComponent;
import ch.giantific.qwittig.di.RepositoriesModule;
import ch.giantific.qwittig.di.scopes.PerScreen;
import ch.giantific.qwittig.presentation.home.purchases.addedit.EditPurchaseDraftFragment;
import ch.giantific.qwittig.presentation.home.purchases.addedit.EditPurchaseFragment;
import dagger.Component;

/**
 * Provides the dependencies for the edit purchase screen.
 */
@PerScreen
@Component(dependencies = {ApplicationComponent.class},
        modules = {EditPurchaseViewModelModule.class, RepositoriesModule.class})
public interface EditPurchaseComponent {

    void inject(EditPurchaseFragment purchaseEditFragment);

    void inject(EditPurchaseDraftFragment editPurchaseDraftFragment);
}
