/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.users.di;

import ch.giantific.qwittig.di.RepositoriesModule;
import ch.giantific.qwittig.di.scopes.PerFragment;
import ch.giantific.qwittig.presentation.settings.users.SettingsUsersFragment;
import dagger.Component;

/**
 * Created by fabio on 12.01.16.
 */
@PerFragment
@Component(modules = {SettingsUsersViewModelModule.class, RepositoriesModule.class})
public interface SettingsUsersComponent {

    void inject(SettingsUsersFragment settingsUsersFragment);
}
