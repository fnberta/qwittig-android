/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.tasks.details;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import ch.giantific.qwittig.databinding.RowGenericHeaderBinding;
import ch.giantific.qwittig.databinding.RowTaskDetailsHistoryBinding;
import ch.giantific.qwittig.presentation.common.adapters.rows.BindingRow;
import ch.giantific.qwittig.presentation.tasks.details.items.TaskDetailsBaseItem;
import ch.giantific.qwittig.presentation.tasks.details.items.TaskDetailsBaseItem.Type;
import ch.giantific.qwittig.presentation.tasks.details.items.TaskDetailsHeaderItem;
import ch.giantific.qwittig.presentation.tasks.details.items.TaskDetailsHistoryItem;


/**
 * Handles the display of users who already completed a task by showing the user and the date
 * he/she completed the task on.
 * <p/>
 * Subclass of {@link RecyclerView.Adapter}.
 */
public class TaskHistoryRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final TaskDetailsViewModel mViewModel;

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
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, @Type int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case Type.HISTORY: {
                RowTaskDetailsHistoryBinding binding =
                        RowTaskDetailsHistoryBinding.inflate(inflater, parent, false);
                return new BindingRow<>(binding);
            }
            case Type.HEADER: {
                RowGenericHeaderBinding binding =
                        RowGenericHeaderBinding.inflate(inflater, parent, false);
                return new BindingRow<>(binding);
            }
            default:
                throw new RuntimeException("there is no type that matches the type " + viewType +
                        " + make sure your using types correctly");
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

    @SuppressWarnings("unchecked")
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        final TaskDetailsBaseItem detailsItem = mViewModel.getItemAtPosition(position);
        final int viewType = getItemViewType(position);
        switch (viewType) {
            case Type.HISTORY: {
                final BindingRow<RowTaskDetailsHistoryBinding> historyRow =
                        (BindingRow<RowTaskDetailsHistoryBinding>) viewHolder;
                final RowTaskDetailsHistoryBinding binding = historyRow.getBinding();

                binding.setItem((TaskDetailsHistoryItem) detailsItem);
                binding.executePendingBindings();
                break;
            }
            case Type.HEADER: {
                final BindingRow<RowGenericHeaderBinding> headerRow = (BindingRow<RowGenericHeaderBinding>) viewHolder;
                final RowGenericHeaderBinding binding = headerRow.getBinding();

                binding.setViewModel((TaskDetailsHeaderItem) detailsItem);
                binding.executePendingBindings();
                break;
            }
        }
    }
}
