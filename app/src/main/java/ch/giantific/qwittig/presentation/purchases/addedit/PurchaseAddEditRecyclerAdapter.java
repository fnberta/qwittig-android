/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.addedit;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.databinding.RowGenericHeaderBinding;
import ch.giantific.qwittig.databinding.RowPurchaseAddAddRowBinding;
import ch.giantific.qwittig.databinding.RowPurchaseAddDateBinding;
import ch.giantific.qwittig.databinding.RowPurchaseAddItemBinding;
import ch.giantific.qwittig.databinding.RowPurchaseAddItemUsersBinding;
import ch.giantific.qwittig.databinding.RowPurchaseAddStoreBinding;
import ch.giantific.qwittig.databinding.RowPurchaseAddTotalBinding;
import ch.giantific.qwittig.presentation.common.adapters.BaseRecyclerAdapter;
import ch.giantific.qwittig.presentation.common.adapters.rows.BindingRow;
import ch.giantific.qwittig.presentation.purchases.addedit.itemmodels.PurchaseAddEditHeaderItem;
import ch.giantific.qwittig.presentation.purchases.addedit.itemmodels.PurchaseAddEditItem;
import ch.giantific.qwittig.presentation.purchases.addedit.itemmodels.PurchaseAddEditItemIdentities;
import ch.giantific.qwittig.presentation.purchases.addedit.itemmodels.PurchaseAddEditItemIdentity;
import ch.giantific.qwittig.presentation.purchases.addedit.itemmodels.PurchaseAddEditItemModel;
import ch.giantific.qwittig.presentation.purchases.addedit.itemmodels.PurchaseAddEditItemModel.Type;


/**
 * Provides a {@link RecyclerView} adapter that manages the list for the add or edit purchase
 * screen.
 * <p/>
 * Subclass of {@link RecyclerView.Adapter}.
 */
public class PurchaseAddEditRecyclerAdapter extends BaseRecyclerAdapter {

    private final PurchaseAddEditViewModel viewModel;

