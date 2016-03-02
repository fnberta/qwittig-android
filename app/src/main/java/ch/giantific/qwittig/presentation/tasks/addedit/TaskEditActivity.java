/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.tasks.addedit;

import ch.giantific.qwittig.presentation.tasks.list.TasksFragment;

/**
 * Hosts {@link TaskAddEditFragment} that allows the user to edit a task.
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
        return TaskAddEditFragment.newEditInstance(editTaskId);
    }
}
