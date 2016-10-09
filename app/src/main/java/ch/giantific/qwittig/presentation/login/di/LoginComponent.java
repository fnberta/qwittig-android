/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.login.di;

import ch.giantific.qwittig.di.ApplicationComponent;
import ch.giantific.qwittig.di.scopes.PerActivity;
import ch.giantific.qwittig.presentation.common.di.GoogleApiClientDelegateModule;
import ch.giantific.qwittig.presentation.common.di.NavigatorModule;
import ch.giantific.qwittig.presentation.common.di.PersistentViewModelsModule;
import ch.giantific.qwittig.presentation.common.di.SimplePresentersModule;
import ch.giantific.qwittig.presentation.login.LoginActivity;
import ch.giantific.qwittig.presentation.login.accounts.LoginAccountsFragment;
import ch.giantific.qwittig.presentation.login.email.LoginEmailFragment;
import ch.giantific.qwittig.presentation.login.firstgroup.LoginFirstGroupFragment;
import ch.giantific.qwittig.presentation.login.invitation.LoginInvitationFragment;
import ch.giantific.qwittig.presentation.login.profile.LoginProfileFragment;
import dagger.Component;

/**
 * Provides the dependencies for the login accounts screen.
 */
@PerActivity
@Component(dependencies = {ApplicationComponent.class},
        modules = {SimplePresentersModule.class, PersistentViewModelsModule.class,
                NavigatorModule.class, GoogleApiClientDelegateModule.class})
public interface LoginComponent {

    void inject(LoginActivity loginActivity);

    void inject(LoginAccountsFragment loginAccountsFragment);

    void inject(LoginEmailFragment loginEmailFragment);

    void inject(LoginInvitationFragment loginInvitationFragment);

    void inject(LoginProfileFragment loginProfileFragment);

    void inject(LoginFirstGroupFragment loginFirstGroupFragment);
}
