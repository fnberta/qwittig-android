/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.presentation.ui.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.databinding.RowHeaderBinding;
import ch.giantific.qwittig.databinding.RowTaskDetailsHistoryBinding;
import ch.giantific.qwittig.domain.models.TaskHistory;
import ch.giantific.qwittig.presentation.ui.adapters.rows.BindingRow;
import ch.giantific.qwittig.presentation.viewmodels.TaskDetailsViewModel;
import ch.giantific.qwittig.presentation.viewmodels.rows.HeaderRowViewModel;


/**
 * Handles the display of users who already completed a task by showing the user and the date
 * he/she completed the task on.
 * <p/>
 * Subclass of {@link RecyclerView.Adapter}.
 */
public class TaskHistoryRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private TaskDetailsViewModel mViewModel;

    /**
     * Constructs a new {@link TaskHistoryRecyclerAdapter}.
     *
     * @param viewModel the view model that provides access to the data
     */
    public TaskHistoryRecyclerAdapter(@NonNull TaskDetailsViewModel viewModel) {
        super();

        mViewModel = viewModel;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case TaskDetailsViewModel.TYPE_ITEM: {
                RowTaskDetailsHistoryBinding binding =
                        RowTaskDetailsHistoryBinding.inflate(inflater, parent, false);
                return new BindingRow<>(binding);
            }
            case TaskDetailsViewModel.TYPE_HEADER: {
                RowHeaderBinding binding = RowHeaderBinding.inflate(inflater, parent, false);
                return new BindingRow<>(binding);
            }
            default:
                throw new RuntimeException("there is no type that matches the type " + viewType +
                        " + make sure your using types correctly");
        }
    }

    @Override
    public int getItemViewType(int position) {
        return mViewModel.getItemViewType(position);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        int viewType = getItemViewType(position);
        switch (viewType) {
            case TaskDetailsViewModel.TYPE_ITEM: {
                final BindingRow<RowTaskDetailsHistoryBinding> historyRow =
                        (BindingRow<RowTaskDetailsHistoryBinding>) viewHolder;
                final RowTaskDetailsHistoryBinding binding = historyRow.getBinding();

                final TaskHistory taskHistory = mViewModel.getTaskHistoryForPosition(position);
                binding.setTaskHistory(taskHistory);
                binding.executePendingBindings();
                break;
            }
            case TaskDetailsViewModel.TYPE_HEADER: {
                final BindingRow<RowHeaderBinding> headerRow = (BindingRow<RowHeaderBinding>) viewHolder;
                final RowHeaderBinding binding = headerRow.getBinding();

                final HeaderRowViewModel viewModel = new HeaderRowViewModel(R.string.header_task_history);
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
}
