/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.tasks.addedit;

import android.os.Bundle;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.Qwittig;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.tasks.addedit.di.DaggerTaskEditComponent;
import ch.giantific.qwittig.presentation.tasks.addedit.di.TaskEditComponent;
import ch.giantific.qwittig.presentation.tasks.addedit.di.TaskEditViewModelModule;

/**
 * Hosts {@link TaskAddFragment} that allows the user to edit a task.
 * <p/>
 * Almost identical to {@link TaskAddActivity}, but separate activity allows to set a different
 * transition in XML.
 * <p/>
 * Subclass of {@link TaskAddActivity}.
 */
public class TaskEditActivity extends BaseTaskAddEditActivity<TaskEditComponent> {

    @Override
    protected void injectDependencies(@Nullable Bundle savedInstanceState) {
        final String editTaskId = getIntent().getStringExtra(Navigator.INTENT_TASK_ID);
        component = DaggerTaskEditComponent.builder()
                .applicationComponent(Qwittig.getAppComponent(this))
                .taskEditViewModelModule(new TaskEditViewModelModule(savedInstanceState, editTaskId))
                .build();
        component.inject(this);
    }

    @Override
    protected BaseTaskAddEditFragment getTaskFragment() {
        return new TaskEditFragment();
    }
}
