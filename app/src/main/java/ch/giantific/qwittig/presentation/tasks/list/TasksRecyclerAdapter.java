/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.tasks.list;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.List;
import java.util.Objects;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.databinding.RowGenericHeaderBinding;
import ch.giantific.qwittig.databinding.RowTasksBinding;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.presentation.common.adapters.BaseRecyclerAdapter;
import ch.giantific.qwittig.presentation.common.adapters.rows.BindingRow;
import ch.giantific.qwittig.presentation.tasks.list.itemmodels.TasksHeaderItem;
import ch.giantific.qwittig.presentation.tasks.list.itemmodels.TasksItem;
import ch.giantific.qwittig.presentation.tasks.list.itemmodels.TasksItemModel;
import ch.giantific.qwittig.presentation.tasks.list.itemmodels.TasksItemModel.Type;

/**
 * Handles the display of recent tasks assigned to users in a group.
 * <p/>
 * Subclass of {@link RecyclerView.Adapter}.
 */
public class TasksRecyclerAdapter extends BaseRecyclerAdapter {

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
                return new TaskRow(parent.getContext(), binding);
            }
            case Type.HEADER: {
                final RowGenericHeaderBinding binding = RowGenericHeaderBinding.inflate(inflater, parent, false);
                return new BindingRow<>(binding);
            }
            default:
                return super.onCreateViewHolder(parent, viewType);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, @Type int position) {
        final TasksItemModel tasksItemModel = mViewModel.getItemAtPosition(position);
        final int viewType = getItemViewType(position);
        switch (viewType) {
            case Type.TASK: {
                final TaskRow taskRow = (TaskRow) viewHolder;
                final RowTasksBinding binding = taskRow.getBinding();

                final TasksItem tasksItem = (TasksItem) tasksItemModel;
                tasksItem.setView(taskRow);
                binding.setItemModel(tasksItem);
                binding.setViewModel(mViewModel);
                binding.executePendingBindings();

                break;
            }
            case Type.HEADER: {
                final BindingRow<RowGenericHeaderBinding> headerRow =
                        (BindingRow<RowGenericHeaderBinding>) viewHolder;
                final RowGenericHeaderBinding binding = headerRow.getBinding();

                binding.setItemModel((TasksHeaderItem) tasksItemModel);
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
     * Provides a {@link RecyclerView} row that displays a task with all its information.
     * <p/>
     * Subclass of {@link RecyclerView.ViewHolder}.
     */
    private static class TaskRow extends BindingRow<RowTasksBinding>
            implements TasksItem.ViewListener {

        private final Context mContext;

        /**
         * Constructs a new {@link TaskRow} and sets the click listeners.
         *
         * @param binding the binding for the view
         */
        public TaskRow(@NonNull Context context, @NonNull RowTasksBinding binding) {
            super(binding);

            mContext = context;
        }

        @Override
        public String buildIdentitiesString(@NonNull List<Identity> identities) {
            final Identity identityResponsible = identities.get(0);
            String identitiesFormatted = "";
            if (identities.size() > 1) {
                final StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(mContext.getString(R.string.task_users_involved_next)).append(" ");
                for (Identity identity : identities) {
                    if (!Objects.equals(identity, identityResponsible)) {
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
        public String buildDeadlineString(@StringRes int deadline, Object... args) {
            return mContext.getString(deadline, args);
        }
    }
}
