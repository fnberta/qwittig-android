/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.assignments.addedit.di;

import ch.giantific.qwittig.di.ApplicationComponent;
import ch.giantific.qwittig.di.RepositoriesModule;
import ch.giantific.qwittig.di.scopes.PerActivity;
import ch.giantific.qwittig.presentation.assignments.addedit.AssignmentAddFragment;
import ch.giantific.qwittig.presentation.common.di.NavigatorModule;
import ch.giantific.qwittig.presentation.assignments.addedit.AssignmentAddActivity;
import dagger.Component;

/**
 * Provides the dependencies for the add task screen.
 */
@PerActivity
@Component(dependencies = {ApplicationComponent.class},
        modules = {AssignmentAddViewModelModule.class, RepositoriesModule.class, NavigatorModule.class})
public interface AssignmentAddComponent {

    void inject(AssignmentAddActivity assignmentAddActivity);

    void inject(AssignmentAddFragment assignmentAddFragment);
}
