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

import ch.giantific.qwittig.BuildConfig;
import ch.giantific.qwittig.data.services.ParseQueryService;
import ch.giantific.qwittig.databinding.FragmentTasksBinding;
import ch.giantific.qwittig.domain.models.Task;
import ch.giantific.qwittig.presentation.common.BaseActivity;
import ch.giantific.qwittig.presentation.common.fragments.BaseFragment;
import ch.giantific.qwittig.presentation.common.fragments.BaseRecyclerViewFragment;
import ch.giantific.qwittig.presentation.tasks.addedit.TaskAddActivity;
import ch.giantific.qwittig.presentation.tasks.details.TaskDetailsActivity;
import ch.giantific.qwittig.presentation.tasks.list.di.DaggerTasksListComponent;
import ch.giantific.qwittig.presentation.tasks.list.di.TasksListViewModelModule;
import ch.giantific.qwittig.presentation.tasks.list.models.TaskDeadline;

/**
 * Displays a {@link RecyclerView} list of all the ongoing tasks in a group in card base interface.
 * <p/>
 * Subclass {@link BaseRecyclerViewFragment}.
 */
public class TasksFragment extends BaseRecyclerViewFragment<TasksViewModel, TasksFragment.ActivityListener>
        implements TasksViewModel.ViewListener {

    public static final String INTENT_EXTRA_TASK_ID = BuildConfig.APPLICATION_ID + ".intents.INTENT_EXTRA_TASK_ID";
    private static final String KEY_DEADLINE = "DEADLINE";
    private FragmentTasksBinding mBinding;

    public TasksFragment() {
    }

    /**
     * Returns a new instance of a {@link TasksFragment} with a default task deadline set.
     *
     * @param deadline the default task deadline to set
     * @return a new instance of a {@link TasksFragment}
     */
    public static TasksFragment newInstance(@NonNull TaskDeadline deadline) {
        final TasksFragment fragment = new TasksFragment();
        final Bundle args = new Bundle();
        args.putParcelable(KEY_DEADLINE, deadline);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Bundle args = getArguments();
        final TaskDeadline taskDeadline = args.getParcelable(KEY_DEADLINE);

        DaggerTasksListComponent.builder()
                .tasksListViewModelModule(new TasksListViewModelModule(savedInstanceState, this, taskDeadline))
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
    protected RecyclerView getRecyclerView() {
        return mBinding.srlRv.rvBase;
    }

    @Override
    protected RecyclerView.Adapter getRecyclerAdapter() {
        return new TasksRecyclerAdapter(mViewModel);
    }

    @Override
    public void startUpdateTasksService() {
        ParseQueryService.startUpdateTasks(getActivity());
    }

    @SuppressWarnings("unchecked")
    @Override
    public void startTaskDetailsScreen(@NonNull Task task) {
        final FragmentActivity activity = getActivity();
        final Intent intent = new Intent(activity, TaskDetailsActivity.class);
        intent.putExtra(INTENT_EXTRA_TASK_ID, task.getObjectId());
        final ActivityOptionsCompat options =
                ActivityOptionsCompat.makeSceneTransitionAnimation(activity);
        activity.startActivityForResult(intent, BaseActivity.INTENT_REQUEST_TASK_DETAILS, options.toBundle());
    }

    @SuppressWarnings("unchecked")
    @Override
    public void startTaskAddScreen() {
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

    /**
     * Defines the interaction with the hosting activity.
     */
    public interface ActivityListener extends BaseFragment.ActivityListener {
        /**
         * Sets and binds the view model to the activity's layout.
         *
         * @param viewModel the view model to bind
         */
        void setViewModel(@NonNull TasksViewModel viewModel);
    }
}
