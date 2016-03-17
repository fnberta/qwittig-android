/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.general.di;

import ch.giantific.qwittig.di.ApplicationComponent;
import ch.giantific.qwittig.di.RepositoriesModule;
import ch.giantific.qwittig.di.scopes.PerScreen;
import ch.giantific.qwittig.presentation.settings.general.SettingsFragment;
import dagger.Component;

/**
 * Provides the dependencies for the main settings screen.
 */
@PerScreen
@Component(dependencies = {ApplicationComponent.class},
        modules = {SettingsViewModelModule.class, RepositoriesModule.class})
public interface SettingsComponent {

    void inject(SettingsFragment settingsFragment);
}
