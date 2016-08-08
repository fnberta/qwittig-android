/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.finance.unpaid;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import ch.giantific.qwittig.databinding.RowCompUnpaidCreditBinding;
import ch.giantific.qwittig.databinding.RowCompUnpaidDebtBinding;
import ch.giantific.qwittig.presentation.common.adapters.BaseRecyclerAdapter;
import ch.giantific.qwittig.presentation.common.adapters.rows.BindingRow;
import ch.giantific.qwittig.presentation.finance.unpaid.itemmodels.CompsUnpaidItemModel;
import ch.giantific.qwittig.presentation.finance.unpaid.itemmodels.CompsUnpaidItemModel.ViewType;


/**
 * Handles the display of different unpaid compensations.
 * <p/>
 * Subclass of {@link RecyclerView.Adapter}.
 */
public class CompsUnpaidRecyclerAdapter extends BaseRecyclerAdapter {

    private final CompsUnpaidViewModel mViewModel;

    /**
     * Constructs a new {@link CompsUnpaidRecyclerAdapter}.
     *
     * @param viewModel the view's model
     */
    public CompsUnpaidRecyclerAdapter(@NonNull CompsUnpaidViewModel viewModel) {
        super();

        mViewModel = viewModel;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case ViewType.CREDIT: {
                final RowCompUnpaidCreditBinding binding =
                        RowCompUnpaidCreditBinding.inflate(inflater, parent, false);
                return new BindingRow<>(binding);
            }
            case ViewType.DEBT: {
                final RowCompUnpaidDebtBinding binding =
                        RowCompUnpaidDebtBinding.inflate(inflater, parent, false);
                return new BindingRow<>(binding);
            }
            default:
                return super.onCreateViewHolder(parent, viewType);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        final CompsUnpaidItemModel itemModel = mViewModel.getItemAtPosition(position);
        final int viewType = getItemViewType(position);
        switch (viewType) {
            case ViewType.CREDIT: {
                final BindingRow<RowCompUnpaidCreditBinding> row = (BindingRow<RowCompUnpaidCreditBinding>) holder;
                final RowCompUnpaidCreditBinding binding = row.getBinding();

                binding.setItemModel(itemModel);
                binding.setViewModel(mViewModel);
                binding.executePendingBindings();
                break;
            }
            case ViewType.DEBT: {
                final BindingRow<RowCompUnpaidDebtBinding> row =
                        (BindingRow<RowCompUnpaidDebtBinding>) holder;
                final RowCompUnpaidDebtBinding binding = row.getBinding();

                binding.setItemModel(itemModel);
                binding.executePendingBindings();
                break;
            }
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
