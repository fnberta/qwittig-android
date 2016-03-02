/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.users.di;

import ch.giantific.qwittig.di.RepositoriesModule;
import ch.giantific.qwittig.di.scopes.PerFragment;
import ch.giantific.qwittig.presentation.settings.users.SettingsUsersFragment;
import dagger.Component;

/**
 * Provides the dependencies for the manage users settings screen.
 */
@PerFragment
@Component(modules = {SettingsUsersViewModelModule.class, RepositoriesModule.class})
public interface SettingsUsersComponent {

    void inject(SettingsUsersFragment settingsUsersFragment);
}
