package ch.giantific.qwittig.ui.activities;

import ch.giantific.qwittig.ui.fragments.TaskAddFragment;
import ch.giantific.qwittig.ui.fragments.TaskEditFragment;
import ch.giantific.qwittig.ui.fragments.TasksFragment;

public class TaskEditActivity extends TaskAddActivity {

    @Override
    TaskAddFragment getTaskFragment() {
        String editTaskId = getIntent().getStringExtra(TasksFragment.INTENT_TASK_ID);
        return TaskEditFragment.newInstance(editTaskId);
    }
}
