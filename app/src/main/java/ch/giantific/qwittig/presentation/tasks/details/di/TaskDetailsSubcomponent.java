/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.tasks.details.di;

import ch.giantific.qwittig.di.scopes.PerActivity;
import ch.giantific.qwittig.presentation.tasks.details.TaskDetailsActivity;
import ch.giantific.qwittig.presentation.tasks.details.TaskDetailsFragment;
import dagger.Subcomponent;

/**
 * Provides the dependencies for the task details screen.
 */
@PerActivity
@Subcomponent(modules = {TaskDetailsViewModelModule.class})
public interface TaskDetailsSubcomponent {

    void inject(TaskDetailsActivity taskDetailsActivity);

    void inject(TaskDetailsFragment taskDetailsFragment);
}
