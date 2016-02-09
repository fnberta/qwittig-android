/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.di.components;

import ch.giantific.qwittig.di.modules.LoginEmailViewModelModule;
import ch.giantific.qwittig.di.modules.RepositoriesModule;
import ch.giantific.qwittig.di.scopes.PerActivity;
import ch.giantific.qwittig.di.scopes.PerFragment;
import ch.giantific.qwittig.presentation.login.LoginEmailFragment;
import dagger.Component;

/**
 * Created by fabio on 05.02.16.
 */
@PerFragment
@Component(modules = {LoginEmailViewModelModule.class, RepositoriesModule.class})
public interface LoginEmailComponent {
    void inject(LoginEmailFragment loginEmailFragment);
}
