/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.tasks.addedit.di;

import ch.giantific.qwittig.di.ApplicationComponent;
import ch.giantific.qwittig.di.RepositoriesModule;
import ch.giantific.qwittig.di.scopes.PerScreen;
import ch.giantific.qwittig.presentation.tasks.addedit.TaskAddEditFragment;
import dagger.Component;

/**
 * Provides the dependencies for the edit task screen.
 */
@PerScreen
@Component(dependencies = {ApplicationComponent.class},
        modules = {TaskEditViewModelModule.class, RepositoriesModule.class})
public interface TaskEditComponent {

    void inject(TaskAddEditFragment taskAddEditFragment);
}
