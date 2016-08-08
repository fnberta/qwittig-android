/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.details;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import ch.giantific.qwittig.databinding.RowPurchaseDetailsItemBinding;
import ch.giantific.qwittig.presentation.common.adapters.BaseRecyclerAdapter;
import ch.giantific.qwittig.presentation.common.adapters.rows.BindingRow;
import ch.giantific.qwittig.presentation.purchases.details.itemmodels.PurchaseDetailsItemModel;

/**
 * Handles the display of the detail view of a purchase including the different headers,
 * the users involved, the items and the total value.
 * <p>
 * Subclass of {@link RecyclerView.Adapter}.
 */
public class PurchaseDetailsRecyclerAdapter extends BaseRecyclerAdapter<BindingRow<RowPurchaseDetailsItemBinding>> {

    private final PurchaseDetailsViewModel mViewModel;

    public PurchaseDetailsRecyclerAdapter(@NonNull PurchaseDetailsViewModel viewModel) {
        super();

        mViewModel = viewModel;
    }

    @Override
    public BindingRow<RowPurchaseDetailsItemBinding> onCreateViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final RowPurchaseDetailsItemBinding binding =
                RowPurchaseDetailsItemBinding.inflate(inflater, parent, false);
        return new BindingRow<>(binding);
    }

    @Override
    public void onBindViewHolder(BindingRow<RowPurchaseDetailsItemBinding> holder, int position) {
        final RowPurchaseDetailsItemBinding binding = holder.getBinding();
        final PurchaseDetailsItemModel itemModel = mViewModel.getItemAtPosition(position);

        binding.setItemModel(itemModel);
        binding.executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return mViewModel.getItemCount();
    }
}
