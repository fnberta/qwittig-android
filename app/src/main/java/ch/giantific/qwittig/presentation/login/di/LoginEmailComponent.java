/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.login.di;

import ch.giantific.qwittig.di.RepositoriesModule;
import ch.giantific.qwittig.di.scopes.PerFragment;
import ch.giantific.qwittig.presentation.login.LoginEmailFragment;
import dagger.Component;

/**
 * Provides the dependencies for the login/sign-up with email screen.
 */
@PerFragment
@Component(modules = {LoginEmailViewModelModule.class, RepositoriesModule.class})
public interface LoginEmailComponent {
    void inject(LoginEmailFragment loginEmailFragment);
}
