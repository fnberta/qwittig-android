/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.assignments.addedit;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;

import ch.giantific.qwittig.Qwittig;
import ch.giantific.qwittig.presentation.assignments.addedit.di.AssignmentAddComponent;
import ch.giantific.qwittig.presentation.assignments.addedit.di.AssignmentAddViewModelModule;
import ch.giantific.qwittig.presentation.assignments.addedit.di.DaggerAssignmentAddComponent;
import ch.giantific.qwittig.presentation.common.BaseActivity;
import ch.giantific.qwittig.presentation.common.di.NavigatorModule;

/**
 * Hosts {@link AssignmentAddFragment} that allows the user to create a new task.
 * <p/>
 * Handles transition animations and displays the task's title in the {@link Toolbar}.
 * <p/>
 * Subclass of {@link BaseActivity}.
 */
public class AssignmentAddActivity extends BaseAssignmentAddEditActivity<AssignmentAddComponent> {

    @Override
    protected void injectDependencies(@Nullable Bundle savedInstanceState) {
        component = DaggerAssignmentAddComponent.builder()
                .applicationComponent(Qwittig.getAppComponent(this))
                .navigatorModule(new NavigatorModule(this))
                .assignmentAddViewModelModule(new AssignmentAddViewModelModule(savedInstanceState))
                .build();
        component.inject(this);
    }

    @Override
    protected BaseAssignmentAddEditFragment getAssignmentFragment() {
        return new AssignmentAddFragment();
    }
}
