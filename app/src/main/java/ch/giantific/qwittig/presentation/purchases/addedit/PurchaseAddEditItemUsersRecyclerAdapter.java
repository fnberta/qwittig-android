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

    private final PurchaseAddEditViewModel mViewModel;
    private final List<PurchaseAddEditItemIdentity> mUsers;

    public PurchaseAddEditItemUsersRecyclerAdapter(@NonNull PurchaseAddEditViewModel viewModel,
                                                   @NonNull List<PurchaseAddEditItemIdentity> users) {
        mViewModel = viewModel;
        mUsers = users;
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
        final PurchaseAddEditItemIdentity addEditPurchaseItemUsersUser = mUsers.get(position);
        binding.setItemModel(addEditPurchaseItemUsersUser);
        binding.executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    @Override
    public void onItemRowUserClick(int position) {
        final PurchaseAddEditItemIdentity user = mUsers.get(position);
        user.setSelected(!user.isSelected());
        notifyItemChanged(position);

        // notify main view model because total and my share values need to be updated
        mViewModel.onItemRowUserClick();
    }

    @Override
    public void onItemRowUserLongClick(int position) {
        final PurchaseAddEditItemIdentity user = mUsers.get(position);
        user.setSelected(!user.isSelected());
        notifyItemChanged(position);

        mViewModel.onItemRowUserLongClick(user);
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
                    listener.onItemRowUserClick(getAdapterPosition());
                }
            });
            root.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    listener.onItemRowUserLongClick(getAdapterPosition());
                    return true;
                }
            });
        }
    }
}
