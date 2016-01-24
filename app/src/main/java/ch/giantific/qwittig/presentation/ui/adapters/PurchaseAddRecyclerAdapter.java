/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.ui.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.databinding.RowHeaderBinding;
import ch.giantific.qwittig.databinding.RowPurchaseAddAddRowBinding;
import ch.giantific.qwittig.databinding.RowPurchaseAddDateBinding;
import ch.giantific.qwittig.databinding.RowPurchaseAddItemBinding;
import ch.giantific.qwittig.databinding.RowPurchaseAddItemUsersBinding;
import ch.giantific.qwittig.databinding.RowPurchaseAddStoreBinding;
import ch.giantific.qwittig.domain.models.RowItem;
import ch.giantific.qwittig.domain.models.RowItemUser;
import ch.giantific.qwittig.presentation.ui.adapters.rows.BindingRow;
import ch.giantific.qwittig.presentation.viewmodels.PurchaseAddViewModel;
import ch.giantific.qwittig.presentation.viewmodels.rows.HeaderRowViewModel;

/**
 * Created by fabio on 24.01.16.
 */
public class PurchaseAddRecyclerAdapter extends RecyclerView.Adapter {

    private PurchaseAddViewModel mViewModel;

    public PurchaseAddRecyclerAdapter(@NonNull PurchaseAddViewModel viewModel) {
        mViewModel = viewModel;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final Context context = parent.getContext();
        final LayoutInflater inflater = LayoutInflater.from(context);
        switch (viewType) {
            case PurchaseAddViewModel.TYPE_HEADER: {
                final RowHeaderBinding binding = RowHeaderBinding.inflate(inflater, parent, false);
                return new BindingRow<>(binding);
            }
            case PurchaseAddViewModel.TYPE_DATE: {
                final RowPurchaseAddDateBinding binding =
                        RowPurchaseAddDateBinding.inflate(inflater, parent, false);
                return new BindingRow<>(binding);
            }
            case PurchaseAddViewModel.TYPE_STORE: {
                final RowPurchaseAddStoreBinding binding =
                        RowPurchaseAddStoreBinding.inflate(inflater, parent, false);
                return new BindingRow<>(binding);
            }
            case PurchaseAddViewModel.TYPE_ITEM: {
                final RowPurchaseAddItemBinding binding =
                        RowPurchaseAddItemBinding.inflate(inflater, parent, false);
                return new ItemRow(binding, mViewModel);
            }
            case PurchaseAddViewModel.TYPE_USERS: {
                final RowPurchaseAddItemUsersBinding binding =
                        RowPurchaseAddItemUsersBinding.inflate(inflater, parent, false);
                return new ItemUsersRow(binding, context);
            }
            case PurchaseAddViewModel.TYPE_ADD_ROW: {
                final RowPurchaseAddAddRowBinding binding =
                        RowPurchaseAddAddRowBinding.inflate(inflater, parent, false);
                return new AddRow(binding, mViewModel);
            }
            case PurchaseAddViewModel.TYPE_TOTAL: {
                // TODO: add total row
                break;
            }
            default:
                throw new RuntimeException("there is no type that matches the type " + viewType +
                        " + make sure your using types correctly");
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final int type = getItemViewType(position);
        switch (type) {
            case PurchaseAddViewModel.TYPE_HEADER: {
                final BindingRow<RowHeaderBinding> row = (BindingRow<RowHeaderBinding>) holder;
                final RowHeaderBinding binding = row.getBinding();
                final HeaderRowViewModel viewModel;
                if (position == 0) {
                    viewModel = new HeaderRowViewModel(R.string.header_purchase);
                } else {
                    viewModel = new HeaderRowViewModel(R.string.header_items);
                }
                binding.setViewModel(viewModel);
                binding.executePendingBindings();
                break;
            }
            case PurchaseAddViewModel.TYPE_DATE: {
                final BindingRow<RowPurchaseAddDateBinding> row =
                        (BindingRow<RowPurchaseAddDateBinding>) holder;
                final RowPurchaseAddDateBinding binding = row.getBinding();
                binding.setViewModel(mViewModel);
                binding.executePendingBindings();
                break;
            }
            case PurchaseAddViewModel.TYPE_STORE: {
                final BindingRow<RowPurchaseAddStoreBinding> row =
                        (BindingRow<RowPurchaseAddStoreBinding>) holder;
                final RowPurchaseAddStoreBinding binding = row.getBinding();
                binding.setViewModel(mViewModel);
                binding.executePendingBindings();
                break;
            }
            case PurchaseAddViewModel.TYPE_ITEM: {
                final ItemRow row = (ItemRow) holder;
                final RowPurchaseAddItemBinding binding = row.getBinding();
                final RowItem rowItem = mViewModel.getRowItemAtPosition(position);
                binding.setRowItem(rowItem);

                binding.executePendingBindings();
                break;
            }
            case PurchaseAddViewModel.TYPE_USERS: {
                final ItemUsersRow row = (ItemUsersRow) holder;
                // get parent row and set users to adapter
                final RowItem rowItem = mViewModel.getRowItemAtPosition(position - 1);
                row.setUsers(rowItem.getUsers());
                break;
            }
            case PurchaseAddViewModel.TYPE_ADD_ROW: {
                // do nothing
                break;
            }
        }
    }

    @Override
    public int getItemCount() {
        return mViewModel.getItemCount();
    }

    @Override
    public int getItemViewType(int position) {
        return mViewModel.getItemViewType(position);
    }

    public interface AdapterListener {
        void onToggleUsersClick(int position);

        void onAddRowClick(int position);
    }

    public static class ItemRow extends BindingRow<RowPurchaseAddItemBinding> {

        public ItemRow(@NonNull RowPurchaseAddItemBinding binding,
                       @NonNull final AdapterListener listener) {
            super(binding);

            binding.ivPurchaseAddToggleUsers.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onToggleUsersClick(getAdapterPosition());
                }
            });
        }
    }

    public static class ItemUsersRow extends BindingRow<RowPurchaseAddItemUsersBinding> {

        private PurchaseAddItemUsersRecyclerAdapter mRecyclerAdapter;
        private List<RowItemUser> mUsers = new ArrayList<>();

        public ItemUsersRow(@NonNull RowPurchaseAddItemUsersBinding binding,
                            @NonNull Context context) {
            super(binding);

            binding.rvPurchaseAddItemUsers.setHasFixedSize(true);
            binding.rvPurchaseAddItemUsers.setLayoutManager(new LinearLayoutManager(context,
                    LinearLayoutManager.HORIZONTAL, false));
            mRecyclerAdapter = new PurchaseAddItemUsersRecyclerAdapter(mUsers);
            binding.rvPurchaseAddItemUsers.setAdapter(mRecyclerAdapter);
        }

        public void setUsers(@NonNull RowItemUser[] users) {
            mUsers.clear();
            mUsers.addAll(Arrays.asList(users));
            mRecyclerAdapter.notifyDataSetChanged();
        }
    }

    public static class AddRow extends BindingRow<RowPurchaseAddAddRowBinding> {

        public AddRow(@NonNull RowPurchaseAddAddRowBinding binding,
                      @NonNull final AdapterListener listener) {
            super(binding);

            binding.btItemAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onAddRowClick(getAdapterPosition());
                }
            });
        }
    }
}
