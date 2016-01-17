/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.di.components;

import ch.giantific.qwittig.di.modules.RepositoriesModule;
import ch.giantific.qwittig.di.modules.TasksListViewModelModule;
import ch.giantific.qwittig.di.scopes.PerFragment;
import ch.giantific.qwittig.presentation.ui.fragments.TasksFragment;
import ch.giantific.qwittig.presentation.viewmodels.TasksViewModel;
import dagger.Component;

/**
 * Created by fabio on 12.01.16.
 */
@PerFragment
@Component(modules = {TasksListViewModelModule.class, RepositoriesModule.class})
public interface TasksListComponent {

    void inject(TasksFragment tasksFragment);

    TasksViewModel getTasksListViewModel();
}
