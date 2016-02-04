/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.presentation.ui.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.databinding.RowGenericHeaderBinding;
import ch.giantific.qwittig.databinding.RowTasksBinding;
import ch.giantific.qwittig.domain.models.parse.Task;
import ch.giantific.qwittig.domain.models.parse.User;
import ch.giantific.qwittig.presentation.ui.adapters.rows.BindingRow;
import ch.giantific.qwittig.presentation.viewmodels.TasksViewModel;
import ch.giantific.qwittig.presentation.viewmodels.rows.HeaderRowViewModel;
import ch.giantific.qwittig.presentation.viewmodels.rows.HeaderRowViewModelImpl;
import ch.giantific.qwittig.presentation.viewmodels.rows.TaskRowViewModel;

/**
 * Handles the display of recent tasks assigned to users in a group.
 * <p/>
 * Subclass of {@link RecyclerView.Adapter}.
 */
public class TasksRecyclerAdapter extends RecyclerView.Adapter {

    private TasksViewModel mViewModel;

    /**
     * Constructs a new {@link TasksRecyclerAdapter}.
     *
     * @param viewModel the main view's model
     */
    public TasksRecyclerAdapter(@NonNull TasksViewModel viewModel) {
        super();

        mViewModel = viewModel;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case TasksViewModel.TYPE_ITEM: {
                final RowTasksBinding binding = RowTasksBinding.inflate(inflater, parent, false);
                return new TaskRow(binding, mViewModel);
            }
            case TasksViewModel.TYPE_HEADER: {
                final RowGenericHeaderBinding binding = RowGenericHeaderBinding.inflate(inflater, parent, false);
                return new BindingRow<>(binding);
            }
            default:
                throw new RuntimeException("there is no type that matches the type " + viewType +
                        " + make sure your using types correctly");
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        final int viewType = getItemViewType(position);
        switch (viewType) {
            case TasksViewModel.TYPE_ITEM: {
                final TaskRow taskRow = (TaskRow) viewHolder;
                final Task task = mViewModel.getItemAtPosition(position);
                final RowTasksBinding binding = taskRow.getBinding();

                TaskRowViewModel viewModel = binding.getViewModel();
                final User currentUser = mViewModel.getCurrentUser();
                if (viewModel == null) {
                    viewModel = new TaskRowViewModel(task, currentUser);
                    binding.setViewModel(viewModel);
                } else {
                    viewModel.updateTaskInfo(task);
                }
                binding.executePendingBindings();

                break;
            }
            case TasksViewModel.TYPE_HEADER: {
                final BindingRow<RowGenericHeaderBinding> headerRow = (BindingRow<RowGenericHeaderBinding>) viewHolder;
                final RowGenericHeaderBinding binding = headerRow.getBinding();

                final int header = position == 0
                        ? R.string.task_header_my
                        : R.string.task_header_group;
                final HeaderRowViewModel viewModel = new HeaderRowViewModelImpl(header);
                binding.setViewModel(viewModel);
                binding.executePendingBindings();

                break;
            }
        }
    }

    @Override
    public int getItemCount() {
        return mViewModel.getItemCount();
    }

    @Override
    public int getItemViewType(int position) {
        return mViewModel.getItemViewType(position);
    }

    /**
     * Defines the actions to take when a user clicks on a task.
     */
    public interface AdapterInteractionListener {
        /**
         * Handles the click on the task row itself.
         *
         * @param position the adapter postition of the task
         */
        void onTaskRowClicked(int position);

        /**
         * Handles the click on the mark task as done button.
         *
         * @param position the adapter postition of the task
         */
        void onDoneButtonClicked(int position);

        /**
         * Handles the click on the remind user to finish a task button.
         *
         * @param position the adapter postition of the task
         */
        void onRemindButtonClicked(int position);
    }

    /**
     * Provides a {@link RecyclerView} row that displays a task with all its information.
     * <p/>
     * Subclass of {@link RecyclerView.ViewHolder}.
     */
    private static class TaskRow extends BindingRow<RowTasksBinding> {

        /**
         * Constructs a new {@link TaskRow} and sets the click listeners.
         *
         * @param binding the binding for the view
         */
        public TaskRow(@NonNull RowTasksBinding binding, @NonNull final TasksViewModel viewModel) {
            super(binding);

            binding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    viewModel.onTaskRowClicked(getAdapterPosition());
                }
            });
            binding.btTaskDone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    viewModel.onDoneButtonClicked(getAdapterPosition());
                }
            });
            binding.btTaskRemind.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    viewModel.onRemindButtonClicked(getAdapterPosition());
                }
            });
        }
    }
}
