/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.ui.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.helpers.query.TaskQueryHelper;
import ch.giantific.qwittig.data.helpers.reminder.TaskRemindHelper;
import ch.giantific.qwittig.ui.adapters.StringResSpinnerAdapter;
import ch.giantific.qwittig.ui.fragments.TasksFragment;
import ch.giantific.qwittig.ui.fragments.dialogs.GroupCreateDialogFragment;
import ch.giantific.qwittig.utils.AnimUtils;
import ch.giantific.qwittig.utils.Utils;

/**
 * Hosts {@link TasksFragment} that displays a list of recent tasks. Only loads the fragment if the
 * user is logged in.
 * <p/>
 * Allows the user to change the tasks display by changing the value of the spinner in the
 * {@link Toolbar}.
 * <p/>
 * Subclass of {@link BaseNavDrawerActivity}.
 */
public class TasksActivity extends BaseNavDrawerActivity implements
        TasksFragment.FragmentInteractionListener,
        TaskQueryHelper.HelperInteractionListener,
        TaskRemindHelper.HelperInteractionListener,
        GroupCreateDialogFragment.DialogInteractionListener {

    private static final String STATE_TASKS_FRAGMENT = "STATE_TASKS_FRAGMENT";
    private static final String LOG_TAG = TasksActivity.class.getSimpleName();
    private Spinner mSpinnerDeadline;
    private TasksFragment mTaskFragment;
    private FloatingActionButton mFab;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
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
            } else {
                mTaskFragment = (TasksFragment) getFragmentManager()
                        .getFragment(savedInstanceState, STATE_TASKS_FRAGMENT);
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
                    public void onLayoutChange(@NonNull View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
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
        Animator reveal = AnimUtils.getCircularRevealAnimator(mFab);
        reveal.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(@NonNull Animator animation) {
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
        mTaskFragment = new TasksFragment();
        getFragmentManager().beginTransaction()
                .add(R.id.container, mTaskFragment)
                .commit();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mUserIsLoggedIn) {
            getFragmentManager().putFragment(outState, STATE_TASKS_FRAGMENT, mTaskFragment);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mUserIsLoggedIn) {
            mSpinnerDeadline.post(new Runnable() {
                @Override
                public void run() {
                    setDeadlineListener();
                }
            });
        }
    }

    private void setDeadlineListener() {
        mSpinnerDeadline.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(@NonNull AdapterView<?> parent, View view, int position, long id) {
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
        setDeadlineListener();
    }

    @Override
    public void onTasksUpdated() {
        super.onTasksUpdated();

        mTaskFragment.onTasksUpdated();
    }

    @Override
    public void onTasksUpdatedFailed(int errorCode) {
        mTaskFragment.onTasksUpdatedFailed(errorCode);
    }

    @Override
    public void onUserReminded(@NonNull String taskId) {
        mTaskFragment.onUserReminded(taskId);
    }

    @Override
    public void onUserRemindFailed(@NonNull String taskId, int errorCode) {
        mTaskFragment.onUserRemindFailed(taskId, errorCode);
    }

    @Override
    public void onCreateGroupSelected() {
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
