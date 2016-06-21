/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.addedit.di;

import ch.giantific.qwittig.di.ApplicationComponent;
import ch.giantific.qwittig.di.RepositoriesModule;
import ch.giantific.qwittig.di.scopes.PerActivity;
import ch.giantific.qwittig.presentation.common.di.NavigatorModule;
import ch.giantific.qwittig.presentation.purchases.addedit.PurchaseAddActivity;
import ch.giantific.qwittig.presentation.purchases.addedit.BasePurchaseAddEditNoteFragment;
import ch.giantific.qwittig.presentation.purchases.addedit.PurchaseAddEditViewModel;
import ch.giantific.qwittig.presentation.purchases.addedit.PurchaseAddFragment;
import ch.giantific.qwittig.presentation.purchases.addedit.PurchaseAddNoteFragment;
import ch.giantific.qwittig.presentation.purchases.addedit.PurchaseAddOcrViewModel;
import ch.giantific.qwittig.presentation.purchases.addedit.PurchaseAddReceiptFragment;
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

    void inject(PurchaseAddReceiptFragment purchaseAddReceiptFragment);

    void inject(PurchaseAddNoteFragment purchaseAddNoteFragment);

    PurchaseAddEditViewModel getAddViewModel();

    PurchaseAddOcrViewModel getAddOcrViewModel();
}
