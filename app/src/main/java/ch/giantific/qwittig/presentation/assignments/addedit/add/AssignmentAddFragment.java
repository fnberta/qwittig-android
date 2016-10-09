/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.assignments.addedit.add;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;

import ch.giantific.qwittig.presentation.assignments.addedit.BaseAssignmentAddEditFragment;
import ch.giantific.qwittig.presentation.assignments.addedit.di.AssignmentAddComponent;
import ch.giantific.qwittig.presentation.common.BaseFragment;

/**
 * Provides an interface for the user to addItemAtPosition a new task. Allows the selection of the time
 * frame, the deadline and the users involved. The title of the task is set in the {@link Toolbar}
 * of the hosting {@link Activity}.
 * <p/>
 * Subclass of {@link BaseFragment}.
 */
public class AssignmentAddFragment extends BaseAssignmentAddEditFragment<AssignmentAddComponent> {

    public AssignmentAddFragment() {
        // required empty constructor
    }

    @Override
    protected void injectDependencies(@NonNull AssignmentAddComponent component) {
        component.inject(this);
    }
}
