/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.finance.di;

import ch.giantific.qwittig.di.RepositoriesModule;
import ch.giantific.qwittig.di.scopes.PerFragment;
import ch.giantific.qwittig.presentation.finance.identities.IdentitiesFragment;
import dagger.Component;

/**
 * Created by fabio on 12.01.16.
 */
@PerFragment
@Component(modules = {FinanceUsersViewModelModule.class, RepositoriesModule.class})
public interface FinanceIdentitiesComponent {

    void inject(IdentitiesFragment identitiesFragment);
}
