/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.assignments.list.di;

import ch.giantific.qwittig.di.scopes.PerActivity;
import ch.giantific.qwittig.presentation.assignments.list.AssignmentsActivity;
import ch.giantific.qwittig.presentation.assignments.list.AssignmentsFragment;
import ch.giantific.qwittig.presentation.common.di.PersistentViewModelsModule;
import ch.giantific.qwittig.presentation.common.di.SimplePresentersModule;
import dagger.Subcomponent;

/**
 * Provides the dependencies for the task list screen.
 */
@PerActivity
@Subcomponent(modules = {SimplePresentersModule.class, PersistentViewModelsModule.class})
public interface AssignmentsSubcomponent {

    void inject(AssignmentsActivity assignmentsActivity);

    void inject(AssignmentsFragment assignmentsFragment);
}
