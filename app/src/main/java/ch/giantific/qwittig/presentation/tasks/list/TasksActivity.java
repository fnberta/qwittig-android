/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.tasks.list;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.bus.LocalBroadcast;
import ch.giantific.qwittig.databinding.ActivityTasksBinding;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.viewmodels.ViewModel;
import ch.giantific.qwittig.presentation.navdrawer.BaseNavDrawerActivity;
import ch.giantific.qwittig.presentation.navdrawer.di.NavDrawerComponent;
import ch.giantific.qwittig.presentation.tasks.addedit.TaskAddEditViewModel.TaskResult;
import ch.giantific.qwittig.presentation.tasks.details.TaskDetailsViewModel.TaskDetailsResult;
import ch.giantific.qwittig.presentation.tasks.list.di.TasksListSubcomponent;
import ch.giantific.qwittig.presentation.tasks.list.di.TasksListViewModelModule;
import ch.giantific.qwittig.presentation.tasks.list.models.TaskDeadline;
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
public class TasksActivity extends BaseNavDrawerActivity<TasksListSubcomponent>
        implements TaskRemindWorkerListener {

    @Inject
    TasksViewModel mTasksViewModel;
    private ActivityTasksBinding mBinding;
    private TaskDeadline[] mDeadlines;

    @Override
    protected void handleLocalBroadcast(Intent intent, int dataType) {
        super.handleLocalBroadcast(intent, dataType);

        if (dataType == LocalBroadcast.DataType.TASKS_UPDATED) {
            final boolean successful = intent.getBooleanExtra(LocalBroadcast.INTENT_EXTRA_SUCCESSFUL, false);
            mTasksViewModel.onDataUpdated(successful);
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_tasks);
        mBinding.setViewModel(mTasksViewModel);

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
    protected void injectDependencies(@NonNull NavDrawerComponent navComp,
                                      @Nullable Bundle savedInstanceState) {
        mDeadlines = new TaskDeadline[]{
                TaskDeadline.newAllInstance(getString(R.string.deadline_all)),
                TaskDeadline.newTodayInstance(getString(R.string.deadline_today)),
                TaskDeadline.newWeekInstance(getString(R.string.deadline_week)),
                TaskDeadline.newMonthInstance(getString(R.string.deadline_month)),
                TaskDeadline.newYearInstance(getString(R.string.deadline_year))
        };

        mComponent = navComp.plus(new TasksListViewModelModule(savedInstanceState, mDeadlines[0]));
        mComponent.inject(this);
    }

    @Override
    protected List<ViewModel> getViewModels() {
        return Arrays.asList(new ViewModel[]{mTasksViewModel});
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

    @SuppressWarnings("ConstantConditions")
    private void setupDeadlineSpinner() {
        final ActionBar actionBar = getSupportActionBar();
        final Context themedContext = actionBar.getThemedContext();
        final ArrayAdapter<TaskDeadline> adapter =
                new ArrayAdapter<>(themedContext, R.layout.spinner_item_toolbar, mDeadlines);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mBinding.spTasksDeadline.setAdapter(adapter);
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
            case Navigator.INTENT_REQUEST_TASK_DETAILS:
                switch (resultCode) {
                    case TaskDetailsResult.TASK_DELETED:
                        showMessage(R.string.toast_task_deleted);
                        break;
                }
                break;
            case Navigator.INTENT_REQUEST_TASK_NEW:
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
    protected void onLoginSuccessful() {
        super.onLoginSuccessful();

        addTasksFragment();
    }

    @Override
    public void setTaskReminderStream(@NonNull Single<String> single, @NonNull String taskId,
                                      @NonNull String workerTag) {
        mTasksViewModel.setTaskReminderStream(single, taskId, workerTag);
    }
}
