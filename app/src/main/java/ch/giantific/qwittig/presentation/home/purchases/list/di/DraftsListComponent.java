/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home.purchases.list.di;

import ch.giantific.qwittig.di.ApplicationComponent;
import ch.giantific.qwittig.di.RepositoriesModule;
import ch.giantific.qwittig.di.scopes.PerScreen;
import ch.giantific.qwittig.presentation.home.purchases.list.DraftsFragment;
import dagger.Component;

/**
 * Provides the dependencies for the list of drafts screen.
 */
@PerScreen
@Component(dependencies = {ApplicationComponent.class},
        modules = {DraftsListViewModelModule.class, RepositoriesModule.class})
public interface DraftsListComponent {

    void inject(DraftsFragment draftsFragment);
}
