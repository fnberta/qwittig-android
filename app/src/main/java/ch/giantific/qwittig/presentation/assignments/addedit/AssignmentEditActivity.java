/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.assignments.addedit;

import android.os.Bundle;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.Qwittig;
import ch.giantific.qwittig.presentation.assignments.addedit.di.AssignmentEditComponent;
import ch.giantific.qwittig.presentation.assignments.addedit.di.AssignmentEditViewModelModule;
import ch.giantific.qwittig.presentation.assignments.addedit.di.DaggerAssignmentEditComponent;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.di.NavigatorModule;

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
        final String assignmentId = getIntent().getStringExtra(Navigator.INTENT_ASSIGNMENT_ID);
        component = DaggerAssignmentEditComponent.builder()
                .applicationComponent(Qwittig.getAppComponent(this))
                .navigatorModule(new NavigatorModule(this))
                .assignmentEditViewModelModule(new AssignmentEditViewModelModule(savedInstanceState, assignmentId))
                .build();
        component.inject(this);
    }

    @Override
    protected BaseAssignmentAddEditFragment getAssignmentFragment() {
        return new AssignmentEditFragment();
    }
}
