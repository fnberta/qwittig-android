/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.finance.identities.di;

import ch.giantific.qwittig.di.RepositoriesModule;
import ch.giantific.qwittig.di.scopes.PerScreen;
import ch.giantific.qwittig.presentation.finance.identities.IdentitiesFragment;
import dagger.Component;

/**
 * Provides the dependencies for the identities list screen.
 */
@PerScreen
@Component(modules = {FinanceIdentitiesViewModelModule.class, RepositoriesModule.class})
public interface FinanceIdentitiesComponent {

    void inject(IdentitiesFragment identitiesFragment);
}
