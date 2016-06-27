/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.profile.di;

import ch.giantific.qwittig.di.ApplicationComponent;
import ch.giantific.qwittig.di.RepositoriesModule;
import ch.giantific.qwittig.di.scopes.PerActivity;
import ch.giantific.qwittig.presentation.common.di.NavigatorModule;
import ch.giantific.qwittig.presentation.settings.profile.SettingsProfileActivity;
import ch.giantific.qwittig.presentation.settings.profile.SettingsProfileFragment;
import dagger.Component;

/**
 * Provides the dependencies for the profile settings screen.
 */
@PerActivity
@Component(dependencies = {ApplicationComponent.class},
        modules = {SettingsProfileViewModelModule.class, RepositoriesModule.class, NavigatorModule.class})
public interface SettingsProfileComponent {

    void inject(SettingsProfileActivity settingsProfileActivity);

    void inject(SettingsProfileFragment settingsProfileFragment);
}
