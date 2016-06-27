/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.tasks.addedit.di;

import ch.giantific.qwittig.di.ApplicationComponent;
import ch.giantific.qwittig.di.RepositoriesModule;
import ch.giantific.qwittig.di.scopes.PerActivity;
import ch.giantific.qwittig.presentation.common.di.NavigatorModule;
import ch.giantific.qwittig.presentation.tasks.addedit.TaskEditActivity;
import ch.giantific.qwittig.presentation.tasks.addedit.TaskEditFragment;
import dagger.Component;

/**
 * Provides the dependencies for the edit task screen.
 */
@PerActivity
@Component(dependencies = {ApplicationComponent.class},
        modules = {TaskEditViewModelModule.class, RepositoriesModule.class, NavigatorModule.class})
public interface TaskEditComponent {

    void inject(TaskEditActivity taskEditActivity);

    void inject(TaskEditFragment taskEditFragment);
}
