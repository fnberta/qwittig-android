/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.di.components;

import ch.giantific.qwittig.di.modules.PurchaseAddAutoViewModelModule;
import ch.giantific.qwittig.di.modules.PurchaseAddViewModelModule;
import ch.giantific.qwittig.di.modules.RepositoriesModule;
import ch.giantific.qwittig.di.scopes.PerFragment;
import ch.giantific.qwittig.presentation.ui.fragments.PurchaseAddFragment;
import dagger.Component;

/**
 * Created by fabio on 12.01.16.
 */
@PerFragment
@Component(dependencies = {ApplicationComponent.class},
        modules = {PurchaseAddViewModelModule.class, RepositoriesModule.class})
public interface PurchaseAddComponent {

    void inject(PurchaseAddFragment purchaseAddFragment);
}
