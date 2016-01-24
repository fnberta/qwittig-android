/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.presentation.ui.activities;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;

import ch.giantific.qwittig.LocalBroadcastImpl;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.databinding.ActivityTasksBinding;
import ch.giantific.qwittig.di.components.NavDrawerComponent;
import ch.giantific.qwittig.domain.models.parse.Task;
import ch.giantific.qwittig.presentation.ui.adapters.StringResSpinnerAdapter;
import ch.giantific.qwittig.presentation.ui.fragments.TasksFragment;
import ch.giantific.qwittig.presentation.viewmodels.TasksViewModel;
import ch.giantific.qwittig.presentation.workerfragments.query.TasksUpdateListener;
import ch.giantific.qwittig.presentation.workerfragments.reminder.TaskReminderListener;
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
        TasksUpdateListener,
        TaskReminderListener {

    private static final String LOG_TAG = TasksActivity.class.getSimpleName();
    private ActivityTasksBinding mBinding;

    @Override
    void handleLocalBroadcast(Intent intent, int dataType) {
        super.handleLocalBroadcast(intent, dataType);

        if (dataType == LocalBroadcastImpl.DATA_TYPE_TASKS_UPDATED) {
            mViewModel.updateList();
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

        if (isUserLoggedIn() && savedInstanceState == null) {
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
        getFragmentManager().beginTransaction()
                .add(R.id.container, tasksFragment)
                .commit();
    }

    @Override
    public void setViewModel(@NonNull TasksViewModel viewModel) {
        mViewModel = viewModel;
        mBinding.setViewModel(viewModel);
    }

    @Override
    void onLoginSuccessful() {
        super.onLoginSuccessful();

        // TODO: fix setLoading(true) because online query is still happening
        addTasksFragment();
    }

    @Override
    int getSelfNavDrawerItem() {
        return R.id.nav_tasks;
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
}
