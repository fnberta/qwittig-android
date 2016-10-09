/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.assignments.details.di;

import ch.giantific.qwittig.di.scopes.PerActivity;
import ch.giantific.qwittig.presentation.assignments.details.AssignmentDetailsActivity;
import ch.giantific.qwittig.presentation.assignments.details.AssignmentDetailsFragment;
import ch.giantific.qwittig.presentation.common.di.PersistentViewModelsModule;
import dagger.Subcomponent;

/**
 * Provides the dependencies for the task details screen.
 */
@PerActivity
@Subcomponent(modules = {AssignmentDetailsPresenterModule.class, PersistentViewModelsModule.class})
public interface AssignmentDetailsSubcomponent {

    void inject(AssignmentDetailsActivity assignmentDetailsActivity);

    void inject(AssignmentDetailsFragment assignmentDetailsFragment);
}
