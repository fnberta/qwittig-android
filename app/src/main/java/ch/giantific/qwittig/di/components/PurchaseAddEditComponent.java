/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.di.components;

import ch.giantific.qwittig.di.modules.PurchaseAddAutoViewModelModule;
import ch.giantific.qwittig.di.modules.PurchaseAddViewModelModule;
import ch.giantific.qwittig.di.modules.PurchaseEditViewModelModule;
import ch.giantific.qwittig.di.modules.RepositoriesModule;
import ch.giantific.qwittig.di.scopes.PerFragment;
import ch.giantific.qwittig.presentation.ui.fragments.PurchaseAddFragment;
import ch.giantific.qwittig.presentation.ui.fragments.PurchaseEditDraftFragment;
import ch.giantific.qwittig.presentation.ui.fragments.PurchaseEditFragment;
import dagger.Component;

/**
 * Created by fabio on 12.01.16.
 */
@PerFragment
@Component(modules = {PurchaseAddViewModelModule.class, PurchaseAddAutoViewModelModule.class,
        PurchaseEditViewModelModule.class, RepositoriesModule.class})
public interface PurchaseAddEditComponent {

    void inject(PurchaseAddFragment purchaseAddFragment);

    void inject(PurchaseEditFragment purchaseEditFragment);

    void inject(PurchaseEditDraftFragment purchaseEditDraftFragment);
}
