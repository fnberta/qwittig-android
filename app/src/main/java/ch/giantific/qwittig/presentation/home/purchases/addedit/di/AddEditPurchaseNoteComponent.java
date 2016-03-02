/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home.purchases.addedit.di;

import ch.giantific.qwittig.di.RepositoriesModule;
import ch.giantific.qwittig.di.scopes.PerFragment;
import ch.giantific.qwittig.presentation.home.purchases.addedit.AddEditPurchaseNoteFragment;
import dagger.Component;

/**
 * Provides the dependencies for the purchase note screen.
 */
@PerFragment
@Component(modules = {AddEditPurchaseNoteViewModelModule.class, RepositoriesModule.class})
public interface AddEditPurchaseNoteComponent {

    void inject(AddEditPurchaseNoteFragment addEditPurchaseNoteFragment);
}
