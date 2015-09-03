package ch.giantific.qwittig.ui;

import android.os.Bundle;

import ch.giantific.qwittig.R;

public class TasksActivity extends BaseNavDrawerActivity {

    private static final String TASKS_FRAGMENT = "tasks_fragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasks);

        // check item in NavDrawer
        checkNavDrawerItem(R.id.nav_tasks);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new TasksFragment(), TASKS_FRAGMENT)
                    .commit();
        }
    }

    @Override
    protected void onNewGroupSet() {
        // TODO: empty implementation
    }

    @Override
    int getSelfNavDrawerItem() {
        return R.id.nav_tasks;
    }
}
