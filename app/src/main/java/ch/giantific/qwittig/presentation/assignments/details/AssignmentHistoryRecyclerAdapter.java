/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.assignments.details;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import ch.giantific.qwittig.databinding.RowGenericHeaderBinding;
import ch.giantific.qwittig.databinding.RowAssignmentDetailsHistoryBinding;
import ch.giantific.qwittig.presentation.assignments.details.itemmodels.AssignmentDetailsHeaderItem;
import ch.giantific.qwittig.presentation.assignments.details.itemmodels.AssignmentDetailsHistoryItem;
import ch.giantific.qwittig.presentation.common.adapters.BaseRecyclerAdapter;
import ch.giantific.qwittig.presentation.common.adapters.rows.BindingRow;
import ch.giantific.qwittig.presentation.assignments.details.itemmodels.AssignmentDetailsItemModel;
import ch.giantific.qwittig.presentation.assignments.details.itemmodels.AssignmentDetailsItemModel.Type;


/**
 * Handles the display of users who already completed a task by showing the user and the date
 * he/she completed the task on.
 * <p/>
 * Subclass of {@link RecyclerView.Adapter}.
 */
public class AssignmentHistoryRecyclerAdapter extends BaseRecyclerAdapter<RecyclerView.ViewHolder> {

    private final AssignmentDetailsViewModel viewModel;

    /**
     * Constructs a new {@link AssignmentHistoryRecyclerAdapter}.
     *
     * @param viewModel the view model that provides access to the data
     */
    public AssignmentHistoryRecyclerAdapter(@NonNull AssignmentDetailsViewModel viewModel) {
        super();

        this.viewModel = viewModel;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, @Type int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case Type.HISTORY: {
                RowAssignmentDetailsHistoryBinding binding =
                        RowAssignmentDetailsHistoryBinding.inflate(inflater, parent, false);
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
        return viewModel.getItemCount();
    }

    @Override
    public int getItemViewType(int position) {
        return viewModel.getItemViewType(position);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        final AssignmentDetailsItemModel detailsItem = viewModel.getItemAtPosition(position);
        final int viewType = getItemViewType(position);
        switch (viewType) {
            case Type.HISTORY: {
                final BindingRow<RowAssignmentDetailsHistoryBinding> historyRow =
                        (BindingRow<RowAssignmentDetailsHistoryBinding>) viewHolder;
                final RowAssignmentDetailsHistoryBinding binding = historyRow.getBinding();

                binding.setItemModel((AssignmentDetailsHistoryItem) detailsItem);
                binding.executePendingBindings();
                break;
            }
            case Type.HEADER: {
                final BindingRow<RowGenericHeaderBinding> headerRow = (BindingRow<RowGenericHeaderBinding>) viewHolder;
                final RowGenericHeaderBinding binding = headerRow.getBinding();

                binding.setItemModel((AssignmentDetailsHeaderItem) detailsItem);
                binding.executePendingBindings();
                break;
            }
        }
    }
}
