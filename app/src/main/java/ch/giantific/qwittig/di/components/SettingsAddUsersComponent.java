/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.di.components;

import ch.giantific.qwittig.di.modules.RepositoriesModule;
import ch.giantific.qwittig.di.modules.SettingsAddUsersViewModelModule;
import ch.giantific.qwittig.di.scopes.PerFragment;
import ch.giantific.qwittig.presentation.settings.addusers.SettingsAddUsersFragment;
import dagger.Component;

/**
 * Created by fabio on 12.01.16.
 */
@PerFragment
@Component(modules = {SettingsAddUsersViewModelModule.class, RepositoriesModule.class})
public interface SettingsAddUsersComponent {

    void inject(SettingsAddUsersFragment settingsAddUsersFragment);
}
