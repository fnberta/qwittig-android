package ch.giantific.qwittig.ui;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.parse.models.Task;
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
                List<ParseUser> usersInvolved = new ArrayList<>();
                usersInvolved.add(mCurrentUser);
                Task task = new Task("Küche putzen", mCurrentGroup, mCurrentUser, usersInvolved);
                task.saveEventually(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e != null) {
                            Log.e(LOG_TAG, "e " + e.toString());
                        }
                    }
                });
                // TODO: add new task
            }
        });
        mSpinnerDeadline = (Spinner) findViewById(R.id.sp_tasks_deadline);
        setupTimeFrameSpinner();

        if (mUserIsLoggedIn && savedInstanceState == null) {
            addTasksFragment();
        }
    }

    private void setupTimeFrameSpinner() {
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
        // TODO: empty implementation
    }

    @Override
    int getSelfNavDrawerItem() {
        return R.id.nav_tasks;
    }
}
