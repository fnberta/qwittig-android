package ch.giantific.qwittig.ui;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

import com.parse.ParseException;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.constants.AppConstants;
import ch.giantific.qwittig.helpers.TaskQueryHelper;
import ch.giantific.qwittig.ui.adapters.StringResSpinnerAdapter;

public class TasksActivity extends BaseNavDrawerActivity implements
        TaskQueryHelper.HelperInteractionListener {

    private Spinner mSpinnerDeadline;
    private TasksFragment mTaskFragment;
    private FloatingActionButton mFab;
    private static final String TASKS_FRAGMENT = "tasks_fragment";
    private static final String LOG_TAG = TasksActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasks);

        // check item in NavDrawer
        checkNavDrawerItem(R.id.nav_tasks);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(null);
        }

        mFab = (FloatingActionButton) findViewById(R.id.fab_task_add);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTaskFragment.addNewTask();
            }
        });
        mFab.postDelayed(new Runnable() {
            @Override
            public void run() {
                mFab.show();
            }
        }, AppConstants.FAB_CIRCULAR_REVEAL_DELAY * 4);

        mSpinnerDeadline = (Spinner) findViewById(R.id.sp_tasks_deadline);
        setupDeadlineSpinner();

        if (mUserIsLoggedIn && savedInstanceState == null) {
            addTasksFragment();
        }
    }

    private void setupDeadlineSpinner() {
        final int[] deadlines = new int[]{
                R.string.deadline_all,
                R.string.deadline_today,
                R.string.deadline_week,
                R.string.deadline_month,
                R.string.deadline_year};
        final StringResSpinnerAdapter stringResSpinnerAdapter =
                new StringResSpinnerAdapter(this, R.layout.spinner_item_toolbar, deadlines);
        mSpinnerDeadline.setAdapter(stringResSpinnerAdapter);
    }

    private void addTasksFragment() {
        getFragmentManager().beginTransaction()
                .add(R.id.container, new TasksFragment(), TASKS_FRAGMENT)
                .commit();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mUserIsLoggedIn) {
            findTasksFragment();
            mSpinnerDeadline.post(new Runnable() {
                @Override
                public void run() {
                    setDeadlineListener();
                }
            });
        }
    }

    private void findTasksFragment() {
        mTaskFragment = (TasksFragment) getFragmentManager().findFragmentByTag(TASKS_FRAGMENT);
    }

    private void setDeadlineListener() {
        mSpinnerDeadline.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int deadline = (int) parent.getItemAtPosition(position);
                mTaskFragment.onDeadlineSelected(deadline);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    void afterLoginSetup() {
        super.afterLoginSetup();

        addTasksFragment();
        getFragmentManager().executePendingTransactions();
        findTasksFragment();
        setDeadlineListener();
    }

    @Override
    public void onTasksPinFailed(ParseException e) {
        mTaskFragment.onTasksPinFailed(e);
    }

    @Override
    public void onTasksPinned() {
        super.onTasksPinned();

        mTaskFragment.onTasksPinned();
    }

    @Override
    public void onAllTaskQueriesFinished() {
        mTaskFragment.onAllTasksQueriesFinished();
    }

    @Override
    protected void onNewGroupSet() {
        if (mTaskFragment.isAdded()) {
            mTaskFragment.updateAdapter();
        }
    }

    @Override
    int getSelfNavDrawerItem() {
        return R.id.nav_tasks;
    }
}
