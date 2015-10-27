/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.ui.activities;

import ch.giantific.qwittig.ui.fragments.TaskAddFragment;
import ch.giantific.qwittig.ui.fragments.TaskEditFragment;
import ch.giantific.qwittig.ui.fragments.TasksFragment;

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
    TaskAddFragment getTaskFragment() {
        String editTaskId = getIntent().getStringExtra(TasksFragment.INTENT_TASK_ID);
        return TaskEditFragment.newInstance(editTaskId);
    }
}
