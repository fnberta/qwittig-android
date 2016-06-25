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
import ch.giantific.qwittig.presentation.purchases.addedit.itemmodels.PurchaseAddEditItemUsersUser;

/**
 * Provides an adapter for a {@link RecyclerView} showing a list of users.
 * <p/>
 * Subclass of {@link RecyclerView.Adapter}.
 */
public class PurchaseAddEditItemUsersRecyclerAdapter extends BaseRecyclerAdapter<PurchaseAddEditItemUsersRecyclerAdapter.ItemUserRow>
        implements PurchaseAddEditItemUsersClickListener {

    private final PurchaseAddEditViewModel mViewModel;
    private final List<PurchaseAddEditItemUsersUser> mUsers;

    public PurchaseAddEditItemUsersRecyclerAdapter(@NonNull PurchaseAddEditViewModel viewModel,
                                                   @NonNull List<PurchaseAddEditItemUsersUser> users) {
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
        final PurchaseAddEditItemUsersUser addEditPurchaseItemUsersUser = mUsers.get(position);
        binding.setItemModel(addEditPurchaseItemUsersUser);
        binding.executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    @Override
    public void onItemRowUserClick(int position) {
        final PurchaseAddEditItemUsersUser user = mUsers.get(position);
        if (user.isSelected()) {
            boolean anotherUserSelected = false;
            for (PurchaseAddEditItemUsersUser addEditPurchaseItemUsersUser : mUsers) {
                if (addEditPurchaseItemUsersUser != user && addEditPurchaseItemUsersUser.isSelected()) {
                    anotherUserSelected = true;
                    break;
                }
            }

            if (anotherUserSelected) {
                user.setSelected(false);
                notifyItemChanged(position);
            } else {
                mViewModel.onTooFewUsersSelected();
            }
        } else {
            user.setSelected(true);
            notifyItemChanged(position);
        }

        // notify main view model because total and my share values need to be updated
        mViewModel.onItemRowUserClick(position);
    }

    @Override
    public void onTooFewUsersSelected() {
        // nothing to do here
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

            binding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemRowUserClick(getAdapterPosition());
                }
            });
        }
    }
}
