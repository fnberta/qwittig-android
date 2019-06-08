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

import ch.giantific.qwittig.databinding.RowPurchaseDetailsIdentityBinding;
import ch.giantific.qwittig.presentation.common.listadapters.rows.BindingRow;
import ch.giantific.qwittig.presentation.purchases.details.viewmodels.items.PurchaseDetailsIdentityItemViewModel;

/**
 * Provides a {@link RecyclerView} adapter that manages the list of identities involved in a
 * purchase.
 * <p/>
 * Subclass of {@link RecyclerView.Adapter}.
 */
public class PurchaseDetailsIdentitiesRecyclerAdapter extends RecyclerView.Adapter<BindingRow<RowPurchaseDetailsIdentityBinding>> {

    private final List<PurchaseDetailsIdentityItemViewModel> items;

    public PurchaseDetailsIdentitiesRecyclerAdapter() {
        super();

        items = new ArrayList<>();
    }

    @Override
    public BindingRow<RowPurchaseDetailsIdentityBinding> onCreateViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final RowPurchaseDetailsIdentityBinding binding =
                RowPurchaseDetailsIdentityBinding.inflate(inflater, parent, false);
        return new BindingRow<>(binding);
    }

    @Override
    public void onBindViewHolder(BindingRow<RowPurchaseDetailsIdentityBinding> holder, int position) {
        final RowPurchaseDetailsIdentityBinding binding = holder.getBinding();
        final PurchaseDetailsIdentityItemViewModel viewModel = items.get(position);

        binding.setViewModel(viewModel);
        binding.executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void addItems(@NonNull List<PurchaseDetailsIdentityItemViewModel> items) {
        this.items.addAll(items);
    }

    public void clearItems() {
        items.clear();
    }
}
