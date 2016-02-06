/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.finance;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ch.giantific.qwittig.databinding.RowCompensationsPaidBinding;
import ch.giantific.qwittig.domain.models.parse.Compensation;
import ch.giantific.qwittig.presentation.common.adapters.rows.BindingRow;
import ch.giantific.qwittig.presentation.common.adapters.rows.ProgressRow;


/**
 * Handles the display of different paid compensations.
 * <p/>
 * Subclass of {@link RecyclerView.Adapter}.
 */
public class CompensationsPaidRecyclerAdapter extends RecyclerView.Adapter {

    private FinanceCompsPaidViewModel mViewModel;

    /**
     * Constructs a new {@link CompensationsPaidRecyclerAdapter}.
     *
     * @param viewModel the view's model
     */
    public CompensationsPaidRecyclerAdapter(@NonNull FinanceCompsPaidViewModel viewModel) {
        super();

        mViewModel = viewModel;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case FinanceCompsPaidViewModel.TYPE_ITEM: {
                final RowCompensationsPaidBinding binding =
                        RowCompensationsPaidBinding.inflate(inflater, parent, false);
                return new BindingRow<>(binding);
            }
            case FinanceCompsPaidViewModel.TYPE_PROGRESS: {
                View view = inflater
                        .inflate(ProgressRow.VIEW_RESOURCE, parent, false);
                return new ProgressRow(view);
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
            case FinanceCompsPaidViewModel.TYPE_ITEM:
                final BindingRow<RowCompensationsPaidBinding> row = (BindingRow<RowCompensationsPaidBinding>) viewHolder;
                final RowCompensationsPaidBinding binding = row.getBinding();
                final Compensation compensation = mViewModel.getItemAtPosition(position);

                CompPaidRowViewModel viewModel = binding.getViewModel();
                if (viewModel == null) {
                    viewModel = new CompPaidRowViewModel(compensation, mViewModel.getCurrentIdentity());
                    binding.setViewModel(viewModel);
                } else {
                    viewModel.updateCompInfo(compensation);
                }

                binding.executePendingBindings();
                break;
            case FinanceCompsPaidViewModel.TYPE_PROGRESS:
                // do nothing
                break;
        }
    }

    @Override
    public int getItemViewType(int position) {
        return mViewModel.getItemViewType(position);
    }

    @Override
    public int getItemCount() {
        return mViewModel.getItemCount();
    }
}
