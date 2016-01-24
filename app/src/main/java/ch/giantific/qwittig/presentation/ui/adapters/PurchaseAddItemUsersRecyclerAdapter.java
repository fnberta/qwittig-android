/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.ui.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.List;

import ch.giantific.qwittig.databinding.RowPurchaseAddItemUsersUserBinding;
import ch.giantific.qwittig.domain.models.RowItemUser;
import ch.giantific.qwittig.presentation.ui.adapters.rows.BindingRow;

/**
 * Created by fabio on 24.01.16.
 */
public class PurchaseAddItemUsersRecyclerAdapter extends RecyclerView.Adapter<PurchaseAddItemUsersRecyclerAdapter.ItemUserRow> {

    private List<RowItemUser> mUsers;

    public PurchaseAddItemUsersRecyclerAdapter(@NonNull List<RowItemUser> users) {
        mUsers = users;
    }

    @Override
    public ItemUserRow onCreateViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final RowPurchaseAddItemUsersUserBinding binding =
                RowPurchaseAddItemUsersUserBinding.inflate(inflater, parent, false);
        return new ItemUserRow(binding);
    }

    @Override
    public void onBindViewHolder(ItemUserRow holder, int position) {
        final RowPurchaseAddItemUsersUserBinding binding = holder.getBinding();
        final RowItemUser rowItemUser = mUsers.get(position);
        binding.setRowItemUser(rowItemUser);
        binding.executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public static class ItemUserRow extends BindingRow<RowPurchaseAddItemUsersUserBinding> {

        public ItemUserRow(@NonNull RowPurchaseAddItemUsersUserBinding binding) {
            super(binding);
        }
    }
}
