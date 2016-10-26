/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.assignments.addedit.di;

import ch.giantific.qwittig.di.ApplicationComponent;
import ch.giantific.qwittig.di.scopes.PerActivity;
import ch.giantific.qwittig.presentation.assignments.addedit.edit.AssignmentEditActivity;
import ch.giantific.qwittig.presentation.assignments.addedit.edit.AssignmentEditFragment;
import ch.giantific.qwittig.presentation.common.di.NavigatorModule;
import ch.giantific.qwittig.presentation.common.di.PersistentViewModelsModule;
import dagger.Component;

/**
 * Provides the dependencies for the edit task screen.
 */
@PerActivity
@Component(dependencies = {ApplicationComponent.class},
        modules = {AssignmentEditPresenterModule.class, PersistentViewModelsModule.class,
                NavigatorModule.class})
public interface AssignmentEditComponent {

    void inject(AssignmentEditActivity assignmentEditActivity);

    void inject(AssignmentEditFragment assignmentEditFragment);
}
