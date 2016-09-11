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
import ch.giantific.qwittig.databinding.RowPurchaseAddEditAddRowBinding;
import ch.giantific.qwittig.databinding.RowPurchaseAddEditDateBinding;
import ch.giantific.qwittig.databinding.RowPurchaseAddEditArticleBinding;
import ch.giantific.qwittig.databinding.RowPurchaseAddEditArticleIdentitiesBinding;
import ch.giantific.qwittig.databinding.RowPurchaseAddEditStoreBinding;
import ch.giantific.qwittig.databinding.RowPurchaseAddEditTotalBinding;
import ch.giantific.qwittig.presentation.common.adapters.BaseRecyclerAdapter;
import ch.giantific.qwittig.presentation.common.adapters.rows.BindingRow;
import ch.giantific.qwittig.presentation.purchases.addedit.itemmodels.PurchaseAddEditArticleIdentity;
import ch.giantific.qwittig.presentation.purchases.addedit.itemmodels.PurchaseAddEditArticleItem;
import ch.giantific.qwittig.presentation.purchases.addedit.itemmodels.PurchaseAddEditHeaderItem;
import ch.giantific.qwittig.presentation.purchases.addedit.itemmodels.PurchaseAddEditArticleIdentitiesItem;
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
                final RowPurchaseAddEditDateBinding binding =
                        RowPurchaseAddEditDateBinding.inflate(inflater, parent, false);
                return new BindingRow<>(binding);
            }
            case Type.STORE: {
                final RowPurchaseAddEditStoreBinding binding =
                        RowPurchaseAddEditStoreBinding.inflate(inflater, parent, false);
                return new BindingRow<>(binding);
            }
            case Type.ARTICLE: {
                final RowPurchaseAddEditArticleBinding binding =
                        RowPurchaseAddEditArticleBinding.inflate(inflater, parent, false);
                return new BindingRow<>(binding);
            }
            case Type.IDENTITIES: {
                final RowPurchaseAddEditArticleIdentitiesBinding binding =
                        RowPurchaseAddEditArticleIdentitiesBinding.inflate(inflater, parent, false);
                return new ArticleIdentitiesRow(context, binding, viewModel);
            }
            case Type.ADD_ROW: {
                final RowPurchaseAddEditAddRowBinding binding =
                        RowPurchaseAddEditAddRowBinding.inflate(inflater, parent, false);
                return new BindingRow<>(binding);
            }
            case Type.TOTAL: {
                final RowPurchaseAddEditTotalBinding binding =
                        RowPurchaseAddEditTotalBinding.inflate(inflater, parent, false);
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
                final BindingRow<RowPurchaseAddEditDateBinding> row =
                        (BindingRow<RowPurchaseAddEditDateBinding>) holder;
                final RowPurchaseAddEditDateBinding binding = row.getBinding();

                binding.setViewModel(viewModel);
                binding.executePendingBindings();
                break;
            }
            case Type.STORE: {
                final BindingRow<RowPurchaseAddEditStoreBinding> row =
                        (BindingRow<RowPurchaseAddEditStoreBinding>) holder;
                final RowPurchaseAddEditStoreBinding binding = row.getBinding();

                binding.setViewModel(viewModel);
                binding.executePendingBindings();
                break;
            }
            case Type.ARTICLE: {
                final BindingRow<RowPurchaseAddEditArticleBinding> row = (BindingRow<RowPurchaseAddEditArticleBinding>) holder;
                final RowPurchaseAddEditArticleBinding binding = row.getBinding();

                final PurchaseAddEditArticleItem articleItem = (PurchaseAddEditArticleItem) itemModel;
                binding.setItemModel(articleItem);
                binding.setViewModel(viewModel);
                binding.executePendingBindings();
                break;
            }
            case Type.IDENTITIES: {
                final ArticleIdentitiesRow row = (ArticleIdentitiesRow) holder;

                final PurchaseAddEditArticleIdentitiesItem identitiesItem = (PurchaseAddEditArticleIdentitiesItem) itemModel;
                row.setIdentities(identitiesItem.getIdentities());
                break;
            }
            case Type.ADD_ROW: {
                final BindingRow<RowPurchaseAddEditAddRowBinding> row =
                        (BindingRow<RowPurchaseAddEditAddRowBinding>) holder;
                final RowPurchaseAddEditAddRowBinding binding = row.getBinding();

                binding.setItemModel(itemModel);
                binding.setViewModel(viewModel);
                binding.executePendingBindings();
                break;
            }
            case Type.TOTAL: {
                final TotalRow row = (TotalRow) holder;
                final RowPurchaseAddEditTotalBinding binding = row.getBinding();

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

    private static class ArticleIdentitiesRow extends BindingRow<RowPurchaseAddEditArticleIdentitiesBinding> {

        private final PurchaseAddEditArticleIdentitiesRecyclerAdapter recyclerAdapter;
        private final List<PurchaseAddEditArticleIdentity> identities = new ArrayList<>();

        public ArticleIdentitiesRow(@NonNull Context context,
                                    @NonNull RowPurchaseAddEditArticleIdentitiesBinding binding,
                                    @NonNull PurchaseAddEditViewModel viewModel) {
            super(binding);

            binding.rvPurchaseAddItemUsers.setHasFixedSize(true);
            binding.rvPurchaseAddItemUsers.setLayoutManager(new LinearLayoutManager(context,
                    LinearLayoutManager.HORIZONTAL, false));
            recyclerAdapter = new PurchaseAddEditArticleIdentitiesRecyclerAdapter(viewModel, identities);
            binding.rvPurchaseAddItemUsers.setAdapter(recyclerAdapter);
        }

        public void setIdentities(@NonNull PurchaseAddEditArticleIdentity[] identities) {
            this.identities.clear();
            this.identities.addAll(Arrays.asList(identities));
            recyclerAdapter.notifyDataSetChanged();
        }
    }

    private static class TotalRow extends BindingRow<RowPurchaseAddEditTotalBinding> {

        public TotalRow(@NonNull Context context, @NonNull RowPurchaseAddEditTotalBinding binding,
                        @NonNull PurchaseAddEditViewModel viewModel) {
            super(binding);

            final ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                    R.layout.spinner_item_title, viewModel.getSupportedCurrencies());
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            binding.spCurrency.setAdapter(adapter);
        }
    }
}
