/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.details;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import ch.giantific.qwittig.databinding.RowPurchaseDetailsArticleBinding;
import ch.giantific.qwittig.presentation.common.listadapters.rows.BindingRow;
import ch.giantific.qwittig.presentation.purchases.details.viewmodels.items.PurchaseDetailsArticleItemViewModel;

/**
 * Handles the display of the detail view of a purchase including the different headers,
 * the users involved, the items and the total value.
 * <p>
 * Subclass of {@link RecyclerView.Adapter}.
 */
public class PurchaseDetailsRecyclerAdapter extends RecyclerView.Adapter<BindingRow<RowPurchaseDetailsArticleBinding>> {

    private final List<PurchaseDetailsArticleItemViewModel> items;

    public PurchaseDetailsRecyclerAdapter() {
        super();

        items = new ArrayList<>();
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
        final PurchaseDetailsArticleItemViewModel viewModel = items.get(position);

        binding.setViewModel(viewModel);
        binding.executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void addItem(@NonNull PurchaseDetailsArticleItemViewModel item) {
        items.add(item);
    }

    public void clearItems() {
        items.clear();
    }
}
