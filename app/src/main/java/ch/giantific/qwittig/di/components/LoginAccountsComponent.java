/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.di.components;

import ch.giantific.qwittig.di.modules.LoginAccountsViewModelModule;
import ch.giantific.qwittig.di.modules.RepositoriesModule;
import ch.giantific.qwittig.di.scopes.PerActivity;
import ch.giantific.qwittig.di.scopes.PerFragment;
import ch.giantific.qwittig.presentation.login.LoginAccountsFragment;
import dagger.Component;

/**
 * Created by fabio on 05.02.16.
 */
@PerFragment
@Component(modules = {LoginAccountsViewModelModule.class, RepositoriesModule.class})
public interface LoginAccountsComponent {
    void inject(LoginAccountsFragment loginAccountsFragment);
}
