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
    TasksViewModel tasksViewModel;
    private ActivityTasksBinding binding;
    private TaskDeadline[] deadlines;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_tasks);
        binding.setViewModel(tasksViewModel);

        checkNavDrawerItem(R.id.nav_tasks);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(null);
        }

        showFab();
        setupDeadlineSpinner();

        if (userLoggedIn && savedInstanceState == null) {
            addTasksFragment();
        }
    }

    @Override
    protected void injectDependencies(@NonNull NavDrawerComponent navComp,
                                      @Nullable Bundle savedInstanceState) {
        deadlines = new TaskDeadline[]{
                TaskDeadline.newAllInstance(getString(R.string.deadline_all)),
                TaskDeadline.newTodayInstance(getString(R.string.deadline_today)),
                TaskDeadline.newWeekInstance(getString(R.string.deadline_week)),
                TaskDeadline.newMonthInstance(getString(R.string.deadline_month)),
                TaskDeadline.newYearInstance(getString(R.string.deadline_year))
        };

        component = navComp.plus(new TasksListViewModelModule(savedInstanceState, deadlines[0]));
        component.inject(this);
    }

    @Override
    protected List<ViewModel> getViewModels() {
        return Arrays.asList(new ViewModel[]{tasksViewModel});
    }

    private void showFab() {
        if (ViewCompat.isLaidOut(binding.fabTaskAdd)) {
            binding.fabTaskAdd.show();
        } else {
            binding.fabTaskAdd.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(@NonNull View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    v.removeOnLayoutChangeListener(this);
                    binding.fabTaskAdd.show();
                }
            });
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void setupDeadlineSpinner() {
        final ActionBar actionBar = getSupportActionBar();
        final Context themedContext = actionBar.getThemedContext();
        final ArrayAdapter<TaskDeadline> adapter =
                new ArrayAdapter<>(themedContext, R.layout.spinner_item_toolbar, deadlines);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spTasksDeadline.setAdapter(adapter);
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
    public void setupScreenAfterLogin() {
        super.setupScreenAfterLogin();

        addTasksFragment();
    }

    @Override
    public void setTaskReminderStream(@NonNull Single<String> single, @NonNull String taskId,
                                      @NonNull String workerTag) {
        tasksViewModel.setTaskReminderStream(single, taskId, workerTag);
    }
}
