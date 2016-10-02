/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.details;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import ch.giantific.qwittig.databinding.RowPurchaseDetailsIdentityBinding;
import ch.giantific.qwittig.presentation.common.listadapters.BaseRecyclerAdapter;
import ch.giantific.qwittig.presentation.common.listadapters.rows.BindingRow;
import ch.giantific.qwittig.presentation.purchases.details.viewmodels.items.PurchaseDetailsIdentityItemViewModel;

/**
 * Provides a {@link RecyclerView} adapter that manages the list of identities involved in a
 * purchase.
 * <p/>
 * Subclass of {@link RecyclerView.Adapter}.
 */
public class PurchaseDetailsIdentitiesRecyclerAdapter extends BaseRecyclerAdapter<BindingRow<RowPurchaseDetailsIdentityBinding>> {

    private final PurchaseDetailsContract.Presenter presenter;

    public PurchaseDetailsIdentitiesRecyclerAdapter(@NonNull PurchaseDetailsContract.Presenter presenter) {
        super();

        this.presenter = presenter;
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
        final PurchaseDetailsIdentityItemViewModel viewModel = presenter.getIdentityAtPosition(position);

        binding.setViewModel(viewModel);
        binding.executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return presenter.getIdentityCount();
    }
}
