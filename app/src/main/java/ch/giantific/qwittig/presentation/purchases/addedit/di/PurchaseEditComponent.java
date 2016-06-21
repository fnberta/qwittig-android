/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.addedit.di;

import ch.giantific.qwittig.di.ApplicationComponent;
import ch.giantific.qwittig.di.RepositoriesModule;
import ch.giantific.qwittig.di.scopes.PerActivity;
import ch.giantific.qwittig.presentation.common.di.NavigatorModule;
import ch.giantific.qwittig.presentation.purchases.addedit.PurchaseAddEditViewModel;
import ch.giantific.qwittig.presentation.purchases.addedit.PurchaseEditActivity;
import ch.giantific.qwittig.presentation.purchases.addedit.PurchaseEditDraftFragment;
import ch.giantific.qwittig.presentation.purchases.addedit.PurchaseEditDraftViewModel;
import ch.giantific.qwittig.presentation.purchases.addedit.PurchaseEditFragment;
import ch.giantific.qwittig.presentation.purchases.addedit.PurchaseEditNoteFragment;
import ch.giantific.qwittig.presentation.purchases.addedit.PurchaseEditReceiptFragment;
import dagger.Component;

/**
 * Provides the dependencies for the edit purchase screen.
 */
@PerActivity
@Component(dependencies = {ApplicationComponent.class},
        modules = {PurchaseEditViewModelModule.class, RepositoriesModule.class, NavigatorModule.class})
public interface PurchaseEditComponent {

    void inject(PurchaseEditActivity purchaseEditActivity);

    void inject(PurchaseEditFragment purchaseEditFragment);

    void inject(PurchaseEditDraftFragment purchaseEditDraftFragment);

    void inject(PurchaseEditReceiptFragment purchaseEditReceiptFragment);

    void inject(PurchaseEditNoteFragment purchaseEditNoteFragment);

    PurchaseAddEditViewModel getEditViewModel();

    PurchaseEditDraftViewModel getEditDraftViewModel();
}
