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
import ch.giantific.qwittig.databinding.RowPurchaseAddEditArticleBinding;
import ch.giantific.qwittig.databinding.RowPurchaseAddEditArticleIdentitiesBinding;
import ch.giantific.qwittig.databinding.RowPurchaseAddEditDateBinding;
import ch.giantific.qwittig.databinding.RowPurchaseAddEditStoreBinding;
import ch.giantific.qwittig.databinding.RowPurchaseAddEditTotalBinding;
import ch.giantific.qwittig.presentation.common.listadapters.rows.BindingRow;
import ch.giantific.qwittig.presentation.purchases.addedit.viewmodels.PurchaseAddEditViewModel;
import ch.giantific.qwittig.presentation.purchases.addedit.viewmodels.items.BasePurchaseAddEditItemViewModel;
import ch.giantific.qwittig.presentation.purchases.addedit.viewmodels.items.BasePurchaseAddEditItemViewModel.ViewType;
import ch.giantific.qwittig.presentation.purchases.addedit.viewmodels.items.PurchaseAddEditArticleIdentitiesItemViewModel;
import ch.giantific.qwittig.presentation.purchases.addedit.viewmodels.items.PurchaseAddEditArticleIdentityItemViewModel;
import ch.giantific.qwittig.presentation.purchases.addedit.viewmodels.items.PurchaseAddEditArticleItemViewModel;
import ch.giantific.qwittig.presentation.purchases.addedit.viewmodels.items.PurchaseAddEditHeaderItemViewModel;


/**
 * Provides a {@link RecyclerView} adapter that manages the list for the addItemAtPosition or edit purchase
 * screen.
 * <p/>
 * Subclass of {@link RecyclerView.Adapter}.
 */
public class PurchaseAddEditRecyclerAdapter extends RecyclerView.Adapter {

    private final PurchaseAddEditContract.Presenter presenter;
    private final PurchaseAddEditViewModel viewModel;

