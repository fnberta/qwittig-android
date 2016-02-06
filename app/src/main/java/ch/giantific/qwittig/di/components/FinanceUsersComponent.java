/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.di.components;

import ch.giantific.qwittig.di.modules.FinanceUsersViewModelModule;
import ch.giantific.qwittig.di.modules.RepositoriesModule;
import ch.giantific.qwittig.di.scopes.PerFragment;
import ch.giantific.qwittig.presentation.finance.IdentitiesFragment;
import dagger.Component;

/**
 * Created by fabio on 12.01.16.
 */
@PerFragment
@Component(modules = {FinanceUsersViewModelModule.class, RepositoriesModule.class})
public interface FinanceUsersComponent {

    void inject(IdentitiesFragment identitiesFragment);
}
