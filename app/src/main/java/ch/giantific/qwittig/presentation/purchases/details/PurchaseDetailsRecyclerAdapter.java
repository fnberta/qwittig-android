/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.details;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ch.giantific.qwittig.databinding.RowGenericHeaderBinding;
import ch.giantific.qwittig.databinding.RowPurchaseDetailsIdentitiesBinding;
import ch.giantific.qwittig.databinding.RowPurchaseDetailsItemBinding;
import ch.giantific.qwittig.databinding.RowPurchaseDetailsMyShareBinding;
import ch.giantific.qwittig.databinding.RowPurchaseDetailsNoteBinding;
import ch.giantific.qwittig.databinding.RowPurchaseDetailsTotalBinding;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.presentation.common.adapters.BaseRecyclerAdapter;
import ch.giantific.qwittig.presentation.common.adapters.rows.BindingRow;
import ch.giantific.qwittig.presentation.purchases.details.items.PurchaseDetailsBaseItem;
import ch.giantific.qwittig.presentation.purchases.details.items.PurchaseDetailsBaseItem.Type;
import ch.giantific.qwittig.presentation.purchases.details.items.PurchaseDetailsHeaderItem;
import ch.giantific.qwittig.presentation.purchases.details.items.PurchaseDetailsIdentitiesItem;
import ch.giantific.qwittig.presentation.purchases.details.items.PurchaseDetailsItem;
import ch.giantific.qwittig.presentation.purchases.details.items.PurchaseDetailsMyShareItem;
import ch.giantific.qwittig.presentation.purchases.details.items.PurchaseDetailsNoteItem;
import ch.giantific.qwittig.presentation.purchases.details.items.PurchaseDetailsTotalItem;

/**
 * Handles the display of the detail view of a purchase including the different headers,
 * the users involved, the items and the total value.
 * <p/>
 * Subclass of {@link RecyclerView.Adapter}.
 */
public class PurchaseDetailsRecyclerAdapter extends BaseRecyclerAdapter<RecyclerView.ViewHolder> {

    private final PurchaseDetailsViewModel mViewModel;

