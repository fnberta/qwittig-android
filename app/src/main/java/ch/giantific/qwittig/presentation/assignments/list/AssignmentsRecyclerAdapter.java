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
import ch.giantific.qwittig.presentation.assignments.list.viewmodels.items.AssignmentHeaderViewModel;
import ch.giantific.qwittig.presentation.assignments.list.viewmodels.items.AssignmentItemViewModel;
import ch.giantific.qwittig.presentation.assignments.list.viewmodels.items.BaseAssignmentItemViewModel;
import ch.giantific.qwittig.presentation.assignments.list.viewmodels.items.BaseAssignmentItemViewModel.ViewType;
import ch.giantific.qwittig.presentation.common.listadapters.BaseRecyclerAdapter;
import ch.giantific.qwittig.presentation.common.listadapters.rows.BindingRow;

/**
 * Handles the display of recent tasks assigned to users in a group.
 * <p/>
 * Subclass of {@link RecyclerView.Adapter}.
 */
public class AssignmentsRecyclerAdapter extends BaseRecyclerAdapter {

    private final AssignmentsContract.Presenter presenter;

    /**
     * Constructs a new {@link AssignmentsRecyclerAdapter}.
     *
     * @param presenter the main view's model
     */
    public AssignmentsRecyclerAdapter(@NonNull AssignmentsContract.Presenter presenter) {
        super();

        this.presenter = presenter;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, @ViewType int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case ViewType.ASSIGNMENT: {
                final RowAssignmentsBinding binding = RowAssignmentsBinding.inflate(inflater, parent, false);
                return new BindingRow<>(binding);
            }
            case ViewType.HEADER_MY:
                // fall through
            case ViewType.HEADER_GROUP: {
                final RowGenericHeaderBinding binding = RowGenericHeaderBinding.inflate(inflater, parent, false);
                return new BindingRow<>(binding);
            }
            default:
                return super.onCreateViewHolder(parent, viewType);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, @ViewType int position) {
        final BaseAssignmentItemViewModel viewModel = presenter.getItemAtPosition(position);
        final int viewType = getItemViewType(position);
        switch (viewType) {
            case ViewType.ASSIGNMENT: {
                final BindingRow<RowAssignmentsBinding> assignmentRow =
                        (BindingRow<RowAssignmentsBinding>) viewHolder;
                final RowAssignmentsBinding binding = assignmentRow.getBinding();

                binding.setViewModel((AssignmentItemViewModel) viewModel);
                binding.setPresenter(presenter);
                binding.executePendingBindings();

                break;
            }
            case ViewType.HEADER_MY:
                // fall through
            case ViewType.HEADER_GROUP: {
                final BindingRow<RowGenericHeaderBinding> headerRow =
                        (BindingRow<RowGenericHeaderBinding>) viewHolder;
                final RowGenericHeaderBinding binding = headerRow.getBinding();

                binding.setViewModel((AssignmentHeaderViewModel) viewModel);
                binding.executePendingBindings();

                break;
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        return presenter.getItemAtPosition(position).getViewType();
    }

    @Override
    public int getItemCount() {
        return presenter.getItemCount();
    }
}
