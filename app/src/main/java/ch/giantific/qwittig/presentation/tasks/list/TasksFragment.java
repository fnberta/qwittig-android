/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.tasks.list;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ch.giantific.qwittig.data.services.ParseQueryService;
import ch.giantific.qwittig.databinding.FragmentTasksBinding;
import ch.giantific.qwittig.presentation.common.adapters.BaseRecyclerAdapter;
import ch.giantific.qwittig.presentation.common.fragments.BaseRecyclerViewFragment;
import ch.giantific.qwittig.presentation.tasks.list.di.TasksListSubcomponent;

/**
 * Displays a {@link RecyclerView} list of all the ongoing tasks in a group in card base interface.
 * <p/>
 * Subclass {@link BaseRecyclerViewFragment}.
 */
public class TasksFragment extends BaseRecyclerViewFragment<TasksListSubcomponent, TasksViewModel, BaseRecyclerViewFragment.ActivityListener<TasksListSubcomponent>>
        implements TasksViewModel.ViewListener {

    private FragmentTasksBinding mBinding;

    public TasksFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentTasksBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mViewModel.attachView(this);
        mViewModel.setListInteraction(mRecyclerAdapter);
        mBinding.setViewModel(mViewModel);
    }

    @Override
    protected void injectDependencies(@NonNull TasksListSubcomponent component) {
        component.inject(this);
    }

    @Override
    protected RecyclerView getRecyclerView() {
        return mBinding.srlRv.rvBase;
    }

    @Override
    protected BaseRecyclerAdapter getRecyclerAdapter() {
        return new TasksRecyclerAdapter(mViewModel);
    }

    @Override
    public void startUpdateTasksService() {
        ParseQueryService.startUpdateTasks(getActivity());
    }

    @Override
    public void loadRemindUserWorker(@NonNull String taskId) {
        TaskRemindWorker.attach(getFragmentManager(), taskId);
    }
}
