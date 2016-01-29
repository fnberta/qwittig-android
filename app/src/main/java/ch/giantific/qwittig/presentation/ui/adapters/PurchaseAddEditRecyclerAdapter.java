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
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.databinding.RowHeaderBinding;
import ch.giantific.qwittig.databinding.RowPurchaseAddAddRowBinding;
import ch.giantific.qwittig.databinding.RowPurchaseAddDateBinding;
import ch.giantific.qwittig.databinding.RowPurchaseAddExchangeRateBinding;
import ch.giantific.qwittig.databinding.RowPurchaseAddItemBinding;
import ch.giantific.qwittig.databinding.RowPurchaseAddItemUsersBinding;
import ch.giantific.qwittig.databinding.RowPurchaseAddStoreBinding;
import ch.giantific.qwittig.databinding.RowPurchaseAddTotalBinding;
import ch.giantific.qwittig.domain.models.RowItem;
import ch.giantific.qwittig.domain.models.RowItemUser;
import ch.giantific.qwittig.presentation.ui.adapters.rows.BindingRow;
import ch.giantific.qwittig.presentation.viewmodels.PurchaseAddEditViewModel;
import ch.giantific.qwittig.presentation.viewmodels.rows.HeaderRowViewModel;
import ch.giantific.qwittig.utils.parse.ParseUtils;

/**
 * Created by fabio on 24.01.16.
 */
public class PurchaseAddEditRecyclerAdapter extends RecyclerView.Adapter {

    private PurchaseAddEditViewModel mViewModel;

    public PurchaseAddEditRecyclerAdapter(@NonNull PurchaseAddEditViewModel viewModel) {
        mViewModel = viewModel;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final Context context = parent.getContext();
        final LayoutInflater inflater = LayoutInflater.from(context);
        switch (viewType) {
            case PurchaseAddEditViewModel.TYPE_HEADER: {
                final RowHeaderBinding binding = RowHeaderBinding.inflate(inflater, parent, false);
                return new BindingRow<>(binding);
            }
            case PurchaseAddEditViewModel.TYPE_DATE: {
                final RowPurchaseAddDateBinding binding =
                        RowPurchaseAddDateBinding.inflate(inflater, parent, false);
                return new BindingRow<>(binding);
            }
            case PurchaseAddEditViewModel.TYPE_STORE: {
                final RowPurchaseAddStoreBinding binding =
                        RowPurchaseAddStoreBinding.inflate(inflater, parent, false);
                return new BindingRow<>(binding);
            }
            case PurchaseAddEditViewModel.TYPE_ITEM: {
                final RowPurchaseAddItemBinding binding =
                        RowPurchaseAddItemBinding.inflate(inflater, parent, false);
                return new ItemRow(binding, mViewModel);
            }
            case PurchaseAddEditViewModel.TYPE_USERS: {
                final RowPurchaseAddItemUsersBinding binding =
                        RowPurchaseAddItemUsersBinding.inflate(inflater, parent, false);
                return new ItemUsersRow(context, binding, mViewModel);
            }
            case PurchaseAddEditViewModel.TYPE_ADD_ROW: {
                final RowPurchaseAddAddRowBinding binding =
                        RowPurchaseAddAddRowBinding.inflate(inflater, parent, false);
                return new AddRow(binding, mViewModel);
            }
            case PurchaseAddEditViewModel.TYPE_TOTAL: {
                final RowPurchaseAddTotalBinding binding =
                        RowPurchaseAddTotalBinding.inflate(inflater, parent, false);
                return new TotalRow(context, binding);
            }
            case PurchaseAddEditViewModel.TYPE_EXCHANGE_RATE: {
                final RowPurchaseAddExchangeRateBinding binding =
                        RowPurchaseAddExchangeRateBinding.inflate(inflater, parent, false);
                return new BindingRow<>(binding);
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
            case PurchaseAddEditViewModel.TYPE_HEADER: {
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
            case PurchaseAddEditViewModel.TYPE_DATE: {
                final BindingRow<RowPurchaseAddDateBinding> row =
                        (BindingRow<RowPurchaseAddDateBinding>) holder;
                final RowPurchaseAddDateBinding binding = row.getBinding();
                binding.setViewModel(mViewModel);

                binding.executePendingBindings();
                break;
            }
            case PurchaseAddEditViewModel.TYPE_STORE: {
                final BindingRow<RowPurchaseAddStoreBinding> row =
                        (BindingRow<RowPurchaseAddStoreBinding>) holder;
                final RowPurchaseAddStoreBinding binding = row.getBinding();
                binding.setViewModel(mViewModel);

                binding.executePendingBindings();
                break;
            }
            case PurchaseAddEditViewModel.TYPE_ITEM: {
                final ItemRow row = (ItemRow) holder;
                final RowPurchaseAddItemBinding binding = row.getBinding();
                final RowItem rowItem = mViewModel.getItemAtPosition(position);
                rowItem.setPriceChangedListener(mViewModel);
                binding.setRowItem(rowItem);

                binding.executePendingBindings();
                break;
            }
            case PurchaseAddEditViewModel.TYPE_USERS: {
                final ItemUsersRow row = (ItemUsersRow) holder;
                // get parent row and set users to adapter
                final RowItem rowItem = mViewModel.getItemAtPosition(position - 1);
                row.setUsers(rowItem.getUsers());
                break;
            }
            case PurchaseAddEditViewModel.TYPE_ADD_ROW: {
                // do nothing
                break;
            }
            case PurchaseAddEditViewModel.TYPE_TOTAL: {
                final TotalRow row = (TotalRow) holder;
                final RowPurchaseAddTotalBinding binding = row.getBinding();
                binding.setViewModel(mViewModel);

                binding.executePendingBindings();
                break;
            }
            case PurchaseAddEditViewModel.TYPE_EXCHANGE_RATE: {
                final BindingRow<RowPurchaseAddExchangeRateBinding> row =
                        (BindingRow<RowPurchaseAddExchangeRateBinding>) holder;
                final RowPurchaseAddExchangeRateBinding binding = row.getBinding();
                binding.setViewModel(mViewModel);

                binding.executePendingBindings();
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

        private PurchaseAddEditItemUsersRecyclerAdapter mRecyclerAdapter;
        private List<RowItemUser> mUsers = new ArrayList<>();

        public ItemUsersRow(@NonNull Context context,
                            @NonNull RowPurchaseAddItemUsersBinding binding,
                            @NonNull PurchaseAddEditViewModel viewModel) {
            super(binding);

            binding.rvPurchaseAddItemUsers.setHasFixedSize(true);
            binding.rvPurchaseAddItemUsers.setLayoutManager(new LinearLayoutManager(context,
                    LinearLayoutManager.HORIZONTAL, false));
            mRecyclerAdapter = new PurchaseAddEditItemUsersRecyclerAdapter(viewModel, mUsers);
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

    public static class TotalRow extends BindingRow<RowPurchaseAddTotalBinding> {

        public TotalRow(@NonNull Context context, @NonNull RowPurchaseAddTotalBinding binding) {
            super(binding);

            final ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                    R.layout.spinner_item_title, ParseUtils.getSupportedCurrencyCodes());
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            binding.spCurrency.setAdapter(adapter);
        }
    }
}
