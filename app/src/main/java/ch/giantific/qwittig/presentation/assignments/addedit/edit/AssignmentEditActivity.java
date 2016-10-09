/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.assignments.addedit.edit;

import android.os.Bundle;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.Qwittig;
import ch.giantific.qwittig.presentation.assignments.addedit.BaseAssignmentAddEditActivity;
import ch.giantific.qwittig.presentation.assignments.addedit.BaseAssignmentAddEditFragment;
import ch.giantific.qwittig.presentation.assignments.addedit.add.AssignmentAddActivity;
import ch.giantific.qwittig.presentation.assignments.addedit.add.AssignmentAddFragment;
import ch.giantific.qwittig.presentation.assignments.addedit.di.AssignmentEditComponent;
import ch.giantific.qwittig.presentation.assignments.addedit.di.AssignmentEditPresenterModule;
import ch.giantific.qwittig.presentation.assignments.addedit.di.DaggerAssignmentEditComponent;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.di.NavigatorModule;
import ch.giantific.qwittig.presentation.common.di.PersistentViewModelsModule;

/**
 * Hosts {@link AssignmentAddFragment} that allows the user to edit a task.
 * <p/>
 * Almost identical to {@link AssignmentAddActivity}, but separate activity allows to set a different
 * transition in XML.
 * <p/>
 * Subclass of {@link AssignmentAddActivity}.
 */
public class AssignmentEditActivity extends BaseAssignmentAddEditActivity<AssignmentEditComponent> {

    @Override
    protected void injectDependencies(@Nullable Bundle savedInstanceState) {
        final String assignmentId = getIntent().getStringExtra(Navigator.EXTRA_ASSIGNMENT_ID);
        component = DaggerAssignmentEditComponent.builder()
                .applicationComponent(Qwittig.getAppComponent(this))
                .navigatorModule(new NavigatorModule(this))
                .assignmentEditPresenterModule(new AssignmentEditPresenterModule(assignmentId))
                .persistentViewModelsModule(new PersistentViewModelsModule(savedInstanceState))
                .build();
        component.inject(this);
    }

    @Override
    protected BaseAssignmentAddEditFragment getAssignmentFragment() {
        return new AssignmentEditFragment();
    }
}
