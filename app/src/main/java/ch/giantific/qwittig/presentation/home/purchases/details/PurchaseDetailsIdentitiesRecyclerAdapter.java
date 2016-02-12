/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home.purchases.details;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.List;

import ch.giantific.qwittig.databinding.RowPurchaseDetailsIdentitiesIdentityBinding;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.presentation.common.adapters.rows.BindingRow;
import ch.giantific.qwittig.presentation.home.purchases.details.items.IdentitiesItemIdentity;

/**
 * Created by fabio on 30.01.16.
 */
public class PurchaseDetailsIdentitiesRecyclerAdapter extends RecyclerView.Adapter<BindingRow<RowPurchaseDetailsIdentitiesIdentityBinding>> {

    private PurchaseDetailsViewModel mViewModel;
    private List<Identity> mIdentities;

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
        IdentitiesItemIdentity item = binding.getItem();

        final boolean boldName = mViewModel.getPurchaseBuyer().getObjectId().equals(identity.getObjectId());
        if (item == null) {
            item = new IdentitiesItemIdentity(identity, boldName);
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
