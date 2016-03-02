/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.profile.di;

import ch.giantific.qwittig.di.RepositoriesModule;
import ch.giantific.qwittig.di.scopes.PerFragment;
import ch.giantific.qwittig.presentation.settings.profile.SettingsProfileFragment;
import dagger.Component;

/**
 * Provides the dependencies for the profile settings screen.
 */
@PerFragment
@Component(modules = {SettingsProfileViewModelModule.class, RepositoriesModule.class})
public interface SettingsProfileComponent {
    void inject(SettingsProfileFragment settingsProfileFragment);
}
