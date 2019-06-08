/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.groupusers.di;

import ch.giantific.qwittig.di.ApplicationComponent;
import ch.giantific.qwittig.di.scopes.PerActivity;
import ch.giantific.qwittig.presentation.common.di.NavigatorModule;
import ch.giantific.qwittig.presentation.common.di.PersistentViewModelsModule;
import ch.giantific.qwittig.presentation.common.di.SimplePresentersModule;
import ch.giantific.qwittig.presentation.settings.groupusers.addgroup.SettingsAddGroupActivity;
import ch.giantific.qwittig.presentation.settings.groupusers.addgroup.SettingsAddGroupContract;
import ch.giantific.qwittig.presentation.settings.groupusers.addgroup.SettingsAddGroupFragment;
import ch.giantific.qwittig.presentation.settings.groupusers.users.SettingsUsersActivity;
import ch.giantific.qwittig.presentation.settings.groupusers.users.SettingsUsersContract;
import ch.giantific.qwittig.presentation.settings.groupusers.users.SettingsUsersFragment;
import dagger.Component;

/**
 * Provides the dependencies for the addItemAtPosition new group settings screen.
 */
@PerActivity
@Component(dependencies = {ApplicationComponent.class},
        modules = {SimplePresentersModule.class, PersistentViewModelsModule.class,
                NavigatorModule.class})
public interface SettingsGroupUsersComponent {

    void inject(SettingsAddGroupActivity settingsAddGroupActivity);

    void inject(SettingsAddGroupFragment settingsAddGroupFragment);

    void inject(SettingsUsersActivity settingsUsersActivity);

    void inject(SettingsUsersFragment settingsUsersFragment);

    SettingsAddGroupContract.Presenter getAddGroupPresenter();

    SettingsUsersContract.Presenter getUsersPresenter();
}
