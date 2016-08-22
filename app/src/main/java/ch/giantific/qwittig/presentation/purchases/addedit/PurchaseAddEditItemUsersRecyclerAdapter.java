/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.addedit;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import ch.giantific.qwittig.databinding.RowPurchaseAddItemUsersUserBinding;
import ch.giantific.qwittig.presentation.common.adapters.BaseRecyclerAdapter;
import ch.giantific.qwittig.presentation.common.adapters.rows.BindingRow;
import ch.giantific.qwittig.presentation.purchases.addedit.itemmodels.PurchaseAddEditItemIdentity;

/**
 * Provides an adapter for a {@link RecyclerView} showing a list of users.
 * <p/>
 * Subclass of {@link RecyclerView.Adapter}.
 */
public class PurchaseAddEditItemUsersRecyclerAdapter extends BaseRecyclerAdapter<PurchaseAddEditItemUsersRecyclerAdapter.ItemUserRow>
        implements PurchaseAddEditItemUsersClickListener {

    private final PurchaseAddEditViewModel viewModel;
    private final List<PurchaseAddEditItemIdentity> identities;

    public PurchaseAddEditItemUsersRecyclerAdapter(@NonNull PurchaseAddEditViewModel viewModel,
                                                   @NonNull List<PurchaseAddEditItemIdentity> identities) {
        this.viewModel = viewModel;
        this.identities = identities;
    }

    @Override
    public ItemUserRow onCreateViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final RowPurchaseAddItemUsersUserBinding binding =
                RowPurchaseAddItemUsersUserBinding.inflate(inflater, parent, false);
        return new ItemUserRow(binding, this);
    }

    @Override
    public void onBindViewHolder(ItemUserRow holder, int position) {
        final RowPurchaseAddItemUsersUserBinding binding = holder.getBinding();
        final PurchaseAddEditItemIdentity addEditPurchaseItemUsersUser = identities.get(position);
        binding.setItemModel(addEditPurchaseItemUsersUser);
        binding.executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return identities.size();
    }

    @Override
    public void onItemRowIdentityClick(int position) {
        final PurchaseAddEditItemIdentity user = identities.get(position);
        user.setSelected(!user.isSelected());
        notifyItemChanged(position);

        // notify main view model because total and my share values need to be updated
        viewModel.onItemRowUserClick();
    }

    @Override
    public void onItemRowIdentityLongClick(int position) {
        final PurchaseAddEditItemIdentity identity = identities.get(position);
        identity.setSelected(!identity.isSelected());
        notifyItemChanged(position);

        viewModel.onItemRowUserLongClick(identity);
    }

    /**
     * Provides a {@link RecyclerView} row showing a user.
     * <p/>
     * Subclass of {@link BindingRow}.
     */
    public static class ItemUserRow extends BindingRow<RowPurchaseAddItemUsersUserBinding> {

        public ItemUserRow(@NonNull RowPurchaseAddItemUsersUserBinding binding,
                           @NonNull final PurchaseAddEditItemUsersClickListener listener) {
            super(binding);

            final View root = binding.getRoot();
            root.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemRowIdentityClick(getAdapterPosition());
                }
            });
            root.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    listener.onItemRowIdentityLongClick(getAdapterPosition());
                    return true;
                }
            });
        }
    }
}
