/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.login.di;

import ch.giantific.qwittig.di.RepositoriesModule;
import ch.giantific.qwittig.di.scopes.PerScreen;
import ch.giantific.qwittig.presentation.login.LoginFirstGroupFragment;
import ch.giantific.qwittig.presentation.login.LoginProfileFragment;
import dagger.Component;

/**
 * Provides the dependencies for the login accounts screen.
 */
@PerScreen
@Component(modules = {LoginFirstGroupViewModelModule.class, RepositoriesModule.class})
public interface LoginFirstGroupComponent {
    void inject(LoginFirstGroupFragment loginFirstGroupFragment);
}
