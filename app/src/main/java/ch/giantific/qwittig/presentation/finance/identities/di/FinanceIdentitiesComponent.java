/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.finance.identities.di;

import ch.giantific.qwittig.di.RepositoriesModule;
import ch.giantific.qwittig.di.scopes.PerFragment;
import ch.giantific.qwittig.presentation.finance.identities.IdentitiesFragment;
import dagger.Component;

/**
 * Provides the dependencies for the identities list screen.
 */
@PerFragment
@Component(modules = {FinanceIdentitiesViewModelModule.class, RepositoriesModule.class})
public interface FinanceIdentitiesComponent {

    void inject(IdentitiesFragment identitiesFragment);
}
