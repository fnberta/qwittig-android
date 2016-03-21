/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.tasks.addedit.di;

import ch.giantific.qwittig.di.RepositoriesModule;
import ch.giantific.qwittig.di.scopes.PerScreen;
import ch.giantific.qwittig.presentation.tasks.addedit.TaskAddEditFragment;
import dagger.Component;

/**
 * Provides the dependencies for the add task screen.
 */
@PerScreen
@Component(modules = {TaskAddViewModelModule.class, RepositoriesModule.class})
public interface TaskAddComponent {

    void inject(TaskAddEditFragment taskAddEditFragment);
}