    public PurchaseAddEditRecyclerAdapter(@NonNull PurchaseAddEditViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final Context context = parent.getContext();
        final LayoutInflater inflater = LayoutInflater.from(context);
        switch (viewType) {
            case Type.HEADER: {
                final RowGenericHeaderBinding binding =
                        RowGenericHeaderBinding.inflate(inflater, parent, false);
                return new BindingRow<>(binding);
            }
            case Type.DATE: {
                final RowPurchaseAddDateBinding binding =
                        RowPurchaseAddDateBinding.inflate(inflater, parent, false);
                return new BindingRow<>(binding);
            }
            case Type.STORE: {
                final RowPurchaseAddStoreBinding binding =
                        RowPurchaseAddStoreBinding.inflate(inflater, parent, false);
                return new BindingRow<>(binding);
            }
            case Type.ITEM: {
                final RowPurchaseAddItemBinding binding =
                        RowPurchaseAddItemBinding.inflate(inflater, parent, false);
                return new BindingRow<>(binding);
            }
            case Type.USERS: {
                final RowPurchaseAddItemUsersBinding binding =
                        RowPurchaseAddItemUsersBinding.inflate(inflater, parent, false);
                return new ItemIdentitiesRow(context, binding, viewModel);
            }
            case Type.ADD_ROW: {
                final RowPurchaseAddAddRowBinding binding =
                        RowPurchaseAddAddRowBinding.inflate(inflater, parent, false);
                return new BindingRow<>(binding);
            }
            case Type.TOTAL: {
                final RowPurchaseAddTotalBinding binding =
                        RowPurchaseAddTotalBinding.inflate(inflater, parent, false);
                return new TotalRow(context, binding, viewModel);
            }
            default:
                return super.onCreateViewHolder(parent, viewType);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final PurchaseAddEditItemModel itemModel = viewModel.getItemAtPosition(position);
        final int type = getItemViewType(position);
        switch (type) {
            case Type.HEADER: {
                final BindingRow<RowGenericHeaderBinding> row = (BindingRow<RowGenericHeaderBinding>) holder;
                final RowGenericHeaderBinding binding = row.getBinding();

                final PurchaseAddEditHeaderItem headerRow = (PurchaseAddEditHeaderItem) itemModel;
                binding.setItemModel(headerRow);
                binding.executePendingBindings();
                break;
            }
            case Type.DATE: {
                final BindingRow<RowPurchaseAddDateBinding> row =
                        (BindingRow<RowPurchaseAddDateBinding>) holder;
                final RowPurchaseAddDateBinding binding = row.getBinding();

                binding.setViewModel(viewModel);
                binding.executePendingBindings();
                break;
            }
            case Type.STORE: {
                final BindingRow<RowPurchaseAddStoreBinding> row =
                        (BindingRow<RowPurchaseAddStoreBinding>) holder;
                final RowPurchaseAddStoreBinding binding = row.getBinding();

                binding.setViewModel(viewModel);
                binding.executePendingBindings();
                break;
            }
            case Type.ITEM: {
                final BindingRow<RowPurchaseAddItemBinding> row = (BindingRow<RowPurchaseAddItemBinding>) holder;
                final RowPurchaseAddItemBinding binding = row.getBinding();

                final PurchaseAddEditItem addEditItem = (PurchaseAddEditItem) itemModel;
                binding.setItemModel(addEditItem);
                binding.setViewModel(viewModel);
                binding.executePendingBindings();
                break;
            }
            case Type.USERS: {
                final ItemIdentitiesRow row = (ItemIdentitiesRow) holder;

                final PurchaseAddEditItemIdentities itemUsersRow = (PurchaseAddEditItemIdentities) itemModel;
                row.setIdentities(itemUsersRow.getIdentities());
                break;
            }
            case Type.ADD_ROW: {
                final BindingRow<RowPurchaseAddAddRowBinding> row =
                        (BindingRow<RowPurchaseAddAddRowBinding>) holder;
                final RowPurchaseAddAddRowBinding binding = row.getBinding();

                binding.setItemModel(itemModel);
                binding.setViewModel(viewModel);
                binding.executePendingBindings();
                break;
            }
            case Type.TOTAL: {
                final TotalRow row = (TotalRow) holder;
                final RowPurchaseAddTotalBinding binding = row.getBinding();

                binding.setViewModel(viewModel);
                binding.executePendingBindings();
                break;
            }
        }
    }

    @Override
    public int getItemCount() {
        return viewModel.getItemCount();
    }

    @Override
    public int getItemViewType(int position) {
        return viewModel.getItemViewType(position);
    }

    private static class ItemIdentitiesRow extends BindingRow<RowPurchaseAddItemUsersBinding> {

        private final PurchaseAddEditItemUsersRecyclerAdapter recyclerAdapter;
        private final List<PurchaseAddEditItemIdentity> identities = new ArrayList<>();

        public ItemIdentitiesRow(@NonNull Context context,
                                 @NonNull RowPurchaseAddItemUsersBinding binding,
                                 @NonNull PurchaseAddEditViewModel viewModel) {
            super(binding);

            binding.rvPurchaseAddItemUsers.setHasFixedSize(true);
            binding.rvPurchaseAddItemUsers.setLayoutManager(new LinearLayoutManager(context,
                    LinearLayoutManager.HORIZONTAL, false));
            recyclerAdapter = new PurchaseAddEditItemUsersRecyclerAdapter(viewModel, identities);
            binding.rvPurchaseAddItemUsers.setAdapter(recyclerAdapter);
        }

        public void setIdentities(@NonNull PurchaseAddEditItemIdentity[] identities) {
            this.identities.clear();
            this.identities.addAll(Arrays.asList(identities));
            recyclerAdapter.notifyDataSetChanged();
        }
    }

    private static class TotalRow extends BindingRow<RowPurchaseAddTotalBinding> {

        public TotalRow(@NonNull Context context, @NonNull RowPurchaseAddTotalBinding binding,
                        @NonNull PurchaseAddEditViewModel viewModel) {
            super(binding);

            final ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                    R.layout.spinner_item_title, viewModel.getSupportedCurrencies());
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            binding.spCurrency.setAdapter(adapter);
        }
    }
}
