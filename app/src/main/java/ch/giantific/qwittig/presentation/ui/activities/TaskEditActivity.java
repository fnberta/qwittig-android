/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.presentation.ui.activities;

import ch.giantific.qwittig.presentation.ui.fragments.TaskAddEditFragment;
import ch.giantific.qwittig.presentation.ui.fragments.TaskEditFragment;
import ch.giantific.qwittig.presentation.ui.fragments.TasksFragment;

/**
 * Hosts {@link TaskEditFragment} that allows the user to edit a task.
 * <p/>
 * Almost identical to {@link TaskAddActivity}, but separate activity allows to set a different
 * transition in XML.
 * <p/>
 * Subclass of {@link TaskAddActivity}.
 */
public class TaskEditActivity extends TaskAddActivity {

    @Override
    TaskAddEditFragment getTaskFragment() {
        final String editTaskId = getIntent().getStringExtra(TasksFragment.INTENT_TASK_ID);
        return TaskEditFragment.newInstance(editTaskId);
    }
}
