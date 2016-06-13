/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.tasks.list;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;
import java.util.Objects;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.databinding.RowGenericHeaderBinding;
import ch.giantific.qwittig.databinding.RowTasksBinding;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.presentation.common.adapters.rows.BindingRow;
import ch.giantific.qwittig.presentation.tasks.list.items.TaskItem;
import ch.giantific.qwittig.presentation.tasks.list.items.TasksBaseItem;
import ch.giantific.qwittig.presentation.tasks.list.items.TasksBaseItem.Type;
import ch.giantific.qwittig.presentation.tasks.list.items.TasksHeaderItem;

/**
 * Handles the display of recent tasks assigned to users in a group.
 * <p/>
 * Subclass of {@link RecyclerView.Adapter}.
 */
public class TasksRecyclerAdapter extends RecyclerView.Adapter {

    private final TasksViewModel mViewModel;

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
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, @Type int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case Type.TASK: {
                final RowTasksBinding binding = RowTasksBinding.inflate(inflater, parent, false);
                return new TaskRow(parent.getContext(), binding, mViewModel);
            }
            case Type.HEADER: {
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
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, @Type int position) {
        final TasksBaseItem tasksBaseItem = mViewModel.getItemAtPosition(position);
        final int viewType = getItemViewType(position);
        switch (viewType) {
            case Type.TASK: {
                final TaskRow taskRow = (TaskRow) viewHolder;
                final RowTasksBinding binding = taskRow.getBinding();

                final TaskItem taskItem = (TaskItem) tasksBaseItem;
                taskItem.setView(taskRow);
                binding.setItem(taskItem);
                binding.executePendingBindings();

                break;
            }
            case Type.HEADER: {
                final BindingRow<RowGenericHeaderBinding> headerRow =
                        (BindingRow<RowGenericHeaderBinding>) viewHolder;
                final RowGenericHeaderBinding binding = headerRow.getBinding();

                binding.setViewModel((TasksHeaderItem) tasksBaseItem);
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
         * @param position the adapter position of the task
         */
        void onTaskRowClicked(int position);

        /**
         * Handles the click on the mark task as done button.
         *
         * @param position the adapter position of the task
         */
        void onDoneButtonClicked(int position);

        /**
         * Handles the click on the remind user to finish a task button.
         *
         * @param position the adapter position of the task
         */
        void onRemindButtonClicked(int position);
    }

    /**
     * Provides a {@link RecyclerView} row that displays a task with all its information.
     * <p/>
     * Subclass of {@link RecyclerView.ViewHolder}.
     */
    private static class TaskRow extends BindingRow<RowTasksBinding>
            implements TaskItem.ViewListener {

        private final Context mContext;

        /**
         * Constructs a new {@link TaskRow} and sets the click listeners.
         *
         * @param binding the binding for the view
         */
        public TaskRow(@NonNull Context context, @NonNull RowTasksBinding binding,
                       @NonNull final TasksViewModel viewModel) {
            super(binding);

            mContext = context;
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

        @Override
        public String buildTaskIdentitiesString(@NonNull List<Identity> identities) {
            final Identity identityResponsible = identities.get(0);
            String identitiesFormatted = "";
            if (identities.size() > 1) {
                final StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(mContext.getString(R.string.task_users_involved_next)).append(" ");
                for (Identity identity : identities) {
                    if (!Objects.equals(identity.getObjectId(), identityResponsible.getObjectId())) {
                        stringBuilder.append(identity.getNickname()).append(" - ");
                    }
                }
                // delete last -
                final int length = stringBuilder.length();
                stringBuilder.delete(length - 3, length - 1);
                identitiesFormatted = stringBuilder.toString();
            }

            return identitiesFormatted;
        }

        @Override
        public String buildTaskDeadlineString(@StringRes int deadline, Object... args) {
            return mContext.getString(deadline, args);
        }
    }
}
