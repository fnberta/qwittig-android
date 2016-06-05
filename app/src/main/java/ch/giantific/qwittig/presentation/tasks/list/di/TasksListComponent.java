/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.tasks.list.di;

import ch.giantific.qwittig.di.ApplicationComponent;
import ch.giantific.qwittig.di.RepositoriesModule;
import ch.giantific.qwittig.di.scopes.PerScreen;
import ch.giantific.qwittig.presentation.tasks.list.TasksFragment;
import dagger.Component;

/**
 * Provides the dependencies for the task list screen.
 */
@PerScreen
@Component(dependencies = {ApplicationComponent.class},
        modules = {TasksListViewModelModule.class, RepositoriesModule.class})
public interface TasksListComponent {

    void inject(TasksFragment tasksFragment);
}