    public PurchaseAddEditRecyclerAdapter(@NonNull PurchaseAddEditContract.Presenter presenter,
                                          @NonNull PurchaseAddEditViewModel viewModel) {
        super();

        this.presenter = presenter;
        this.viewModel = viewModel;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final Context context = parent.getContext();
        final LayoutInflater inflater = LayoutInflater.from(context);
        switch (viewType) {
            case ViewType.HEADER: {
                final RowGenericHeaderBinding binding =
                        RowGenericHeaderBinding.inflate(inflater, parent, false);
                return new BindingRow<>(binding);
            }
            case ViewType.DATE: {
                final RowPurchaseAddEditDateBinding binding =
                        RowPurchaseAddEditDateBinding.inflate(inflater, parent, false);
                return new BindingRow<>(binding);
            }
            case ViewType.STORE: {
                final RowPurchaseAddEditStoreBinding binding =
                        RowPurchaseAddEditStoreBinding.inflate(inflater, parent, false);
                return new BindingRow<>(binding);
            }
            case ViewType.ARTICLE: {
                final RowPurchaseAddEditArticleBinding binding =
                        RowPurchaseAddEditArticleBinding.inflate(inflater, parent, false);
                return new BindingRow<>(binding);
            }
            case ViewType.IDENTITIES: {
                final RowPurchaseAddEditArticleIdentitiesBinding binding =
                        RowPurchaseAddEditArticleIdentitiesBinding.inflate(inflater, parent, false);
                return new ArticleIdentitiesRow(context, binding, presenter);
            }
            case ViewType.ADD_ROW: {
                final RowPurchaseAddEditAddRowBinding binding =
                        RowPurchaseAddEditAddRowBinding.inflate(inflater, parent, false);
                return new BindingRow<>(binding);
            }
            case ViewType.TOTAL: {
                final RowPurchaseAddEditTotalBinding binding =
                        RowPurchaseAddEditTotalBinding.inflate(inflater, parent, false);
                return new TotalRow(context, binding, viewModel.getSupportedCurrencies());
            }
            default:
                throw new RuntimeException("There is no type that matches the type " + viewType +
                        ", make sure your using types correctly!");
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final BasePurchaseAddEditItemViewModel itemViewModel = viewModel.getItemAtPosition(position);
        final int type = getItemViewType(position);
        switch (type) {
            case ViewType.HEADER: {
                final BindingRow<RowGenericHeaderBinding> row = (BindingRow<RowGenericHeaderBinding>) holder;
                final RowGenericHeaderBinding binding = row.getBinding();

                final PurchaseAddEditHeaderItemViewModel headerViewModel = (PurchaseAddEditHeaderItemViewModel) itemViewModel;
                binding.setViewModel(headerViewModel);
                binding.executePendingBindings();
                break;
            }
            case ViewType.DATE: {
                final BindingRow<RowPurchaseAddEditDateBinding> row =
                        (BindingRow<RowPurchaseAddEditDateBinding>) holder;
                final RowPurchaseAddEditDateBinding binding = row.getBinding();

                binding.setPresenter(presenter);
                binding.setViewModel(viewModel);
                binding.executePendingBindings();
                break;
            }
            case ViewType.STORE: {
                final BindingRow<RowPurchaseAddEditStoreBinding> row =
                        (BindingRow<RowPurchaseAddEditStoreBinding>) holder;
                final RowPurchaseAddEditStoreBinding binding = row.getBinding();

                binding.setViewModel(viewModel);
                binding.executePendingBindings();
                break;
            }
            case ViewType.ARTICLE: {
                final BindingRow<RowPurchaseAddEditArticleBinding> row =
                        (BindingRow<RowPurchaseAddEditArticleBinding>) holder;
                final RowPurchaseAddEditArticleBinding binding = row.getBinding();

                binding.setViewModel((PurchaseAddEditArticleItemViewModel) itemViewModel);
                binding.setPresenter(presenter);
                binding.executePendingBindings();
                break;
            }
            case ViewType.IDENTITIES: {
                final ArticleIdentitiesRow row = (ArticleIdentitiesRow) holder;

                final PurchaseAddEditArticleIdentitiesItemViewModel identitiesViewModel =
                        (PurchaseAddEditArticleIdentitiesItemViewModel) itemViewModel;
                row.setIdentities(identitiesViewModel.getIdentities());
                break;
            }
            case ViewType.ADD_ROW: {
                final BindingRow<RowPurchaseAddEditAddRowBinding> row =
                        (BindingRow<RowPurchaseAddEditAddRowBinding>) holder;
                final RowPurchaseAddEditAddRowBinding binding = row.getBinding();

                binding.setPresenter(presenter);
                binding.setViewModel(itemViewModel);
                binding.executePendingBindings();
                break;
            }
            case ViewType.TOTAL: {
                final TotalRow row = (TotalRow) holder;
                final RowPurchaseAddEditTotalBinding binding = row.getBinding();

                binding.setPresenter(presenter);
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
        return viewModel.getItemAtPosition(position).getViewType();
    }

    public static class ArticleIdentitiesRow extends BindingRow<RowPurchaseAddEditArticleIdentitiesBinding> {

        private final PurchaseAddEditArticleIdentitiesRecyclerAdapter recyclerAdapter;
        private final List<PurchaseAddEditArticleIdentityItemViewModel> identities = new ArrayList<>();

        ArticleIdentitiesRow(@NonNull Context context,
                             @NonNull RowPurchaseAddEditArticleIdentitiesBinding binding,
                             @NonNull PurchaseAddEditContract.Presenter presenter) {
            super(binding);

            binding.rvPurchaseAddItemUsers.setHasFixedSize(true);
            binding.rvPurchaseAddItemUsers.setLayoutManager(new LinearLayoutManager(context,
                    LinearLayoutManager.HORIZONTAL, false));
            recyclerAdapter = new PurchaseAddEditArticleIdentitiesRecyclerAdapter(presenter, identities);
            binding.rvPurchaseAddItemUsers.setAdapter(recyclerAdapter);
        }

        void setIdentities(@NonNull PurchaseAddEditArticleIdentityItemViewModel[] identities) {
            this.identities.clear();
            this.identities.addAll(Arrays.asList(identities));
            recyclerAdapter.notifyDataSetChanged();
        }

        void notifyItemChanged(@NonNull PurchaseAddEditArticleIdentityItemViewModel itemViewModel) {
            recyclerAdapter.notifyItemChanged(identities.indexOf(itemViewModel));
        }
    }

    private static class TotalRow extends BindingRow<RowPurchaseAddEditTotalBinding> {

        TotalRow(@NonNull Context context, @NonNull RowPurchaseAddEditTotalBinding binding,
                 @NonNull List<String> supportedCurrencies) {
            super(binding);

            final ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                    R.layout.spinner_item_title, supportedCurrencies);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            binding.spCurrency.setAdapter(adapter);
        }
    }
}
