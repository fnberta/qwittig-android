/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.tasks.list;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ch.giantific.qwittig.databinding.FragmentTasksBinding;
import ch.giantific.qwittig.di.components.DaggerTasksListComponent;
import ch.giantific.qwittig.di.modules.TasksListViewModelModule;
import ch.giantific.qwittig.domain.models.parse.Task;
import ch.giantific.qwittig.presentation.common.BaseActivity;
import ch.giantific.qwittig.presentation.common.fragments.BaseFragment;
import ch.giantific.qwittig.presentation.common.fragments.BaseRecyclerViewOnlineFragment;
import ch.giantific.qwittig.presentation.tasks.addedit.TaskAddActivity;
import ch.giantific.qwittig.presentation.tasks.details.TaskDetailsActivity;

/**
 * Displays a {@link RecyclerView} list of all the ongoing tasks in a group in card base interface.
 * <p/>
 * Subclass {@link BaseRecyclerViewOnlineFragment}.
 */
public class TasksFragment extends BaseRecyclerViewOnlineFragment<TasksViewModel, TasksFragment.ActivityListener>
        implements TasksViewModel.ViewListener {

    public static final String INTENT_TASK_ID = "ch.giantific.qwittig.INTENT_TASK_ID";
    private FragmentTasksBinding mBinding;

    public TasksFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DaggerTasksListComponent.builder()
                .tasksListViewModelModule(new TasksListViewModelModule(savedInstanceState, this))
                .build()
                .inject(this);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentTasksBinding.inflate(inflater, container, false);
        mBinding.setViewModel(mViewModel);
        return mBinding.getRoot();
    }

    @Override
    protected void setViewModelToActivity() {
        mActivity.setViewModel(mViewModel);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


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
        TasksUpdateWorker.attach(getFragmentManager());
    }

    @SuppressWarnings("unchecked")
    @Override
    public void startTaskDetailsActivity(@NonNull Task task) {
        final FragmentActivity activity = getActivity();
        final Intent intent = new Intent(activity, TaskDetailsActivity.class);
        intent.putExtra(INTENT_TASK_ID, task.getObjectId());
        final ActivityOptionsCompat options =
                ActivityOptionsCompat.makeSceneTransitionAnimation(activity);
        activity.startActivityForResult(intent, BaseActivity.INTENT_REQUEST_TASK_DETAILS, options.toBundle());
    }

    @SuppressWarnings("unchecked")
    @Override
    public void startTaskAddActivity() {
        final FragmentActivity activity = getActivity();
        final Intent intent = new Intent(activity, TaskAddActivity.class);
        final ActivityOptionsCompat options =
                ActivityOptionsCompat.makeSceneTransitionAnimation(activity);
        activity.startActivityForResult(intent, BaseActivity.INTENT_REQUEST_TASK_NEW, options.toBundle());
    }

    @Override
    public void loadRemindUserWorker(@NonNull String taskId) {
        TaskRemindWorker.attach(getFragmentManager(), taskId);
    }

    public interface ActivityListener extends BaseFragment.ActivityListener {
        void setViewModel(@NonNull TasksViewModel viewModel);
    }
}
