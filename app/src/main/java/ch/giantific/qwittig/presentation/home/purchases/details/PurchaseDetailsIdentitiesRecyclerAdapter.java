/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home.purchases.details;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.List;
import java.util.Objects;

import ch.giantific.qwittig.databinding.RowPurchaseDetailsIdentitiesIdentityBinding;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.presentation.common.adapters.rows.BindingRow;
import ch.giantific.qwittig.presentation.home.purchases.details.items.PurchaseDetailsIdentitiesItemIdentity;

/**
 * Provides a {@link RecyclerView} adapter that manages the list of identities involved in a
 * purchase.
 * <p/>
 * Subclass of {@link RecyclerView.Adapter}.
 */
public class PurchaseDetailsIdentitiesRecyclerAdapter extends RecyclerView.Adapter<BindingRow<RowPurchaseDetailsIdentitiesIdentityBinding>> {

    private final PurchaseDetailsViewModel mViewModel;
    private final List<Identity> mIdentities;

    public PurchaseDetailsIdentitiesRecyclerAdapter(@NonNull PurchaseDetailsViewModel viewModel,
                                                    @NonNull List<Identity> identities) {
        mViewModel = viewModel;
        mIdentities = identities;
    }

    @Override
    public BindingRow<RowPurchaseDetailsIdentitiesIdentityBinding> onCreateViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final RowPurchaseDetailsIdentitiesIdentityBinding binding =
                RowPurchaseDetailsIdentitiesIdentityBinding.inflate(inflater, parent, false);
        return new BindingRow<>(binding);
    }

    @Override
    public void onBindViewHolder(BindingRow<RowPurchaseDetailsIdentitiesIdentityBinding> holder, int position) {
        final Identity identity = mIdentities.get(position);
        final RowPurchaseDetailsIdentitiesIdentityBinding binding = holder.getBinding();
        PurchaseDetailsIdentitiesItemIdentity item = binding.getItem();

        final boolean boldName = Objects.equals(mViewModel.getPurchaseBuyer().getObjectId(), identity.getObjectId());
        if (item == null) {
            item = new PurchaseDetailsIdentitiesItemIdentity(identity, boldName);
            binding.setItem(item);
        } else {
            item.updateIdentity(identity, boldName);
        }

        binding.executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return mIdentities.size();
    }
}
