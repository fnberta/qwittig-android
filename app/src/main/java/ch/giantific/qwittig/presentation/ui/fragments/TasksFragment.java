/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.presentation.ui.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.databinding.FragmentTasksBinding;
import ch.giantific.qwittig.di.components.DaggerTasksListComponent;
import ch.giantific.qwittig.di.modules.TasksListViewModelModule;
import ch.giantific.qwittig.domain.models.parse.Task;
import ch.giantific.qwittig.presentation.ui.activities.BaseActivity;
import ch.giantific.qwittig.presentation.ui.activities.TaskAddActivity;
import ch.giantific.qwittig.presentation.ui.activities.TaskDetailsActivity;
import ch.giantific.qwittig.presentation.ui.adapters.TasksRecyclerAdapter;
import ch.giantific.qwittig.presentation.viewmodels.TaskDetailsViewModel;
import ch.giantific.qwittig.presentation.viewmodels.TasksViewModel;
import ch.giantific.qwittig.presentation.workerfragments.query.TasksUpdateWorker;
import ch.giantific.qwittig.presentation.workerfragments.reminder.TaskRemindWorker;
import ch.giantific.qwittig.utils.WorkerUtils;

/**
 * Displays a {@link RecyclerView} list of all the ongoing tasks in a group in card base interface.
 * <p/>
 * Subclass {@link BaseRecyclerViewOnlineFragment}.
 */
public class TasksFragment extends BaseRecyclerViewOnlineFragment<TasksViewModel, TasksFragment.ActivityListener>
        implements TasksViewModel.ViewListener {

    public static final String INTENT_TASK_ID = "ch.giantific.qwittig.INTENT_TASK_ID";
    private static final String LOG_TAG = TasksFragment.class.getSimpleName();
    private FragmentTasksBinding mBinding;

    public TasksFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DaggerTasksListComponent.builder()
                .tasksListViewModelModule(new TasksListViewModelModule(savedInstanceState))
                .build()
                .inject(this);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentTasksBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    protected void setViewModelToActivity() {
        mActivity.setViewModel(mViewModel);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case BaseActivity.INTENT_REQUEST_TASK_DETAILS:
                switch (resultCode) {
                    case TaskDetailsViewModel.RESULT_TASK_DELETED:
                        showMessage(R.string.toast_task_deleted);
                        break;
                }
                break;
            case BaseActivity.INTENT_REQUEST_TASK_NEW:
                switch (resultCode) {
                    case TaskAddEditFragment.RESULT_TASK_SAVED:
                        showMessage(R.string.toast_task_added_new);
                        break;
                    case TaskAddEditFragment.RESULT_TASK_DISCARDED:
                        showMessage(R.string.toast_task_discarded);
                        break;
                }
                break;
        }
    }

    @Override
    protected RecyclerView getRecyclerView() {
        return mBinding.srlRv.rvBase;
    }

    @Override
    protected RecyclerView.Adapter getRecyclerAdapter() {
        return new TasksRecyclerAdapter(mViewModel);
    }

    @Override
    protected View getSnackbarView() {
        return mBinding.flMain;
    }

    @Override
    public void loadUpdateTasksWorker() {
        FragmentManager fragmentManager = getFragmentManager();
        Fragment tasksUpdateWorker = WorkerUtils.findWorker(fragmentManager,
                TasksUpdateWorker.WORKER_TAG);

        if (tasksUpdateWorker == null) {
            tasksUpdateWorker = new TasksUpdateWorker();
            fragmentManager.beginTransaction()
                    .add(tasksUpdateWorker, TasksUpdateWorker.WORKER_TAG)
                    .commit();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void startTaskDetailsActivity(@NonNull Task task) {
        final Activity activity = getActivity();
        final Intent intent = new Intent(activity, TaskDetailsActivity.class);
        intent.putExtra(INTENT_TASK_ID, task.getObjectId());
        final ActivityOptionsCompat options =
                ActivityOptionsCompat.makeSceneTransitionAnimation(activity);
        startActivityForResult(intent, BaseActivity.INTENT_REQUEST_TASK_DETAILS, options.toBundle());
    }

    @SuppressWarnings("unchecked")
    @Override
    public void startTaskAddActivity() {
        final Activity activity = getActivity();
        final Intent intent = new Intent(activity, TaskAddActivity.class);
        final ActivityOptionsCompat options =
                ActivityOptionsCompat.makeSceneTransitionAnimation(activity);
        startActivityForResult(intent, BaseActivity.INTENT_REQUEST_TASK_NEW, options.toBundle());
    }

    @Override
    public void loadRemindUserWorker(@NonNull String taskId) {
        final FragmentManager fragmentManager = getFragmentManager();
        final String workerTag = TaskRemindWorker.WORKER_TAG + taskId;
        Fragment taskRemindWorker = WorkerUtils.findWorker(fragmentManager, workerTag);

        if (taskRemindWorker == null) {
            taskRemindWorker = TaskRemindWorker.newInstance(taskId);
            fragmentManager.beginTransaction()
                    .add(taskRemindWorker, workerTag)
                    .commit();
        }
    }

    public interface ActivityListener extends BaseFragment.ActivityListener {
        void setViewModel(@NonNull TasksViewModel viewModel);
    }
}
