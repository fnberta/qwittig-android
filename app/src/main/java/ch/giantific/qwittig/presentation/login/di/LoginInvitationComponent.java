/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.login.di;

import ch.giantific.qwittig.di.ApplicationComponent;
import ch.giantific.qwittig.di.RepositoriesModule;
import ch.giantific.qwittig.di.scopes.PerScreen;
import ch.giantific.qwittig.presentation.login.LoginFirstGroupFragment;
import ch.giantific.qwittig.presentation.login.LoginInvitationFragment;
import dagger.Component;

/**
 * Provides the dependencies for the login accounts screen.
 */
@PerScreen
@Component(dependencies = {ApplicationComponent.class},
        modules = {LoginInvitationViewModelModule.class, RepositoriesModule.class})
public interface LoginInvitationComponent {
    void inject(LoginInvitationFragment loginInvitationFragment);
}
