/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.di.components;

import ch.giantific.qwittig.di.modules.PurchaseReceiptIdViewModelModule;
import ch.giantific.qwittig.di.modules.PurchaseReceiptPathViewModelModule;
import ch.giantific.qwittig.di.modules.RepositoriesModule;
import ch.giantific.qwittig.di.scopes.PerFragment;
import ch.giantific.qwittig.presentation.ui.fragments.PurchaseReceiptAddEditFragment;
import ch.giantific.qwittig.presentation.ui.fragments.PurchaseReceiptDetailFragment;
import dagger.Component;

/**
 * Created by fabio on 12.01.16.
 */
@PerFragment
@Component(dependencies = {ApplicationComponent.class},
        modules = {PurchaseReceiptIdViewModelModule.class, RepositoriesModule.class})
public interface PurchaseReceiptIdComponent {

    void inject(PurchaseReceiptAddEditFragment purchaseReceiptAddEditFragment);

    void inject(PurchaseReceiptDetailFragment purchaseReceiptDetailFragment);
}
