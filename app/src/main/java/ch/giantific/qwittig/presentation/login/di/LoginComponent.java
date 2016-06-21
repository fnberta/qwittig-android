/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.login.di;

import ch.giantific.qwittig.di.ApplicationComponent;
import ch.giantific.qwittig.di.RepositoriesModule;
import ch.giantific.qwittig.di.scopes.PerActivity;
import ch.giantific.qwittig.presentation.common.di.NavigatorModule;
import ch.giantific.qwittig.presentation.login.LoginAccountsFragment;
import ch.giantific.qwittig.presentation.login.LoginAccountsViewModel;
import ch.giantific.qwittig.presentation.login.LoginActivity;
import ch.giantific.qwittig.presentation.login.LoginEmailFragment;
import ch.giantific.qwittig.presentation.login.LoginEmailViewModel;
import ch.giantific.qwittig.presentation.login.LoginFirstGroupFragment;
import ch.giantific.qwittig.presentation.login.LoginFirstGroupViewModel;
import ch.giantific.qwittig.presentation.login.LoginInvitationFragment;
import ch.giantific.qwittig.presentation.login.LoginInvitationViewModel;
import ch.giantific.qwittig.presentation.login.LoginProfileFragment;
import ch.giantific.qwittig.presentation.login.LoginProfileViewModel;
import dagger.Component;

/**
 * Provides the dependencies for the login accounts screen.
 */
@PerActivity
@Component(dependencies = {ApplicationComponent.class},
        modules = {NavigatorModule.class, RepositoriesModule.class, LoginAccountsViewModelModule.class,
                LoginEmailViewModelModule.class, LoginInvitationViewModelModule.class,
                LoginProfileViewModelModule.class, LoginFirstGroupViewModelModule.class})
public interface LoginComponent {

    void inject(LoginActivity loginActivity);

    void inject(LoginAccountsFragment loginAccountsFragment);

    void inject(LoginEmailFragment loginEmailFragment);

    void inject(LoginInvitationFragment loginInvitationFragment);

    void inject(LoginProfileFragment loginProfileFragment);

    void inject(LoginFirstGroupFragment loginFirstGroupFragment);

    LoginAccountsViewModel getLoginAccountsViewModel();

    LoginEmailViewModel getLoginEmailViewModel();

    LoginInvitationViewModel getLoginInvitationViewModel();

    LoginProfileViewModel getLoginProfileViewModel();

    LoginFirstGroupViewModel getLoginFirstGroupViewModel();
}
