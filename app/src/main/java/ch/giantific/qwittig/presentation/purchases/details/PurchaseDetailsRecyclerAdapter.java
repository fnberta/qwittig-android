/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.details;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import ch.giantific.qwittig.databinding.RowPurchaseDetailsArticleBinding;
import ch.giantific.qwittig.presentation.common.adapters.BaseRecyclerAdapter;
import ch.giantific.qwittig.presentation.common.adapters.rows.BindingRow;
import ch.giantific.qwittig.presentation.purchases.details.itemmodels.PurchaseDetailsArticleItemModel;

/**
 * Handles the display of the detail view of a purchase including the different headers,
 * the users involved, the items and the total value.
 * <p>
 * Subclass of {@link RecyclerView.Adapter}.
 */
public class PurchaseDetailsRecyclerAdapter extends BaseRecyclerAdapter<BindingRow<RowPurchaseDetailsArticleBinding>> {

    private final PurchaseDetailsViewModel viewModel;

    public PurchaseDetailsRecyclerAdapter(@NonNull PurchaseDetailsViewModel viewModel) {
        super();

        this.viewModel = viewModel;
    }

    @Override
    public BindingRow<RowPurchaseDetailsArticleBinding> onCreateViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final RowPurchaseDetailsArticleBinding binding =
                RowPurchaseDetailsArticleBinding.inflate(inflater, parent, false);
        return new BindingRow<>(binding);
    }

    @Override
    public void onBindViewHolder(BindingRow<RowPurchaseDetailsArticleBinding> holder, int position) {
        final RowPurchaseDetailsArticleBinding binding = holder.getBinding();
        final PurchaseDetailsArticleItemModel itemModel = viewModel.getItemAtPosition(position);

        binding.setItemModel(itemModel);
        binding.executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return viewModel.getItemCount();
    }
}
