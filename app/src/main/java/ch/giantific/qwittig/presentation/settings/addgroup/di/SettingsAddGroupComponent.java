/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.addgroup.di;

import ch.giantific.qwittig.di.RepositoriesModule;
import ch.giantific.qwittig.di.scopes.PerScreen;
import ch.giantific.qwittig.presentation.settings.addgroup.SettingsAddGroupFragment;
import dagger.Component;

/**
 * Provides the dependencies for the add new group settings screen.
 */
@PerScreen
@Component(modules = {SettingsAddGroupViewModelModule.class, RepositoriesModule.class})
public interface SettingsAddGroupComponent {

    void inject(SettingsAddGroupFragment settingsAddGroupFragment);
}
