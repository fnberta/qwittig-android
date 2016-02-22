/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home.purchases.common.di;

import ch.giantific.qwittig.di.ApplicationComponent;
import ch.giantific.qwittig.di.RepositoriesModule;
import ch.giantific.qwittig.di.scopes.PerFragment;
import ch.giantific.qwittig.presentation.home.purchases.addedit.PurchaseReceiptAddEditFragment;
import ch.giantific.qwittig.presentation.home.purchases.details.PurchaseReceiptDetailFragment;
import dagger.Component;

/**
 * Provides the dependencies for the purchase receipt screen.
 */
@PerFragment
@Component(dependencies = {ApplicationComponent.class},
        modules = {PurchaseReceiptIdViewModelModule.class, RepositoriesModule.class})
public interface PurchaseReceiptIdComponent {

    void inject(PurchaseReceiptAddEditFragment purchaseReceiptAddEditFragment);

    void inject(PurchaseReceiptDetailFragment purchaseReceiptDetailFragment);
}
