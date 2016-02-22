/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.tasks.list;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;

import ch.giantific.qwittig.LocalBroadcast;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.databinding.ActivityTasksBinding;
import ch.giantific.qwittig.presentation.navdrawer.di.NavDrawerComponent;
import ch.giantific.qwittig.domain.models.Task;
import ch.giantific.qwittig.presentation.common.adapters.StringResSpinnerAdapter;
import ch.giantific.qwittig.presentation.navdrawer.BaseNavDrawerActivity;
import ch.giantific.qwittig.presentation.tasks.addedit.TaskAddEditViewModel.TaskResult;
import ch.giantific.qwittig.presentation.tasks.details.TaskDetailsViewModel.TaskDetailsResult;
import rx.Observable;
import rx.Single;

/**
 * Hosts {@link TasksFragment} that displays a list of recent tasks. Only loads the fragment if the
 * user is logged in.
 * <p/>
 * Allows the user to change the tasks display by changing the value of the spinner in the
 * {@link Toolbar}.
 * <p/>
 * Subclass of {@link BaseNavDrawerActivity}.
 */
public class TasksActivity extends BaseNavDrawerActivity<TasksViewModel> implements
        TasksFragment.ActivityListener,
        TasksUpdateWorkerListener,
        TaskRemindWorkerListener {

    private ActivityTasksBinding mBinding;

    @Override
    protected void handleLocalBroadcast(Intent intent, int dataType) {
        super.handleLocalBroadcast(intent, dataType);

        if (dataType == LocalBroadcast.DataType.TASKS_UPDATED) {
            mViewModel.loadData();
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_tasks);

        // check item in NavDrawer
        checkNavDrawerItem(R.id.nav_tasks);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(null);
        }

        showFab();
        setupDeadlineSpinner();

        if (mUserLoggedIn && savedInstanceState == null) {
            addTasksFragment();
        }
    }

    @Override
    protected void injectNavDrawerDependencies(@NonNull NavDrawerComponent navComp) {
        navComp.inject(this);
    }

    private void showFab() {
        if (ViewCompat.isLaidOut(mBinding.fabTaskAdd)) {
            mBinding.fabTaskAdd.show();
        } else {
            mBinding.fabTaskAdd.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(@NonNull View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    v.removeOnLayoutChangeListener(this);
                    mBinding.fabTaskAdd.show();
                }
            });
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
        mBinding.spTasksDeadline.setAdapter(stringResSpinnerAdapter);
    }

    private void addTasksFragment() {
        final TasksFragment tasksFragment = new TasksFragment();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.container, tasksFragment)
                .commit();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case INTENT_REQUEST_TASK_DETAILS:
                switch (resultCode) {
                    case TaskDetailsResult.TASK_DELETED:
                        showMessage(R.string.toast_task_deleted);
                        break;
                }
                break;
            case INTENT_REQUEST_TASK_NEW:
                switch (resultCode) {
                    case TaskResult.TASK_SAVED:
                        showMessage(R.string.toast_task_added_new);
                        break;
                    case TaskResult.TASK_DISCARDED:
                        showMessage(R.string.toast_task_discarded);
                        break;
                }
                break;
        }
    }

    @Override
    protected int getSelfNavDrawerItem() {
        return R.id.nav_tasks;
    }

    @Override
    public void setViewModel(@NonNull TasksViewModel viewModel) {
        mViewModel = viewModel;
        mBinding.setViewModel(viewModel);
    }

    @Override
    public void setTasksUpdateStream(@NonNull Observable<Task> observable,
                                     @NonNull String workerTag) {
        mViewModel.setTasksUpdateStream(observable, workerTag);
    }

    @Override
    public void setTaskReminderStream(@NonNull Single<String> single, @NonNull String taskId,
                                      @NonNull String workerTag) {
        mViewModel.setTaskReminderStream(single, taskId, workerTag);
    }

    @Override
    protected void onLoginSuccessful() {
        super.onLoginSuccessful();

        // TODO: fix setLoading(true) because online query is still happening
        addTasksFragment();
    }
}
