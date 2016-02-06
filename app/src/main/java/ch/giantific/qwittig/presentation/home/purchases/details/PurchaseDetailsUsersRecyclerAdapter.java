/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home.purchases.details;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.List;

import ch.giantific.qwittig.databinding.RowPurchaseDetailsUsersUserBinding;
import ch.giantific.qwittig.domain.models.parse.Identity;
import ch.giantific.qwittig.presentation.common.adapters.rows.BindingRow;

/**
 * Created by fabio on 30.01.16.
 */
public class PurchaseDetailsUsersRecyclerAdapter extends RecyclerView.Adapter<BindingRow<RowPurchaseDetailsUsersUserBinding>> {

    private PurchaseDetailsViewModel mViewModel;
    private List<Identity> mIdentities;

    public PurchaseDetailsUsersRecyclerAdapter(@NonNull PurchaseDetailsViewModel viewModel,
                                               @NonNull List<Identity> identities) {
        mViewModel = viewModel;
        mIdentities = identities;
    }

    @Override
    public BindingRow<RowPurchaseDetailsUsersUserBinding> onCreateViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final RowPurchaseDetailsUsersUserBinding binding =
                RowPurchaseDetailsUsersUserBinding.inflate(inflater, parent, false);
        return new BindingRow<>(binding);
    }

    @Override
    public void onBindViewHolder(BindingRow<RowPurchaseDetailsUsersUserBinding> holder, int position) {
        final Identity identity = mIdentities.get(position);
        final RowPurchaseDetailsUsersUserBinding binding = holder.getBinding();
        PurchaseDetailsUsersUserRowViewModel viewModel = binding.getViewModel();

        final boolean boldName = mViewModel.getPurchaseBuyer().getObjectId().equals(identity.getObjectId());
        if (viewModel == null) {
            viewModel = new PurchaseDetailsUsersUserRowViewModel(identity, boldName);
            binding.setViewModel(viewModel);
        } else {
            viewModel.updateIdentity(identity, boldName);
        }

        binding.executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return mIdentities.size();
    }
}
