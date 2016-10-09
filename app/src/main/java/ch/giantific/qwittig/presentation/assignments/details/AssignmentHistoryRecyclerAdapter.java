/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.assignments.details;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import ch.giantific.qwittig.databinding.RowAssignmentDetailsHistoryBinding;
import ch.giantific.qwittig.databinding.RowGenericHeaderBinding;
import ch.giantific.qwittig.presentation.assignments.details.viewmodels.items.AssignmentDetailsHeaderItemViewModel;
import ch.giantific.qwittig.presentation.assignments.details.viewmodels.items.AssignmentDetailsHistoryItemViewModel;
import ch.giantific.qwittig.presentation.assignments.details.viewmodels.items.BaseAssignmentDetailsItemViewModel;
import ch.giantific.qwittig.presentation.assignments.details.viewmodels.items.BaseAssignmentDetailsItemViewModel.ViewType;
import ch.giantific.qwittig.presentation.common.listadapters.rows.BindingRow;


/**
 * Handles the display of users who already completed a task by showing the user and the date
 * he/she completed the task on.
 * <p/>
 * Subclass of {@link RecyclerView.Adapter}.
 */
public class AssignmentHistoryRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<BaseAssignmentDetailsItemViewModel> items;

    /**
     * Constructs a new {@link AssignmentHistoryRecyclerAdapter}.
     */
    public AssignmentHistoryRecyclerAdapter() {
        super();

        items = new ArrayList<>();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, @ViewType int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case ViewType.HISTORY: {
                RowAssignmentDetailsHistoryBinding binding =
                        RowAssignmentDetailsHistoryBinding.inflate(inflater, parent, false);
                return new BindingRow<>(binding);
            }
            case ViewType.HEADER: {
                RowGenericHeaderBinding binding =
                        RowGenericHeaderBinding.inflate(inflater, parent, false);
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
        final BaseAssignmentDetailsItemViewModel viewModel = items.get(position);
        final int viewType = getItemViewType(position);
        switch (viewType) {
            case ViewType.HISTORY: {
                final BindingRow<RowAssignmentDetailsHistoryBinding> historyRow =
                        (BindingRow<RowAssignmentDetailsHistoryBinding>) viewHolder;
                final RowAssignmentDetailsHistoryBinding binding = historyRow.getBinding();

                binding.setViewModel((AssignmentDetailsHistoryItemViewModel) viewModel);
                binding.executePendingBindings();
                break;
            }
            case ViewType.HEADER: {
                final BindingRow<RowGenericHeaderBinding> headerRow = (BindingRow<RowGenericHeaderBinding>) viewHolder;
                final RowGenericHeaderBinding binding = headerRow.getBinding();

                binding.setViewModel((AssignmentDetailsHeaderItemViewModel) viewModel);
                binding.executePendingBindings();
                break;
            }
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).getViewType();
    }

    public void addItem(@NonNull AssignmentDetailsHeaderItemViewModel itemViewModel) {
        items.add(itemViewModel);
    }

    public void addItems(@NonNull List<AssignmentDetailsHistoryItemViewModel> itemViewModels) {
        items.addAll(itemViewModels);
    }

    public void clearItems() {
        items.clear();
    }
}
