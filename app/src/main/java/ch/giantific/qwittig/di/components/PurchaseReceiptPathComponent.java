/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.di.components;

import ch.giantific.qwittig.di.modules.PurchaseReceiptPathViewModelModule;
import ch.giantific.qwittig.di.modules.RepositoriesModule;
import ch.giantific.qwittig.di.scopes.PerFragment;
import ch.giantific.qwittig.presentation.home.purchases.addedit.PurchaseReceiptAddEditFragment;
import dagger.Component;

/**
 * Created by fabio on 12.01.16.
 */
@PerFragment
@Component(dependencies = {ApplicationComponent.class},
        modules = {PurchaseReceiptPathViewModelModule.class, RepositoriesModule.class})
public interface PurchaseReceiptPathComponent {

    void inject(PurchaseReceiptAddEditFragment purchaseReceiptAddEditFragment);
}
