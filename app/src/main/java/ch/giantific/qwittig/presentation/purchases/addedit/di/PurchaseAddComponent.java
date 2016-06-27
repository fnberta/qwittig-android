/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.addedit.di;

import ch.giantific.qwittig.di.ApplicationComponent;
import ch.giantific.qwittig.di.RepositoriesModule;
import ch.giantific.qwittig.di.scopes.PerActivity;
import ch.giantific.qwittig.presentation.common.di.NavigatorModule;
import ch.giantific.qwittig.presentation.purchases.addedit.PurchaseAddEditViewModel;
import ch.giantific.qwittig.presentation.purchases.addedit.add.PurchaseAddActivity;
import ch.giantific.qwittig.presentation.purchases.addedit.add.PurchaseAddFragment;
import ch.giantific.qwittig.presentation.purchases.addedit.add.PurchaseAddOcrFragment;
import ch.giantific.qwittig.presentation.purchases.addedit.add.PurchaseAddOcrReceiptFragment;
import ch.giantific.qwittig.presentation.purchases.addedit.add.PurchaseAddOcrViewModel;
import ch.giantific.qwittig.presentation.purchases.addedit.add.PurchaseAddReceiptFragment;
import dagger.Component;

/**
 * Provides the dependencies for the purchase add screen.
 */
@PerActivity
@Component(dependencies = {ApplicationComponent.class},
        modules = {PurchaseAddViewModelModule.class, RepositoriesModule.class, NavigatorModule.class})
public interface PurchaseAddComponent {

    void inject(PurchaseAddActivity purchaseAddActivity);

    void inject(PurchaseAddFragment purchaseAddFragment);

    void inject(PurchaseAddOcrFragment purchaseAddOcrFragment);

    void inject(PurchaseAddReceiptFragment purchaseAddReceiptFragment);

    void inject(PurchaseAddOcrReceiptFragment purchaseAddOcrReceiptFragment);

    PurchaseAddEditViewModel getAddViewModel();

    PurchaseAddOcrViewModel getAddOcrViewModel();
}
