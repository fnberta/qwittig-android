/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.di.components;

import ch.giantific.qwittig.di.modules.RepositoriesModule;
import ch.giantific.qwittig.di.modules.TaskDetailsViewModelModule;
import ch.giantific.qwittig.di.scopes.PerFragment;
import ch.giantific.qwittig.presentation.tasks.details.TaskDetailsFragment;
import dagger.Component;

/**
 * Created by fabio on 12.01.16.
 */
@PerFragment
@Component(modules = {TaskDetailsViewModelModule.class, RepositoriesModule.class})
public interface TaskDetailsComponent {

    void inject(TaskDetailsFragment taskDetailsFragment);
}
