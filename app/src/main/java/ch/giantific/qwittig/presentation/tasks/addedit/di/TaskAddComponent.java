/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.tasks.addedit.di;

import ch.giantific.qwittig.di.ApplicationComponent;
import ch.giantific.qwittig.di.RepositoriesModule;
import ch.giantific.qwittig.di.scopes.PerActivity;
import ch.giantific.qwittig.presentation.common.di.NavigatorModule;
import ch.giantific.qwittig.presentation.tasks.addedit.TaskAddActivity;
import ch.giantific.qwittig.presentation.tasks.addedit.TaskAddFragment;
import dagger.Component;

/**
 * Provides the dependencies for the add task screen.
 */
@PerActivity
@Component(dependencies = {ApplicationComponent.class},
        modules = {TaskAddViewModelModule.class, RepositoriesModule.class, NavigatorModule.class})
public interface TaskAddComponent {

    void inject(TaskAddActivity taskAddActivity);

    void inject(TaskAddFragment taskAddFragment);
}