    public PurchaseDetailsRecyclerAdapter(@NonNull PurchaseDetailsViewModel viewModel) {
        super();

        mViewModel = viewModel;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, @Type int viewType) {
        final Context context = parent.getContext();
        final LayoutInflater inflater = LayoutInflater.from(context);
        switch (viewType) {
            case Type.HEADER: {
                final RowGenericHeaderBinding binding = RowGenericHeaderBinding.inflate(inflater, parent, false);
                return new BindingRow<>(binding);
            }
            case Type.ITEM: {
                final RowPurchaseDetailsItemBinding binding =
                        RowPurchaseDetailsItemBinding.inflate(inflater, parent, false);
                return new BindingRow<>(binding);
            }
            case Type.MY_SHARE: {
                final RowPurchaseDetailsMyShareBinding binding =
                        RowPurchaseDetailsMyShareBinding.inflate(inflater, parent, false);
                return new BindingRow<>(binding);
            }
            case Type.NOTE: {
                final RowPurchaseDetailsNoteBinding binding =
                        RowPurchaseDetailsNoteBinding.inflate(inflater, parent, false);
                return new BindingRow<>(binding);
            }
            case Type.TOTAL: {
                final RowPurchaseDetailsTotalBinding binding =
                        RowPurchaseDetailsTotalBinding.inflate(inflater, parent, false);
                return new BindingRow<>(binding);
            }
            case Type.IDENTITIES: {
                final RowPurchaseDetailsIdentitiesBinding binding =
                        RowPurchaseDetailsIdentitiesBinding.inflate(inflater, parent, false);
                return new UsersRow(context, binding, mViewModel);
            }
            default:
                return super.onCreateViewHolder(parent, viewType);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final PurchaseDetailsBaseItem detailsItem = mViewModel.getItemAtPosition(position);

        @Type final int viewType = getItemViewType(position);
        switch (viewType) {
            case Type.HEADER: {
                final BindingRow<RowGenericHeaderBinding> row = (BindingRow<RowGenericHeaderBinding>) holder;
                final RowGenericHeaderBinding binding = row.getBinding();

                binding.setViewModel((PurchaseDetailsHeaderItem) detailsItem);
                binding.executePendingBindings();
                break;
            }
            case Type.ITEM: {
                final BindingRow<RowPurchaseDetailsItemBinding> row =
                        (BindingRow<RowPurchaseDetailsItemBinding>) holder;
                final RowPurchaseDetailsItemBinding binding = row.getBinding();

                binding.setItem((PurchaseDetailsItem) detailsItem);
                binding.executePendingBindings();
                break;
            }
            case Type.MY_SHARE: {
                final BindingRow<RowPurchaseDetailsMyShareBinding> row =
                        (BindingRow<RowPurchaseDetailsMyShareBinding>) holder;
                final RowPurchaseDetailsMyShareBinding binding = row.getBinding();

                binding.setItem((PurchaseDetailsMyShareItem) detailsItem);
                binding.executePendingBindings();
                break;
            }
            case Type.NOTE: {
                final BindingRow<RowPurchaseDetailsNoteBinding> row =
                        (BindingRow<RowPurchaseDetailsNoteBinding>) holder;
                final RowPurchaseDetailsNoteBinding binding = row.getBinding();

                binding.setItem((PurchaseDetailsNoteItem) detailsItem);
                binding.executePendingBindings();
                break;
            }
            case Type.TOTAL: {
                final BindingRow<RowPurchaseDetailsTotalBinding> row =
                        (BindingRow<RowPurchaseDetailsTotalBinding>) holder;
                final RowPurchaseDetailsTotalBinding binding = row.getBinding();

                binding.setItem((PurchaseDetailsTotalItem) detailsItem);
                binding.executePendingBindings();
                break;
            }
            case Type.IDENTITIES: {
                final UsersRow row = (UsersRow) holder;
                final PurchaseDetailsIdentitiesItem identitiesItem = (PurchaseDetailsIdentitiesItem) detailsItem;
                row.setIdentities(identitiesItem.getIdentities());
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

    /**
     * Provides a {@link RecyclerView} row that displays the users involved in the purchase by
     * displaying them in another {@link RecyclerView}.
     * <p/>
     * Subclass of {@link RecyclerView.ViewHolder}.
     */
    private static class UsersRow extends BindingRow<RowPurchaseDetailsIdentitiesBinding> {

        private final RecyclerView.Adapter mRecyclerAdapter;
        private final List<Identity> mIdentities = new ArrayList<>();

        /**
         * Constructs a new {@link UsersRow} by initialising a new
         * {@link PurchaseDetailsIdentitiesRecyclerAdapter}.
         *
         * @param context   the context to use for the layout manager
         * @param binding   the binding for the view
         * @param viewModel the main view's model
         */
        public UsersRow(@NonNull Context context,
                        @NonNull RowPurchaseDetailsIdentitiesBinding binding,
                        @NonNull PurchaseDetailsViewModel viewModel) {
            super(binding);

            mRecyclerAdapter = new PurchaseDetailsIdentitiesRecyclerAdapter(viewModel, mIdentities);
            binding.rvPurchaseDetailsUsers.setHasFixedSize(true);
            binding.rvPurchaseDetailsUsers.setLayoutManager(new LinearLayoutManager(context,
                    LinearLayoutManager.HORIZONTAL, false));
            binding.rvPurchaseDetailsUsers.setAdapter(mRecyclerAdapter);
        }

        public void setIdentities(@NonNull List<Identity> identities) {
            mIdentities.clear();
            mIdentities.addAll(identities);
            Collections.sort(mIdentities);
            mRecyclerAdapter.notifyDataSetChanged();
        }
    }
}
