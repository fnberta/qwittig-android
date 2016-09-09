/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.assignments.list;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import ch.giantific.qwittig.databinding.RowAssignmentsBinding;
import ch.giantific.qwittig.databinding.RowGenericHeaderBinding;
import ch.giantific.qwittig.presentation.assignments.list.itemmodels.AssignmentHeaderItem;
import ch.giantific.qwittig.presentation.assignments.list.itemmodels.AssignmentItem;
import ch.giantific.qwittig.presentation.assignments.list.itemmodels.AssignmentItemModel;
import ch.giantific.qwittig.presentation.assignments.list.itemmodels.AssignmentItemModel.Type;
import ch.giantific.qwittig.presentation.common.adapters.BaseRecyclerAdapter;
import ch.giantific.qwittig.presentation.common.adapters.rows.BindingRow;

/**
 * Handles the display of recent tasks assigned to users in a group.
 * <p/>
 * Subclass of {@link RecyclerView.Adapter}.
 */
public class AssignmentsRecyclerAdapter extends BaseRecyclerAdapter {

    private final AssignmentsViewModel viewModel;

    /**
     * Constructs a new {@link AssignmentsRecyclerAdapter}.
     *
     * @param viewModel the main view's model
     */
    public AssignmentsRecyclerAdapter(@NonNull AssignmentsViewModel viewModel) {
        super();

        this.viewModel = viewModel;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, @Type int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case Type.ASSIGNMENT: {
                final RowAssignmentsBinding binding = RowAssignmentsBinding.inflate(inflater, parent, false);
                return new BindingRow<>(binding);
            }
            case Type.HEADER_MY:
                // fall through
            case Type.HEADER_GROUP: {
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
        final AssignmentItemModel assignmentItemModel = viewModel.getItemAtPosition(position);
        final int viewType = getItemViewType(position);
        switch (viewType) {
            case Type.ASSIGNMENT: {
                final BindingRow<RowAssignmentsBinding> assignmentRow =
                        (BindingRow<RowAssignmentsBinding>) viewHolder;
                final RowAssignmentsBinding binding = assignmentRow.getBinding();

                binding.setItemModel((AssignmentItem) assignmentItemModel);
                binding.setViewModel(viewModel);
                binding.executePendingBindings();

                break;
            }
            case Type.HEADER_MY:
                // fall through
            case Type.HEADER_GROUP: {
                final BindingRow<RowGenericHeaderBinding> headerRow =
                        (BindingRow<RowGenericHeaderBinding>) viewHolder;
                final RowGenericHeaderBinding binding = headerRow.getBinding();

                binding.setItemModel((AssignmentHeaderItem) assignmentItemModel);
                binding.executePendingBindings();

                break;
            }
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
}
