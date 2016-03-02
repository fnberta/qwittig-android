/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.tasks.addedit.di;

import ch.giantific.qwittig.di.RepositoriesModule;
import ch.giantific.qwittig.di.scopes.PerFragment;
import ch.giantific.qwittig.presentation.tasks.addedit.TaskAddEditFragment;
import dagger.Component;

/**
 * Created by fabio on 12.01.16.
 */
@PerFragment
@Component(modules = {TaskAddViewModelModule.class, RepositoriesModule.class})
public interface TaskAddComponent {

    void inject(TaskAddEditFragment taskAddEditFragment);
}
