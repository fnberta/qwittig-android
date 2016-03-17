/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.tasks.details.di;

import ch.giantific.qwittig.di.RepositoriesModule;
import ch.giantific.qwittig.di.scopes.PerScreen;
import ch.giantific.qwittig.presentation.tasks.details.TaskDetailsFragment;
import dagger.Component;

/**
 * Created by fabio on 12.01.16.
 */
@PerScreen
@Component(modules = {TaskDetailsViewModelModule.class, RepositoriesModule.class})
public interface TaskDetailsComponent {

    void inject(TaskDetailsFragment taskDetailsFragment);
}
