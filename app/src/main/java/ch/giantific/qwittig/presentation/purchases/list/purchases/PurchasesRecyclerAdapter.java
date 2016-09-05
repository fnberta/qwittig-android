/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.list.purchases;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import ch.giantific.qwittig.databinding.RowPurchasesBinding;
import ch.giantific.qwittig.presentation.common.adapters.BaseRecyclerAdapter;
import ch.giantific.qwittig.presentation.common.adapters.rows.BindingRow;
import ch.giantific.qwittig.presentation.purchases.list.purchases.itemmodels.PurchaseItemModel;

/**
 * Handles the display of recent purchases.
 * <p/>
 * Subclass of {@link RecyclerView.Adapter}.
 */
public class PurchasesRecyclerAdapter extends BaseRecyclerAdapter<BindingRow<RowPurchasesBinding>> {

    private final PurchasesViewModel viewModel;

    /**
     * Constructs a new {@link PurchasesRecyclerAdapter}.
     *
     * @param viewModel the view's model
     */
    public PurchasesRecyclerAdapter(@NonNull PurchasesViewModel viewModel) {
        super();

        this.viewModel = viewModel;
    }

    @NonNull
    @Override
    public BindingRow<RowPurchasesBinding> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final RowPurchasesBinding binding = RowPurchasesBinding.inflate(inflater, parent, false);
        return new BindingRow<>(binding);
    }

    @Override
    public void onBindViewHolder(BindingRow<RowPurchasesBinding> holder, int position) {
        final RowPurchasesBinding binding = holder.getBinding();
        final PurchaseItemModel itemModel = viewModel.getItemAtPosition(position);
        binding.setItemModel(itemModel);
        binding.setViewModel(viewModel);
        binding.executePendingBindings();
    }

    @Override
    public int getItemViewType(int position) {
        return viewModel.getItemViewType(position);
    }

    @Override
    public int getItemCount() {
        return viewModel.getItemCount();
    }
}
