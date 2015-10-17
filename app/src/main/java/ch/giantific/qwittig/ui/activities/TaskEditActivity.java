package ch.giantific.qwittig.ui.activities;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.ui.fragments.TaskEditFragment;
import ch.giantific.qwittig.ui.fragments.TasksFragment;

public class TaskEditActivity extends TaskAddActivity {

    private static final String TASK_EDIT_FRAGMENT = "task_edit_fragment";

    @Override
    void addTaskFragment() {
        String editTaskId = getIntent().getStringExtra(TasksFragment.INTENT_TASK_ID);

        getFragmentManager().beginTransaction()
                .add(R.id.container, TaskEditFragment.newInstance(editTaskId), TASK_EDIT_FRAGMENT)
                .commit();
    }

    @Override
    void findTaskFragment() {
        mTaskAddFragment = (TaskEditFragment) getFragmentManager()
                .findFragmentByTag(TASK_EDIT_FRAGMENT);
    }
}
