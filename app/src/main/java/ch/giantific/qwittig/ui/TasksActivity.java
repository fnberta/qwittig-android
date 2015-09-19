package ch.giantific.qwittig.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

import com.parse.ParseException;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.constants.AppConstants;
import ch.giantific.qwittig.helpers.TaskQueryHelper;
import ch.giantific.qwittig.helpers.TaskRemindHelper;
import ch.giantific.qwittig.ui.adapters.StringResSpinnerAdapter;
import ch.giantific.qwittig.ui.dialogs.GroupCreateDialogFragment;
import ch.giantific.qwittig.utils.Utils;

public class TasksActivity extends BaseNavDrawerActivity implements
        TasksFragment.FragmentInteractionListener,
        TaskQueryHelper.HelperInteractionListener,
        TaskRemindHelper.HelperInteractionListener,
        GroupCreateDialogFragment.DialogInteractionListener {

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
        showFab();

        mSpinnerDeadline = (Spinner) findViewById(R.id.sp_tasks_deadline);
        setupDeadlineSpinner();

        if (mUserIsLoggedIn) {
            if (savedInstanceState == null) {
                addTasksFragment();
            }

            fetchCurrentUserGroups();
        }
    }

    private void showFab() {
        if (Utils.isRunningLollipopAndHigher()) {
            if (ViewCompat.isLaidOut(mFab)) {
                circularRevealFab();
            } else {
                mFab.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                    @Override
                    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                        v.removeOnLayoutChangeListener(this);
                        circularRevealFab();
                    }
                });
            }
        } else {
            mFab.show();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void circularRevealFab() {
        Animator reveal = Utils.getCircularRevealAnimator(mFab);
        reveal.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                animation.removeListener(this);

                mFab.setVisibility(View.VISIBLE);
            }
        });
        reveal.start();
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
    public void onUserReminded(String taskId) {
        mTaskFragment.onUserReminded(taskId);
    }

    @Override
    public void onFailedToRemindUser(ParseException e, String taskId) {
        mTaskFragment.onFailedToRemindUser(e, taskId);
    }

    /**
     * Called from dialog that is shown when user tries to add new task and is not yet part of
     * any group.
     */
    @Override
    public void createNewGroup() {
        Intent intent = new Intent(this, SettingsGroupNewActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onNewGroupSet() {
        mTaskFragment.updateAdapter();
    }

    @Override
    int getSelfNavDrawerItem() {
        return R.id.nav_tasks;
    }
}
