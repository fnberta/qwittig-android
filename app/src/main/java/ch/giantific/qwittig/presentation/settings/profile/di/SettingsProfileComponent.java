/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.profile.di;

import ch.giantific.qwittig.di.RepositoriesModule;
import ch.giantific.qwittig.di.scopes.PerFragment;
import ch.giantific.qwittig.presentation.settings.profile.SettingsProfileFragment;
import dagger.Component;

/**
 * Created by fabio on 12.01.16.
 */
@PerFragment
@Component(modules = {SettingsProfileViewModelModule.class, RepositoriesModule.class})
public interface SettingsProfileComponent {
    void inject(SettingsProfileFragment settingsProfileFragment);
}
