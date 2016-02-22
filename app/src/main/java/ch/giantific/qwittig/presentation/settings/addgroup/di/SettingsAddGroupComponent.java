/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.addgroup.di;

import ch.giantific.qwittig.di.RepositoriesModule;
import ch.giantific.qwittig.di.scopes.PerFragment;
import ch.giantific.qwittig.presentation.settings.addgroup.SettingsAddGroupFragment;
import dagger.Component;

/**
 * Created by fabio on 12.01.16.
 */
@PerFragment
@Component(modules = {SettingsAddGroupViewModelModule.class, RepositoriesModule.class})
public interface SettingsAddGroupComponent {

    void inject(SettingsAddGroupFragment settingsAddGroupFragment);
}
