/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.general.di;

import ch.giantific.qwittig.di.ApplicationComponent;
import ch.giantific.qwittig.di.scopes.PerActivity;
import ch.giantific.qwittig.presentation.common.di.GoogleApiClientDelegateModule;
import ch.giantific.qwittig.presentation.common.di.NavigatorModule;
import ch.giantific.qwittig.presentation.common.di.PersistentViewModelsModule;
import ch.giantific.qwittig.presentation.common.di.SimplePresentersModule;
import ch.giantific.qwittig.presentation.settings.general.SettingsActivity;
import ch.giantific.qwittig.presentation.settings.general.SettingsFragment;
import dagger.Component;

/**
 * Provides the dependencies for the main settings screen.
 */
@PerActivity
@Component(dependencies = {ApplicationComponent.class},
        modules = {SimplePresentersModule.class, PersistentViewModelsModule.class,
                NavigatorModule.class, GoogleApiClientDelegateModule.class})
public interface SettingsComponent {

    void inject(SettingsActivity settingsActivity);

    void inject(SettingsFragment settingsFragment);
}
