/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.tasks.list.di;

import ch.giantific.qwittig.di.scopes.PerActivity;
import ch.giantific.qwittig.presentation.tasks.list.TasksActivity;
import ch.giantific.qwittig.presentation.tasks.list.TasksFragment;
import dagger.Subcomponent;

/**
 * Provides the dependencies for the task list screen.
 */
@PerActivity
@Subcomponent(modules = {TasksListViewModelModule.class})
public interface TasksListSubcomponent {

    void inject(TasksActivity tasksActivity);

    void inject(TasksFragment tasksFragment);
}
