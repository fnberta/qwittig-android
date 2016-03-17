/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.login.di;

import ch.giantific.qwittig.di.RepositoriesModule;
import ch.giantific.qwittig.di.scopes.PerScreen;
import ch.giantific.qwittig.presentation.login.LoginAccountsFragment;
import dagger.Component;

/**
 * Provides the dependencies for the login accounts screen.
 */
@PerScreen
@Component(modules = {LoginAccountsViewModelModule.class, RepositoriesModule.class})
public interface LoginAccountsComponent {
    void inject(LoginAccountsFragment loginAccountsFragment);
}
