/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.list.purchases;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ch.giantific.qwittig.databinding.RowPurchasesBinding;
import ch.giantific.qwittig.domain.models.Purchase;
import ch.giantific.qwittig.presentation.common.adapters.BaseRecyclerAdapter;
import ch.giantific.qwittig.presentation.common.adapters.rows.BindingRow;
import ch.giantific.qwittig.presentation.common.adapters.rows.ProgressRow;
import ch.giantific.qwittig.presentation.purchases.list.purchases.itemmodels.PurchasesItemModel;

/**
 * Handles the display of recent purchases.
 * <p/>
 * Subclass of {@link RecyclerView.Adapter}.
 */
public class PurchasesRecyclerAdapter extends BaseRecyclerAdapter {

    private final PurchasesViewModel mViewModel;

    /**
     * Constructs a new {@link PurchasesRecyclerAdapter}.
     *
     * @param viewModel the view's model
     */
    public PurchasesRecyclerAdapter(@NonNull PurchasesViewModel viewModel) {
        super();

        mViewModel = viewModel;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case PurchasesViewModel.TYPE_ITEM: {
                final RowPurchasesBinding binding = RowPurchasesBinding.inflate(inflater, parent, false);
                return new BindingRow<>(binding);
            }
            case PurchasesViewModel.TYPE_PROGRESS: {
                View view = inflater
                        .inflate(ProgressRow.VIEW_RESOURCE, parent, false);
                return new ProgressRow(view);
            }
            default:
                return super.onCreateViewHolder(parent, viewType);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        int viewType = getItemViewType(position);
        switch (viewType) {
            case PurchasesViewModel.TYPE_ITEM: {
                final BindingRow<RowPurchasesBinding> row =
                        (BindingRow<RowPurchasesBinding>) viewHolder;
                final RowPurchasesBinding binding = row.getBinding();
                final Purchase purchase = mViewModel.getItemAtPosition(position);

                PurchasesItemModel itemModel = binding.getItemModel();
                if (itemModel == null) {
                    itemModel = new PurchasesItemModel(purchase, mViewModel.getCurrentIdentity());
                    binding.setItemModel(itemModel);
                    binding.setViewModel(mViewModel);
                } else {
                    itemModel.updatePurchaseInfo(purchase);
                }

                binding.executePendingBindings();
                break;
            }
            case PurchasesViewModel.TYPE_PROGRESS:
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
